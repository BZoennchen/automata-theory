package grammar.gen;

import grammar.inter.ICFG;
import grammar.inter.IReferencedWord;
import grammar.inter.IProduction;
import symbol.IJezSymbol;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The class of a generic abstract context-free grammar.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 */
public abstract class GenAbstractCFG<N, S extends IJezSymbol<? extends N>, W extends IReferencedWord<? extends N, S>, P extends IProduction<? extends N, S, W>> implements ICFG<N, S, W, P> {

    @Override
    public Set<S> getNonTerminals() {
        Set<S> nonTerminals = getNonTerminalStream().collect(Collectors.toSet());
        getAxioms().forEach(s -> nonTerminals.add(s));
        return nonTerminals;
    }

    @Override
    public Set<S> getTerminals() {
        return getNonTerminalStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<S> getTerminalStream() {
        return getProductions().stream().flatMap(p -> p.getRight().stream()).filter(s -> s.isTerminal());
    }

    @Override
    public Stream<S> getNonTerminalStream() {
        return Stream.concat(getProductions().stream().map(p -> p.getLeft()), getProductions().stream().flatMap(p -> p.getRight().stream()).filter(s -> !s.isTerminal()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(P production : getProductions()) {
            builder.append(production + "\n");
        }
        return builder.toString();
    }

    @Override
    public abstract ICFG<N, S, W, P> clone();
}
