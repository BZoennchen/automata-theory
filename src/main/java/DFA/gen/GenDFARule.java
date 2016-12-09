package DFA.gen;

import DFA.inter.IDFARule;
import symbol.INamedSymbol;

/**
 * The generic implementation of a transition rule of a DFA i.e. q(a) -> p.
 * The transition rule is immutable, therefore we have to compute the hash value only once.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of the letters of the input alphabet of the DFA
 * @param <B>   the type of the identifier of the states of the DFA
 * @param <F>   the type of the letter of the input alphabet of the DFA
 * @param <Q>   the type of the states of the DFA
 */
public class GenDFARule<N, B, F extends INamedSymbol<N>, Q extends INamedSymbol<B>> implements IDFARule<N, B,F,Q> {

    /**
     * The source state of the rule.
     */
    private final Q srcState;

    /**
     * The destination state of the rule.
     */
    private final Q destState;

    /**
     * The symbol of the alphabet of the DFA the rule read.
     */
    private final F symbol;

    /**
     * the hash value of the rule.
     */
    private final int hash;

    public GenDFARule(final Q srcState, final Q destState, final F symbol) {
        this.srcState = srcState;
        this.destState = destState;
        this.symbol = symbol;
        this.hash = calcHash();
    }

    @Override
    public F getSymbol() {
        return symbol;
    }

    @Override
    public Q getDestState() {
        return destState;
    }

    @Override
    public Q getSrcState() {
        return srcState;
    }

    @Override
    public String toString() {
        return "[" + srcState + "]("+symbol+") -> " + "[" + destState + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenDFARule<?, ?, ?, ?> that = (GenDFARule<?, ?, ?, ?>) o;

        if (!srcState.equals(that.srcState)) return false;
        if (!destState.equals(that.destState)) return false;
        return !(symbol != null ? !symbol.equals(that.symbol) : that.symbol != null);

    }

    private int calcHash() {
        int result = srcState.hashCode();
        result = 31 * result + destState.hashCode();
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        return result;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
