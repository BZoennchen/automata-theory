package transducerEquality;

import LTW.impl.LTW;
import LTW.impl.LTWCreator;
import LTW.impl.LTWTransformation;
import grammar.impl.CFGCreatorFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the solver for the STW equivalence problem. Here we test a very simple complete example.
 *
 * @author Benedikt Zoennchen
 */
public class TestSTWEquivalence {
    private static Logger logger = LogManager.getLogger(TestSTWEquivalence.class);

    @Test
    public void testSmallReduction() {
        CFGCreatorFactory<Object> ruleCFGCreatorFactory = new CFGCreatorFactory<>();
        CFGCreatorFactory<Character> charSLPCreatorFactory = new CFGCreatorFactory<>();
        LTWCreator ltwCreator = new LTWCreator();
        LTW ltw = LTW.smallExampleN2W(ltwCreator);
        LTWTransformation ltwOp = new LTWTransformation(ltwCreator, charSLPCreatorFactory, ruleCFGCreatorFactory);

        long ms = System.currentTimeMillis();
        assertTrue(ltwOp.isEqualLTWDomain(ltw, ltw));
        assertTrue(ltwOp.isEqualSTWImage(ltw, ltw));
        logger.info("Running time: " + (System.currentTimeMillis() - ms) + "[ms]");

    }
}
