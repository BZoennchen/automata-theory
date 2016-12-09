package DFA.gen;

import DFA.inter.IDFA;
import DFA.inter.IDFACreator;
import DFA.inter.IDFARule;
import symbol.INamedSymbol;
import utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GenDFAOp implements all required operations on DFAs of generic types.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of the letters of the input alphabet of the DFA
 * @param <B>   the type of the identifier of the states of the DFA
 * @param <F>   the type of the letter of the input alphabet of the DFA
 * @param <Q>   the type of the states of the DFA
 * @param <R>   the type of the transition rules of the DFA
 * @param <D>   the type of the DFA
 */
public class GenDFAOp<N, B, F extends INamedSymbol<N>, Q extends INamedSymbol<B>, R extends IDFARule<N, B, F, Q>, D extends IDFA<N, B, F, Q, R>> {

    /**
     * Equivalence test for deterministic word automaton. We implement the Hoopcraft-algorithm.
     *
     * @param dfa1      the first DFA
     * @param dfa2      the second DFA
     * @return true => L(dfa1) = L(dfa2), otherwise false
     */
    public boolean isEquals(final D dfa1, final D dfa2) {

        if(!dfa1.getSymbols().equals(dfa2.getSymbols())) {
            return false;
        }

        Set<F> symbols = dfa1.getSymbols();
        Set<Q> finalStates1 = dfa1.getFinalStates();
        Set<Q> finalStates2 = dfa2.getFinalStates();
        Set<Q> nonFinalStates1 = dfa1.getStates().stream().filter(q -> !finalStates1.contains(q)).collect(Collectors.toSet());
        Set<Q> nonFinalStates2 = dfa2.getStates().stream().filter(q -> !finalStates2.contains(q)).collect(Collectors.toSet());

        Map<Pair<Q, F>, R> rules1 = new HashMap<>();
        Map<Pair<Q, F>, R> rules2 = new HashMap<>();

        Map<Q, GenNode<Q>> mapping1 = dfa1.getStates().stream().map(q -> new Pair<>(q, new GenNode<>(q))).collect(Collectors.toMap(p -> p.a, p -> p.b));
        Map<Q, GenNode<Q>> mapping2 = dfa2.getStates().stream().map(q -> new Pair<>(q, new GenNode<>(q))).collect(Collectors.toMap(p -> p.a, p -> p.b));

        for(R rule : dfa1.getRules()) {
            rules1.put(new Pair<>(rule.getSrcState(), rule.getSymbol()), rule);
        }

        for(R rule : dfa2.getRules()) {
            rules2.put(new Pair<>(rule.getSrcState(), rule.getSymbol()), rule);
        }


        GenNode<Q> initialNode1 = mapping1.get(dfa1.getInitialState());
        GenNode<Q> initialNode2 = mapping2.get(dfa2.getInitialState());

        Set<Pair<GenNode<Q>, GenNode<Q>>> L = new HashSet<>();
        L.add(new Pair<>(initialNode1, initialNode2));


        while(!L.isEmpty()) {
            Pair<GenNode<Q>, GenNode<Q>> triple = L.iterator().next();
            L.remove(triple);

            // Find
            if(triple.a.getRoot() != triple.b.getRoot()) {
                if(finalStates1.contains(triple.a.getElement()) && nonFinalStates2.contains(triple.b.getElement())
                        || nonFinalStates1.contains(triple.a.getElement()) && finalStates2.contains(triple.b.getElement())) {
                    return false;
                }
                else {
                    Q q1 = triple.a.getElement();
                    Q q2 = triple.b.getElement();

                    // Union
                    GenNode<Q> root1 = triple.a.getRoot();
                    GenNode<Q> root2 = triple.b.getRoot();
                    if(root1.getSize() <= root2.getSize()) {
                        root1.append(root2);
                    }
                    else {
                        root2.append(root1);
                    }

                    for(F f : symbols) {
                        Pair<Q, F> key1 = new Pair<>(q1, f);
                        Pair<Q, F> key2 = new Pair<>(q2, f);
                        if(rules1.containsKey(key1) && rules2.containsKey(key2)) {
                            R r1 = rules1.get(key1);
                            R r2 = rules2.get(key2);
                            L.add(new Pair<>(mapping1.get(r1.getDestState()), mapping2.get(r2.getDestState())));
                        }
                        else if(rules1.containsKey(key1) || rules2.containsKey(key2)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Transform the DFA into an equivalent minimal DFA.
     *
     * @param dfa       the DFA
     * @param creator   the creator that creates the minimal DFA
     * @return the minimal DFA
     */
    public D minimize(final D dfa, final IDFACreator<N, B, F, Q, R, D> creator) {
        D minimizedDFA = eliminateUnreachables(dfa, creator);
        Map<Pair<Q, F>, Set<Q>> reverse = new HashMap<>();

        // compute delta^{-1}(q, a) for all q, and a
        for(R rule : dfa.getRules()) {
            Pair<Q, F> pair = new Pair<>(rule.getDestState(), rule.getSymbol());
            if(!reverse.containsKey(pair)) {
                reverse.put(pair, new HashSet<Q>());
            }
            reverse.get(pair).add(rule.getSrcState());
        }
        Set<Set<Q>> groups = new HashSet<>();
        Set<Q> nonFinalStates = dfa.getStates().stream().filter(q -> !dfa.getFinalStates().contains(q)).collect(Collectors.toSet());
        Set<Q> finalStates = dfa.getFinalStates();
        groups.add(nonFinalStates);
        groups.add(finalStates);

        Set<Set<Q>> active = new HashSet<>();
        active.add(dfa.getFinalStates());

        while (!active.isEmpty()) {
            Set<Q> next = active.iterator().next();
            active.remove(next);

            for(F f : dfa.getSymbols()) {
                // construct delta^{-1}(active, f)
                Set<Q> reverseActive = next.stream().filter(q -> reverse.containsKey(new Pair<>(q, f))).map(q -> reverse.get(new Pair<>(q, f))).flatMap(set -> set.stream()).collect(Collectors.toSet());
                Set<Set<Q>> tmp = new HashSet<>();

                for(Set<Q> grp : groups) {
                    // grpCap = grp \cap reverse
                    Set<Q> grpCap = grp.stream().filter(q -> reverseActive.contains(q)).collect(Collectors.toSet());
                    // grpSetminus = grp \setminus grpCap
                    Set<Q> grpSetminus = grp.stream().filter(q -> !grpCap.contains(q)).collect(Collectors.toSet());

                    if(!grpCap.isEmpty() && !grpSetminus.isEmpty()) {
                        tmp.add(grpCap);
                        tmp.add(grpSetminus);
                        if(active.contains(grp)) {
                            active.remove(grp);
                            active.add(grpCap);
                            active.add(grpSetminus);
                        }
                        else {
                            // add the smaller one!
                            active.add(grpCap.size() <= grpSetminus.size() ? grpCap : grpSetminus);
                        }
                    }
                    else {
                        tmp.add(grp);
                    }
                }
            }
        }

        Map<Set<Q>, Q> groupsToState = new HashMap<>();
        Set<Q> newStates = new HashSet<>();
        Q newInitialState = null;
        Set<Q> newFinalStates = new HashSet<>();
        for(Set<Q> grp : groups)  {
            Q newState = creator.createState();
            newStates.add(newState);
            groupsToState.put(grp, newState);
            if(grp.contains(dfa.getInitialState())) {
                newInitialState = newState;
            }

            if(grp.containsAll(dfa.getFinalStates())) {
                newFinalStates.add(newState);
            }
        }

        Set<R> newRules = new HashSet<>();
        for(R rule : dfa.getRules()) {
            Q g = null;
            Q f = null;
            for(Set<Q> grp : groups) {
                if(grp.contains(rule.getSrcState())) {
                    g = groupsToState.get(grp);
                }

                if(grp.contains(rule.getDestState())) {
                    f = groupsToState.get(grp);
                }

                if(g != null && f != null) {
                    newRules.add(creator.createRule(g, f, rule.getSymbol()));
                    break;
                }
            }
        }

        return creator.create(newRules, newInitialState, newFinalStates);
    }

    /**
     * Returns a new DFA without any unreachable states recognizing the same language.
     * Complexity: O(|Q| * |Delta|)
     *
     * @param dfa the deterministic word automata
     * @return  set of reachable states with respect to the initial state
     */
    public D eliminateUnreachables(final D dfa, final IDFACreator<N, B, F, Q, R, D> creator) {
        Set<Q> newFinalStates = new HashSet<>();
        Set<R> newRules = new HashSet<>();
        Set<Q> reachables = new HashSet<>();
        Set<Q> active = new HashSet<>();

        Map<Q, List<R>> rulesByState = dfa.getRules().stream().collect(Collectors.groupingBy(r -> r.getSrcState()));

        reachables.add(dfa.getInitialState());
        active.add(dfa.getInitialState());
        while (!active.isEmpty()) {
            // Choose and remove a state q from active
            Iterator<Q> it = active.iterator();
            Q next = it.next();
            active.remove(next);

            // if delta(q,a) not in reachable add this state
            for(R rule : rulesByState.get(next)) {
                if(!reachables.contains(rule.getDestState())) {
                    reachables.add(rule.getDestState());
                    active.add(rule.getDestState());
                }
                newRules.add(rule);
            }
        }

        newFinalStates.addAll(dfa.getFinalStates().stream().filter(q -> reachables.contains(q)).collect(Collectors.toSet()));
        D newDFA = creator.create(newRules, dfa.getInitialState(), newFinalStates);
        return newDFA;
    }

}
