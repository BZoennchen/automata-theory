package transducerEquality;

import N2W.impl.std.StdN2W;
import N2W.impl.std.StdReduction;
import grammar.impl.CFGCreatorFactory;
import morphismEq.morphisms.N2WRuleMorphismList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the reduction starting from 2 N2Ws and constructing a grammar of parallel successful runs
 * and solving the morphism equivalence problem on CFGs.
 *
 * @author Benedikt Zoennchen
 */
public class TestN2WEquivalence {
    private static Logger logger = LogManager.getLogger(TestN2WEquivalence.class);

    @Test
    public void testSmallReduction() {
        CFGCreatorFactory<Object> factory = new CFGCreatorFactory<>();
        StdReduction reduction = new StdReduction(factory);

        StdN2W n2W1 = StdN2W.exampleN2WSimple();
        StdN2W n2W2 = StdN2W.exampleN2WSimple();
        assertTrue(n2W1.equals(n2W2));

        long ms = System.currentTimeMillis();
        assertTrue(reduction.isEquals(n2W1, n2W2, new N2WRuleMorphismList<>(true), new N2WRuleMorphismList<>(false), new CFGCreatorFactory<>(), new CFGCreatorFactory<>()));
        logger.info("Running time: " + (System.currentTimeMillis() - ms) + "[ms]");
    }

    @Test
    public void testTrivialReduction() {
        CFGCreatorFactory<Object> factory = new CFGCreatorFactory<>();
        StdReduction reduction = new StdReduction(factory);
        StdN2W n2W1 = StdN2W.exampleN2WLaurencePaper();
        StdN2W n2W2 = StdN2W.exampleN2WLaurencePaper();
        assertTrue(n2W1.equals(n2W2));

        long ms = System.currentTimeMillis();
        assertTrue(reduction.isEquals(n2W1, n2W2, new N2WRuleMorphismList<>(true), new N2WRuleMorphismList<>(false), new CFGCreatorFactory<>(), new CFGCreatorFactory<>()));
        logger.info("Running time: " + (System.currentTimeMillis() - ms) + "[ms]");
    }
}
