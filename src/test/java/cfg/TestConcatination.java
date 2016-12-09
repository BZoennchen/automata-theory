package cfg;

import grammar.impl.*;
import grammar.parser.CharGrammarParser;
import symbol.IJezSymbol;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the concatenation operation for SLPs.
 *
 * @author Benedikt Zoennchen
 */
public class TestConcatination {

    @Test
    public void testSimpleConcatenation() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp<Character> slpOp = new SLPOp();

        String code =
                "S -> aBa \n" +
                "B -> bAb \n" +
                "A -> aCa \n" +
                "C -> cDc \n" +
                "D -> FF \n" +
                "F -> ffGff \n" +
                "G -> g \n";

        String text = "abacffgffffgffcaba";

        Set<Production<Character>> productionSet = parser.createProductions(code);
        Set<IJezSymbol<Character>> axioms = new HashSet<>();
        axioms.add(cfgCreator.lookupSymbol('S', false));
        SLP<Character> slp = cfgCreator.createSLP(productionSet, axioms);

        List<IJezSymbol<Character>> list = new ArrayList<>();
        list.add(cfgCreator.lookupSymbol('S', false));
        list.add(cfgCreator.lookupSymbol('S', false));
        SLP<Character> concatSLP = slpOp.concatenate(slp, cfgCreator, list);
        assertTrue(concatSLP.toString(concatSLP.getAxioms().iterator().next()).equals(text+""+text));
    }


    @Test
    public void testConcatenationEquality() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp<Character> slpOp = new SLPOp();

        String code =
            "S -> aBa \n" +
            "B -> bAb \n" +
            "A -> aCa \n" +
            "C -> cDc \n" +
            "D -> FF \n" +
            "F -> GfGfabgabg \n" +
            "H -> abgfGfGG \n" +
            "G -> abg \n";

        String text = "abacffgffffgffcaba";

        Set<Production<Character>> productionSet = parser.createProductions(code);
        Set<IJezSymbol<Character>> axioms = new HashSet<>();
        axioms.add(cfgCreator.lookupSymbol('S', false));
        SLP<Character> slp = cfgCreator.createSLP(productionSet, axioms);

        List<IJezSymbol<Character>> word1 = new ArrayList<>();
        List<IJezSymbol<Character>> word2 = new ArrayList<>();
        word1.add(cfgCreator.lookupSymbol('B', false));
        word1.add(cfgCreator.lookupSymbol('S', false));
        word1.add(cfgCreator.lookupSymbol('H', false));

        word2.add(cfgCreator.lookupSymbol('B', false));
        word2.add(cfgCreator.lookupSymbol('S', false));
        word2.add(cfgCreator.lookupSymbol('F', false));

        List<List<IJezSymbol<Character>>> words = new ArrayList<>();
        words.add(word1);
        words.add(word2);

        SLP<Character> concatSLP = slpOp.concatenateWords(slp, cfgCreator, words);
        assertTrue(slpOp.equal(concatSLP, factory));
    }
}
