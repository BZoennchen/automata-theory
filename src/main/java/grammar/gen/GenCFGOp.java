package grammar.gen;

import data.Node;
import grammar.inter.*;
import symbol.IJezSymbol;
import utils.CountNode;
import utils.Counter;
import utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GenCFGOp implements all required operations on generic CFGs and SLPs.
 *
 * Note: The CFGCreator has to be the creator that creates the productions and axioms the operations work with,
 * since the operations may have to create new fresh non-terminals. If this is not the case a fresh created non-terminal
 * might be already in use and the result of the construction is invalid!!!
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 * @param <C>   the type of the CFGs
 * @param <Z>   the type of the SLPs
 */
public class GenCFGOp<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>, P extends IProduction<N, S, W>, C extends ICFG<N, S, W, P>, Z extends ISLP<N, S, W, P>> {

    /**
     * Transforms a the productions of a grammar of a SLP into Chomsky normal form CNF without any useless non-terminals.
     * This operation does not change the language of the grammar. Parameters will not change.
     *
     * Complexity: O(|G|)
     *
     * @param productions   the productions of the SLP
     * @param axiom         the axiom of the SLP
     * @param creator       the creator that created the productions and the axiom
     * @return a map of production representing the SLP in CNF.
     */
    public Map<S, P> toCNF(final Map<S, P> productions, final S axiom, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Set<P> newProductions = replaceAllTerminalsByRule(productions.values(), creator);
        newProductions = toBinaryCFG(newProductions, creator);
        Map<S, P> productionMap = newProductions.stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p));
        productionMap = eliminateEpsilon(productionMap, axiom);
        eliminateUnitProductions(productionMap, creator);
        productionMap= deleteUseless(productionMap, axiom);
        return productionMap;
    }

    /**
     * Eliminate all epsilon rules from SLP-productions. This operation changes the the parameters.
     * The language of the SLP will not be changed.
     *
     * Complexity: O(|G|)
     *
     * @param slpProductions    the SLP-productions, that will be changed by this operation
     * @param axioms            the set of axioms of the SLP
     * @return the mapping : X -> p such that lhs(p) = X where p does not contain any nullable non-terminals
     */
    public Map<S, P> eliminateEpsilon(final Map<S, P> slpProductions, final Set<S> axioms) {
        Set<P> nullableProductions = getNullableProductions(slpProductions.values());
        Set<S> nullableNonTerminals = nullableProductions.stream().map(p -> p.getLeft()).collect(Collectors.toSet());

        Map<S, P> newProducitons = slpProductions.values().stream()
                .filter(p -> axioms.contains(p.getLeft()) || !nullableNonTerminals.contains(p.getLeft()))
                .collect(Collectors.toMap(p -> p.getLeft(), p -> p));

        newProducitons.values().forEach(p -> p.getRight().deleteAll(s -> nullableNonTerminals.contains(s)));

        return newProducitons;
    }

    /**
     * Eliminate all epsilon rules from SLP-productions. This operation changes the the parameters.
     * The language of the SLP will not be changed.
     *
     * Complexity: O(|G|)
     *
     * @param slpProductions the SLP-productions, that will be changed by this operation
     * @param axiom          the set of axioms of the SLP
     * @return the mapping : X -> p such that lhs(p) = X where p does not contain any nullable non-terminals
     */
    public Map<S, P> eliminateEpsilon(final Map<S, P> slpProductions, final S axiom) {
        Set<S> axioms = new HashSet<>();
        axioms.add(axiom);
        return eliminateEpsilon(slpProductions, axioms);
    }

    /**
     * Eliminate unit productions from SLPs i.e. chains like A->B, B->C, C->D will be changed by changing A->B to A->D.
     * This may create new useless productions. The parameters will be changed.
     *
     * Complexity: O(|G|)
     *
     * @param slpProductions    the set of productions defining a SLP
     * @param creator           the creator of the set of productions
     */
    public void eliminateUnitProductions(final Map<S, P> slpProductions, final ICFGCreator<N, S, W, P, C, Z> creator) {
        LinkedList<S> reverseTopologicalOrder = getOrder(slpProductions.values());
        Iterator<S> it = reverseTopologicalOrder.descendingIterator();

        while (it.hasNext()) {
            P p = slpProductions.get(it.next());
            // is p a unit production?
            if(p.getRight().isSingleton() && !p.getRight().getFirst().isTerminal()) {
                S rhs = p.getRight().getFirst();
                p.getRight().deleteFirst();
                P newP = creator.createProduction(p.getLeft(), creator.createWord(slpProductions.get(rhs).getRight().toList()));
                slpProductions.put(newP.getLeft(), newP);
            }
        }
    }

    /**
     * Transforms the set of productions into weak Chomsky normal form. The parameters will not be changed.
     *
     * @param productions   the set of productions that will be transformed
     * @param axioms        the set of axioms
     * @param creator       the creator of the set of productions
     * @return a set of fresh productions that are in Chomsky normal form and define the same language like productions
     */
    public Set<P> toWeakCNF(final Set<P> productions, final Set<S> axioms, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Set<P> newProductions = deleteUseless(productions, axioms);
        newProductions = replaceAllTerminalsByRule(newProductions, creator);
        newProductions = toBinaryCFG(newProductions, creator);
        return newProductions;
    }

    /**
     * Transform the set of productions and axiom into a binary CFG without non-trivial epsilon-productions.
     * This operation changes the argument productions.
     *
     * @param productions   the set of productions
     * @param axiom         the axiom
     * @param creator       the creator of the set of productions and the axioms.
     * @return a binary CFG without non-trivial epsilon-productions
     */
    public C toBinaryEpsilonFree(final Set<P> productions, final S axiom, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Set<S> axioms = new HashSet<>();
        axioms.add(axiom);
        return toBinaryEpsilonFree(productions, axioms, creator);
    }

    /**
     * Transform the set of productions and the set of axioms into a binary CFG without non-trivial epsilon-productions.
     * This operation changes the argument productions.
     *
     * @param productions   the set of productions
     * @param axioms        the set of axioms
     * @param creator       the creator of the set of productions and the axioms.
     * @return a binary CFG without non-trivial epsilon-productions
     */
    public C toBinaryEpsilonFree(final Set<P> productions, final Set<S> axioms, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Set<P> newProductions = toBinaryCFG(productions, creator);
        C cfg = eliminateEpsilon(newProductions, axioms, creator);
        newProductions = deleteUseless(cfg.getProductions(), cfg.getAxioms());
        //newProductions = replaceAllTerminalsByRule(newProductions, creator);
        return creator.createCFG(newProductions, cfg.getAxioms());
    }

    /**
     * Return the set of productions that are reachable and productive i.e. useful.
     *
     * @param productions   a set of productions (of a SLP)
     * @param axiom         the axiom
     * @return the set of productions that are reachable and productive i.e. useful
     */
    public Map<S, P> deleteUseless(final Map<S, P> productions, final S axiom) {
        Set<P> newProductions = deleteUseless(productions.values(), axiom);
        return newProductions.stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p));
    }

    /**
     * Return the set of productions that are reachable and productive i.e. useful.
     *
     * @param productions   a set of productions (of a CFG)
     * @param axiom         the axiom
     * @return the set of productions that are reachable and productive i.e. useful
     */
    public Set<P> deleteUseless(final Collection<P> productions, final S axiom) {
        Set<S> axioms = new HashSet<>();
        axioms.add(axiom);
        return deleteUseless(productions, axioms);
    }

    /**
     * Return the set of productions that are reachable and productive i.e. useful.
     *
     * @param productions   a set of productions (of a CFG)
     * @param axioms        a set of axioms
     * @return the set of productions that are reachable and productive i.e. useful
     */
    public Set<P> deleteUseless(final Collection<P> productions, final Set<S> axioms) {
        Collection<P> newProductions = productions;
        newProductions = eliminateUnproductives(newProductions);
        return eliminateUnreachables(newProductions, axioms);
    }

    /**
     * Transforms a productions of the form form X -> a X_1 X_2 b X_3 b X_4 into a set of binary productions i.e.
     *      X -> a X A_1
     *      A_1 -> X_2 b A_2
     *      A_2 -> X_3 b X_4
     * this operation changes the argument productions.
     *
     * @param productions   the set of productions
     * @param creator       the creator of the productions
     * @return set of binary productions representing the productions
     */
    public Set<P> toBinaryCFG(final Set<P> productions, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Set<P> binaryCFG = new HashSet<>();
        for(P production : productions) {
            binaryCFG.addAll(toBin(production, creator));
        }
        return binaryCFG;
    }

    /**
     * Transforms a production of the form form X -> a X_1 X_2 b X_3 b X_4 into a set of binary production i.e.
     *      X -> a X A_1
     *      A_1 -> X_2 b A_2
     *      A_2 -> X_3 b X_4
     * this operation changes the argument production.
     *
     * @param production    the production
     * @param creator       the creator of the production
     * @return a set of binary productions representing the production
     */
    protected Set<P> toBin(final P production, final ICFGCreator<N, S, W, P, C, Z> creator) {
        List<S> nonTerminals = production.getRight().findAll(symbol -> !symbol.isTerminal());
        Set<P> productions = new HashSet<>();
        if(nonTerminals.size() > 2) {
            S left = production.getLeft();
            List<List<S>> words = production.getRight().split(symbol -> !symbol.isTerminal());
            for(int i = 0; i < words.size()-3; i++) {
                S next = creator.createFreshNonTerminal();
                words.get(i).add(next);
                W word = creator.createWord(words.get(i));

                productions.add(creator.createProduction(left, word));
                left = next;
            }
            int j = words.size()-3;
            words.get(j).addAll(words.get(j +1));
            W word = creator.createWord(words.get(j));
            productions.add(creator.createProduction(left, word));
        }
        else {
            productions.add(production);
        }
        return productions;
    }

    /**
     * Creates a new set of productions based on productions. A production of the form
     * X -> abYc will be replaced by X -> N_a N_b Y N_c and N_a -> a, N_b -> b, N_c -> c
     * will be added. N_a -> a is called terminal production. If there are already
     * terminal productions in productions the algorithm uses these terminal productions.
     *
     * @param productions   the base of the set of productions on which we create the new productions
     * @param creator       the creator of productions
     * @return
     */
    public Set<P> replaceAllTerminalsByRule(final Collection<P> productions, final ICFGCreator<N, S, W, P, C, Z> creator) {

        // 1. all terminals
        Set<S> terminals = productions.stream().flatMap(p -> p.getRight().stream()).filter(s -> s.isTerminal()).collect(Collectors.toSet());

        // 2. remove all terminals that had already a unique non-terminal
        Map<S, List<P>> nonTerminals = productions.stream()
                .filter(p -> p.getNumberOfNonTerminals() == 0)
                .filter(p -> p.getNumberOfTerminals() == 1)
                .collect(Collectors.groupingBy(p -> p.getRight().get(0)));

        Map<S, S> replacement = nonTerminals.entrySet().stream()
                .filter(entry -> entry.getValue().size() == 1)
                .collect(Collectors.toMap(entry -> entry.getValue().get(0).getLeft(), entry -> entry.getValue().get(0).getRight().getFirst()));

        // 3. createTerminal new rules
        Set<P> newProductives = new HashSet<>();
        for(P production : productions) {
            List<S> rhs = new ArrayList<>();
            for(S symbol : production.getRight()) {
                if(symbol.isTerminal()) {
                    if(!replacement.containsKey(symbol)){
                        S nonTerminal = creator.createFreshNonTerminal();
                        replacement.put(symbol, nonTerminal);
                        List<S> list = new ArrayList<>();
                        list.add(symbol);
                        newProductives.add(creator.createProduction(nonTerminal, creator.createWord(list)));
                    }
                    rhs.add(replacement.get(symbol));
                }
                else {
                    rhs.add(symbol);
                }
            }
            W word = creator.createWord(rhs);
            P p = creator.createProduction(production.getLeft(), word);
            newProductives.add(p);
        }
        return newProductives;
    }

    public Set<P> eliminateUnreachables(final Collection<P> productions, final S axiom) {
        Set<S> axioms = new HashSet<>();
        axioms.add(axiom);
        return eliminateUnreachables(productions, axioms);
    }

    /**
     * Returns the set of reachable productions.
     *
     * @param productions   a set of productions
     * @param axioms        a set of axioms
     * @return the set of reachable productions
     */
    public Set<P> eliminateUnreachables(final Collection<P> productions, final Set<S> axioms) {

        Map<S, Set<P>> productionMap = new HashMap<>();
        for(P production : productions) {
            if(!productionMap.containsKey(production.getLeft())) {
                productionMap.put(production.getLeft(), new HashSet<>());
            }
            productionMap.get(production.getLeft()).add(production);
        }

        Set<S> set = new HashSet<>();
        Set<S> added = new HashSet<>();
        set.addAll(axioms);
        added.addAll(axioms);

        while(!added.isEmpty()) {
            added = added.stream()
                    .filter(symbol -> productionMap.containsKey(symbol))
                    .map(symbol -> productionMap.get(symbol))
                    .flatMap(ruleSet -> ruleSet.stream())
                    .map(rule -> rule.getRight().findAll(s -> !s.isTerminal()))
                    .flatMap(list -> list.stream().filter(symbol -> !set.contains(symbol)))
                    .distinct().collect(Collectors.toSet());
            set.addAll(added);
        }

        return productions.stream().filter(p -> set.contains(p.getLeft())).collect(Collectors.toSet());
    }

    /**
     * Transforms a set of productions into another set of productions containing no unit-productions and
     * generating the same language.
     *
     * @param productions a set of productions
     * @param creator     the creator of the productions
     * @return a set of productions containing no unit-productions
     */
    public Set<P> eliminateUnitProductions(final Set<P> productions, final ICFGCreator<N, S, W, P, C, Z> creator) {
        // 1. find pairs of unit productions
        Set<P> result = new HashSet<>();

        // partition into true = unit production and false = non unit production
        List<P> unitProductionList = productions.stream().filter(p -> p.getRight().isSingleton() && p.getRight().allMatch(s -> !s.isTerminal())).collect(Collectors.toList());

        List<P> nonUnitProductionList = productions.stream().filter(p -> !p.getRight().isSingleton() || !p.getRight().allMatch(s -> !s.isTerminal())).collect(Collectors.toList());
        result.addAll(nonUnitProductionList);

        List<P> nextUnitProductions = new LinkedList<>();
        Set<Pair<S, S>> resolvedUnitProductions = new HashSet<>();
        while (!unitProductionList.isEmpty()) {

            for(P unitProduction : unitProductionList) {
                S right = unitProduction.getRight().getFirst();
                resolvedUnitProductions.add(new Pair<S, S>(unitProduction.getLeft(), right));
                productions.stream()
                        .filter(p -> !p.getRight().isSingleton() || !p.getRight().getFirst().equals(unitProduction.getLeft()))
                        .filter(p -> p.getLeft().equals(right)).forEach(p -> {
                            P copy = creator.createProduction(unitProduction.getLeft(), creator.createWord(p.getRight().toList()));

                            // new production is a unit production
                            if(copy.getRight().isSingleton() && !copy.getRight().getFirst().isTerminal()) {

                                // the unit production was not added jet
                                if(!resolvedUnitProductions.contains(new Pair<>(copy.getLeft(), copy.getRight().getFirst()))) {
                                    nextUnitProductions.add(copy);
                                }
                            }
                            else {
                                result.add(copy);
                            }

                        }
                );
            }

            unitProductionList = nextUnitProductions;
            nextUnitProductions.clear();

        }

        return result;
    }

    /**
     * Tests for a grammar defined by the set of productions and the set of axiom
     * if the grammar does only generate a single word.
     *
     * @param productions   a set of productions
     * @param axioms        a set of axioms
     * @param factory       a creator factory to create a fresh creator
     * @return
     */
    public boolean isSingleton(final Set<P> productions, final Set<S> axioms, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        GenSLPOp<N, S, W, P, C, Z>  slpOp = new GenSLPOp<>();
        Set<P> newProductions = deleteUseless(productions, axioms);
        LinkedList<S> orderedNonTerminals = getOrder(newProductions);

        // its ok to use a fresh one, since we do not introduce fresh non-terminals.
        ICFGCreator<N, S, W, P, C, Z> creator = factory.create();

        // grammar is not acyclic
        if(orderedNonTerminals.size() != newProductions.stream().map(p -> p.getLeft()).distinct().count()) {
            return false;
        }

        Map<S, List<P>> productionMap = productions.stream().collect(Collectors.groupingBy(p -> p.getLeft(), Collectors.toList()));
        Iterator<S> descendingIterator = orderedNonTerminals.descendingIterator();
        Map<S, P> slpProductionMap = new HashMap<>();
        while (descendingIterator.hasNext()) {
            S left = descendingIterator.next();
            List<P> productionList = productionMap.get(left);
            if(productionList.size() > 1) {

                for(int i = 0; i < productionList.size()-1; i++) {
                    Map<S, P> copy1 = creator.copyProductions(slpProductionMap);
                    Map<S, P> copy2 = creator.copyProductions(slpProductionMap);
                    copy1.put(productionList.get(i).getLeft(), productionList.get(i));
                    copy2.put(productionList.get(i+1).getLeft(), productionList.get(i+1));

                    Z slp1 = creator.createSLP(copy1, left);
                    Z slp2 = creator.createSLP(copy2, left);

                    if(!slpOp.equals(slp1, slp2, factory, false, false)) {
                        return false;
                    }
                }

                slpProductionMap.put(left, productionList.get(0));
            }
            else {
                slpProductionMap.put(left, productionList.get(0));
            }
        }
        return true;
    }

    /**
     * Computes the set of non-terminal singletons. A non-terminal X is a singleton if the
     * the language of X is a singleton set.
     *
     * @param productions the set of productions
     * @param factory     the creator factory for creating fresh non-terminals
     * @return the set of non-terminal singletons
     */
    public Set<S> getSingletonNonTerminals(final Set<P> productions, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        Set<S> nonTerminals = productions.stream().map(p -> p.getLeft()).collect(Collectors.toSet());
        Set<S> result = new HashSet<>();
        for(S nonTerminal : nonTerminals) {
            Set<S> axioms = new HashSet<>();
            axioms.add(nonTerminal);
            if(isSingleton(productions, axioms, factory)) {
                result.add(nonTerminal);
            }
        }

        return result;
    }

    /**
     * Tests whether the grammar defined by the productions and the axiom is acyclic.
     *
     * @param productions   a set of productions
     * @param axioms        a set of axioms
     * @return returns true if the grammar defined by the productions and the axiom is acyclic, otherwise false
     */
    public boolean isAcyclic(final Set<P> productions, final Set<S> axioms) {
        Set<P> newProductions = eliminateUnproductives(productions);
        newProductions = eliminateUnreachables(productions, axioms);
        LinkedList<S> orderedNonTerminals = getOrder(newProductions);
        return orderedNonTerminals.size() == newProductions.stream().map(p -> p.getLeft()).distinct().count();
    }

    /**
     * Computes a list of non-terminals in a topographical order for those non-terminals that can be ordered.
     * The first element is like axiom -> aABd...
     *
     * Complexity: O(|G|).
     *
     * @param productions   a set of productions
     * @return a list of non-terminals in a topographical order for those non-terminals that can be ordered
     */
    public LinkedList<S> getOrder(final Collection<P> productions) {
        // result order
        LinkedList<S> orderedNonTerminals = new LinkedList<>();

        // productions by lhs
        Map<S, List<P>> productionMap = productions.stream().collect(Collectors.groupingBy(p -> p.getLeft(), Collectors.toList()));

        // mapping X -> number of occurrences of X in the right-hand sides
        Map<S, Integer> occurrenceCount = new HashMap<>();

        productions.stream().flatMap(p -> p.getRight().stream()).filter(s -> !s.isTerminal()).forEach(s -> {
            if (!occurrenceCount.containsKey(s)) {
                occurrenceCount.put(s, 1);
            } else {
                occurrenceCount.put(s, occurrenceCount.get(s) + 1);
            }
        });

        LinkedList<S> heap = new LinkedList<>();
        for(S nt : productionMap.keySet()) {
            if(!occurrenceCount.containsKey(nt)) {
                heap.add(nt);
                orderedNonTerminals.add(nt);
            }
        }

        while (!heap.isEmpty()) {
            S left = heap.poll();
            productionMap.get(left).stream().flatMap(p -> p.getRight().stream()).filter(s -> !s.isTerminal()).forEach(s -> {
                occurrenceCount.put(s, occurrenceCount.get(s) - 1);
                if (occurrenceCount.get(s) == 0) {
                    heap.add(s);
                    orderedNonTerminals.add(s);
                }
            });
        }

        return orderedNonTerminals;
    }

    /**
     * Computes a mapping : X -> {Y, Z, ...} such that there is a production Y -> ... X... and Z -> ... X ... and so on.
     *
     * Complexity: O(|G|)
     *
     * @param productions a set of productions
     * @return a mapping that maps a non-terminal X to the left-hand sides of productions containing X
     */
    private Map<S, Set<S>> getAllPredecessors(final Set<P> productions) {
        // initial list
        Map<S, Set<S>> predecessorMap = new HashMap<>();

        for(P production : productions) {
            if(!predecessorMap.containsKey(production.getLeft())) {
                predecessorMap.put(production.getLeft(), new HashSet<S>());
            }

            for(S symbol : production.getRight()) {
                if(!symbol.isTerminal()) {
                    if(!predecessorMap.containsKey(symbol)) {
                        predecessorMap.put(symbol, new HashSet<S>());
                    }
                    predecessorMap.get(symbol).add(production.getLeft());
                }
            }
        }
        return predecessorMap;
    }

    /**
     * Creates a CFG based on the set of productions and the set of axioms that does not contain non-trivial epsilon-productions.
     *
     * Requirement: The set of productions has to contain only productions with less or equal than 2 non-terminals on the right-hand side.
     * Complexity: O(|G|)
     *
     * @param productions   a set of productions with at most 2 non-terminals at the right-hand sides
     * @param axioms        a set of axioms
     * @param creator       the creator of the productions and the axiom
     * @return a CFG based without any non-trivial epsilon-productions
     */
    public C eliminateEpsilon(final Set<P> productions, final Set<S> axioms, final ICFGCreator<N, S, W, P, C, Z> creator) {
        S axiom = creator.createFreshNonTerminal();
        Set<P> result = new HashSet<>();
        Set<P> nullableProductions = getNullableProductions(productions);
        Set<S> nullableNonTerminals = nullableProductions.stream().map(p -> p.getLeft()).collect(Collectors.toSet());

        for(P production : productions) {
            if(production.getNumberOfNonTerminals() > 2) {
                throw new IllegalArgumentException("production contains more than 2 non-terminals.");
            }

            if(!production.getRight().isEmpty()) {
                S left = production.getLeft();
                //P p1 = creator.createProduction(left, creator.createWord(production.getRight().toList()));
                //result.add(p1);

                LinkedList<S> rightWord = production.getRight().stream().collect(Collectors.toCollection(LinkedList::new));
                LinkedList<S> copy1 = new LinkedList<>();
                LinkedList<S> copy2 = new LinkedList<>();
                LinkedList<S> copy3 = new LinkedList<>();
                LinkedList<S> copy4 = new LinkedList<>();
                int ntCount = 0;
                Iterator<S> iterator = rightWord.iterator();
                // production looks like X -> aAbBc, or X -> aAb
                while(iterator.hasNext()) {
                    S symbol = iterator.next();
                    if(!symbol.isTerminal() && nullableNonTerminals.contains(symbol)) {
                        //iterator.remove();
                        ntCount++;
                        if(ntCount == 1) {
                            copy1.add(symbol);
                            copy4.add(symbol);
                        }
                        else if(ntCount == 2) {
                            copy2.add(symbol);
                            copy4.add(symbol);
                        }
                        else {
                            throw new IllegalArgumentException("there are too many non-terminals on the right hand side of the production.");
                        }
                    }
                    else {
                        copy1.add(symbol);
                        copy2.add(symbol);
                        copy3.add(symbol);
                        copy4.add(symbol);
                    }
                }

                result.add(creator.createProduction(left, creator.createWord(copy4)));

                // X -> aAb
                if(ntCount == 1) {
                    result.add(creator.createProduction(left, creator.createWord(copy1)));
                } // X -> aAbBc
                else if(ntCount == 2) {
                    result.add(creator.createProduction(left, creator.createWord(copy1)));
                    result.add(creator.createProduction(left, creator.createWord(copy2)));
                }


                if(!copy3.isEmpty()) {
                    result.add(creator.createProduction(left, creator.createWord(copy3)));
                }
            }
        }

        // replace old axioms by a single axiom!
        for(S oldAxiom : axioms) {
            result.add(creator.createProduction(axiom, oldAxiom));
        }

        final Set<P> usefulProductions = deleteUseless(result, axiom);

        // if there is any axiom generating the empty word add a single production S -> epsilon
        axioms.stream().filter(oldAxiom -> nullableNonTerminals.contains(oldAxiom)).findAny().ifPresent(oldAxiom -> usefulProductions.add(creator.createProduction(axiom, creator.createWord(new LinkedList<S>()))));

        return creator.createCFG(usefulProductions, axiom);
    }

    /**
     * Returns the set of productive productions.
     *
     * @param productions the productions of a CFG
     * @return all non-terminals that are SLPs.
     */
    public Set<P> eliminateUnproductives(final Collection<P> productions) {
        Set<P> productiveRules = getProductives(productions, new HashSet<S>(), false, false);
        return productiveRules;
    }


    /**
     * Let G be the grammar defined by P and S and N be the set of non-terminals in P.
     * The method computes two mappings:
     *      1. N -> long, X |-> k, the length (k) of a shortest word generated by the non-terminal (X)
     *      2. N -> P, the SLP representing a shortest word
     *
     * @param productions   the production that defines the grammar
     * @param axioms        the set of axioms that defines the grammar
     * @param useful        true => the generated SLP will only contain useful productions, otherwise useless productions may still in the SLP but also in the mapping for the length!
     * @return a mapping that gives for a non-terminal the length of a shortest word that this non-terminal generates, and an SLP for a shortest word.
     */
    public Z getMinimalWord(final Set<P> productions, final Set<S> axioms, final boolean useful, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Map<S, P> allProductions = getMinimalWord(productions);
        if(useful) {
            Set<P> newProductions = deleteUseless(allProductions.values(), axioms);
            return creator.createSLP(newProductions, axioms);
        }
        else {
            return creator.createSLP(allProductions, axioms);
        }
    }

    /**
     * Let G be the grammar defined by P and S and N be the set of non-terminals in P.
     * The method computes two mappings:
     *      1. N -> long, X |-> k, the length (k) of a shortest word generated by the non-terminal (X)
     *      2. N -> P, the SLP representing a shortest word
     *
     * @param productions   the production that defines the grammar
     * @param axiom         the axiom that defines the grammar
     * @param useful        true => the generated SLP will only contain useful productions
     * @return a mapping that gives for a non-terminal the length of a shortest word that this non-terminal generates, and an SLP for a shortest word.
     */
    public Z getMinimalWord(final Set<P> productions, final S axiom, final boolean useful, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Set<S> axioms = new HashSet<>();
        axioms.add(axiom);
        return getMinimalWord(productions, axioms, useful, creator);
    }

    /**
     * Let G be the grammar defined by P and S and N be the set of non-terminals in P.
     * The method computes two mappings:
     *      1. N -> long, X |-> k, the length (k) of a shortest word generated by the non-terminal (X)
     *      2. N -> P, the SLP representing a shortest word
     *
     * @param productions   the production that defines the grammar
     * @return a mapping that gives for a non-terminal the length of a shortest word that this non-terminal generates, and an SLP for a shortest word.
     */
    private Pair<Map<S, Long>, Map<S, P>> getMinimalWordAndLengths(final Set<P> productions) {
        // get all non productions that generate the empty word!
        Map<S, Long> minimalWordLength = new HashMap<>();
        Map<S, P> slpProductions = new HashMap<>();

        // 1. Construct the chain data structure from Hopcroft et.
        List<P> productionList = productions.stream().collect(Collectors.toList());
        List<Long> minLengthList = new ArrayList<>(productionList.size());

        // mark as undefined
        productions.stream().forEach(p -> minLengthList.add(-1L));

        Map<S, LinkedList<CountNode<S>>> chain = new HashMap<>();
        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            List<Node<S>> rhsNonTerminals = production.getRight().findAllNodes(symbol -> !symbol.isTerminal());
            Counter counter = new Counter(rhsNonTerminals.size());

            for(Node<S> nonTerminalNode : rhsNonTerminals) {
                S nonTerminal = nonTerminalNode.getElement();
                CountNode<S> countNode = new CountNode();
                countNode.count = counter;
                countNode.letter = nonTerminalNode;
                countNode.ruleId = rId;
                countNode.ruleLetter = production.getLeft();

                // add to the correct chain!
                if(!chain.containsKey(nonTerminal)) {
                    chain.put(nonTerminal, new LinkedList<>());
                }
                chain.get(nonTerminal).add(countNode);
            }
        }

        Comparator<Integer> comparator = (a, b) -> {
            assert minLengthList.get(a) != -1 && minLengthList.get(b) != -1;
            if(minLengthList.get(a) < minLengthList.get(b)) {
                return -1;
            }
            else if(minLengthList.get(a) > minLengthList.get(b)){
                return 1;
            }
            else {
                return 0;
            }
        };
        PriorityQueue<Integer> heap = new PriorityQueue<>(comparator);

        // calculate minWordLength for all terminal rules
        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            S left = production.getLeft();
            W right = production.getRight();

            if(right.allMatch(s -> s.isTerminal())) {
                long thisLen = production.getNumberOfTerminals();
                minLengthList.set(rId, thisLen);
                heap.add(rId);
            }
        }

        /**
         * All non-terminals with one rule r: A -> B_1B_2w... where all non-terminals on the right hand side are SLPs is a non-trivial SLP.
         */
        while(!heap.isEmpty()) {
            int rId = heap.poll();
            P mainProduction = productionList.get(rId);
            S nonTerminal = mainProduction.getLeft();

            assert !minimalWordLength.containsKey(nonTerminal) || minimalWordLength.get(nonTerminal) <= minLengthList.get(rId);


            if(!minimalWordLength.containsKey(mainProduction.getLeft())) {
                minimalWordLength.put(nonTerminal, minLengthList.get(rId));
                slpProductions.put(nonTerminal, mainProduction);

                if(chain.containsKey(nonTerminal)) {
                    for(CountNode<S> countNode : chain.get(nonTerminal)) {
                        if(!countNode.count.isZero()) {
                            countNode.count.dec();
                            if(countNode.count.isZero()) {
                                P p = productionList.get(countNode.ruleId);
                                long thisLen = p.getNumberOfTerminals();
                                List<S> rightNonTerminals = p.getRight().findAll(s -> !s.isTerminal());
                                thisLen += rightNonTerminals.stream()
                                        .map(nt -> minimalWordLength.get(nt))
                                        .reduce(0L, (a, b) -> a + b);


                                if(minLengthList.get(countNode.ruleId) != -1 && minLengthList.get(rId) > thisLen) {
                                    throw new IllegalArgumentException("the size of a rule should be set only once");
                                }

                                minLengthList.set(countNode.ruleId, thisLen);

                                if(!minimalWordLength.containsKey(countNode.ruleLetter)) {
                                    heap.add(countNode.ruleId);
                                }
                            }
                        }
                    }
                }
            }
        }

        return new Pair<>(minimalWordLength, slpProductions);
    }

    /**
     * Let G be the grammar defined by P.
     *
     * The method computes a mappings: N -> long, X |-> k, the length (k) of a shortest word generated by the non-terminal (X)
     *
     * @param productions   the production that defines the grammar
     * @return a mapping that gives for a non-terminal the length of a shortest word that this non-terminal generates, and an SLP for a shortest word.
     */
    public Map<S, P> getMinimalWord(final Set<P> productions) {
        return getMinimalWordAndLengths(productions).b;
    }

    /**
     * Let G be the grammar defined by P and S and N be the set of non-terminals in P.
     * The method computes two mappings:
     *      1. N -> long, X |-> k, the length (k) of a shortest non-empty word generated by the non-terminal (X)
     *      2. N -> P, the SLP representing a shortest word
     *
     * @param productions   the production that defines the grammar
     * @param axiom         the axiom that defines the grammar
     * @param creator       the creator that created the set of productions and the axiom
     * @return a mapping that gives for a non-terminal the length of a shortest word that this non-terminal generates, and an SLP for a shortest word.
     */
    public Optional<Z> getMinimalNonEmptyWord(final Set<P> productions, final S axiom, final ICFGCreator<N, S, W, P, C, Z> creator) {
        Set<P> copyProductions = creator.copyProductions(productions);

        // 1. eliminateQuasiperiodicity into weak cnf without epsilon containing only useful productions
        C epsilonFreeCFG = toBinaryEpsilonFree(copyProductions, axiom, creator);

        // 2. delete epsilon from L(G)
        Set<P> newProductions = epsilonFreeCFG.getProductions();
        newProductions.removeIf(p -> p.getLeft().equals(epsilonFreeCFG.getAxiom()) && p.getRight().isEmpty());

        // if there is no more production then G does only generate the empty word and no shortest non empty word exists!
        if(newProductions.isEmpty()) {
            return Optional.empty();
        }
        else {
            // if there are production then clearly the shortest word is now the shortest non-mepty word of L(G)
            return Optional.of(getMinimalWord(newProductions, epsilonFreeCFG.getAxiom(), true, creator));
        }
    }

    /**
     * Returns true if L(G) = {emptyWord}, otherwise false;
     *
     * @param productions   the production of the CFG
     * @param axioms        the axioms of the CFG
     * @return true if L(G) = {emptyWord}, otherwise false;
     */
    public boolean isEmptyWord(final Set<P> productions, final Set<S> axioms) {
        Set<P> usefulProductions = deleteUseless(productions, axioms);
        // if all useful productions are nullable and the language is not empty => L(G) = {emptyWord}.
        return usefulProductions.size() > 0 && getNullableProductions(usefulProductions).size() == usefulProductions.size();
    }

    /**
     * Returns true if L(G) = {emptyWord}, otherwise false;
     *
     * @param productions   the production of the CFG
     * @param axiom         the axiom of the CFG
     * @return true if L(G) = {emptyWord}, otherwise false;
     */
    public boolean isEmptyWord(final Set<P> productions, final S axiom) {
        Set<S> axioms = new HashSet<>();
        axioms.add(axiom);
        return isEmptyWord(productions, axioms);
    }

    /**
     * Returns true if the empty word is in the language defined by the set of productions and axioms.
     *
     * @param productions   the set of productions defining the language
     * @param axioms        the set of axioms defining the language
     * @return true => the empty word is in the language, otherwise false.
     */
    public boolean isNullable(final Set<P> productions, final Set<S> axioms) {
        // all axiom production are nullable?
        return getNullableProductions(productions).stream().anyMatch(p -> axioms.contains(p.getLeft()));
    }

    /**
     * Returns true if the empty word is in the language defined by the set of productions and axioms.
     *
     * @param productions   the set of productions defining the language
     * @param axiom         the axiom defining the language
     * @return true => the empty word is in the language, otherwise false.
     */
    public boolean isNullable(final Set<P> productions, final S axiom) {
        Set<S> axioms = new HashSet();
        axioms.add(axiom);
        // all axiom production are nullable?
        return getNullableProductions(productions).stream().anyMatch(p -> axioms.contains(p.getLeft()));
    }

    /**
     * Computes the set of all nullable productions in the collection of productions.
     *
     * @param productions the collection of productions.
     * @return all nullable productions
     */
    public Set<P> getNullableProductions(final Collection<P> productions) {
        /**
         * Filter non-terminals with more than 1 rule.
         */
        List<P> productionList = productions.stream().collect(Collectors.toList());
        Set<P> emptyProductions = new HashSet<>();

        // 1. Construct the chain data structure from Hopcroft et. all
        Map<S, LinkedList<CountNode<S>>> chain = new HashMap<>();
        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            List<Node<S>> rhsNonTerminals = production.getRight().findAllNodes(symbol -> !symbol.isTerminal());
            Counter counter = new Counter(rhsNonTerminals.size());

            for(Node<S> nonTerminalNode : rhsNonTerminals) {
                S nonTerminal = nonTerminalNode.getElement();
                CountNode<S> countNode = new CountNode();
                countNode.count = counter;
                countNode.letter = nonTerminalNode;
                countNode.ruleId = rId;
                countNode.ruleLetter = production.getLeft();

                // add to the correct chain!
                if(!chain.containsKey(nonTerminal)) {
                    chain.put(nonTerminal, new LinkedList<>());
                }
                chain.get(nonTerminal).add(countNode);
            }
        }

        // non-terminals that are the left of a empty production
        Set<S> emptyNonTerminals = new HashSet<>();

        /**
         * All non-terminals with one rule r: A -> w where w is a word are trivial SLPs.
         */
        LinkedList<Integer> heap = new LinkedList<>();
        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            S left = production.getLeft();
            W right = production.getRight();

            if (right.isEmpty()) {
                emptyProductions.add(productionList.get(rId));

                if(!emptyNonTerminals.contains(left)) {
                    emptyNonTerminals.add(left);
                    heap.add(rId);
                }
            }
        }

        /**
         * All non-terminals with one rule r: A -> B_1B_2w... where all non-terminals on the right hand side are SLPs is a non-trivial SLP.
         */
        while(!heap.isEmpty()) {
            int rId = heap.poll();
            P mainProduction = productionList.get(rId);
            S nonTerminal = mainProduction.getLeft();

            if(chain.containsKey(nonTerminal)) {
                for(CountNode<S> countNode : chain.get(nonTerminal)) {

                    if(!countNode.count.isZero()) {
                        countNode.count.dec();
                        if(countNode.count.isZero() && productionList.get(countNode.ruleId).getNumberOfTerminals() == 0) {
                            emptyProductions.add(productionList.get(countNode.ruleId));
                            if(!emptyNonTerminals.contains(countNode.ruleLetter)) {
                                emptyNonTerminals.add(countNode.ruleLetter);
                                heap.add(countNode.ruleId);
                            }
                        }
                    }
                }
            }
        }

        return emptyProductions;
    }

    /**
     * Returns all productions that generate the empty word.
     * @param productions
     * @return
     */
    public Set<S> getNullableNonTerminals(final Set<P> productions) {
        return getNullableProductions(productions).stream().map(p -> p.getLeft()).collect(Collectors.toSet());
    }

    /**
     * Returns non-terminals that are axioms of an SLPs i.e. at that point in the derivation
     * there is only one derivation tree to derive from these non-terminals!
     *
     * Note: that a non-terminal might be a singleton but not an SLP for example:
     * A -> B
     * A -> C
     * B -> a
     * C -> a
     * here A is a singleton but not an SLP since there are 2 rules for A.
     *
     * Complexity: O(|G|)
     *
     * @param productions the productions of a CFG
     * @return all non-terminals that are SLPs.
     */
    public Set<S> getSLPNonTerminals(final Set<P> productions) {
        /**
         * Filter non-terminals with more than 1 rule.
         */
        Map<S, List<P>> map = productions.stream().collect(Collectors.groupingBy(p -> p.getLeft(), Collectors.toList()));
        List<P> productionList = map.entrySet().stream().filter(entry -> entry.getValue().size() == 1).map(e -> e.getValue().get(0)).collect(Collectors.toList());


        // 1. Construct the chain data structure from Hopcroft et. all
        Map<S, LinkedList<CountNode<S>>> chain = new HashMap<>();
        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            List<Node<S>> rhsNonTerminals = production.getRight().findAllNodes(symbol -> !symbol.isTerminal());
            Counter counter = new Counter(rhsNonTerminals.size());

            for(Node<S> nonTerminalNode : rhsNonTerminals) {
                S nonTerminal = nonTerminalNode.getElement();
                CountNode<S> countNode = new CountNode();
                countNode.count = counter;
                countNode.letter = nonTerminalNode;
                countNode.ruleId = rId;
                countNode.ruleLetter = production.getLeft();

                // add to the correct chain!
                if(!chain.containsKey(nonTerminal)) {
                    chain.put(nonTerminal, new LinkedList<>());
                }
                chain.get(nonTerminal).add(countNode);
            }
        }

        /**
         * All non-terminals with one rule r: A -> w where w is a word are trivial SLPs.
         */
        Set<S> slp = new HashSet<>();
        LinkedList<Integer> heap = new LinkedList<>();
        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            S left = production.getLeft();
            W right = production.getRight();

            if(right.allMatch(s -> s.isTerminal())) {
                slp.add(left);
                heap.add(rId);
            }
        }

        /**
         * All non-terminals with one rule r: A -> B_1B_2w... where all non-terminals on the right hand side are SLPs is a non-trivial SLP.
         */
        while(!heap.isEmpty()) {
            int rId = heap.poll();
            P mainProduction = productionList.get(rId);
            S nonTerminal = mainProduction.getLeft();

            if(chain.containsKey(nonTerminal)) {
                for(CountNode<S> countNode : chain.get(nonTerminal)) {

                    if(!countNode.count.isZero()) {
                        countNode.count.dec();
                        if(countNode.count.isZero() && !slp.contains(countNode.ruleLetter)) {
                            slp.add(countNode.ruleLetter);
                            heap.add(countNode.ruleId);
                        }
                    }
                }
            }
        }

        return slp;
    }

    /**
     * Creates an SLP out of a CFG. This has not to be a shortest word of the CFG!
     *
     * @param productions   productions of the CFG.
     * @param axioms        axioms of the CFG.
     * @param deleteEmpty   true => deletes epsilon productions.
     * @return a SLP, representing a word of the CFL.
     */
    public Set<P> toSLP(final Set<P> productions, final Set<S> axioms, final boolean deleteEmpty) {
        Map<S, P> slpProductions = getProductives(productions, axioms, false, true).stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p));
        if(deleteEmpty) {
            slpProductions = eliminateEpsilon(slpProductions, axioms);
        }
        return slpProductions.values().stream().collect(Collectors.toSet());
    }

    /**
     * Convert a CFG G to a SLP S that generates a word w with w is in L(G).
     * The arguments (i.e. productions) will not be changed by this conversion.
     *
     * @param productions   set of productions of the CFG G
     * @param axioms        set of non-terminal symbols that are axioms
     * @param deleteEmpty   if true => delete all rules E -> '' were E is not an axiom
     * @return              a SLP that generates a word w s.t. w is in L(G).
     */
    public Set<P> getProductives(final Collection<P> productions, final Set<S> axioms, final boolean deleteEmpty, final boolean takeSingle) {

        // 1. Construct the chain data structure from Hopcroft et. all
        List<P> productionList = productions.stream().collect(Collectors.toList());
        Map<S, LinkedList<CountNode<S>>> chain = new HashMap<>();
        Set<S> productives = new HashSet<>();
        Set<P> result = new HashSet<>();
        Set<S> deleted = new HashSet<>();

        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            List<Node<S>> rhsNonTerminals = production.getRight().findAllNodes(symbol -> !symbol.isTerminal());
            Counter counter = new Counter(rhsNonTerminals.size());

            for(Node<S> nonTerminalNode : rhsNonTerminals) {
                S nonTerminal = nonTerminalNode.getElement();
                CountNode<S> countNode = new CountNode();
                countNode.count = counter;
                countNode.letter = nonTerminalNode;
                countNode.ruleId = rId;
                countNode.ruleLetter = production.getLeft();

                // add to the correct chain!
                if(!chain.containsKey(nonTerminal)) {
                    chain.put(nonTerminal, new LinkedList<>());
                }
                chain.get(nonTerminal).add(countNode);
            }
        }

        LinkedList<Integer> heap = new LinkedList<>();
        for(int rId = 0; rId < productionList.size(); rId++) {
            P production = productionList.get(rId);
            if(production.getRight().allMatch(s -> s.isTerminal())) {
                boolean emptyCondition = (!deleteEmpty || !production.getRight().isEmpty() || axioms.contains(production.getLeft()));
                if((!productives.contains(production.getLeft()) && emptyCondition) || (!takeSingle && emptyCondition)) {
                    result.add(production);
                }

                if(!productives.contains(production.getLeft())) {
                    heap.add(rId);
                    productives.add(production.getLeft());
                }
            }
        }

        // 2. get the start symbols
        while(!heap.isEmpty()) {
            int rId = heap.poll();
            S nonTerminal = productionList.get(rId).getLeft();
            P mainProduction = productionList.get(rId);

            boolean isEmpty = mainProduction.getRight().isEmpty();

            if(chain.containsKey(nonTerminal)) {
                for(CountNode<S> countNode : chain.get(nonTerminal)) {

                    if(isEmpty && deleteEmpty && !deleted.contains(nonTerminal)) {
                        countNode.letter.remove();
                    }

                    if(!countNode.count.isZero()) {
                        countNode.count.dec();

                        if(countNode.count.isZero()) {
                            P slpProduction = productionList.get(countNode.ruleId);
                            boolean emptyCondition = (!deleteEmpty || !slpProduction.getRight().isEmpty() || axioms.contains(slpProduction.getLeft()));
                            if((!productives.contains(slpProduction.getLeft()) && emptyCondition) || (!takeSingle && emptyCondition)) {
                                result.add(productionList.get(countNode.ruleId));
                            }

                            if(!productives.contains(countNode.ruleLetter)) {
                                productives.add(countNode.ruleLetter);
                                heap.add(countNode.ruleId);
                            }
                        }
                    }
                }
            }

            if(isEmpty && deleteEmpty) {
                deleted.add(nonTerminal);
            }
        }

        // 3. remove non-terminals that are not an axiom and generate the empty-word.
        Set<P> clean;
        if(deleteEmpty) {
            clean = result.stream().filter(p -> !p.getRight().isEmpty()).collect(Collectors.toSet());
        }
        else {
            clean = result;
        }


        return clean;
    }
}
