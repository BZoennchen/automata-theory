package grammar.impl;

import grammar.gen.GenPointedWord;
import grammar.inter.IReferencedWord;
import symbol.IJezSymbol;

import java.util.List;

/**
 * The standard implementation of GenPointedWord.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class JezWord<N> extends GenPointedWord<N, IJezSymbol<N>> {
    public JezWord(final IJezSymbol<N> symbol) {
        super(symbol);
    }

    public JezWord(final List<IJezSymbol<N>> symbols) {
        super(symbols);
    }

    @Override
    public IReferencedWord<N, IJezSymbol<N>> clone() {
        IReferencedWord<N, IJezSymbol<N>> clone = super.clone();
        return new JezWord<>(clone.findAll(s -> true));
    }
}
