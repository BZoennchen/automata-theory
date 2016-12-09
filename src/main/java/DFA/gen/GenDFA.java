package DFA.gen;

import DFA.inter.IDFA;
import DFA.inter.IDFARule;
import symbol.INamedSymbol;
import utils.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The generic implementation of a deterministic final word automaton.
 * A GenDFA represents the data structure of a DFA without any operations.
 * A DFA is immutable, therefore we compute the hash value only once.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of the letters of the input alphabet of the DFA
 * @param <B>   the type of the identifier of the states of the DFA
 * @param <F>   the type of the letter of the input alphabet of the DFA
 * @param <Q>   the type of the states of the DFA
 * @param <R>   the type of the transition rules of the DFA
 */
public class GenDFA<N, B, F extends INamedSymbol<N>, Q extends INamedSymbol<B>, R extends IDFARule<N, B, F, Q>> implements IDFA<N, B, F, Q, R> {

    /**
     * The mapping (q, f) -> rule, where q is a state of the DFA, f is a symbol of its alphabet and rule is a transition rule.
     */
    private final Map<Pair<Q, F>, R> rules;

    /**
     * The initial state.
     */
    private final Q initialStates;

    /**
     * The set of final states.
     */
    private final Set<Q> finalStates;

    /**
     * The hash value of this DFA.
     */
    private final int hash;

    /**
     * Default constructor of the DFA.
     *
     * @param rules         the transition rules of the DFA
     * @param initialStates the set of initial states of the DFA
     * @param finalStates   the set of final states of the DFA
     * @throws IllegalArgumentException if the parameters define not a well-defined deterministic finite word automaton
     */
    public GenDFA(final Set<R> rules, final Q initialStates, final Set<Q> finalStates) {
        Map<Pair<Q, F>, R> modifyableRules = new HashMap<>();

        for(R rule : rules) {
            Pair<Q, F> pair = new Pair<>(rule.getSrcState(), rule.getSymbol());
            if(modifyableRules.containsKey(pair)) {
                throw new IllegalArgumentException("the word automaton is not deterministic.");
            }
	        modifyableRules.put(pair, rule);
        }

	    this.rules = Collections.unmodifiableMap(modifyableRules);
        this.initialStates = initialStates;
        this.finalStates = finalStates;
        this.hash = calcHash();
    }

    @Override
    public Set<F> getSymbols() {
        return rules.values().stream().map(rule -> rule.getSymbol()).collect(Collectors.toSet());
    }

    @Override
    public Set<Q> getStates() {
        Set<Q> states = new HashSet<>();
        states.add(initialStates);
        getRules().stream().forEach(r -> {
            states.add(r.getDestState());
            states.add(r.getSrcState());
        });
        return states;
    }

    @Override
    public Set<R> getRules() {
        return new HashSet<>(rules.values());
    }

    @Override
    public Q getInitialState() {
        return initialStates;
    }

    @Override
    public Set<Q> getFinalStates() {
        return finalStates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenDFA<?, ?, ?, ?, ?> genDFA = (GenDFA<?, ?, ?, ?, ?>) o;

        if (!rules.equals(genDFA.rules)) return false;
        if (!initialStates.equals(genDFA.initialStates)) return false;
        return finalStates.equals(genDFA.finalStates);
    }

    private int calcHash() {
        int result = rules.hashCode();
        result = 31 * result + initialStates.hashCode();
        result = 31 * result + finalStates.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
