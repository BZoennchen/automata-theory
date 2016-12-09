package morphismEquality;

import grammar.impl.*;
import morphismEq.morphisms.GenDefaultMorphism;
import grammar.parser.CharGrammarParser;
import morphismEq.impl.MorphismEQSolver;
import symbol.IJezSymbol;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the solver for the morphism equivalence problem on CFGs.
 *
 * @author Benedikt Zoennchen
 */
public class TestMorphismEQSolver {

    private static Logger logger = LogManager.getLogger(TestMorphismEQSolver.class);

    @Test
    public void testGraph() {

        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);

        String text =
                "S -> abB \n" +
                "B ->  \n" +
                "B -> abB";

        Set<Production<Character>> productions = parser.createProductions(text);
        Set<IJezSymbol<Character>> axiom = new HashSet<>();
        axiom.add(cfgCreator.lookupSymbol('S', false));

        Map<IJezSymbol<Character>, List<IJezSymbol<Character>>> f = new HashMap<>();
        Map<IJezSymbol<Character>, List<IJezSymbol<Character>>>  g = new HashMap<>();
        Map<IJezSymbol<Character>, List<IJezSymbol<Character>>>  h = new HashMap<>();

        IJezSymbol<Character> a = cfgCreator.lookupSymbol('a', true);
        IJezSymbol<Character> b = cfgCreator.lookupSymbol('b', true);

        f.put(a, Arrays.asList(a, b));        // abab -> ab ba ab ba  = abbaabba
        f.put(b, Arrays.asList(b, a));

        g.put(a, Arrays.asList(a));          // abab -> a bba a bba = abbaabba
        g.put(b, Arrays.asList(b, b, a));

        h.put(a, Arrays.asList(a));          // abab -> a abb a abb = aabbaabb
        h.put(b, Arrays.asList(a, b, b));

        GenDefaultMorphism<Character> morphism1 = new GenDefaultMorphism<>(f::get);
        GenDefaultMorphism<Character> morphism2 = new GenDefaultMorphism<>(g::get);
        GenDefaultMorphism<Character> morphism3 = new GenDefaultMorphism<>(h::get);


        CFGOp<Character> cfgOp = new CFGOp<>();
        Set<Production<Character>> wCNFProductions = cfgOp.toWeakCNF(productions, axiom, cfgCreator);

        MorphismEQSolver<Character, Character> morphismEQSolver = new MorphismEQSolver<>(cfgCreator.createCFG(wCNFProductions, axiom), cfgCreator, factory, new CFGCreatorFactory<>());
        assertTrue(morphismEQSolver.equivalentOnMorphisms(morphism1, morphism2));
        assertFalse(morphismEQSolver.equivalentOnMorphisms(morphism1, morphism3));
    }


    @Test
    public void testSLPEqualityGraph() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);

        String text =
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
                "V -> ghiXmn \n" +
                "Z -> jk \n" +
                "X -> Zllllkm";

        Set<Production<Character>> productions = parser.createProductions(text);
        Set<IJezSymbol<Character>> axiom = new HashSet<>();
        axiom.add(cfgCreator.lookupSymbol('S', false));

        CFGOp<Character> cfgOp = new CFGOp<>();
        Set<Production<Character>> wCNFProductions = cfgOp.toWeakCNF(productions, axiom, cfgCreator);
        MorphismEQSolver<Character, Character> morphismEQSolver = new MorphismEQSolver<>(cfgCreator.createCFG(wCNFProductions, axiom), cfgCreator, factory, factory);

        GenDefaultMorphism<Character> identity = new GenDefaultMorphism<>(s -> Arrays.asList(s));
        assertTrue(morphismEQSolver.equivalentOnMorphisms(identity, identity));
    }

    /**
     * Extended example from the Plandowski-Phd.
     */
    @Test
    public void testComplexGraph() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        SLPOp<Character> slpOp = new SLPOp();
        CFGOp<Character> cfgOp = new CFGOp<>();

        // L(G) = {(ab)^1 : i >= 1}
        String text =
                "S -> bAs \n" +
                "A -> iBa \n" +
                "B -> aCi \n" +
                "C -> sd \n" +
                "D -> SSESS \n" +
                "E -> zSSESSr \n" +
                "E -> uu \n";

        Map<IJezSymbol<Character>, List<IJezSymbol<Character>>> f = new HashMap<>();
        Map<IJezSymbol<Character>, List<IJezSymbol<Character>>>  g = new HashMap<>();

        IJezSymbol<Character> a = cfgCreator.lookupSymbol('a', true);
        IJezSymbol<Character> b = cfgCreator.lookupSymbol('b', true);
        IJezSymbol<Character> d = cfgCreator.lookupSymbol('d', true);
        IJezSymbol<Character> i = cfgCreator.lookupSymbol('i', true);
        IJezSymbol<Character> s = cfgCreator.lookupSymbol('s', true);
        IJezSymbol<Character> r = cfgCreator.lookupSymbol('r', true);
        IJezSymbol<Character> u = cfgCreator.lookupSymbol('u', true);
        IJezSymbol<Character> z = cfgCreator.lookupSymbol('z', true);

        IJezSymbol<Character> one = cfgCreator.lookupSymbol('1', true);
        IJezSymbol<Character> two = cfgCreator.lookupSymbol('2', true);
        IJezSymbol<Character> three = cfgCreator.lookupSymbol('3', true);

        f.put(a, Arrays.asList(one, two));
        f.put(b, Arrays.asList(three, two));
        f.put(d, Arrays.asList(one, one, one, two));
        f.put(i, Arrays.asList(two));
        f.put(s, Arrays.asList(one, one, two));
        f.put(r, Arrays.asList(r));
        f.put(u, Arrays.asList(u));
        f.put(z, Arrays.asList(z));


        g.put(a, Arrays.asList(one, two, one));
        g.put(b, Arrays.asList(three));
        g.put(d, Arrays.asList(one, one, one));
        g.put(i, Arrays.asList(two, two));
        g.put(s, Arrays.asList(one, two));
        g.put(r, Arrays.asList(r));
        g.put(u, Arrays.asList(u));
        g.put(z, Arrays.asList(z));


        Set<Production<Character>> productions = parser.createProductions(text);
        Set<IJezSymbol<Character>> axiom = new HashSet<>();
        axiom.add(cfgCreator.lookupSymbol('D', false));


        Set<Production<Character>> wCNFProductions = cfgOp.toWeakCNF(productions, axiom, cfgCreator);
        MorphismEQSolver<Character, Character> morphismEQSolver = new MorphismEQSolver<>(cfgCreator.createCFG(wCNFProductions, axiom), cfgCreator, new CFGCreatorFactory<>(), new CFGCreatorFactory<>());

        GenDefaultMorphism<Character> morphism1 = new GenDefaultMorphism<>(f::get);
        GenDefaultMorphism<Character> morphism2 = new GenDefaultMorphism<>(g::get);
        long ms = System.currentTimeMillis();
        assertTrue(morphismEQSolver.equivalentOnMorphisms(morphism1, morphism2));
        logger.info("Running time: " + (System.currentTimeMillis() - ms) + "[ms]");
    }
}
