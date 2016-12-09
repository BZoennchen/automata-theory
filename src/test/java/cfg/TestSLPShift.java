package cfg;

import grammar.impl.CFGCreator;
import grammar.impl.Production;
import grammar.impl.SLP;
import grammar.impl.SLPOp;
import grammar.parser.CharGrammarParser;
import symbol.IJezSymbol;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing shift suffix/prefix operation for SLPs.
 *
 * @author Benedikt Zoennchen
 */
public class TestSLPShift {

    private static Logger logger = LogManager.getLogger(TestSLPShift.class);

    @Test
    public void testSimpleLCP() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp<Character> slpOp = new SLPOp();

        // acdbdrrdbdrrddboc -> (left) rrdbdrrddbocacdbd
        // acdbdrrdbdrrddboc -> (right) ddbocacdbdrrdbdrr
        String code =
                "S -> aAbBc \n" +
                "B -> o \n" +
                "A -> cdCCd \n" +
                "C -> bDrrE \n" +
                "D -> d \n" +
                "E -> d";

        Set<Production<Character>> productionSet = parser.createProductions(code);
        Set<IJezSymbol<Character>> axioms = new HashSet<>();
        axioms.add(cfgCreator.lookupSymbol('S', false));
        SLP<Character> slp = cfgCreator.createSLP(productionSet, axioms);

        SLP<Character> leftShiftedSLP = slpOp.shift(slp, 5, true, cfgCreator);
        SLP<Character> rightShiftedSLP = slpOp.shift(slp, 5, false, cfgCreator);

        logger.info(leftShiftedSLP.toString(leftShiftedSLP.getAxiom()));
        assertTrue(leftShiftedSLP.toString(leftShiftedSLP.getAxiom()).equals("rrdbdrrddbocacdbd"));

        logger.info(rightShiftedSLP.toString(rightShiftedSLP.getAxiom()));
        assertTrue(rightShiftedSLP.toString(rightShiftedSLP.getAxiom()).equals("ddbocacdbdrrdbdrr"));
    }



}
