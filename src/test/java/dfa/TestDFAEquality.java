package dfa;

import DFA.impl.std.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the equivalence test for DFAs.
 *
 * @author Benedikt Zoennchen
 */
public class TestDFAEquality {

    @Test
    public void testSimpleEquality() {
        StdDFACreator creator = new StdDFACreator();

        // DFA 1
        StdDFAState q0 = creator.createState();
        StdDFAState q1 = creator.createState();
        StdDFAState q2 = creator.createState();

        StdDFASymbol a = creator.createSymbol('a');
        StdDFASymbol b = creator.createSymbol('b');

        Set<StdDFARule> rules = new HashSet<>();
        Set<StdDFAState> finalStates = new HashSet<>();
        rules.add(creator.createRule(q0, q1, a));
        rules.add(creator.createRule(q1, q2, a));
        rules.add(creator.createRule(q2, q0, b));
        finalStates.add(q2);

        StdDFA dfa = creator.create(rules, q0, finalStates);

        // DFA 2
        StdDFAState p0 = creator.createState();
        StdDFAState p1 = creator.createState();
        StdDFAState p2 = creator.createState();
        StdDFAState p3 = creator.createState();
        StdDFAState p4 = creator.createState();
        StdDFAState p5 = creator.createState();
        StdDFAState p6 = creator.createState();
        StdDFAState p7 = creator.createState();

        Set<StdDFARule> rules2 = new HashSet<>();
        Set<StdDFAState> finalStates2 = new HashSet<>();

        rules2.add(creator.createRule(p0, p1, a));
        rules2.add(creator.createRule(p1, p2, a));
        rules2.add(creator.createRule(p2, p3, b));

        rules2.add(creator.createRule(p3, p4, a));
        rules2.add(creator.createRule(p4, p5, a));
        rules2.add(creator.createRule(p5, p6, b));

        rules2.add(creator.createRule(p6, p7, a));
        rules2.add(creator.createRule(p7, p5, a));

        finalStates2.add(p2);
        finalStates2.add(p5);

        StdDFA dfa2 = creator.create(rules2, p0, finalStates2);
        DFAOp dfaOp = new DFAOp();
        assertTrue(dfaOp.isEquals(dfa, dfa2));
    }

}
