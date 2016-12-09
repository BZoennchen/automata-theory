package grammar.parser;

import grammar.impl.Production;
import grammar.impl.CFG;
import grammar.impl.CFGCreator;
import grammar.impl.SLP;
import symbol.IJezSymbol;
import utils.Pair;

import java.util.*;

/**
 * A very simple parser which pareses a string of the form
 *
 * S -> bAcdD
 * A -> DeayY
 * ...
 *
 * into a grammar. Each production has to be separated by a newline '\n' and each upper case
 * character will be interpreted as a non-terminal and each lower case character as a terminal
 * symbol. The left-hand side of the first production will be the axiom of the CFG or the SLP.
 *
 * @author Benedikt Zoennchen
 */
public class CharGrammarParser {

    private CFGCreator<Character> cfgCreator;

    public CharGrammarParser(final CFGCreator<Character> cfgCreator) {
        this.cfgCreator = cfgCreator;
    }

    public CFG<Character> create(final String code) {
        Pair<Set<Production<Character>>, Set<IJezSymbol<Character>>> grammarPair = createGrammar(code);
        return cfgCreator.createCFG(grammarPair.a, grammarPair.b);
    }

    public SLP<Character> createSLP(final String code) {
        Pair<Set<Production<Character>>, Set<IJezSymbol<Character>>> grammarPair = createGrammar(code);
        return cfgCreator.createSLP(grammarPair.a, grammarPair.b);
    }

    private Pair<Set<Production<Character>>, Set<IJezSymbol<Character>>> createGrammar(final String code) {
        Set<Production<Character>> set = new HashSet<>();
        Set<IJezSymbol<Character>> axioms = new HashSet<>();
        String[] productions = code.split("\n");

        for(int i = 0; i < productions.length; i++) {
            String[] production = productions[i].split("->");

            if(production.length != 2) {
                error();
            }

            if(production[0].trim().length() != 1) {
                error();
            }

            IJezSymbol<Character> left = cfgCreator.lookupSymbol(production[0].trim().charAt(0), false);
            List<IJezSymbol<Character>> right = new ArrayList<>();


            for(char c : production[1].trim().toCharArray()) {
                right.add(cfgCreator.lookupSymbol(c, Character.isLowerCase(c)));
            }
            set.add(cfgCreator.createProduction(left, cfgCreator.createWord(right)));
            if(axioms.isEmpty()) {
                axioms.add(left);
            }
        }

        return new Pair<>(set, axioms);
    }

    public Set<Production<Character>> createProductions(final String code) {
        Set<Production<Character>> set = new HashSet<>();
        String[] productions = code.split("\n");

        for(int i = 0; i < productions.length; i++) {
            String[] production = productions[i].split("->");

            if(production.length != 2) {
                error();
            }

            if(production[0].trim().length() != 1) {
                error();
            }

            IJezSymbol<Character> left = cfgCreator.lookupSymbol(production[0].trim().charAt(0), false);
            List<IJezSymbol<Character>> right = new ArrayList<>();


            for(char c : production[1].trim().toCharArray()) {
                right.add(cfgCreator.lookupSymbol(c, Character.isLowerCase(c)));
            }
            set.add(cfgCreator.createProduction(left, cfgCreator.createWord(right)));
        }
        return set;
    }

    private void error() throws IllegalFormatException {
        throw new IllegalArgumentException("could not parse grammar.");
    }

}
