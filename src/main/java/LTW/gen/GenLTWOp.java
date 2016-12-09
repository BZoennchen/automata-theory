package LTW.gen;

import DFA.impl.tree.*;
import LTW.inter.ILTWRule;
import grammar.impl.CFG;
import grammar.impl.CFGCreator;
import grammar.impl.Production;
import grammar.impl.SLP;
import LTW.inter.ILTW;
import LTW.inter.ILTWCreator;
import LTW.inter.IRankedSymbol;
import symbol.IJezSymbol;
import symbol.INamedSymbol;
import utils.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class supports basic operations on LTWs.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output alphabet
 * @param <A> the type of the identifier of the ranked symbol
 * @param <B> the type of the identifier of the state of the LTW
 * @param <F> the type of the ranked symbol
 * @param <Q> the type of the states of the LTW
 * @param <R> the type of the rules of the LTW
 * @param <L> the type of the LTW
 */
public class GenLTWOp<N, A, B,
        F extends IRankedSymbol<A>,
        Q extends INamedSymbol<B>,
        R extends ILTWRule<N, A, B, F, Q>,
        L extends ILTW<N, A, B, F, Q, R>> {


    /**
     * Returns the set of accessible states of the LTW accessible from the state.
     *
     * @param ltw   the LTW
     * @param state a state of the LTW
     * @return the set of accessible states of the LTW accessible from the state
     */
    public Set<Q> getAccessibles(final L ltw, final Q state) {
        Set<Q> accessibles = new HashSet<>();
        final Set<Q> added = new HashSet<>();
        LinkedList<Q> heap = new LinkedList<>();
        heap.add(state);

        while (!added.isEmpty()) {
            Set<Q> newStates = ltw.getRules().stream().filter(r -> added.contains(r.getSrcState())).flatMap(r -> r.getDestStates().stream()).filter(q -> !accessibles.contains(q)).collect(Collectors.toSet());
            accessibles.addAll(newStates);
            added.clear();
            added.addAll(newStates);
        }

        return accessibles;
    }

    /**
     * Tests whether two LTWs are same-ordered.
     *
     * @param m1    the first LTW
     * @param m2    the second LTW
     * @return true => the LTWs are same-ordered, otherwise false
     */
    public boolean isSameOrdered(final L m1, final L m2) {
        // O(|Q_1| * |Q_2|)
        Set<Pair<Q, Q>> coreachableStates = getCoreachableStates(m1, m2);

        // average O(n)
        Map<Q, Set<R>> rulesBySrcState1 = m1.getRules().stream().collect(Collectors.groupingBy(r -> r.getSrcState(), Collectors.toSet()));
        Map<Q, Set<R>> rulesBySrcState2 = m2.getRules().stream().collect(Collectors.groupingBy(r -> r.getSrcState(), Collectors.toSet()));

        // average O(n)
        Map<Q, Set<F>> symbolsByStates1 = rulesBySrcState1.entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().stream().map(r -> r.getSymbol()).collect(Collectors.toSet())))
                .collect(Collectors.toMap(pair -> pair.a, pair -> pair.b));

        Map<Q, Set<F>> symbolsByStates2 = rulesBySrcState2.entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().stream().map(r -> r.getSymbol()).collect(Collectors.toSet())))
                .collect(Collectors.toMap(pair -> pair.a, pair -> pair.b));

        // average O(n)
        Map<Pair<Q, F>, Set<R>> rulesBySrcAndSymbol1 = m1.getRules().stream().collect(Collectors.groupingBy(r -> new Pair<>(r.getSrcState(), r.getSymbol()), Collectors.toSet()));
        Map<Pair<Q, F>, Set<R>> rulesBySrcAndSymbol2 = m2.getRules().stream().collect(Collectors.groupingBy(r -> new Pair<>(r.getSrcState(), r.getSymbol()), Collectors.toSet()));

        // average O(|Q_1| * |Q_2| * |rules|) = O(n^3)
        for(Pair<Q, Q> coreachPair : coreachableStates) {
            if(symbolsByStates1.containsKey(coreachPair.a)) {

                if(!symbolsByStates2.containsKey(coreachPair.b)) {
                    return false;
                }

                // both contain some symbols for the co-reachabele pair (q1, q2)
                Set<F> symbols1 = symbolsByStates1.get(coreachPair.a);
                Set<F> symbols2 = symbolsByStates2.get(coreachPair.b);

                for(F f1 : symbols1) {
                    if(!symbols2.contains(f1)) {
                        return false;
                    }
                    else {
                        // check for each rule q1(f) -> ..., and q2(f) -> ... if they are same ordered
                        for(R rule1 : rulesBySrcAndSymbol1.get(new Pair<>(coreachPair.a, f1))) {
                            for(R rule2 : rulesBySrcAndSymbol2.get(new Pair<>(coreachPair.b, f1))) {
                                if(!isSameOrdered(rule1, rule2)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            else if(symbolsByStates2.containsKey(coreachPair.b)) {
                return false;
            }

        }

        return true;
    }

    private boolean isSameOrdered(final R rule1, final R rule2) {
        if(rule1.getDestStates().size() != rule2.getDestStates().size()) {
            return false;
        }
        else {
            for(int i = 0; i < rule1.getDestStates().size(); i++) {
                if(rule1.getInputPermutation().apply(i) != rule2.getInputPermutation().apply(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the set of all co-reachable states of LTW m1 and LTW m2.
     *
     * @param m1    the first LTW
     * @param m2    the second LTW
     * @return  the set of all co-reachable stats of two LTW's
     */
    public Set<Pair<Q, Q>> getCoreachableStates(final L m1, final L m2) {
        Set<Pair<Q, Q>> coreachablePairs = new HashSet<>();
        LinkedList<Pair<Q, Q>> heap = new LinkedList<>();

        for(Q q01 : m1.getInitialStates()) {
            for(Q q02 : m2.getInitialStates()) {
                Pair<Q, Q> coreachPair = new Pair<Q, Q>(q01, q02);
                coreachablePairs.add(coreachPair);
                heap.push(coreachPair);
            }
        }


        while (!heap.isEmpty()) {
            Pair<Q, Q> coreachPair = heap.pop();
            for(R rule1 : m1.getRules(coreachPair.a)) {
                for(R rule2 : m2.getRules(coreachPair.b)) {
                    if(rule1.getSymbol().equals(rule2.getSymbol())) {
                        Function<Integer, Integer> sigma1 = rule1.getInputPermutation();
                        Function<Integer, Integer> sigma2 = rule2.getInputPermutation();

                        for(int i = 0; i < rule1.getSymbol().getArity(); i++) {
                            int j1 = sigma1.apply(i);
                            int j2 = sigma2.apply(i);

                            Pair<Q, Q> newCoreachablePair = new Pair<Q, Q>(rule1.getDestStates().get(j1), rule2.getDestStates().get(j2));

                            // avoid cycles!
                            if(!coreachablePairs.contains(newCoreachablePair)) {
                                coreachablePairs.add(newCoreachablePair);
                                heap.add(newCoreachablePair);
                            }
                        }
                    }
                }
            }
        }

        return coreachablePairs;
    }

    /**
     * Returns the set of reachable states of a LTW.
     *
     * @param ltw the LTW
     * @return the set of reachable states
     */
    public Set<Q> getReachable(final L ltw) {
        Set<Q> reachables = new HashSet<>();
        Set<Q> active = new HashSet<>();
        reachables.addAll(ltw.getInitialStates());
        active.addAll(reachables);

        while (!active.isEmpty()) {
            Set<Q> activeTmp = new HashSet<>();

            for(R rule : ltw.getRules()) {
                if(active.contains((rule.getSrcState()))) {
                    for(Q destState : rule.getDestStates()) {
                        if(!reachables.contains(destState)) {
                            reachables.add(destState);
                            activeTmp.add(destState);
                        }
                    }
                }
            }

            active = activeTmp;
        }

        return reachables;
    }

    /**
     * Tests whether two LTWs have the same domain i.e. reads the same input.
     *
     * @param ltw1          the first LTW
     * @param ltw2          the second LTW
     * @param ltwCreator    the creator that creates both LTWs
     * @return true => the LTWs has the same domain, otherwise false
     */
    public boolean isEqualLTWDomain(final L ltw1, final L ltw2, final ILTWCreator<N, A, B, F, Q, R, L> ltwCreator) {
        TreeDFACreator<F, B, Q> creator = new TreeDFACreator<F, B, Q>();
        TreeDFAOp<F, B, Q> treeDFAOp = new TreeDFAOp<>();
        TreeDFA<F, B, Q> dfa1 = toDFA(ltw1, creator, ltwCreator);
        TreeDFA<F, B, Q> dfa2 = toDFA(ltw2, creator, ltwCreator);
        return treeDFAOp.isEquals(dfa1, dfa2);
    }

    /**
     * Transforms an LTW into a DFA such that the DFA recognizes all paths of the path-closure of the
     * recognized input of the LTW (i.e. of the underlying deterministic top-down tree automaton).
     *
     * @param ltw           the LTW
     * @param creator       a creator for the DFA
     * @param ltwCreator    the creator of the LTW
     * @return a DFA such that the DFA recognizes all paths of the path-closure of the recognized input of the LTW
     */
    public TreeDFA<F, B, Q> toDFA(final L ltw, final TreeDFACreator<F, B, Q> creator, final ILTWCreator<N, A, B, F, Q, R, L> ltwCreator) {
        Set<TreeDFARule<F, B, Q>> automataRules = new HashSet<>();
        Q initialState = ltwCreator.createFreshState();
        Set<Q> finalStates = new HashSet<>();
        Map<F, Q> finalStateMap = new HashMap<>();

        for(F f : ltw.getInputAlphabet()) {
            if(f.getArity() == 0) {
                Q state = ltwCreator.createFreshState();
                finalStateMap.put(f, state);
                finalStates.add(state);
            }
        }


        for(R ltwRule : ltw.getRules()) {
            F f = ltwRule.getSymbol();
            if(f.getArity() == 0) {
                TreeDFASymbol<F> symbol = creator.createSymbol(new Pair<>(f, 0));
                Q state = ltw.getInitialStates().contains(ltwRule.getSrcState()) ? initialState : ltwRule.getSrcState();

                automataRules.add(creator.createRule(state , finalStateMap.get(ltwRule.getSymbol()), symbol));
            }

            for(int i = 0; i < f.getArity(); i++) {
                int j = ltwRule.getInputPermutation().apply(i);
                TreeDFASymbol<F> symbol = creator.createSymbol(new Pair<>(f, i+1));
                Q destState = ltw.getInitialStates().contains(ltwRule.getDestStates().get(j)) ? initialState : ltwRule.getDestStates().get(j);
                Q srcState = ltw.getInitialStates().contains(ltwRule.getSrcState()) ? initialState : ltwRule.getSrcState();
                automataRules.add(creator.createRule(srcState, destState, symbol));
            }
        }

        return creator.create(automataRules, initialState, finalStates);
    }

    /**
     * Tests whether the LTW is deterministic, if not the LTW is not well-defined.
     *
     * @param ltw   the LTW
     * @return true => the LTW is deterministic, otherwise false
     */
    public boolean isDeterministic(final L ltw) {
        Map<Pair<Q, F>, Set<R>> rulesBySrcAndSymbol = ltw.getRules().stream().collect(Collectors.groupingBy(r -> new Pair<>(r.getSrcState(), r.getSymbol()), Collectors.toSet()));
        boolean detRules = rulesBySrcAndSymbol.values().stream().allMatch(set -> set.size() == 1);

        if(!detRules) {
            return false;
        }

        Map<F, List<R>> initRulesBySymbols = ltw.getRules().stream().filter(r -> ltw.getInitialStates().contains(r.getSrcState())).collect(Collectors.groupingBy(r -> r.getSymbol()));
        return initRulesBySymbols.values().stream().allMatch(list -> list.size() == 1);
    }

    /**
     * Returns true if the linear tree to word transducer is a sequential tree to word transducer.
     * A linear tree to word transducer LTW is a sequential tree to word transducer if there is no
     * reordering in the output => the permutation has to be the identity in each rule.
     *
     * @param ltw the linear tree to word transducer.
     * @return true if the LTW is a STW, otherwise false
     */
    public boolean isSTW(final L ltw) {
        for(R rule : ltw.getRules()) {
            Function<Integer, Integer> permutation = rule.getInputPermutation();
            for(int i = 0; i < rule.getOutputWords().size(); i++) {
                if(permutation.apply(i) != i) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Transforms a linear tree to word transducer to a cfg that generates the language L_q1 \cup L_q2 ...
     * where all q's are initial stats of the transducer.
     *
     * @return a cfg that generates the language L_q1 \cup L_q2 ... where all q's are initial stats of the transducer.
     */
    public CFG<N> toCFG(final L ltw, final Map<Q, IJezSymbol<N>> ltwToCfgMap, final CFGCreator<N> cfgCreator) {
        Set<Production<N>> productions = new HashSet<>();
        Set<IJezSymbol<N>> axioms = new HashSet<>();

        for(R rule : ltw.getRules()) {
            List<IJezSymbol<N>> right = new LinkedList<>();
            IJezSymbol<N> left;
            if(!ltwToCfgMap.containsKey(rule.getSrcState())) {
                left = cfgCreator.createFreshNonTerminal();
                ltwToCfgMap.put(rule.getSrcState(), left);
            }
            left = ltwToCfgMap.get(rule.getSrcState());

            List<Q> destStates = rule.getDestStates();
            List<SLP<N>> outputWords = rule.getOutputWords();
            for(int i = 0; i < destStates.size(); i++) {
                // 1. add u_i where u_i is an SLP
                SLP<N> slp = outputWords.get(i);
                if(slp.length() > 0) {
                    slp = cfgCreator.freshNonTerminals(slp, new HashMap<>());

                    if(!slp.isSingleton()) {
                        throw new IllegalArgumentException("the SLP is not a singleton.");
                    }
                    right.add(slp.getAxiom());

                    // add all productions for the slp
                    productions.addAll(slp.getProductions());
                }

                // add q_i where q_i will be a fresh non-terminal
                IJezSymbol<N> nonTerminal;
                if(!ltwToCfgMap.containsKey(destStates.get(i))) {
                    nonTerminal = cfgCreator.createFreshNonTerminal();
                    ltwToCfgMap.put(destStates.get(i), nonTerminal);
                }
                nonTerminal = ltwToCfgMap.get(destStates.get(i));
                right.add(nonTerminal);
            }

            // add the last u_k
            SLP<N> slp = outputWords.get(outputWords.size() - 1);
            if(slp.length() > 0) {
                slp = cfgCreator.freshNonTerminals(slp, new HashMap<>());
                if(!slp.isSingleton()) {
                    throw new IllegalArgumentException("the SLP is not a singleton.");
                }
                right.add(slp.getAxiom());
                // add all productions for the slp
                productions.addAll(slp.getProductions());
            }

            // the new production corresponding to this rule
            productions.add(cfgCreator.createProduction(left, cfgCreator.createWord(right)));
        }

        for(Q initState : ltw.getInitialStates()) {
            if(ltwToCfgMap.containsKey(initState)) {
                axioms.add(ltwToCfgMap.get(initState));
            }
        }

        return cfgCreator.createCFG(productions, axioms);
    }
}
