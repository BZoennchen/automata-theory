package cfg;

import grammar.impl.*;
import grammar.parser.CharGrammarParser;
import symbol.IJezSymbol;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the equivalence of SLP-compressed words.
 *
 * @author Benedikt Zoennchen
 */
public class TestSLPEquality {

    private static Logger logger = LogManager.getLogger(TestSLPEquality.class);

    /**
     * X_1 -> X_2 X_2
     * X_2 -> X_3 X_4
     */
    @Test
    public void testEqualityLargeExample(){
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        Random random = new Random();

        for(int j = 1; j <= 500; j += 1) {
            long size = j;
            Map<IJezSymbol<Character>, Production<Character>> productionSet = new HashMap<>();
            IJezSymbol<Character> axiom = cfgCreator.createFreshNonTerminal();;
            IJezSymbol<Character> X = axiom;
            for(long i = 0; i < size; i++) {
                if(axiom == null) {
                    axiom = X;
                }
                IJezSymbol<Character> a = cfgCreator.lookupSymbol('a', true);

                IJezSymbol<Character> X_1 = cfgCreator.createFreshNonTerminal();

                IJezSymbol<Character> b = cfgCreator.lookupSymbol('b', true);
                IJezSymbol<Character> c = cfgCreator.lookupSymbol('c', true);

                Production<Character> production = cfgCreator.createProduction(X, a, X_1, b, X_1, c);
                productionSet.put(X, production);

                // last added production
                if(i+1 >= size) {
                    IJezSymbol<Character> d = cfgCreator.lookupSymbol('d', true);
                    productionSet.put(X_1, cfgCreator.createProduction(X_1, d));
                }
                X = X_1;
            }

            SLP<Character> slp = cfgCreator.createSLP(productionSet, axiom);
            SLPOp slpOp = new SLPOp();
            long startTime = System.nanoTime();
            assertTrue(slpOp.equals(slp, slp, factory, false, false));
            long endTime = System.nanoTime();
            System.out.println(size + "," + ((endTime - startTime) / 1000000.0));
        }
    }


    @Test
    public void testEqualityOf2SLPs() {
        String grammar1 = "S -> aBaaaBaaaB \n B -> eCCCee \n C -> c";
        String grammar2 = "A -> aDaaaDaaaD \n D -> eECee \n C -> c \n E -> CC";

        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);

        SLP<Character> slp1 = parser.createSLP(grammar1);
        SLP<Character> slp2 = parser.createSLP(grammar2);
        SLPOp cfgOp = new SLPOp();
        assertTrue(cfgOp.equals(slp1, slp2, factory, false, false));
    }

    @Test
    public void testEquality() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp cfgOp = new SLPOp();

        String code1 =
                "V -> eeeeeaSSeaaaaee \n" +
                "X -> eeeeeaZSeaaaaee \n" +

                "S -> sAb \n" +
                "A -> aBabababadaD \n" +
                "D -> dBB \n" +
                "B -> s \n" +

                "Z -> RabEb \n" +
                "E -> abCdBB \n" +
                "R -> sas \n" +
                "C -> abada \n";

        String code2 =
                "V -> eeeeeaSSeaaaaee \n" +
                "M -> eeeeeaZSbeaaaae \n" +

                "S -> sAb \n" +
                "A -> aBabababadaD \n" +
                "D -> dBB \n" +
                "B -> s \n" +

                "Z -> RabEb \n" +
                "E -> abCdBB \n" +
                "R -> sas \n" +
                "C -> abada \n";

        Set<Production<Character>> productionSet1 = parser.createProductions(code1);
        Set<Production<Character>> productionSet2 = parser.createProductions(code2);
        Set<IJezSymbol<Character>> axioms1 = new HashSet<>();
        Set<IJezSymbol<Character>> axioms2 = new HashSet<>();
        axioms1.add(cfgCreator.lookupSymbol('V', false));
        axioms1.add(cfgCreator.lookupSymbol('X', false));

        axioms2.add(cfgCreator.lookupSymbol('V', false));
        axioms2.add(cfgCreator.lookupSymbol('M', false));

        SLP<Character> slp1 = cfgCreator.createSLP(productionSet1, axioms1);
        SLP<Character> slp2 = cfgCreator.createSLP(productionSet2, axioms2);

        logger.info(slp1);
        logger.info(slp2);

        assertTrue(cfgOp.equal(slp1, factory));
        assertFalse(cfgOp.equal(slp2, factory));
    }


    @Test
    public void testEquality2() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp cfgOp = new SLPOp();

        String code =
                "S -> AB \n" +

                "A -> CD \n" +
                "B -> EF \n" +

                "C -> GH \n" +
                "D -> IJ \n" +
                "E -> KL \n" +
                "F -> MN \n" +

                "G -> g \n" +
                "H -> h \n" +
                "I -> i \n" +
                "J -> j \n" +
                "K -> k \n" +
                "L -> llllkm \n" +
                "M -> m \n" +
                "N -> n \n" +

                "V -> ghiXmn \n"+
                "X -> Zllllkm \n"+
                "Z -> jk \n";

        Set<Production<Character>> productionSet = parser.createProductions(code);
        Set<IJezSymbol<Character>> axioms = new HashSet<>();
        axioms.add(cfgCreator.lookupSymbol('V', false));
        axioms.add(cfgCreator.lookupSymbol('S', false));

        SLP slp = new SLP(productionSet.stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p)), axioms);
        logger.info(slp);

        assertTrue(cfgOp.equal(slp, factory));
    }
}
