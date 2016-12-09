package LTW.impl;

import LTW.gen.GenLTW;
import grammar.impl.CFGCreator;
import grammar.impl.SLP;
import grammar.parser.CharGrammarParser;
import symbol.StdState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author Benedikt Zoennchen
 */
public class LTW extends GenLTW<Character,Character,Integer,RankedSymbol,StdState,LTWRule> {
    public LTW(final Set<LTWRule> rules, final Set<StdState> initialStates) {
        super(rules, initialStates);
    }


    /**
     * Creates an LTW for testing equality.
     *
     * @param ltwCreator    the LTWCreator for creating the LTW
     * @param cfgCreator    the CFGCreator for creating output words
     * @return
     */
    public static LTW paperExampleLTW2(final LTWCreator ltwCreator, final CFGCreator<Character> cfgCreator) {
        StdState q0 = ltwCreator.createState(0);
        StdState q1 = ltwCreator.createState(1);
        StdState q2 = ltwCreator.createState(2);
        StdState q3 = ltwCreator.createState(3);
        StdState q4 = ltwCreator.createState(4);
        StdState q5 = ltwCreator.createState(5);
        StdState q6 = ltwCreator.createState(6);

        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        String b = "S -> b";
        String helloa = "S -> helloa";
        String bca = "S -> bca";
        String cab = "S -> cab";
        String cabend = "S -> cabend";

        SLP<Character> slpB = parser.createSLP(b);
        SLP<Character> slpHelloa = parser.createSLP(helloa);
        SLP<Character> slpBca = parser.createSLP(bca);
        SLP<Character> slpCab = parser.createSLP(cab);
        SLP<Character> slpCabend = parser.createSLP(cabend);
        SLP<Character> emptySLP = cfgCreator.emptyWord();


        RankedSymbol symbplF = new RankedSymbol('f', 3);
        RankedSymbol symbolG = new RankedSymbol('g', 1);
        RankedSymbol symbolH = new RankedSymbol('h', 0);

        int[] permutation1 = new int[3];
        permutation1[0] = 1;
        permutation1[1] = 0;
        permutation1[2] = 2;

        LTWRule rule1 = new LTWRule(q0, symbplF, Arrays.asList(q2, q3, q4), Arrays.asList(slpHelloa, emptySLP, slpCabend, emptySLP), i -> permutation1[i]);
        LTWRule rule2 = new LTWRule(q1, symbolG, Arrays.asList(q2), Arrays.asList(slpBca, emptySLP), i -> i);
        LTWRule rule3 = new LTWRule(q2, symbolG, Arrays.asList(q2), Arrays.asList(slpBca, emptySLP), i -> i);
        LTWRule rule4 = new LTWRule(q2, symbolH, new ArrayList<>(), Arrays.asList(slpB), i -> i);
        LTWRule rule5 = new LTWRule(q3, symbolG, Arrays.asList(q5), Arrays.asList(slpCab, emptySLP), i -> i);
        LTWRule rule6 = new LTWRule(q5, symbolG, Arrays.asList(q5), Arrays.asList(slpCab, emptySLP), i -> i);
        LTWRule rule7 = new LTWRule(q5, symbolH, new ArrayList<>(), Arrays.asList(emptySLP), i -> i);
        LTWRule rule8 = new LTWRule(q4, symbolG, Arrays.asList(q6), Arrays.asList(emptySLP, emptySLP), i -> i);
        LTWRule rule9 = new LTWRule(q6, symbolG, Arrays.asList(q6), Arrays.asList(emptySLP, emptySLP), i -> i);
        LTWRule rule10 = new LTWRule(q6, symbolH, new ArrayList<>(), Arrays.asList(emptySLP), i -> i);

        Set<LTWRule> rules = new HashSet<>();
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        rules.add(rule4);
        rules.add(rule5);
        rules.add(rule6);
        rules.add(rule7);
        rules.add(rule8);
        rules.add(rule9);
        rules.add(rule10);

        Set<StdState> initialStates = new HashSet<>();
        initialStates.add(q0);

        return new LTW(rules, initialStates);
    }

    /**
     * Creates an LTW for testing equality. This LTW is the LTW from Raphaela's paper!
     *
     * @param ltwCreator    the LTWCreator for creating the LTW
     * @param cfgCreator    the CFGCreator for creating output words
     * @return
     */
    public static LTW paperExampleLTW1(final LTWCreator ltwCreator, final CFGCreator<Character> cfgCreator) {
        StdState q0 = ltwCreator.createState(0);
        StdState q1 = ltwCreator.createState(1);
        StdState q2 = ltwCreator.createState(2);
        StdState q3 = ltwCreator.createState(3);
        StdState q4 = ltwCreator.createState(4);
        StdState q5 = ltwCreator.createState(5);

        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        String h = "S -> h";
        String a = "S -> a";
        String b = "S -> b";
        String c = "S -> c";
        String o = "S -> o";
        String ell = "S -> ell";
        String bca = "S -> bca";
        String abc = "S -> abc";
        String abend = "S -> abend";

        SLP<Character> slpH = parser.createSLP(h);
        SLP<Character> slpA = parser.createSLP(a);
        SLP<Character> slpB = parser.createSLP(b);
        SLP<Character> slpC = parser.createSLP(c);
        SLP<Character> slpO = parser.createSLP(o);
        SLP<Character> slpEll = parser.createSLP(ell);
        SLP<Character> slpBca = parser.createSLP(bca);
        SLP<Character> slpAbc = parser.createSLP(abc);
        SLP<Character> slpAbend = parser.createSLP(abend);
        SLP<Character> emptySLP = cfgCreator.emptyWord();


        RankedSymbol symbplF = new RankedSymbol('f', 3);
        RankedSymbol symbolG = new RankedSymbol('g', 1);
        RankedSymbol symbolH = new RankedSymbol('h', 0);

        int[] permutation1 = new int[3];
        permutation1[0] = 2;
        permutation1[1] = 1;
        permutation1[2] = 0;
        LTWRule rule1 = new LTWRule(q0, symbplF, Arrays.asList(q1, q2, q3), Arrays.asList(slpH, slpA, slpB, emptySLP), i -> permutation1[i]);

        LTWRule rule2 = new LTWRule(q1, symbolG, Arrays.asList(q4), Arrays.asList(slpEll, emptySLP), i -> i);
        LTWRule rule3 = new LTWRule(q4, symbolG, Arrays.asList(q4), Arrays.asList(emptySLP, emptySLP), i -> i);

        LTWRule rule4 = new LTWRule(q2, symbolG, Arrays.asList(q2), Arrays.asList(emptySLP, slpBca), i -> i);
        LTWRule rule5 = new LTWRule(q4, symbolH, new ArrayList<>(), Arrays.asList(slpO), i -> i);

        LTWRule rule6 = new LTWRule(q2, symbolH, new ArrayList<>(), Arrays.asList(slpBca), i -> i);
        LTWRule rule7 = new LTWRule(q5, symbolG, Arrays.asList(q5), Arrays.asList(slpAbc, emptySLP), i -> i);

        LTWRule rule8 = new LTWRule(q3, symbolG, Arrays.asList(q5), Arrays.asList(slpC, slpAbend), i -> i);
        LTWRule rule9 = new LTWRule(q5, symbolH, new ArrayList<>(), Arrays.asList(emptySLP), i -> i);

        Set<LTWRule> rules = new HashSet<>();
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        rules.add(rule4);
        rules.add(rule5);
        rules.add(rule6);
        rules.add(rule7);
        rules.add(rule8);
        rules.add(rule9);

        Set<StdState> initialStates = new HashSet<>();
        initialStates.add(q0);

        return new LTW(rules, initialStates);
    }

    public static LTW exampleN2W() {
        StdState q0 = new StdState(0);
        StdState q1 = new StdState(1);
        StdState q2 = new StdState(2);

        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        String grammar1 = "S -> aBaaaBaaaB \n B -> eCCCee \n C -> c";
        String grammar2 = "A -> aDaaaDaaaD \n D -> eECee \n C -> c \n E -> CC";

        CharGrammarParser parser = new CharGrammarParser(cfgCreator);

        SLP<Character> slp1 = parser.createSLP(grammar1);
        SLP<Character> slp2 = parser.createSLP(grammar2);

        RankedSymbol f = new RankedSymbol('f', 2);
        RankedSymbol g = new RankedSymbol('g', 1);
        RankedSymbol a = new RankedSymbol('a', 0);

        LTWRule rule1 = new LTWRule(q0, f, Arrays.asList(q1, q2), Arrays.asList(slp1, slp1, slp2), i -> i);
        LTWRule rule2 = new LTWRule(q1, g, Arrays.asList(q2), Arrays.asList(slp1, slp2), i -> i);
        LTWRule rule3 = new LTWRule(q2, a, new ArrayList<>(), Arrays.asList(cfgCreator.emptyWord()), i -> i);
        Set<LTWRule> rules = new HashSet<>();
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);

        Set<StdState> initialStates = new HashSet<>();
        initialStates.add(q0);

        return new LTW(rules, initialStates);

    }

    public static LTW smallExampleN2W(final LTWCreator creator) {
        StdState q0 = creator.createFreshState();

        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        String grammar1 = "S -> aBaaaBaaaB \n B -> eCCCee \n C -> c";

        CharGrammarParser parser = new CharGrammarParser(cfgCreator);

        SLP<Character> slp1 = parser.createSLP(grammar1);

        RankedSymbol a = new RankedSymbol('a', 0);

        LTWRule rule1 = new LTWRule(q0, a, new ArrayList<>(), Arrays.asList(slp1), i -> i);
        Set<LTWRule> rules = new HashSet<>();
        rules.add(rule1);
        Set<StdState> initialStates = new HashSet<>();
        initialStates.add(q0);

        return new LTW(rules, initialStates);

    }
}
