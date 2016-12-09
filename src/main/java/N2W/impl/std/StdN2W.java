package N2W.impl.std;

import N2W.gen.GenN2W;
import symbol.StdState;

import java.util.*;

/**
 * Standard implementation of a GenN2W. This class also support some constructions of N2W examples.
 *
 * @author Benedikt Zoennchen
 */
public class StdN2W extends GenN2W<List<StdNestedWord>, Integer, Character, Integer, StdState, StdNestedWord, StdStackSymbol, StdN2WRule> {
    public StdN2W(Set<StdN2WRule> rules, Set<StdState> initialStates, Set<StdState> finalStates) {
        super(rules, initialStates, finalStates);
    }

    /**
     * Generates the top-down dN2W from the Staworko, Laurence, Lemay paper which read nested words and outputs nested words.
     * The input alphabet is {(op,a),(cl,a),(op,b),(cl,b)} and the output alphabet is {(op,a),(cl,a),(op,b),(cl,b),(op,c),(cl,c)}.
     *
     * @return an sample top-down deterministic nested word to word transducer
     */
    public static StdN2W exampleN2WLaurencePaper() {
        StdCreator creator = new StdCreator();

        StdState q0 = creator.createFreshState();
        StdState q1 = creator.createFreshState();
        StdState q2 = creator.createFreshState();
        StdState q3 = creator.createFreshState();

        StdNestedWord aOpen = creator.createNestedLetter('a', true);
        StdNestedWord aClose = creator.createNestedLetter('a', false);

        StdNestedWord bOpen = creator.createNestedLetter('b', true);
        StdNestedWord bClose = creator.createNestedLetter('b', false);

        StdNestedWord cOpen = creator.createNestedLetter('c', true);
        StdNestedWord cClose = creator.createNestedLetter('c', false);

        StdStackSymbol s0 = creator.createStackSymbol(q0.getName());
        StdStackSymbol s1 = creator.createStackSymbol(q1.getName());
        StdStackSymbol s2 = creator.createStackSymbol(q2.getName());
        StdStackSymbol s3 = creator.createStackSymbol(q3.getName());

        StdN2WRule rule1a = creator.createRule(q0, q1, aOpen, s3, Arrays.asList(cOpen));
        StdN2WRule rule1b = creator.createRule(q0, q1, bOpen, s3, Arrays.asList(cOpen));

        StdN2WRule rule2a = creator.createRule(q1, q1, aOpen, s2, new ArrayList<>());
        StdN2WRule rule2b = creator.createRule(q1, q1, bOpen, s2, new ArrayList<>());

        StdN2WRule rule3a = creator.createRule(q1, q3, aClose, s3, Arrays.asList(aOpen, aClose, cClose));
        StdN2WRule rule3b = creator.createRule(q1, q3, bClose, s3, Arrays.asList(bOpen, bClose, cClose));

        StdN2WRule rule3_a = creator.createRule(q2, q3, aClose, s3, Arrays.asList(aOpen, aClose, cClose));
        StdN2WRule rule3_b = creator.createRule(q2, q3, bClose, s3, Arrays.asList(bOpen, bClose, cClose));

        StdN2WRule rule4a = creator.createRule(q1, q2, aClose, s2, Arrays.asList(aOpen, cClose));
        StdN2WRule rule4b = creator.createRule(q1, q2, bClose, s2, Arrays.asList(bOpen, cClose));

        StdN2WRule rule4_a = creator.createRule(q2, q2, aClose, s2, Arrays.asList(aOpen, cClose));
        StdN2WRule rule4_b = creator.createRule(q2, q2, bClose, s2, Arrays.asList(bOpen, cClose));

        Set<StdN2WRule> rules = new HashSet<>();
        rules.add(rule1a);
        rules.add(rule1b);
        rules.add(rule2a);
        rules.add(rule2b);
        rules.add(rule3a);
        rules.add(rule3b);
        rules.add(rule3_a);
        rules.add(rule3_b);
        rules.add(rule4a);
        rules.add(rule4b);
        rules.add(rule4_a);
        rules.add(rule4_b);

        Set<StdState> initialStates = new HashSet<>();
        initialStates.add(q0);
        Set<StdState> finalStates = new HashSet<>();
        finalStates.add(q3);

        return creator.createTransducer(rules, initialStates, finalStates);
    }

    public static StdN2W exampleN2WSimple() {
        StdCreator creator = new StdCreator();

        StdState q0 = creator.createFreshState();

        StdNestedWord aOpen = creator.createNestedLetter('a', true);
        StdNestedWord aClose = creator.createNestedLetter('a', false);

        StdStackSymbol s0 = creator.createStackSymbol(q0.getName());

        StdN2WRule push = creator.createRule(q0, q0, aOpen, s0, Arrays.asList(aOpen));
        StdN2WRule pop = creator.createRule(q0, q0, aClose, s0, Arrays.asList(aClose));

        Set<StdN2WRule> rules = new HashSet<>();
        rules.add(push);
        rules.add(pop);

        Set<StdState> initialStates = new HashSet<>();
        initialStates.add(q0);
        Set<StdState> finalStates = new HashSet<>();
        finalStates.add(q0);

        return creator.createTransducer(rules, initialStates, finalStates);
    }
}
