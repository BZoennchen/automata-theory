package grammar.impl;

import grammar.inter.ICFG;
import grammar.inter.ICFGCreator;
import grammar.inter.IJezSymbolFactory;
import symbol.IJezSymbol;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The standard implementation of the CFGCreator.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class CFGCreator<N> implements ICFGCreator<N, IJezSymbol<N>, JezWord<N>, Production<N>, CFG<N>, SLP<N>> {

    /**
     * A factory for non-terminal and terminal symbols identified by its elements.
     */
    private IJezSymbolFactory<N, IJezSymbol<N>, JezWord<N>, Production<N>> symbolIJezSymbolFactory;

    /**
     * A SLP representing the emtpy word.
     */
    private SLP<N> emptyWord = null;

    /**
     * The default constructor constructing a frehs creator.
     */
    public CFGCreator() {
        symbolIJezSymbolFactory = new JezSymbolFactory<>();
    }

    /**
     * A Constructor to construct a creator that is initialized by the non-terminals contained in any CFG inside the list of CFGs.
     *
     * @param cfgs the list of CFGs
     */
    protected CFGCreator(final Collection<? extends ICFG<N, ?, ?, ?>> cfgs) {
        int optMaxNonTerminalId = cfgs.stream().flatMap(cfg -> cfg.getNonTerminalStream()).map(s -> s.getId()).max((s1, s2) -> Integer.compare(s1, s2)).orElse(-1);
        symbolIJezSymbolFactory = new JezSymbolFactory<>(optMaxNonTerminalId);
    }

    /**
     * Returns the SLP representing the empty word.
     *
     * @return the SLP representing the empty word
     */
    public SLP<N> emptyWord() {
        if(emptyWord == null) {
            IJezSymbol<N> left = symbolIJezSymbolFactory.createFreshNonTerminal();
            Production<N> emptyProduction = createProduction(left, createWord(new ArrayList<>()));
            Set<Production<N>> productions = new HashSet<>();
            Set<IJezSymbol<N>> axioms = new HashSet<>();
            productions.add(emptyProduction);
            axioms.add(left);
            emptyWord = createSLP(productions, axioms);
        }

        return emptyWord;
    }

    @Override
    public SLP<N> oneProduction(N... elements) {
        List<N> list = new ArrayList<>();
        for (N element : elements) {
            list.add(element);
        }
        return oneProduction(list);
    }

    @Override
    public SLP<N> oneProduction(List<N> elements) {
        IJezSymbol<N> left = symbolIJezSymbolFactory.createFreshNonTerminal();
        List<IJezSymbol<N>> right = new ArrayList<>();
        for (N element : elements) {
            right.add(this.lookupSymbol(element, true));
        }
        Production<N> oneProduction = createProduction(left, createWord(right));
        Map<IJezSymbol<N>, Production<N>> productions = new HashMap<>();
        productions.put(left, oneProduction);
        return createSLP(productions, left);
    }

    @Override
    public SLP<N> freshNonTerminals(final SLP<N> slp, final Map<IJezSymbol<N>, IJezSymbol<N>> replacement) {
        Set<IJezSymbol<N>> newAxioms = new HashSet<>();
        for (IJezSymbol<N> axiom : slp.getAxioms()) {
            IJezSymbol<N> newAxiom = createFreshNonTerminal();
            replacement.put(axiom, newAxiom);
            newAxioms.add(newAxiom);
        }

        Map<IJezSymbol<N>, Production<N>> newProductions = new HashMap<>();

        for (Map.Entry<IJezSymbol<N>, Production<N>> entry : slp.getSLPProductions().entrySet()) {
            Production<N> newProduction = fresh(entry.getValue(), replacement);
            newProductions.put(newProduction.getLeft(), newProduction);
        }

        return createSLP(newProductions, newAxioms);
    }

    @Override
    public CFG<N> createCFG(final Set<Production<N>> productions, final IJezSymbol<N> axiom) {
        Set<IJezSymbol<N>> axioms = new HashSet<>();
        axioms.add(axiom);
        return createCFG(productions, axioms);
    }

    @Override
    public CFG<N> createCFG(final Set<Production<N>> productions, final Set<IJezSymbol<N>> axioms) {
        return new CFG(productions, axioms);
    }

    @Override
    public CFG<N> freshNonTerminals(final CFG<N> cfg, final Map<IJezSymbol<N>, IJezSymbol<N>> replacement) {
        Set<Production<N>> newProductions = new HashSet<>();
        Set<IJezSymbol<N>> newAxioms = new HashSet<>();

        for (Production<N> production : cfg.getProductions()) {
            newProductions.add(fresh(production, replacement));
        }

        for (IJezSymbol<N> axiom : cfg.getAxioms()) {
            if (!replacement.containsKey(axiom)) {
                replacement.put(axiom, symbolIJezSymbolFactory.createFreshNonTerminal());
            }

            IJezSymbol<N> newAxiom = replacement.get(axiom);
            newAxioms.add(newAxiom);
        }

        return createCFG(newProductions, newAxioms);
    }

    @Override
    public SLP<N> createSLP(final Map<IJezSymbol<N>, Production<N>> productions, final IJezSymbol<N> axiom) {
        Set<IJezSymbol<N>> axioms = new HashSet<>();
        axioms.add(axiom);
        return new SLP<N>(productions, axioms);
    }

    @Override
    public SLP<N> createSLP(final Map<IJezSymbol<N>, Production<N>> productions, final Set<IJezSymbol<N>> axioms) {
        return new SLP<>(productions, axioms);
    }

    @Override
    public SLP<N> createSLP(final Set<Production<N>> productions, final Set<IJezSymbol<N>> axioms) {
        return new SLP<>(productions, axioms);
    }

    @Override
    public Production<N> createProduction(final IJezSymbol<N> left, final JezWord<N> right) {
        return new Production(left, right);
    }

    @Override
    public Production<N> createProduction(IJezSymbol<N> left, IJezSymbol<N>... right) {
        List<IJezSymbol<N>> list = new LinkedList<>();
        for (IJezSymbol<N> symbol : right) {
            list.add(symbol);
        }
        return new Production<>(left, createWord(list));
    }

    @Override
    public JezWord<N> createWord(final List<IJezSymbol<N>> symbols) {
        return new JezWord(symbols);
    }

    @Override
    public IJezSymbol<N> createFreshNonTerminal() {
        return symbolIJezSymbolFactory.createFreshNonTerminal();
    }

    @Override
    public IJezSymbol<N> lookupSymbol(final N name, final boolean terminal) {
        return symbolIJezSymbolFactory.makeSymbol(name, terminal);
    }

    @Override
    public IJezSymbolFactory<N, IJezSymbol<N>, JezWord<N>, Production<N>> getSymbolFactory() {
        return symbolIJezSymbolFactory;
    }

    @Override
    public Set<Production<N>> copyProductions(final Set<Production<N>> productions) {
        return productions.stream().map(p -> createProduction(p.getLeft().clone(), createWord(p.getRight().stream().map(s -> s.clone()).collect(Collectors.toList())))).collect(Collectors.toSet());
    }

    @Override
    public Map<IJezSymbol<N>, Production<N>> copyProductions(final Map<IJezSymbol<N>, Production<N>> productions) {
        return productions.entrySet().stream()
                .map(entry -> createProduction(entry.getKey(), createWord(entry.getValue().getRight().stream().map(s -> s.clone()).collect(Collectors.toList()))))
                .collect(Collectors.toMap(prd -> prd.getLeft(), prd -> prd));
    }

    @Override
    public CFG<N> copy(final CFG<N> cfg) {
        return createCFG(copyProductions(cfg.getProductions()), cfg.getAxioms());
    }

    @Override
    public CFG<N> resetNonTerminals(final CFG<N> cfg) {
        Map<IJezSymbol<N>, Integer> replacement = new HashMap<>();
        Set<IJezSymbol<N>> newAxioms = new HashSet<>();
        Set<Production<N>> newProductions = new HashSet<>();

        // assign to each non-terminal a new id, starting from 0.
        int ids = 0;
        for(IJezSymbol<N> nonTerminals : cfg.getNonTerminals()) {
            replacement.put(nonTerminals, ids++);
        }

        // change axioms
        for(IJezSymbol<N> axiom : cfg.getAxioms()) {
            newAxioms.add(getNonTerminal(replacement.get(axiom)));
        }

        // change productions
        for(Production<N> p : cfg.getProductions()) {
            IJezSymbol<N> newLeft = getNonTerminal(replacement.get(p.getLeft()));
            List<IJezSymbol<N>> newRight = new LinkedList<>();
            for(IJezSymbol<N> right : p.getRight()) {
                if(right.isTerminal()) {
                    newRight.add(right);
                }
                else {
                    newRight.add(getNonTerminal(replacement.get(right)));
                }
            }
            newProductions.add(createProduction(newLeft, createWord(newRight)));
        }

        return createCFG(newProductions, newAxioms);
    }

    private IJezSymbol<N> getNonTerminal(final int number) {
        List<IJezSymbol<N>> nonTerminals = symbolIJezSymbolFactory.getNonTerminals();
        if(number < nonTerminals.size()) {
            return nonTerminals.get(number);
        }
        else {
            for(int i = nonTerminals.size()-1; i <= number; i++) {
                symbolIJezSymbolFactory.createFreshNonTerminal();
            }
            return symbolIJezSymbolFactory.getNonTerminals().get(number);
        }
    }

    @Override
    public SLP<N> copy(final SLP<N> slp) {
        return createSLP(copyProductions(slp.getSLPProductions()), slp.getAxioms());
    }

    @Override
    public SLP<N> resetNonTerminals(final SLP<N> slp) {
        Map<IJezSymbol<N>, Integer> replacement = new HashMap<>();
        Set<IJezSymbol<N>> newAxioms = new HashSet<>();
        Set<Production<N>> newProductions = new HashSet<>();

        // assign to each non-terminal a new id, starting from 0.
        int ids = 0;
        for(IJezSymbol<N> nonTerminals : slp.getNonTerminals()) {
            replacement.put(nonTerminals, ids++);
        }

        // change axioms
        for(IJezSymbol<N> axiom : slp.getAxioms()) {
            newAxioms.add(getNonTerminal(replacement.get(axiom)));
        }

        // change productions
        for(Production<N> p : slp.getProductions()) {
            IJezSymbol<N> newLeft = getNonTerminal(replacement.get(p.getLeft()));
            List<IJezSymbol<N>> newRight = new LinkedList<>();
            for(IJezSymbol<N> right : p.getRight()) {
                if(right.isTerminal()) {
                    newRight.add(right);
                }
                else {
                    newRight.add(getNonTerminal(replacement.get(right)));
                }
            }
            newProductions.add(createProduction(newLeft, createWord(newRight)));
        }

        return createSLP(newProductions, newAxioms);
    }

    private Production<N> fresh(final Production<N> production, final Map<IJezSymbol<N>, IJezSymbol<N>> replacementMap) {
        return fresh(production, replacementMap, false);
    }

    private Production<N> fresh(final Production<N> production, final Map<IJezSymbol<N>, IJezSymbol<N>> replacementMap, final boolean terminals) {
        IJezSymbol<N> left = production.getLeft();

        if(!replacementMap.containsKey(left)) {
            replacementMap.put(left, createFreshNonTerminal());
        }

        IJezSymbol<N> newLeft = replacementMap.get(left);

        List<IJezSymbol<N>> newRight = new LinkedList<>();
        for(IJezSymbol<N> symbol : production.getRight()) {
            if(!symbol.isTerminal()) {
                if(!replacementMap.containsKey(symbol)) {
                    replacementMap.put(symbol, createFreshNonTerminal());
                }

                newRight.add(replacementMap.get(symbol));
            }
            else {
                if(terminals) {
                    if(!replacementMap.containsKey(symbol)) {
                        symbol = lookupSymbol(symbol.getName(), true);
                    }
                }
                newRight.add(symbol);
            }
        }

        return createProduction(newLeft, createWord(newRight));
    }
}
