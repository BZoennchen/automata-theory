package LTW.gen;

import LTW.inter.ILTWRule;
import LTW.inter.IRankedSymbol;
import grammar.impl.SLP;
import symbol.INamedSymbol;

import java.util.List;
import java.util.function.Function;

/**
 * The generic implementation of a LTW-rule.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output alphabet
 * @param <A> the type of the identifier of the ranked symbol
 * @param <B> the type of the identifier of the state of the LTW
 * @param <F> the type of the ranked symbol
 * @param <Q> the type of the states of the LTW
 */
public class GenLTWRule<N, A, B, F extends IRankedSymbol<A>, Q extends INamedSymbol<B>> implements ILTWRule<N, A, B ,F, Q> {

    /**
     * The source state of the LTW-rule.
     */
    private Q srcState;

    /**
     * The ordered list of destination states.
     */
    private List<Q> destStates;

    /**
     * The ordered list of SLP-compressed output words.
     */
    private List<SLP<N>> outputWords;

    /**
     * The input symbol of the rule.
     */
    private F symbol;

    /**
     * The permutation of the rule which defines the re-ordering of the output.
     */
    private Function<Integer, Integer> permutation;

    /**
     * Default constructor for a LTW rule.
     *
     * @param srcState      the source state of the rule
     * @param symbol        the input symbol of the rule
     * @param destStates    the ordered list of destination states of the rule
     * @param outputWords   the ordered list of output words
     * @param permutation   the permutation of the rule which defines the re-ordering of the output
     */
    public GenLTWRule(final Q srcState, final F symbol, final List<Q> destStates, final List<SLP<N>> outputWords, final Function<Integer, Integer> permutation) {

        if(destStates.size() != symbol.getArity()) {
            throw new IllegalArgumentException("not the right number of destination states.");
        }

        if(symbol.getArity()+1 != outputWords.size()) {
            throw new IllegalArgumentException("not the right number of output words.");
        }

        this.srcState = srcState;
        this.destStates = destStates;
        this.symbol = symbol;
        this.outputWords = outputWords;
        this.permutation = permutation;
    }

    public GenLTWRule(final Q srcState, final F symbol, final List<Q> destStates, final List<SLP<N>> outputWords) {
        this(srcState, symbol, destStates, outputWords, i -> i);
    }

    @Override
    public Q getSrcState() {
        return srcState;
    }

    @Override
    public List<Q> getDestStates() {
        return destStates;
    }

    @Override
    public List<SLP<N>> getOutputWords() {
        return outputWords;
    }

    @Override
    public F getSymbol() {
        return symbol;
    }

    @Override
    public Function<Integer, Integer> getInputPermutation() {
        return permutation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(srcState + "(" + symbol + ")" + "->{");

        for(int i = 0; i < destStates.size(); i++) {
            builder.append(outputWords.get(i));
            builder.append("["+destStates.get(i) + "x_"+getInputPermutation().apply(i) + "]");
        }
        builder.append(outputWords.get(destStates.size()));
        builder.append("}");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenLTWRule<?, ?, ?, ?, ?> that = (GenLTWRule<?, ?, ?, ?, ?>) o;

        if (srcState != null ? !srcState.equals(that.srcState) : that.srcState != null) return false;
        if (destStates != null ? !destStates.equals(that.destStates) : that.destStates != null) return false;
        return !(symbol != null ? !symbol.equals(that.symbol) : that.symbol != null);

    }

    @Override
    public int hashCode() {
        int result = srcState != null ? srcState.hashCode() : 0;
        result = 31 * result + (destStates != null ? destStates.hashCode() : 0);
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        return result;
    }
}
