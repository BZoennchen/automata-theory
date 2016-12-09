package N2W.impl.reduction;

import grammar.impl.*;
import LTW.impl.LTWRule;
import LTW.impl.RankedSymbol;
import N2W.gen.GenN2WtoCFGReduction;
import symbol.IJezSymbol;
import utils.Pair;

/**
 * This class converts a CFG that has SLPs as non-terminals to a CFG with Characters as non-terminals.
 * All SLPs will be renamed so that there is no conflict between two SLPs.
 *
 * @author Benedikt Zoennchen
 */
public class N2WtoCFGReduction extends GenN2WtoCFGReduction<
        Character,
        SLP<Character>,
        Pair<LTWRule, Integer>,
        RankedSymbol,
        Pair<LTWRule, Integer>,
        N2WState,
        NestedLetter,
        StackSymbol,
        N2WRule,
        N2W,

        IJezSymbol<Object>,
        JezWord<Object>,
        Production<Object>,
        CFG<Object>,
        SLP<Object>,

        IJezSymbol<Character>,
        JezWord<Character>,
        Production<Character>,
        CFG<Character>,
        SLP<Character>
        > {


    public N2WtoCFGReduction(final CFGCreatorFactory<Object> cfgCreator) {
        super(cfgCreator);
    }

    /*@Override
    public Pair<SLP<Character>, Map<IJezSymbol<Object>, IJezSymbol<Character>>> applyMorphism(
            final Set<Production<Object>> baseSLPProductions,
            final Set<IJezSymbol<Object>> axioms,
            final Function<Pair<N2WRule, N2WRule>, N2WRule> chooser,
            final ICFGCreator<Character, IJezSymbol<Character>, JezWord<Character>, Production<Character>, CFG<Character>, SLP<Character>> creator) {
        CFGOp<Character> cfgOp = new CFGOp<>();

        Map<IJezSymbol<Object>, IJezSymbol<Character>> replacementMap = new HashMap<>();
        Set<IJezSymbol<Character>> slpAxioms = new HashSet<>();

        for(IJezSymbol<Object> axiom : axioms) {
            IJezSymbol<Character> slpAxiom = creator.createFreshNonTerminal();
            replacementMap.put(axiom, slpAxiom);
            slpAxioms.add(slpAxiom);
        }

        Set<Production<Character>> slpProductions = new HashSet<>();
        for(Production<Object> production : baseSLPProductions) {

            if(!replacementMap.containsKey(production.getLeft())) {
                replacementMap.put(production.getLeft(), creator.createFreshNonTerminal());
            }

            IJezSymbol<Character> left = replacementMap.get(production.getLeft());
            List<IJezSymbol<Character>> right = new LinkedList<>();

            for(IJezSymbol<Object> symbol : production.getRight()) {
                if(!symbol.isTerminal() && !replacementMap.containsKey(symbol)) {
                    replacementMap.put(symbol, creator.createFreshNonTerminal());
                }

                if(!symbol.isTerminal()) {
                    right.add(replacementMap.get(symbol));
                }
                else {
                    N2WRule rule = chooser.apply((Pair<N2WRule, N2WRule>)symbol.getName());
                    SLP<Character> slp = rule.getOutputWord();

                    // rename SLP!
                    Map<IJezSymbol<Character>, IJezSymbol<Character>> rM = new HashMap<>();
                    Pair<Set<Production<Character>>, Set<IJezSymbol<Character>>> pair = creator.freshNonTerminals(slp.getProductions(), slp.getAxioms(), rM);

                    Set<Production<Character>> tmpSLPProductions = pair.a;
                    Set<IJezSymbol<Character>> tmpSLPAxioms = pair.b;
                    if(pair.b.size() != 1) {
                        throw new IllegalArgumentException("there is an slp that has more than 1 axiom!");
                    }

                    right.add(tmpSLPAxioms.iterator().next());
                    slpProductions.addAll(tmpSLPProductions);
                }
            }

            slpProductions.add(creator.createProduction(left, creator.createWord(right)));
        }

        return new Pair<>(creator.createSLP(cfgOp.toSLP(slpProductions, slpAxioms, true), slpAxioms), replacementMap);
    }*/
}
