package LTW.gen;

import LTW.inter.ILTWRule;
import N2W.inter.IN2WRule;
import grammar.impl.CFGCreator;
import LTW.inter.ILTW;
import LTW.inter.IRankedSymbol;
import N2W.inter.IN2WCreator;
import N2W.inter.IN2WTransducer;
import N2W.inter.INestedLetter;
import grammar.impl.SLP;
import symbol.INamedSymbol;
import utils.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The class that converts an non-copying order-preserving top-down deterministic ranked tree to word transducer S (or a same-ordered LTW) into a
 * top-down deterministic nested word to word transducer T such that [S] = [T].
 *
 * @author Benedikt Zoennchen
 *
 * @param <N1> type of the output word of the N2W and the STW
 * @param <A1> type of the element of the STW ranked alphabet and type of the nested letter of the N2W
 * @param <B1> type of the identifier of the LTW state
 * @param <F1> type of the input symbol of the LTW and identifier of the nested letter of the N2W
 * @param <Q1> type of the LTW state
 * @param <R1> type of the LTW rule and the element of the tuple (r, j) of the N2W which represents a state of the N2W
 * @param <L1> type of the LTW
 *
 * @param <Q2> type of the N2W state
 * @param <W2> type of the nested letter
 * @param <G2> type of the stack symbol of the N2W i.e. a tuple tuple (r, j), where r is the rule of the LTW
 * @param <R2> type of the N2W rule
 * @param <M2> type of the N2W
 */
public class GenSTWtoN2W<N1, A1, B1,
        F1 extends IRankedSymbol<A1>,
        Q1 extends INamedSymbol<B1>,
        R1 extends ILTWRule<N1, A1, B1, F1, Q1>,
        L1 extends ILTW<N1, A1, B1, F1, Q1, R1>,

        Q2 extends INamedSymbol<Pair<R1, Integer>>,
        W2 extends INestedLetter<F1>,
        G2 extends INamedSymbol<Pair<R1, Integer>>,
        R2 extends IN2WRule<SLP<N1>, Pair<R1, Integer>, F1, Pair<R1, Integer>, Q2, W2, G2>,
        M2 extends IN2WTransducer<SLP<N1>, Pair<R1, Integer>, F1, Pair<R1, Integer>, Q2, W2, G2, R2>
        > {

    /**
     * Converts an non-copying order-preserving top-down deterministic ranked tree to word transducer S (or a same-ordered LTW) into a
     * top-down deterministic nested word to word transducer T such that [S] = [T].
     *
     * Algorithm: States of T are tupels (r, j), where r is a rule in S and j is a marker. If the T is in state
     * (r, j) then T is working on the rule r and is just about to handle the rule q_{j+1} or to close the parenthesis
     * a if j = k).
     *
     * @param ltw           the same-ordered LTW or STW
     * @param n2wCreator
     * @return
     */
    public M2 STWtoN2W(final L1 ltw, final IN2WCreator<SLP<N1>, Pair<R1, Integer>, F1, Pair<R1, Integer>, Q2, W2, G2, R2, M2> n2wCreator) {
        SLP<N1> emptyWord = new CFGCreator<N1>().emptyWord();
        Set<R2> n2wRules = new HashSet<>();

        Set<R1> rules = ltw.getRules();
        Set<Q1> initialStates = ltw.getInitialStates();
        Set<R1> initialRules = rules.stream().filter(r -> initialStates.contains(r.getSrcState())).collect(Collectors.toSet());

        /**
         * (1): special init and final states
         */
        Q2 initialState = n2wCreator.createState(new Pair<>(null, -1));
        Q2 finalState = n2wCreator.createState(new Pair<>(null, -2));

        for (R1 r : initialRules) {

            Q2 destState = n2wCreator.createState(new Pair<>(r, 0));
            W2 nestedLetter = n2wCreator.createNestedLetter(r.getSymbol(), true);
            G2 stackSymbol = n2wCreator.createStackSymbol(finalState.getName());
            R2 initRule = n2wCreator.createRule(initialState, destState, nestedLetter, stackSymbol, r.getOutputWords().get(0));

            Q2 srcState = n2wCreator.createState(new Pair<>(r, r.getDestStates().size()));
            nestedLetter = n2wCreator.createNestedLetter(r.getSymbol(), false);
            stackSymbol = n2wCreator.createStackSymbol(finalState.getName());
            R2 finalRule = n2wCreator.createRule(srcState, finalState, nestedLetter, stackSymbol, emptyWord);

            n2wRules.add(initRule);
            n2wRules.add(finalRule);
        }

        /**
         * (2)
         */
        for(R1 r : rules) {
            List<Q1> destinationStates = r.getDestStates();
            for (int i = 0; i < destinationStates.size(); i++) {
                Q1 q = destinationStates.get(i);
                Set<R1> rulesByQ = rules.stream().filter(ltwRule -> ltwRule.getSrcState().equals(q)).collect(Collectors.toSet());

                for(R1 innerRule : rulesByQ) {
                    int j = i + 1;
                    int m = innerRule.getOutputWords().size() - 1;
                    Q2 srcState1 = n2wCreator.createState(new Pair<>(r, j - 1));
                    Q2 srcState2 = n2wCreator.createState(new Pair<>(innerRule, m));

                    Q2 destState1 = n2wCreator.createState(new Pair<>(innerRule, 0));
                    Q2 destState2 = n2wCreator.createState(new Pair<>(r, j));

                    W2 openedLetter = n2wCreator.createNestedLetter(innerRule.getSymbol(), true);
                    W2 closedLetter = n2wCreator.createNestedLetter(innerRule.getSymbol(), false);

                    G2 stackSymbol = n2wCreator.createStackSymbol(destState2.getName());

                    R2 ruleDoQj = n2wCreator.createRule(srcState1, destState1, openedLetter, stackSymbol, innerRule.getOutputWords().get(0));
                    R2 ruleDoInner = n2wCreator.createRule(srcState2, destState2, closedLetter, stackSymbol, r.getOutputWords().get(j));

                    n2wRules.add(ruleDoQj);
                    n2wRules.add(ruleDoInner);
                }
            }
        }
        Set<Q2> n2wInitalStates = new HashSet<>();
        Set<Q2> n2wFinalStates = new HashSet<>();
        n2wInitalStates.add(initialState);
        n2wFinalStates.add(finalState);

        return n2wCreator.createTransducer(n2wRules, n2wInitalStates, n2wFinalStates);
    }
}
