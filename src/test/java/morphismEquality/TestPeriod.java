package morphismEquality;

import grammar.impl.*;
import grammar.parser.CharGrammarParser;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the solver for the periodicity problem.
 *
 * @author Benedikt Zoennchen
 */
public class TestPeriod {

    @Test
    public void testHasSameSimplePeriod() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp<Character> cfgOp = new CFGOp();
        SLPOp<Character> slpOp = new SLPOp<>();

        String cfgCode =
                "S -> A \n" +
                "A -> AA \n" +
                "A -> ab";

        String slpCode =
                "S -> ab";

        CFG<Character> cfg = parser.create(cfgCode);
        SLP<Character> slp = parser.createSLP(slpCode);

        assertTrue(slpOp.hasSamePeriod(cfg, slp, cfgCreator, factory));
    }


    @Test
    public void testSameGrammarPeriodSimple() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp<Character> cfgOp = new CFGOp<>();
        SLPOp<Character> slpOp = new SLPOp<>();

        // period is abab
        String cfgCode1 =
                "S -> TTDTT \n" +
                "T -> ab\n" +
                "D -> abTDTab \n" +
                "D -> ";

        // period is abab
        String cfgCode2 =
                "S -> AA \n" +
                "A -> ab\n" +
                "A -> AAA";

        CFG<Character> cfg1 = parser.create(cfgCode1);
        CFG<Character> cfg2 = parser.create(cfgCode2);

        SLP<Character> slp = cfgOp.getMinimalNonEmptyWord(cfgCreator.copyProductions(cfg1.getProductions()), cfg1.getAxiom(), cfgCreator).get();

//        assertTrue(slpOp.samePeriod(cfg1, slp, cfgCreator, cfgOp));
//        assertTrue(slpOp.samePeriod(cfg2, slp, cfgCreator, cfgOp));

        assertTrue(slpOp.hasSamePeriod(cfg1, slp, cfgCreator, factory));
        assertTrue(slpOp.hasSamePeriod(cfg2, slp, cfgCreator, factory));
    }
}
