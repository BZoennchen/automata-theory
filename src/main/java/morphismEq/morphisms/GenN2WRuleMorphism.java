package morphismEq.morphisms;

import N2W.inter.IN2WRule;
import grammar.impl.CFGCreator;
import grammar.impl.CFGCreatorFactory;
import grammar.impl.Production;
import grammar.impl.SLP;
import symbol.IJezSymbol;
import utils.Pair;

import java.util.*;

/**
 * Morphism that transforms an SLP with terminals equal to pairs of N2W-Rules (r1, r2)
 * into an SLP with terminals of type N by replacing each pair (r1, r2) by output(r1) or
 * output(r2). The type of the output of a rule r1 or r2 is equal to T.
 *
 * The class is abstract since we have to specify T.
 *
 * @author Benedikt Zoennchen
 */
public abstract class GenN2WRuleMorphism<N, T> implements IMorphism<SLP<Object>, SLP<N>> {

    private boolean firstRule;
    protected CFGCreator<N> slpCreator;
    private CFGCreatorFactory<N> factory;

    public GenN2WRuleMorphism(final boolean firstRule) {
        this.firstRule = firstRule;
        this.slpCreator = new CFGCreator<>();
        this.factory = new CFGCreatorFactory<>();
    }

    @Override
    public SLP<N> apply(final SLP<Object> slp) {
        slpCreator = factory.create();
        Map<Object, IJezSymbol<N>> replacement = new HashMap<>();
        Map<IJezSymbol<N>, Production<N>> slpProductions = new HashMap<>();
        IJezSymbol<N> axiom = slpCreator.createFreshNonTerminal();
        replacement.put(slp.getAxiom(), axiom);

        for(Production<Object> production : slp.getProductions()) {
            if(!replacement.containsKey(production.getLeft())) {
                replacement.put(production.getLeft(), slpCreator.createFreshNonTerminal());
            }

            IJezSymbol<N> left = replacement.get(production.getLeft());
            List<IJezSymbol<N>> right = new LinkedList<>();

            for(IJezSymbol<Object> symbol : production.getRight()) {
                IJezSymbol<N> replacedSymbol;
                if(!symbol.isTerminal()) {
                    if(!replacement.containsKey(symbol)) {
                        replacement.put(symbol, slpCreator.createFreshNonTerminal());
                    }
                    replacedSymbol = replacement.get(symbol);
                }
                else {
                    SLP<N> outputWord = getSLP(symbol);
                    outputWord = slpCreator.freshNonTerminals(outputWord, new HashMap<>());
                    replacedSymbol = outputWord.getAxiom();
                    slpProductions.putAll(outputWord.getSLPProductions());
                }

                right.add(replacedSymbol);
            }

            Production<N> newProduction = slpCreator.createProduction(left, slpCreator.createWord(right));
            slpProductions.put(newProduction.getLeft(), newProduction);
        }

        return slpCreator.createSLP(slpProductions, axiom);
    }

    private SLP<N> getSLP(final IJezSymbol<Object> terminal) {
        Object element = terminal.getName();
        if(element instanceof Pair<?, ?>) {
            Pair<?, ?> pair = (Pair<?, ?>) element;

            IN2WRule<T, ?, ?, ?, ?, ?, ?> rule;
            if(firstRule) {
                rule = (IN2WRule<T, ?, ?, ?, ?, ?, ?>)pair.a;
            }
            else {
                rule = (IN2WRule<T, ?, ?, ?, ?, ?, ?>)pair.b;
            }

            return outputToSLP(rule.getOutputWord());
        }
        else {
            throw new IllegalArgumentException("wrong type.");
        }
    }

    protected abstract SLP<N> outputToSLP(final T output);
}
