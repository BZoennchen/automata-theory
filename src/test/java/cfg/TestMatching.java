package cfg;

import grammar.impl.CFGCreator;
import grammar.impl.CFGCreatorFactory;
import grammar.impl.Production;
import grammar.impl.SLPOp;
import grammar.parser.CharGrammarParser;
import symbol.IJezSymbol;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the fully compressed pattern matching for SLPs.
 *
 * @author Benedikt Zoennchen
 */
public class TestMatching {

    private static Logger logger = LogManager.getLogger(TestMatching.class);

    @Test
    public void testMatching() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp<Character> slpOp = new SLPOp();

        String text =
                "T -> bbbbbbAbbbbbabfcbbb \n" +
                "A -> aaBaa \n" +
                "B -> bbaaaCDab \n" +
                "C -> babaE \n" +
                "D -> bbbaa \n" +
                "E -> aabRbb \n" +
                "R -> abaaab \n";

        String pattern =
                "P -> aaAB \n" +
                "A -> abab \n" +
                "B -> aaab";

        Set<Production<Character>> textProductions = parser.createProductions(text);
        Set<IJezSymbol<Character>> textAxiom = new HashSet<>();
        textAxiom.add(cfgCreator.lookupSymbol('T', false));

        Set<Production<Character>> patternProductions = parser.createProductions(pattern);
        Set<IJezSymbol<Character>> patternAxiom = new HashSet<>();
        patternAxiom.add(cfgCreator.lookupSymbol('P', false));

        List<Long> occurrences = slpOp.matchingAll(cfgCreator.createSLP(textProductions, textAxiom), cfgCreator.createSLP(patternProductions, patternAxiom), factory);
        assertTrue(occurrences.size() == 2 && occurrences.get(0) == 10l && occurrences.get(1) == 16l);
        Optional<Long> second = slpOp.matching(cfgCreator.createSLP(textProductions, textAxiom), cfgCreator.createSLP(patternProductions, patternAxiom), 2L, factory);
        assertTrue(second.get() == 16l);
    }


    @Test
    public void testBlockMatching() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp<Character> slpOp = new SLPOp();

        String text =
                "T -> bbbbbbbbAb \n" +
                        "A -> bbBbb \n" +
                        "B -> bbbb \n";

        String pattern =
                "P -> bbbAb \n" +
                        "A -> bbb";

        Set<Production<Character>> textProductions = parser.createProductions(text);
        Set<IJezSymbol<Character>> textAxiom = new HashSet<>();
        textAxiom.add(cfgCreator.lookupSymbol('T', false));

        Set<Production<Character>> patternProductions = parser.createProductions(pattern);
        Set<IJezSymbol<Character>> patternAxiom = new HashSet<>();
        patternAxiom.add(cfgCreator.lookupSymbol('P', false));

        List<Long> occurrences = slpOp.matchingAll(cfgCreator.createSLP(textProductions, textAxiom), cfgCreator.createSLP(patternProductions, patternAxiom), factory);
        assertTrue(occurrences.size() == 11 && occurrences.get(0) == 0l && occurrences.get(10) == 10l);

        Optional<Long> second = slpOp.matching(cfgCreator.createSLP(textProductions, textAxiom), cfgCreator.createSLP(patternProductions, patternAxiom), 2L, factory);
        assertTrue(second.get() == 1l);
    }


}
