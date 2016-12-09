package grammar.gen;

import grammar.inter.ICFG;
import grammar.inter.IReferencedWord;
import grammar.inter.IProduction;
import symbol.IJezSymbol;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The class of a generic context-free grammar.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 */
public class GenCFG<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>, P extends IProduction<N, S, W>> extends GenAbstractCFG<N, S, W, P> {

    /**
     * a mapping : X -> {p in P | lhs(p) = X}.
     */
    private Map<S, Set<P>> productions;

    /**
     * the set of axioms.
     */
    private Set<S> axioms;

    /**
     * The default constructor of the generic context-free grammar.
     *
     * @param productions   the set of grammar productions
     * @param axiom         the set of axioms
     */
    public GenCFG(final Set<P> productions, final Set<S> axiom) {
        this.axioms = axiom;
        this.productions = new HashMap<>();
        for (P production : productions) {
            if (!this.productions.containsKey(production.getLeft())) {
                this.productions.put(production.getLeft(), new HashSet<>());
            }
            this.productions.get(production.getLeft()).add(production);
        }
    }

    @Override
    public Set<P> getProductions() {
        return productions.values().stream().flatMap(rulesSet -> rulesSet.stream()).collect(Collectors.toSet());
    }

    @Override
    public Set<P> getProductions(S symbol) {
        return productions.get(symbol);
    }

    @Override
    public Set<S> getAxioms() {
        return axioms;
    }

    @Override
    public S getAxiom() {
        if(axioms.isEmpty()) {
            return null;
        }
        else {
            return axioms.iterator().next();
        }
    }

    @Override
    public ICFG<N, S, W, P> clone() {
        Set<P> clonedProductions = new HashSet<>();
        Set<S> clonedAxioms = new HashSet<>();

        for(P production : getProductions()) {
            clonedProductions.add((P)production.clone());
        }

        for(S axiom : axioms) {
            clonedAxioms.add((S)axiom.clone());
        }

        return new GenCFG<>(clonedProductions, clonedAxioms);
    }
}
