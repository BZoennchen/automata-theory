package transducerEquality;

import N2W.impl.std.StdN2W;
import N2W.impl.std.StdN2WOp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test class for testing some basic operations on N2Ws.
 *
 * @author Benedikt Zoennchen
 */
public class TestN2WOperations {

    @Test
    public void testDeterministic() {
        StdN2W n2W = StdN2W.exampleN2WLaurencePaper();
        StdN2WOp n2WOp = new StdN2WOp();
        assertTrue(n2WOp.isDeterministic(n2W));
    }

    @Test
    public void testTopDown() {
        StdN2W n2W = StdN2W.exampleN2WLaurencePaper();
        StdN2WOp n2WOp = new StdN2WOp();
        assertTrue(n2WOp.isTopDown(n2W));
    }

    @Test
    public void testTopDownDeterministic() {
        StdN2W n2W = StdN2W.exampleN2WLaurencePaper();
        StdN2WOp n2WOp = new StdN2WOp();
        assertTrue(n2WOp.isTopDownDeterministic(n2W));
    }
}
