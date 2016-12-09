package morphismEq.morphisms;

import grammar.inter.*;
import symbol.IJezSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This morphism realises the morphism for the periodicity test. It removes all terminals
 * of a specific SLP i.e. it removes all non-terminals contained in this SLP.
 *
 * Note: This does only work if there is no renaming of non-terminals after the grammar
 * which we wanna test and the SLP which we wanna erase are constructed.
 *
 * @author Benedikt Zoennchen
 */
public class PeriodicMorphism<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>, P extends IProduction<N, S, W>, C extends ICFG<N, S, W, P>, Z extends ISLP<N, S, W, P>> implements IMorphism<Z, Z> {
    private final Set<S> nonTerminals;
    private ICFGCreator<N, S, W, P, C, Z> cfgCreator;


    public PeriodicMorphism(final Z deleteSLP, final ICFGCreatorFactory<N, S, W, P, C, Z> creatorFactory) {
        this.nonTerminals = deleteSLP.getNonTerminals();
        this.cfgCreator = creatorFactory.create();
    }


    @Override
    public Z apply(final Z slp) {
        Z copySLP = cfgCreator.copy(slp);
        S nonTerminal = copySLP.getAxiom();

        if(nonTerminals.contains(nonTerminal)) {
            return cfgCreator.emptyWord();
        }
        else {
            Map<S, P> newSLPProductions = new HashMap<>();
            copySLP.getSLPProductions().entrySet().stream().filter(entry -> !nonTerminals.contains(entry.getKey())).forEach(e -> newSLPProductions.put(e.getKey(), e.getValue()));
            newSLPProductions.values().stream().map(p -> p.getRight()).forEach(right -> right.deleteAll(s -> nonTerminals.contains(s)));
            return cfgCreator.createSLP(newSLPProductions, copySLP.getAxiom());
        }
    }
}
