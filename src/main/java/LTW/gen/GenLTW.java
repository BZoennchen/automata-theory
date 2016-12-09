package LTW.gen;

import LTW.inter.ILTW;
import LTW.inter.ILTWRule;
import LTW.inter.IRankedSymbol;
import symbol.INamedSymbol;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The generic implementation of a LTW.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output alphabet
 * @param <A> the type of the identifier of the ranked symbol
 * @param <B> the type of the identifier of the state of the LTW
 * @param <F> the type of the ranked symbol
 * @param <Q> the type of the states of the LTW
 * @param <R> the type of the rules of the LTW
 */
public class GenLTW<N, A, B, F extends IRankedSymbol<A>, Q extends INamedSymbol<B>, R extends ILTWRule<N, A, B, F, Q>> implements ILTW<N, A, B, F, Q, R> {

    /**
     * The set of rules of the GenLTW.
     */
    private Set<R> rules;

    /**
     * The set of initial states.
     */
    private Set<Q> initialStates;

    /**
     * The default constructor the generic LTW.
     *
     * @param rules         the set of rules of the generic LTW
     * @param initialStates the set of initial states
     */
    public GenLTW(final Set<R> rules, final Set<Q> initialStates) {
        this.rules = rules;
        this.initialStates = initialStates;
    }

    @Override
    public Set<R> getRules() {
        return Collections.unmodifiableSet(rules);
    }

    @Override
    public Map<Q, List<R>> getRulesBySrcState() {
        return this.rules.stream().collect(Collectors.groupingBy(r -> r.getSrcState(), Collectors.toList()));
    }

    @Override
    public Set<R> getRules(Q srcState) {
        return this.rules.stream().filter(r -> r.getSrcState().equals(srcState)).collect(Collectors.toSet());
    }

    @Override
    public Set<Q> getStates() {
        Set<Q> states = new HashSet<>();

        rules.stream().forEach(r -> {
            states.add(r.getSrcState());
            states.addAll(r.getDestStates());
        });
        states.addAll(initialStates);

        return states;
    }

    @Override
    public Set<Q> getInitialStates() {
        return Collections.unmodifiableSet(initialStates);
    }

    @Override
    public Set<F> getInputAlphabet() {
        return Collections.unmodifiableSet(rules.stream().map(r -> r.getSymbol()).collect(Collectors.toSet()));
    }

    @Override
    public Set<N> getOutputAlphabet() {
        return Collections.unmodifiableSet(rules.stream()
                .flatMap(r -> r.getOutputWords().stream())
                .flatMap(slp -> slp.getTerminals().stream())
                .map(symbol -> symbol.getName()).collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenLTW<?, ?, ?, ?, ?, ?> genLTW = (GenLTW<?, ?, ?, ?, ?, ?>) o;

        if (rules != null ? !rules.equals(genLTW.rules) : genLTW.rules != null) return false;
        return !(initialStates != null ? !initialStates.equals(genLTW.initialStates) : genLTW.initialStates != null);

    }

    @Override
    public int hashCode() {
        int result = rules != null ? rules.hashCode() : 0;
        result = 31 * result + (initialStates != null ? initialStates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        Map<Q, List<R>> rulesByState = getRulesBySrcState();
        StringBuilder builder = new StringBuilder();

        for(Q q : rulesByState.keySet()) {
            for(R rule : rulesByState.get(q)) {
                builder.append(rule + "\n");
            }
        }

        return builder.toString();
    }
}
