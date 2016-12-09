package cfg;

import grammar.impl.CFGCreator;
import grammar.impl.Production;
import grammar.impl.SLP;
import grammar.parser.CharGrammarParser;
import symbol.IJezSymbol;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the toString method for a SLPs.
 *
 * @author Benedikt Zoennchen
 */
public class TestValue {

    @Test
    public void testSLPValue() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);

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


        assertTrue(slp.toString(axioms.iterator().next()).equals(text));
    }

}
