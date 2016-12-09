package grammar.gen;

import grammar.inter.ICFG;
import grammar.inter.IReferencedWord;
import grammar.inter.IProduction;
import grammar.inter.ISLP;
import symbol.IJezSymbol;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A generic implementation of a SLP, note that an SLP can represents more than one
 * word if we have more than one axiom. However all other conditions of SLPs has to be true.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 *
 */
public class GenSLP<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>, P extends IProduction<N, S, W>> extends GenAbstractCFG<N, S, W, P> implements ISLP<N, S, W, P> {

    /**
     * Relation that maps non-terminal -> ruleMap, e |-> r, where r contains all ruleMap
     * with lhs(r) == e.
     */
    private Map<S, P> produtions;

    /**
     * Relation that maps non-terminal -> List of non-terminal, e |-> list, where list contains all
     * non-terminals t with there exists r such that lhs(r) = t and rhs(r) contains e.
     * Only needed for calculate the order of the slp and to check if some non-terminal is a start symbol.
     * The id of the non-terminal is equal to the index in the list! This is due to speed up the computation.
     * If there is a rule t -> EE, then t is maybe twice in the list of t.
     */
    private Map<S, Set<S>> predecessors;

    /**
     * A mapping : X -> |val(X)|
     */
    private Map<S, Long> lengthMap;

    /**
     * A topological order of the non-terminal symbols.
     */
    private LinkedList<P> orderedProductions;

    /**
     * The set of axioms.
     */
    private Set<S> axioms;

    /**
     * Default constructor for a SLP representing possibly more than one word.
     *
     * @param produtions    the map of slp-productions
     * @param axioms        the set of axioms
     */
    public GenSLP(final Map<S, P> produtions, final Set<S> axioms) {
        init(produtions, axioms);
    }

    /**
     * Default constructor for a SLP representing possibly more than one word.
     *
     * @param produtions    the set of slp-productions
     * @param axioms        the set of axioms
     */
    public GenSLP(final Set<P> produtions, final Set<S> axioms) {
        init(toMap(produtions), axioms);
    }

    private void init(final Map<S, P> produtions, final Set<S> axioms) {
        this.lengthMap = new HashMap<>();
        for(Map.Entry<S, P> entry : produtions.entrySet()) {
            if(!entry.getKey().equals(entry.getValue().getLeft())) {
                throw new IllegalArgumentException("the production map is not well defined.");
            }

            if(entry.getKey().isTerminal()) {
                throw new IllegalArgumentException("terminal at the left side of a production.");
            }
        }
        this.produtions = produtions;
        this.axioms = axioms;
        this.init();
    }

    private Map<S, P> toMap(final Set<P> produtions) {
        Map<S, P> produtionMap = new HashMap<>();
        for(P p : produtions) {
            if(produtionMap.containsKey(p.getLeft())) {
                throw new IllegalArgumentException("invalid slp productions.");
            }
            else {
                produtionMap.put(p.getLeft(), p);
            }
        }
        return produtionMap;
    }

    @Override
    public boolean containsProduction(final S left) {
        return produtions.containsKey(left);
    }

    @Override
    public LinkedList<P> getOrderedProductions() {
        return orderedProductions;
    }

    @Override
    public P getProduction(S symbol) {
        return produtions.get(symbol);
    }

    @Override
    public boolean isSingleton() {
        return getAxioms().size() == 1;
    }

    @Override
    public Set<P> getProductions() {
        return Collections.unmodifiableSet(new HashSet<>(produtions.values()));
    }

    @Override
    public Map<S, P> getSLPProductions() {
        return Collections.unmodifiableMap(produtions);
    }

    @Override
    public Set<P> getProductions(S symbol) {
        Set<P> set = new HashSet<>();
        set.add(produtions.get(symbol));
        return Collections.unmodifiableSet(set);
    }


    @Override
    public Set<S> getAxioms() {
        return Collections.unmodifiableSet(axioms);
    }

    @Override
    public void deleteProduction(S left) {
        produtions.remove(left);
        predecessors.remove(left);
    }

    /**
     * Evaluates the val(letter), where letter is a non-terminal or a terminal symbol.
     * If T is a terminal it will just return this terminal symbol.
     * Note: If the transition graph is not acyclic this method would may not stop,
     * so in this case the method returns the EMPTY_WORD.
     * @param left      the non-terminal or terminal symbol
     * @return          val(letter) if the transition graph is acyclic, EMPTY_WORD otherwise
     */
    @Override
    public List<S> value(final S left) {
        LinkedList<S> linkedLetters = new LinkedList<>();
        deptFirst(left, linkedLetters::add);
        return linkedLetters;
    }

    @Override
    public List<?> word(final S left) {
        return value(left).stream().map(symbol -> symbol.getName()).collect(Collectors.toList());
    }

    @Override
    public String toString(final S left) {
        List<?> chars = word(left);
        StringBuilder builder = new StringBuilder();
        chars.forEach(c -> builder.append(c));
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(S axiom : axioms) {
            List<?> chars = word(axiom);
            chars.forEach(c -> builder.append(c));
            builder.append("\n");
        }
        if(builder.charAt(builder.length()-1) == '\n') {
            builder.deleteCharAt(builder.length()-1);
        }

        return builder.toString();
    }

    @Override
    public GenSLP<N, S, W, P> clone() {
        Map<S, P> clonedProductions = new HashMap<>();
        Set<S> clonedAxioms = new HashSet<>();

        for(Map.Entry<S, P> entry : produtions.entrySet()) {
            P clonedProduction = (P)entry.getValue().clone();
            clonedProductions.put(clonedProduction.getLeft(), clonedProduction);
        }

        for(S axiom : axioms) {
            clonedAxioms.add((S) axiom.clone());
        }

        return new GenSLP<>(clonedProductions, axioms);
    }

    @Override
    public ISLP<N, S, W, P> eliminateEpsilon() {
        GenSLP<N, S, W, P> clone = this.clone();
        GenCFGOp<N, S, W, P, ICFG<N, S, W, P>, ISLP<N, S, W, P>> genCFGOp = new GenCFGOp<>();
        clone.produtions = genCFGOp.eliminateEpsilon(clone.produtions, clone.axioms);
        clone.init(clone.produtions, clone.axioms);
        return clone;
    }

    @Override
    public long length(final S left) {
        if(!containsProduction(left)) {
            throw new IllegalArgumentException(left + " is not a non-terminal of this SLP.");
        }

        if(lengthMap.containsKey(left)) {
            return lengthMap.get(left);
        }
        else {
            Iterator<P> descendingIterator = orderedProductions.descendingIterator();
            while (descendingIterator.hasNext()) {
                P production = descendingIterator.next();
                long terminalLength = production.getRight().stream().filter(s -> s.isTerminal()).count();
                long nonTerminalLength = production.getRight().stream().filter(s -> !s.isTerminal()).map(s -> lengthMap.get(s)).reduce(0L, (a, b) -> a + b);
                long productionLength = terminalLength + nonTerminalLength;
                lengthMap.put(production.getLeft(), productionLength);
            }
        }

        return length(left);
    }

    @Override
    public long length(final S left, final boolean changed) {
        if(!containsProduction(left)) {
            throw new IllegalArgumentException(left + " is not a non-terminal of this SLP.");
        }
        if(changed) {
            lengthMap = new HashMap<>();
        }
        return length(left);
    }

    @Override
    public S get(final S left, long k) {
        if(k < 1 || k > length(left)) {
            throw new IllegalArgumentException("the terminal does not exist");
        }
        long position = 0;
        P production = getProduction(left);
        boolean goDeeper = true;

        while (goDeeper && production != null) {
            goDeeper = false;
            for(S symbol : production.getRight()) {
                if(symbol.isTerminal()) {
                    position++;
                    if(position == k) {
                        return symbol;
                    }
                }
                else {
                    if(position + length(symbol) >= k) {
                        production = getProduction(symbol);
                        goDeeper = true;
                        break;
                    }
                    else {
                        position += length(symbol);
                    }
                }
            }
        }

        // this should never happen
        return null;
    }

    @Override
    public long length() {
        return length(getAxiom());
    }

    @Override
    public S getAxiom() {
        if(axioms.isEmpty()) {
            return null;
        }
        return axioms.iterator().next();
    }

    private void deptFirst(final S letter, final Consumer<S> consumer) {
        if(letter != null) {
            if(letter.isTerminal()) {
                consumer.accept(letter);
            }
            else {
                if(!containsProduction(letter)) {
                    throw new IllegalArgumentException(letter + "is neither a terminal nor a non-terminal of this SLP.");
                }
                P production = getProduction(letter);
                W rhs = production.getRight();
                for(S l : rhs) {
                    deptFirst(l, consumer);
                }
            }
        }
    }

    /**
     * Complexity: O(size of non-terminal alphabet)
     */
    private void init() {
        // initial predecessor map
        this.predecessors = getAllPredecessors();
        GenCFGOp<N, S, W, P, ?, ?> genCFG = new GenCFGOp<>();
        LinkedList<S> nonTerminalOrder = genCFG.getOrder(this.getProductions());

        this.orderedProductions = new LinkedList<>();
        for(S nonTerminal : nonTerminalOrder) {
            this.orderedProductions.add(getProduction(nonTerminal));
        }
         //       getOrder();
        if(orderedProductions.size() < produtions.keySet().size()) {
            throw new IllegalArgumentException("the production set does not form a well defined slp.");
        }
    }

    /**
     * The first element is like axiom -> aABd...
     * Complexity: O(|G| * log|G|)
     * @return
     */
    private LinkedList<P> getOrder() {
        LinkedList<P> orderedList = new LinkedList<>();
        //Map<Letter, Integer> deleteCounter = new HashMap<>();
        Map<S, Integer> deleteCounters = new HashMap<>();

        // O(|G|)
        LinkedList<P> startSymbols = getProductions().stream()
                .filter(p -> predecessors.get(p.getLeft()).isEmpty())
                .collect(Collectors.toCollection(LinkedList::new));

        while(!startSymbols.isEmpty()) {
            P production = startSymbols.pollFirst();
            orderedList.add(production);

            // here comes maybe the log|G| into play
            Set<S> set = new HashSet<>();
            for(S rightLetter : production.getRight().findAll(symbol -> !symbol.isTerminal())) {
                if(!set.contains(rightLetter)) {
                    if (!deleteCounters.containsKey(rightLetter)) {
                        deleteCounters.put(rightLetter, new Integer(0));
                    }
                    set.add(rightLetter);
                    deleteCounters.put(rightLetter, deleteCounters.get(rightLetter)+1);
                    // all edges "deleted"
                    if(deleteCounters.get(rightLetter) == predecessors.get(rightLetter).size()) {
                        startSymbols.add(produtions.get(rightLetter));
                    }
                }
            }
        }

        return orderedList;
    }

    /**
     * Complexity: O(|G|)
     * @return
     */
    private Map<S, Set<S>> getAllPredecessors() {
        // initial list
        Map<S, Set<S>> predecessorMap = new HashMap<>();

        for(P production : getProductions()) {
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
}
