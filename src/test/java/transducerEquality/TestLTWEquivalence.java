package transducerEquality;

import LTW.impl.LTW;
import LTW.impl.LTWCreator;
import LTW.impl.LTWTransformation;
import grammar.impl.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the solver for the LTW equivalence problem. Here we test a very simple complete example.
 * The example is the same example as in my masters thesis in chapter results and examples.
 *
 * @author Benedikt Zoennchen
 */
public class TestLTWEquivalence {
    private static Logger logger = LogManager.getLogger(TestN2WOperations.class);

    @Test
    public void testPaperLTWtoCFG() {
        CFGCreatorFactory<Object> ruleCFGCreatorFactory = new CFGCreatorFactory<>();
        CFGCreatorFactory<Character> charSLPCreatorFactory = new CFGCreatorFactory<>();
        CFGOp<Character> cfgOp = new CFGOp<>();

        LTWCreator ltwCreator = new LTWCreator();
        CFGCreator<Character> slpCreator = charSLPCreatorFactory.create();

        /**
         * Two equivalent LTWs also described in my masters thesis.
         */
        LTW ltw1 = LTW.paperExampleLTW1(ltwCreator, slpCreator);
        LTW ltw2 = LTW.paperExampleLTW2(ltwCreator, slpCreator);

        LTWTransformation ltwOp = new LTWTransformation(ltwCreator, charSLPCreatorFactory, ruleCFGCreatorFactory);

        CFG<Character> cfg = ltwOp.toCFG(ltw1, new HashMap<>());
        CFGCreator<Character> cfgCreator = charSLPCreatorFactory.create(cfg);
        SLP<Character> slp = cfgOp.getMinimalNonEmptyWord(cfg.getProductions(), cfg.getAxiom(), cfgCreator).get();
        logger.info(slp.toString(slp.getAxiom()));
        assertTrue(slp.toString(slp.getAxiom()).equals("helloabcabcabend"));

        long startTime = System.nanoTime();
        assertTrue(ltwOp.isEquals(ltw1, ltw2));
        long endTime = System.nanoTime();
        System.out.println(((endTime - startTime) / 1000000.0));
    }
}
