package N2W.gen;

import N2W.inter.IN2WRule;
import N2W.inter.IN2WTransducer;
import N2W.inter.INestedLetter;
import symbol.INamedSymbol;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The generic immutable implementation of a N2W.
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
 * @param <R> the type of the rule of the N2W
 */
public class GenN2W<N, A, B, C, Q extends INamedSymbol<A>, W extends INestedLetter<B>, G extends INamedSymbol<C>, R extends IN2WRule<N, A, B, C, Q, W, G>> implements IN2WTransducer<N, A, B, C, Q, W, G, R> {

    /**
     * The set of rules.
     */
    private final Set<R> rules;

    /**
     * The set of initial states.
     */
    private final Set<Q> initialStates;

    /**
     * The set of final states.
     */
    private final Set<Q> finalStates;

    /**
     * Default constructor of the N2W.
     *
     * @param rules         the rules that defines the N2W
     * @param initialStates the initial states that defines the N2W
     * @param finalStates   the final states that defines the N2W
     */
    public GenN2W(final Set<R> rules, final Set<Q> initialStates, final Set<Q> finalStates) {
        this.rules = rules;
        this.finalStates = finalStates;
        this.initialStates = initialStates;
    }

    @Override
    public Set<R> getRules() {
        return Collections.unmodifiableSet(rules);
    }

    @Override
    public Set<Q> getInitialStates() {
        return Collections.unmodifiableSet(initialStates);
    }

    @Override
    public Set<Q> getFinalStates() {
        return Collections.unmodifiableSet(finalStates);
    }

    @Override
    public Set<Q> getStates() {
        Set<Q> states = new HashSet<>();
        rules.stream().forEach(r -> {
            states.add(r.getEndState());
            states.add(r.getStartState());
        });
        states.addAll(initialStates);
        states.addAll(finalStates);
        return Collections.unmodifiableSet(states);
    }

    @Override
    public Set<B> getInputAlphabet() {
        return Collections.unmodifiableSet(rules.stream().map(r -> r.getNestedWord()).map(nWord -> nWord.getElement()).collect(Collectors.toSet()));
    }

    @Override
    public Set<N> getOutputWords() {
        return Collections.unmodifiableSet(rules.stream().map(r -> r.getOutputWord()).collect(Collectors.toSet()));
    }

    @Override
    public Set<R> getOpeningRules() {
        return Collections.unmodifiableSet(rules.stream().filter(r -> r.getNestedWord().isOpening()).collect(Collectors.toSet()));
    }

    @Override
    public Set<R> getClosingRules() {
        return Collections.unmodifiableSet(rules.stream().filter(r -> r.getNestedWord().isClosing()).collect(Collectors.toSet()));
    }

    @Override
    public Set<G> getStackAlphabet() {
        return Collections.unmodifiableSet(rules.stream().map(r -> r.getStackSymbol()).collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenN2W<?, ?, ?, ?, ?, ?, ?, ?> genN2W = (GenN2W<?, ?, ?, ?, ?, ?, ?, ?>) o;

        if (rules != null ? !rules.equals(genN2W.rules) : genN2W.rules != null) return false;
        if (initialStates != null ? !initialStates.equals(genN2W.initialStates) : genN2W.initialStates != null)
            return false;
        return !(finalStates != null ? !finalStates.equals(genN2W.finalStates) : genN2W.finalStates != null);

    }

    @Override
    public int hashCode() {
        int result = rules != null ? rules.hashCode() : 0;
        result = 31 * result + (initialStates != null ? initialStates.hashCode() : 0);
        result = 31 * result + (finalStates != null ? finalStates.hashCode() : 0);
        return result;
    }
}
