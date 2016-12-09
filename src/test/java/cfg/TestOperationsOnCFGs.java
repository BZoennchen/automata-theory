package cfg;


import grammar.impl.*;
import grammar.parser.CharGrammarParser;
import symbol.IJezSymbol;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for testing basic operations on CFGs.
 *
 * @author Benedikt Zoennchen
 */
public class TestOperationsOnCFGs {

    @Test
    public void testReachableSLP2() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBa \n" +
                "B -> bAb \n" +
                "A -> aCa \n" +
                "C -> cDc \n" +
                "D -> FF \n" +
                "F -> fffff \n" +
                "G -> ffBff";

        CFG<Character> cfg = parser.create(code);
        Set<Production<Character>> productives = cfgOp.eliminateUnproductives(cfg.getProductions());
        SLP<Character> slp = new SLP<>(productives.stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p)), cfg.getAxioms());

        Set<Production<Character>> reachables = cfgOp.eliminateUnreachables(slp.getProductions(), slp.getAxioms());
        Set<IJezSymbol> nonTerminals = reachables.stream().map(p -> p.getLeft()).collect(Collectors.toSet());

        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('S', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('B', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('A', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('C', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('D', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('F', false)));
        assertTrue(!nonTerminals.contains(cfgCreator.lookupSymbol('G', false)));

        assertTrue(slp.length(cfgCreator.lookupSymbol('B', false)) == 16L);
        assertTrue(slp.length(cfgCreator.lookupSymbol('F', false)) == 5L);
        assertTrue(slp.length(cfgCreator.lookupSymbol('D', false)) == 10L);
        assertTrue(slp.length(cfgCreator.lookupSymbol('C', false)) == 12L);
        assertTrue(slp.length(cfgCreator.lookupSymbol('A', false)) == 14L);
        assertTrue(slp.length(cfgCreator.lookupSymbol('S', false)) == 18L);
    }

    @Test
    public void testReachableCFG2() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBa \n" +
                "B -> bAb \n" +
                "B -> bCb \n" +
                "B -> bSb \n" +
                "A -> aCa \n" +
                "C -> cDc \n" +
                "D -> FF \n" +
                "F -> ffBff \n" +
                "G -> ffBff";

        CFG<Character> cfg = parser.create(code);
        Set<Production<Character>> reachables = cfgOp.eliminateUnreachables(cfg.getProductions(), cfg.getAxioms());
        Set<IJezSymbol> nonTerminals = reachables.stream().map(p -> p.getLeft()).collect(Collectors.toSet());

        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('S', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('B', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('A', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('C', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('D', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('F', false)));
        assertTrue(!nonTerminals.contains(cfgCreator.lookupSymbol('G', false)));
    }

    @Test
    public void testProductivesCFG2() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBa \n" +
                "B -> bAAb \n" +
                "B -> bCb \n" +
                "B -> bSb \n" +
                "B -> bAAAb \n" +
                "A -> aCAa \n" +
                "C -> cDc \n" +
                "D -> FF \n" +
                "F -> ffBff \n" +
                "H -> \n" +
                "J -> H \n" +
                "F -> f \n" +
                "G -> ffBAff";

        CFG<Character> cfg = parser.create(code);
        Set<Production<Character>> productives = cfgOp.eliminateUnproductives(cfg.getProductions());
        Set<IJezSymbol<Character>> nonTerminals = productives.stream().map(p -> p.getLeft()).collect(Collectors.toSet());

        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('F', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('D', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('C', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('B', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('S', false)));
        assertTrue(!nonTerminals.contains(cfgCreator.lookupSymbol('A', false)));
        assertTrue(!nonTerminals.contains(cfgCreator.lookupSymbol('G', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('H', false)));
        assertTrue(nonTerminals.contains(cfgCreator.lookupSymbol('J', false)));
    }


    @Test
    public void testCFGtoSLP2() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBa \n" +
                "B -> bAAb \n" +
                "B -> bCb \n" +
                "B -> bSb \n" +
                "B -> bAAAb \n" +
                "A -> aCAa \n" +
                "C -> cDc \n" +
                "D -> FF \n" +
                "F -> ffBff \n" +
                "F -> f \n" +
                "G -> ffBAff";

        CFG<Character> cfg = parser.create(code);
        Set<Production<Character>> slpProductions = cfgOp.toSLP(cfg.getProductions(), cfg.getAxioms(), true);
        SLP<Character> slp = new SLP<>(slpProductions.stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p)), cfg.getAxioms());
        assertTrue(slp.toString(cfgCreator.lookupSymbol('S', false)).equals("abcffcba"));

    }


    @Test
    public void testDeleteUselessTerminalsSLP2() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBAa \n" +
                "B -> bFb \n" +
                "A -> aaCCaa \n" +
                "C -> DccD \n" +
                "D -> d \n";

        CFG<Character> cfg = parser.create(code);
        Set<Production<Character>> set = cfgOp.eliminateUnproductives(cfg.getProductions());
        set = cfgOp.eliminateUnreachables(set, cfg.getAxioms());
        assertTrue(set.isEmpty());
    }

    @Test
    public void testEliminateUnitProductions() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBBa \n" +
                "B -> A \n" +
                "A -> C \n" +
                "C -> A \n" +
                "C -> Ac \n" +
                "C -> CC \n" +
                "A -> a";

        CFG<Character> cfg = parser.create(code);
        Set<Production<Character>> productives = cfgOp.eliminateUnitProductions(cfg.getProductions(), cfgCreator);

        assertTrue(productives.stream().map(p -> p.getRight()).noneMatch(right -> right.isSingleton() && !right.getFirst().isTerminal()));

    }

    @Test
    public void testEmptyWordProductions() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBBa \n" +
                "S -> \n" +
                "B -> bBb \n" +
                "B -> \n" +
                "A -> DaD \n" +
                "C -> DD \n" +
                "C -> A \n" +
                "C -> ccccc \n" +
                "C -> Ac \n" +
                "D -> \n" +
                "F -> BB \n" +
                "D -> dd";

        CFG<Character> cfg = parser.create(code);

        Set<IJezSymbol<Character>> emptyWordGenerators = cfgOp.getNullableNonTerminals(cfg.getProductions());
        assertTrue(emptyWordGenerators.contains(cfgCreator.lookupSymbol('S', false)));
        assertTrue(emptyWordGenerators.contains(cfgCreator.lookupSymbol('B', false)));
        assertTrue(emptyWordGenerators.contains(cfgCreator.lookupSymbol('D', false)));
        assertTrue(emptyWordGenerators.contains(cfgCreator.lookupSymbol('F', false)));
        assertTrue(emptyWordGenerators.contains(cfgCreator.lookupSymbol('C', false)));
        assertTrue(!emptyWordGenerators.contains(cfgCreator.lookupSymbol('A', false)));
    }

    @Test
    public void testMinimalNonEmptyWordLength() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String value = "aBBa";

        String code =
                "S -> aBBa \n" +    // 20
                "B -> bBb \n" +     // loop
                "B -> bAbbbb\n" +   // 9
                "A -> C \n" +       // 4
                "C -> DD \n" +      // 4
                "C -> A \n" +       // loop
                "C -> ccccc \n" +   // 5
                "C -> Ac \n" +      // loop
                "D -> dd";          // 2

        CFG<Character> cfg = parser.create(code);
        assertTrue(cfgOp.getMinimalWord(cfg.getProductions(), cfg.getAxiom(), true, cfgCreator).length() == 20L);

    }

    @Test
    public void testGetEmptyProductions() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> aBBa \n" +
                "B -> bBb \n" +
                "B -> bAbbbb\n" +
                "A -> C \n" +
                "C -> DD \n" +
                "C -> DDD \n" +
                "C -> ccccc \n" +
                "D -> ";

        CFG<Character> cfg = parser.create(code);

        Set<Production<Character>> emptyProductions = cfgOp.getNullableProductions(cfg.getProductions());
        assertTrue(emptyProductions.size() == 4);

    }

    @Test
    public void testDeleteEpsilon() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp<Character> cfgOp = new CFGOp();

        String code =
                "S -> AB \n" +
                "A -> aAA \n" +
                "A -> \n" +
                "B -> bBB \n" +
                "B -> ";

        CFG<Character> cfg = parser.create(code);
        cfg = cfgOp.eliminateEpsilon(cfg.getProductions(), cfg.getAxioms(), cfgCreator);
        assertTrue(cfg.getProductions().size() == 11);

    }

    @Test
    public void testAcyclicEpsilon() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> AB \n" +
                "A -> aAA \n" +
                "A -> \n" +
                "B -> bBB \n" +
                "B -> ";

        CFG<Character> cfg = parser.create(code);

        assertFalse(cfgOp.isAcyclic(cfg.getProductions(), cfg.getAxioms()));
    }

    @Test
    public void testAcyclic2Epsilon() {
        CFGCreator<Character> cfgCreator = new CFGCreator<>();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> AB \n" +
                "A -> aB \n" +
                "A -> \n" +
                "B -> bCCb \n" +
                "C -> c \n" +
                "B -> ";

        CFG<Character> cfg = parser.create(code);

        assertTrue(cfgOp.isAcyclic(cfg.getProductions(), cfg.getAxioms()));
    }

    @Test
    public void testIsSingleton() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> AB \n" +
                "A -> aB \n" +
                "A -> \n" +
                "B -> bCCb \n" +
                "C -> c \n" +
                "B -> ";

        CFG<Character> cfg = parser.create(code);

        assertFalse(cfgOp.isSingleton(cfg.getProductions(), cfg.getAxioms(), factory));
    }

    @Test
    public void testIsSingleton2() {
        CFGCreatorFactory<Character> factory = new CFGCreatorFactory<>();
        CFGCreator<Character> cfgCreator = factory.create();
        CharGrammarParser parser = new CharGrammarParser(cfgCreator);
        CFGOp cfgOp = new CFGOp();

        String code =
                "S -> AB \n" +
                "A -> aB \n" +
                "A -> abccb\n" +
                "B -> bCCb \n" +
                "C -> c \n" +
                "D -> cc \n" +
                "B -> bDb";

        CFG<Character> cfg = parser.create(code);

        assertTrue(cfgOp.isSingleton(cfg.getProductions(), cfg.getAxioms(), factory));
    }
}
