package N2W.gen;

import N2W.inter.IN2WRule;
import symbol.INamedSymbol;
import N2W.inter.INestedLetter;

/**
 * The generic immutable implementation of a N2W-rule.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output !word! of the N2W (not the output). The output word might be a SLP of another type.
 * @param <A> the type of the identifier of the state of the N2W
 * @param <B> the type of the identifier of the symbol of the N2W
 * @param <C> the type of the identifier of the stack symbol
 * @param <Q> the type of the state of the N2W
 * @param <W> the type of the nested word of the N2W
 * @param <G> the type of the stack symbol of the N2W
 */
public class GenN2WRule<N, A, B, C, Q extends INamedSymbol<A>, W extends INestedLetter<B>, G extends INamedSymbol<C>> implements IN2WRule<N, A, B, C, Q, W, G> {

    /**
     * The start state of the rule.
     */
    private final Q startState;

    /**
     * The end state of the rule.
     */
    private final Q endState;

    /**
     * The nested word of the rule.
     */
    private final W nestedWord;

    /**
     * The output word of the rule (this might be a whole SLP of another type).
     */
    private final N outputWord;

    /**
     * The stack symbol of the rule.
     */
    private final G stackSymbol;

    /**
     * The hash value of the rule.
     */
    private final int hashCode;

    public GenN2WRule(final Q startState, final Q endState, final W nestedWord, final N outputWord, final G stackSymbol) {
        if(startState == null) {
            throw new IllegalArgumentException("start state is null.");
        }

        if(endState == null) {
            throw new IllegalArgumentException("end state is null.");
        }

        if(nestedWord == null) {
            throw new IllegalArgumentException("the input nested word is null.");
        }

        this.startState = startState;
        this.endState = endState;
        this.nestedWord = nestedWord;
        this.stackSymbol = stackSymbol;
        this.outputWord = outputWord;
        this.hashCode = calcHash();
    }

    @Override
    public Q getStartState() {
        return startState;
    }

    @Override
    public Q getEndState() {
        return endState;
    }

    @Override
    public W getNestedWord() {
        return nestedWord;
    }

    @Override
    public G getStackSymbol() {
        return stackSymbol;
    }

    @Override
    public N getOutputWord() {
        return outputWord;
    }

    private int calcHash() {
        int result = startState != null ? startState.hashCode() : 0;
        result = 31 * result + (endState != null ? endState.hashCode() : 0);
        result = 31 * result + (nestedWord != null ? nestedWord.hashCode() : 0);
        result = 31 * result + outputWord.hashCode();
        result = 31 * result + stackSymbol.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenN2WRule<?, ?, ?, ?, ?, ?, ?> that = (GenN2WRule<?, ?, ?, ?, ?, ?, ?>) o;

        if (startState != null ? !startState.equals(that.startState) : that.startState != null) return false;
        if (endState != null ? !endState.equals(that.endState) : that.endState != null) return false;
        if (nestedWord != null ? !nestedWord.equals(that.nestedWord) : that.nestedWord != null) return false;
        return stackSymbol.equals(that.stackSymbol);

    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return startState + "->" + endState + "#" + nestedWord + "/" + outputWord + ":" + stackSymbol;
    }
}
