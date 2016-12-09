package N2W.gen;

import N2W.inter.IN2WRule;
import N2W.inter.IN2WTransducer;
import N2W.inter.INestedLetter;
import symbol.INamedSymbol;
import utils.Pair;
import utils.Triple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benedikt Zoennchen
 */
public class GenN2WOp
        <N, A, B, C,
                Q extends INamedSymbol<A>,
                W extends INestedLetter<B>,
                G extends INamedSymbol<C>,
                R extends IN2WRule<N, A, B, C, Q, W, G>,
                M extends IN2WTransducer<N, A, B, C, Q, W, G, R>>
        {

    public boolean isDeterministic(final M n2wTransducer) {
        Set<R> openingRules = n2wTransducer.getOpeningRules();
        Set<R> closingRules = n2wTransducer.getClosingRules();
        Set<Q> initialStates = n2wTransducer.getInitialStates();

        /**
         * (1) there are at most 1 initial state.
         */
        boolean deterministic = initialStates.size() <= 1;

        /**
         * (2) for each state q and input letter a there are at most 1 opening rule to choose from.
         */
        deterministic = deterministic && n2wTransducer.getOpeningRules().stream().map(r -> new Pair<>(r.getStartState(), r.getNestedWord().getElement())).distinct().count() == openingRules.size();

        /**
         * (3) for each state q and input letter a and stack symbol c there are at most 1 closing rule to choose from.
         */
        deterministic = deterministic && n2wTransducer.getClosingRules().stream().map(r -> new Triple<>(r.getStartState(), r.getNestedWord().getElement(), r.getStackSymbol())).distinct().count() == closingRules.size();


        return deterministic;
    }

    public Set<Pair<Q, Q>> getCoreachableStates(final M n2wTransducer1, final M n2wTransducer2) {
        Set<Pair<Q, Q>> coreachablePairs = new HashSet<>();
        LinkedList<Pair<Q, Q>> heap = new LinkedList<>();

        Map<Q, Set<R>> rules1 = n2wTransducer1.getRules().stream().collect(Collectors.groupingBy(r -> r.getStartState(), Collectors.toSet()));
        Map<Pair<Q, B>, Set<R>> opening2 = n2wTransducer2.getOpeningRules().stream().collect(Collectors.groupingBy(r -> new Pair<>(r.getStartState(), r.getNestedWord().getElement()), Collectors.toSet()));
        Map<Pair<Q, B>, Set<R>> closing2 = n2wTransducer2.getClosingRules().stream().collect(Collectors.groupingBy(r -> new Pair<>(r.getStartState(), r.getNestedWord().getElement()), Collectors.toSet()));


        for(Q q01 : n2wTransducer1.getInitialStates()) {
            for(Q q02 : n2wTransducer2.getInitialStates()) {
                Pair<Q, Q> coreachPair = new Pair<>(q01, q02);
                coreachablePairs.add(coreachPair);
                heap.push(coreachPair);
            }
        }

        while (!heap.isEmpty()) {
            Pair<Q, Q> coreachPair = heap.pop();
            Set<R> ruleSet1 = rules1.get(coreachPair.a);

            if(ruleSet1 != null) {
                for(R rule1 : ruleSet1) {
                    Set<R> ruleSet2;
                    if(rule1.getNestedWord().isOpening()) {
                        ruleSet2 = opening2.get(new Pair<>(coreachPair.b, rule1.getNestedWord().getElement()));
                    }
                    else {
                        ruleSet2 = closing2.get(new Pair<>(coreachPair.b, rule1.getNestedWord().getElement()));
                    }

                    if(ruleSet2 != null) {

                        for(R rule2 : ruleSet2) {
                            Pair<Q,Q> newCoreachablePair = new Pair<>(rule1.getEndState(), rule2.getEndState());

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

    public Set<Q> getReachables(final Q q, final M n2w) {
        Set<Q> reachables = new HashSet<>();
        reachables.add(q);
        boolean added = true;
        while (added == true) {
            added = false;
            for(R rule : n2w.getRules()) {
                if (reachables.contains(rule.getStartState()) && !reachables.contains(rule.getEndState())) {
                    reachables.add(rule.getEndState());
                    added = true;
                }
            }
        }
        return reachables;
    }

    public boolean isTopDown(final M n2wTransducer) {
        Set<Q> states = n2wTransducer.getStates();
        Set<G> stackAlphabet = n2wTransducer.getStackAlphabet();
        Set<R> closingRules = n2wTransducer.getClosingRules();
        Set<R> rules = n2wTransducer.getRules();

        /**
         * (1) stack alphabet has to be equals the states, here we test if the states contains all stack symbols because
         * some states may not be part of the stackAlphabet (e.g. the initial state that will not be visited again).
         */
        boolean topDown = states.containsAll(stackAlphabet);

        /**
         * (2) all closing rules have a stack symbol equals to the end state.
         */
        topDown = topDown && closingRules.stream().filter(r -> r.getStackSymbol().equals(r.getEndState())).count() == closingRules.size();

        return topDown;
    }

    public boolean isTopDownDeterministic(final M n2wTransducer) {
        return isTopDown(n2wTransducer) && isDeterministic(n2wTransducer);
    }

}
