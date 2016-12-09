package grammar.gen;

import data.MyLinkedList;
import data.Node;
import grammar.inter.*;
import symbol.IJezSymbol;
import org.apache.log4j.LogManager;
import utils.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * The implementation of the recompression algorithm described in the Jez-Paper (Dio:10.1145/2631920).
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 */
public class GenJez<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>, P extends IProduction<N, S, W>> {

    private static org.apache.log4j.Logger logger = LogManager.getLogger(GenJez.class);

    /**
     * The changing grammar defining two words to compare or to use for the pattern matching.
     */
    private ISLP<N, S, W, P> slp;

    /**
     * The current phase of the recompression algorithm.
     */
    private int phase;

    /**
     * true => the setting is pattern matching, false => the setting is word equality test.
     */
    private boolean matching;

    /**
     * for pattern matching: true => the last compression was a block compression, false => the last compression was a pair compression.
     */
    private boolean matchingBlocks = false;

    /**
     * true => the algorithm uses the greedy pair compression, otherwise the algorithm uses the compression for compressing all pairs.
     */
    private boolean greedyPairCompression = false;

    /**
     *  mapping X -> list of pointers to X
     */
    private final Map<S, List<Node<S>>> occurrences;

    /**
     * X -> appearances of X in the the word i.e. val(axiom)
     */
    private final Map<S, Integer> appearances;

    /**
     * the terminal factory i.e. the terminal alphabet.
     */
    private final IJezSymbolFactory<N,S,W, P> terminalAlphabet;

    /**
     * The class to detect pair and block occurrences.
     */
    private final ReferencedWordProperties wordProperties;

    /**
     * the axiom of the text in the setting of pattern matching, otherwise null.
     */
    private final S text;

    /**
     * the axiom of the pattern in the setting of pattern matching, otherwise null.
     */
    private final S patter;

    /**
     * the largest id of the set of non-terminals of the grammar.
     */
    private final int maxNonTerminal;


    /**
     * the comparator to compare block record entries i.e. tuples of (a, l, ...), where a is the letter and l is the length of a block.
     */
    private final BlockRecordComparator blockComparator;

    /**
     * a function used to identify if the recompression algorithm should do a left-pop.
     */
    private BiFunction<S, Integer, Boolean> doLeftPop = (nonTerminal, j) -> doLeftPop(nonTerminal, head -> head.isTerminal()
            && IndexGenerator.isInRightPartition(j, head));

    /**
     * a function used to identify if the recompression algorithm should do a right-pop.
     */
    private BiFunction<S, Integer, Boolean> doRightPop = (nonTerminal, j) -> doRightPop(nonTerminal, tail -> tail.isTerminal()
            && IndexGenerator.isInLeftPartition(j, tail));

    /**
     * The constructor to construct a recompression instance for solving the equality word problem.
     *
     * @param slp       the SLP defining two or more words we want to check equality for
     * @param factory   a factory to create a fresh SLP with non-terminals in [|N|], where N is the set of non-terminals of the original SLP
     */
    public GenJez(final ISLP<N, S, W, P> slp, final ICFGCreatorFactory<N, S, W, P, ICFG<N, S, W, P>, ISLP<N, S, W, P>> factory) {
        this(slp, null, null, false, factory);
        this.matching = false;
    }

    /**
     * The default constructor for solving the equality word problem or the pattern matching problem.
     *
     * @param slp       the SLP defining the words
     * @param text      the non-terminal of the text (or null in case of word equality)
     * @param pattern   the non-terminal of the pattern (or null in case of word equality)
     * @param matching  true for matching, otherwise false
     * @param factory   a factory to create a fresh SLP with non-terminals in [|N|], where N is the set of non-terminals of the original SLP.
     */
    public GenJez(final ISLP<N, S, W, P> slp,
                  final S text,
                  final S pattern,
                  final boolean matching,
                  final ICFGCreatorFactory<N, S, W, P, ICFG<N, S, W, P>, ISLP<N, S, W, P>> factory) {
        this.phase = 0;
        this.matching = matching;
        ICFGCreator<N, S, W, P, ICFG<N, S, W, P>, ISLP<N, S, W, P>> cfgCreator = factory.create();
        this.slp = slp.eliminateEpsilon();
        Map<S, S> replacementMap = new HashMap<>();
        this.slp = cfgCreator.freshNonTerminals(this.slp, replacementMap);
        this.terminalAlphabet = cfgCreator.getSymbolFactory();
        this.text = replacementMap.get(text);
        this.patter = replacementMap.get(pattern);

        this.maxNonTerminal = terminalAlphabet.getMaxNonterminal();
        this.blockComparator = new BlockRecordComparator();
        this.occurrences = getAllOccurrences();
        this.appearances = getAppearances();
        this.wordProperties = new ReferencedWordProperties();
    }

    /**
     * Returns the current changing SLP of the recompression algorithm.
     *
     * @return the current changing SLP
     */
    public ISLP<N, S, W, P> getSlp() {
        return slp;
    }

    /**
     * Returns the axiom of the pattern.
     *
     * @return the axiom of the pattern
     */
    public P getPattern() {
        return this.slp.getProduction(patter);
    }

    /**
     * Returns the axiom of the text.
     *
     * @return the axiom of the text
     */
    public P getText() {
        return this.slp.getProduction(text);
    }

    /**
     * Returns true if for all axioms X_1, ..., X_n : val(X_1) = ... = val(X_n), otherwise false.
     *
     * @return true if for all axioms X_1, ..., X_n : val(X_1) = ... = val(X_n), otherwise false.
     */
    public boolean isEquals() {
        long len = -1L;

        //1. check the lengths of the words
        for(S left : slp.getAxioms()) {
            if(len == -1L) {
                len = slp.length(left);
            }
            else {
                if(len != slp.length(left)) {
                    return false;
                }
            }
        }

        // all words are the empty word
        if(len == 0) {
            return true;
        }

        // 2. the length are all equals and not zero => execute the recompression
        execute();

        S otherSymbol = null;
        boolean first = true;

        // 3. chech now if for all axioms X : |val(X)| = 1 and all val(X) are equals.
        for(S left : slp.getAxioms()) {
            if(first) {
                slp.length(left, true);
                first = false;
            }

            if(slp.length(left) != 1) {
                return false;
            }
            else {
                S symbol = slp.get(left, 1);
                if(otherSymbol == null) {
                    otherSymbol = symbol;
                }

                if(!symbol.equals(otherSymbol)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void print() {
        for(S left : slp.getAxioms()) {
            StringBuilder builder = new StringBuilder();
            builder.append(left + " -> ");
            slp.value(left).forEach(s -> builder.append(s.getId() + ","));
            logger.info(builder.toString());
        }
    }

    /**
     * Execute the recompression.
     *
     * Complexity: O((n+m)^2 log(n+m)), where n+m is the size of the original SLP.
     */
    public void execute() {
        firstRenameTerminals();
        while (!hasFinished()) {
            phase();
        }
    }

    /**
     * Calculate one phase of Jéz-algorithm. This will change the grammar. Complexity: O(|G| + (n+m)log(n+m)) and we need O(log(M)) phases.
     * Overall complexity: O((n+m)^2 log(n+m)), since |G| = O((n+m)log(n+m)).
     */
    public void phase() {
        int size = terminalAlphabet.getMax(phase) + 1;
        this.phase++;
        terminalAlphabet.nextPhase();

        // for pattern matchingAll
        if(matching && !hasFinished()) {
            // fix the beginning and the end
            Map<S, Pair<S, S>> firstLastMap = getFirstLastMap();
            Pair<S, S> pair = firstLastMap.get(patter);

            if(pair.a == pair.b) {
                fixEndsSame(firstLastMap, patter, text);
            }
            else {
                fixEndsDifferent(firstLastMap, patter, text);
                matchingBlocks = false;
            }
        }

        // 1. compress blocks: O(|G| + (n+m)log(n+m))
        if(!hasFinished()) {
            compressBlocks(phase);
        }

        if(!hasFinished()) {
            // get all pairs: O(|G|)
            List<GPairRecord<S, S>> pairRecords = getPairs(p -> p.a.getPhaseId() < phase && p.b.getPhaseId() < phase);

            // compress non-crossing pairs: O(|G|)
            compressNonCrossingPairs(pairRecords);

            // compress all crossing pairs: O(|G|)
            if(greedyPairCompression) {
                compressEnoughPairs(pairRecords, size);
            }
            // compress enough crossing pairs O((n+m) log(n+m))
            else {
                compressCrossingPairs(pairRecords, phase, size);
            }
        }

        // renumber the alphabet: O(|G|)
        if(!hasFinished()) {
            renameTerminalsFromLastPhase();
        }
    }

    /**
     * Returns a list of all positions of occurrences of a terminal symbol.
     * This could take exponential running time.
     *
     * Complexity: O(2^(|G|))
     *
     * @return          A list containing all positions of occurrences of the terminal
     */
    public List<Long> getPatternOccurrencePositions() {
        if(!hasFinished()) {
            throw new IllegalArgumentException("the recompression is not jet ready.");
        }
        S terminal = slp.get(getPattern().getLeft(), 1);
        if(matchingBlocks) {
            return getBlockOccurrencePositions(terminal);
        }
        else {
            return getNonBlockOccurrencePositions(terminal);
        }
    }

    /**
     * Returns the number of occurrences of the pattern in val(S) i.e. the text.
     *
     * Requirement: the recompression is complete.
     *
     * @return the number of occurrences of the pattern in the text
     */
    public long getNumberOfPatternOccurrences() {
        if(!hasFinished()) {
            throw new IllegalArgumentException("the recompression is not jet ready.");
        }

        S terminal = slp.get(getPattern().getLeft(), 1);
        return getPatternOccurrence(terminal).get(getText());
    }

    /**
     * Returns the position of the k-th occurrence of the terminal in the text, if there is one.
     * Complexity: O(|G|)
     *
     * @param k         number of occurrence
     * @return k-th occurrence of the terminal in the text, if there is one
     */
    public Optional<Long> getPatternOccurrencePosition(final long k) {
        if(!hasFinished()) {
            throw new IllegalArgumentException("the recompression is not jet ready.");
        }
        S terminal = slp.get(getPattern().getLeft(), 1);

        if(matchingBlocks) {
            return getBlockOccurrencePosition(terminal, k);
        }
        else {
            return getNonBlockOccurrencePosition(terminal, k);
        }
    }

    /**
     * Compresses in the first step pairs greedily considering their number of occurrences in the words.
     * After that a second round of pair compression take place compressing pairs greedily considering
     * their number of occurrences in G (for shorten the grammar enough).
     *
     * @param crossingPairs all crossing pairs
     */
    private void compressEnoughPairs(final List<GPairRecord<S, S>> crossingPairs, final int largestTerminal) {
        List<GPairRecord<S,S>> apperenceRecord = calculateAppearenceList(crossingPairs, false);
        compressPairsGreedy(apperenceRecord, crossingPairs, largestTerminal);

        apperenceRecord = calculateAppearenceList(crossingPairs, true);
        compressPairsGreedy(apperenceRecord, crossingPairs, largestTerminal);
    }

    /**
     * Implementation of the greedy pair compression from the Jez-Paper.
     * Complexity: O(|G| + n + m)
     *
     * @param pairApperances    a list of all appearances of all pairs (not occurrences of pairs)
     * @param crossingPairs     a list of all occurrences of pairs in the right-hand sides
     */
    public void compressPairsGreedy(final List<GPairRecord<S, S>> pairApperances, final List<GPairRecord<S, S>> crossingPairs, final int largestTerminal) {

        BitSet bitSet = new BitSet(largestTerminal);
        BitSet leftSigma = new BitSet(largestTerminal);
        BitSet rightSigma = new BitSet(largestTerminal);

        List<List<Pair<S, Long>>> right = new ArrayList<>(largestTerminal);
        List<List<Pair<S, Long>>> left = new ArrayList<>(largestTerminal);

        // initial tables left and right
        for(int i = 0; i < largestTerminal; i++) {
            right.add(new ArrayList<Pair<S, Long>>());
            left.add(new ArrayList<Pair<S, Long>>());
        }

        // fill tables with k_{ab} and k_{ba}
        for(GPairRecord<S, S> record : pairApperances) {
            right.get(record.pair.a.getId()).add(new Pair<S, Long>(record.pair.b, record.getAppearences()));
            left.get(record.pair.b.getId()).add(new Pair<S, Long>(record.pair.a, record.getAppearences()));
        }

        for(GPairRecord<S, S> record : crossingPairs) {
            bitSet.set(record.pair.a.getId());
            bitSet.set(record.pair.b.getId());
        }

        int[] countR = new int[largestTerminal];
        int[] countL = new int[largestTerminal];

        for(int a = 0; a < bitSet.size(); a++) {
            if(bitSet.get(a)) {
                int[] count = null;
                if(countR[a] >= countL[a]) {
                    leftSigma.set(a);
                    count = countL;
                }
                else {
                    rightSigma.set(a);
                    count = countR;
                }

                Iterator<Pair<S, Long>> rightIterator = right.get(a).iterator();
                while (rightIterator.hasNext()) {
                    Pair<S, Long> entry = rightIterator.next();
                    count[entry.a.getId()] += entry.b;
                    rightIterator.remove();
                }

                Iterator<Pair<S, Long>> leftIterator = left.get(a).iterator();
                while (leftIterator.hasNext()) {
                    Pair<S, Long> entry = leftIterator.next();
                    count[entry.a.getId()] += entry.b;
                    leftIterator.remove();
                }
            }
        }

        long sumL = 0;
        long sumR = 0;

        // Sum up appearances: O(|G|)
        for(GPairRecord<S, S> record : pairApperances) {
            Pair<S,S> pair = record.pair;
            if(leftSigma.get(pair.a.getId()) && rightSigma.get(pair.b.getId())) {
                sumL += record.getAppearences();
            }

            if(rightSigma.get(pair.a.getId()) && leftSigma.get(pair.b.getId())) {
                sumR += record.getAppearences();
            }
        }

        // swap
        if(sumL < sumR) {
            BitSet tmp = rightSigma;
            rightSigma = leftSigma;
            leftSigma = tmp;
        }

        Iterator<GPairRecord<S, S>> pairRecordIterator = crossingPairs.iterator();
        while (pairRecordIterator.hasNext()) {
            Pair<S, S> pair = pairRecordIterator.next().pair;
            if(!leftSigma.get(pair.a.getId()) || !rightSigma.get(pair.b.getId())) {
                pairRecordIterator.remove();
            }
        }
        final BitSet fixedRightSigma = rightSigma;
        final BitSet fixedLeftSigma = leftSigma;

        // remove pairs we don't wanna compress: O(|G|)
        final List<GPairRecord<S,S>> uncrossedPairs = crossingPairs.stream()
                .filter(rec -> !rec.isCrossingPair())
                .filter(rec -> wordProperties.isPairAt(rec.node, rec.pair))
                .filter(rec -> fixedLeftSigma.get(rec.pair.a.getId()) && fixedRightSigma.get(rec.pair.b.getId()))
                .collect(Collectors.toList());

        // gather the uncrossed ones: O(|G|)
        if(!uncrossedPairs.isEmpty()) {
            final List<GPairRecord<S,S>> trash = new LinkedList<>();
            final Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction = p ->
            {
                if(fixedLeftSigma.get(p.a.getId()) && fixedRightSigma.get(p.b.getId())) {
                    return uncrossedPairs::add;
                }
                else {
                    return trash::add;
                }
            };

            BiFunction<S, Integer, Boolean> doGreedyLeftPop = (nonTerminal, j) -> doLeftPop(nonTerminal, head -> head.isTerminal() && fixedRightSigma.get(head.getId()));

            BiFunction<S, Integer, Boolean> doGreedyRightPop = (nonTerminal, j) -> doRightPop(nonTerminal, tail -> tail.isTerminal() && fixedLeftSigma.get(tail.getId()));

            //pop: O(n+m)
            pop(phase, nonTerminal -> doGreedyLeftPop.apply(nonTerminal, 0), nonTerminal -> doGreedyRightPop.apply(nonTerminal, 0), consumerFunction);

            // O(|G|)
            sortPairs(uncrossedPairs);

            // O(|G|)
            compressUncrossedPairs(uncrossedPairs);
        }
    }

    /**
     * Calculates the number of appearance of a pair in the words or the grammar.
     * Complexity: O(|G|)
     *
     * @param pairRecords   records including the appearance of the lhs(p) where the pair if the record is in rhs(p).
     * @param grammar       false => appearance in the words, true => appearance in the grammar
     * @return              a single record for each explicit pair ab with his appearance
     */
    private List<GPairRecord<S,S>> calculateAppearenceList(final List<GPairRecord<S, S>> pairRecords, final boolean grammar) {
        List<GPairRecord<S,S>> list = new ArrayList<>();
        Iterator<GPairRecord<S, S>> recordIterator = pairRecords.iterator();

        if(!recordIterator.hasNext()) {
            return list;
        }

        GPairRecord<S, S> record = recordIterator.next();
        long appearence = record.getAppearences();
        GPairRecord<S, S> current = null;

        while (recordIterator.hasNext()) {
            current = recordIterator.next();
            if(current.pair.equals(record.pair)) {
                appearence += grammar ? 1 : current.getAppearences();
            }
            else {
                list.add(new GPairRecord<S, S>(current.pair, null, true, appearence));
                appearence = 0;
                record = current;
            }
        }

        if(current == null || current.pair.equals(record.pair)) {
            list.add(new GPairRecord<S, S>(current.pair, null, true, appearence));
        }

        return list;
    }

    /**
     * Renames all uncompressed letters of this phase.
     * Complexity: O(|G|)
     */
    private void firstRenameTerminals() {
        renameTerminals(node -> true);
    }

    /**
     * Renames all uncomressed terminals such that all terminals define a closed interval of integer numbers starting from 0.
     */
    private void renameTerminalsFromLastPhase() {
        renameTerminals(node -> node.getElement().getPhaseId() < phase);
    }

    /**
     * Renames all uncompressed letters of this phase.
     * Complexity: O(|G|)
     */
    private void renameTerminals(final Predicate<Node<S>> pred) {
        // gather letters
        List<Node<S>> terminals = slp.getSLPProductions().values().stream().flatMap(p -> p.getRight().nodeStream())
                .filter(pred)
                .filter(node -> node.getElement().isTerminal()).collect(Collectors.toList());

        // sort letters
        RadixSort.radixSort(terminals, node -> node.getElement().getId());

        // replace letters
        S current = null;
        S fresh = null;
        for (Node<S> node : terminals) {
            if(current == null || current.getId() != node.getElement().getId()) {
                fresh = terminalAlphabet.createTerminal(phase, node.getElement().getLength(), node.getElement().getWeight(), node.getElement());
                current = node.getElement();
            }
            node.setElement(fresh);
        }

        /*Map<S, S> replacement = new HashMap<>();
        slp.getSLPProductions().values().stream().flatMap(p -> p.getRight().nodeStream())
                .filter(node -> node.getElement().getPhaseId() < phase)
                .filter(node -> node.getElement().isTerminal()).forEach(node -> {
            if (!replacement.containsKey(node.getElement())) {
                replacement.put(node.getElement(), terminalAlphabet.createTerminal(phase, node.getElement().getLength(), node.getElement().getWeight(), node.getElement()));
            }
            node.setElement(replacement.get(node.getElement()));
        });*/
    }

    /**
     * The FixEndsDifferent from Jéz-Paper
     *
     * @param pairs     first and last letter of each non-terminal
     * @param pattern   the non-terminal of the pattern
     * @param text      the non-terminal of the text
     */
    private void fixEndsDifferent(final Map<S, Pair<S, S>> pairs, final S pattern, final S text) {
        P production = slp.getProduction(pattern);
        Pair<S, S> pair = pairs.get(pattern);
        W patternRhs = production.getRight();

        if(slp.length(pattern, true) > 1) {
            // get b <-- p[2]
            S b = slp.get(pattern, 2);
            Pair<S, S> prefixAB = new Pair<>(pair.a, b);
            fixPrefix(prefixAB, pattern, text);

            Map<S, Pair<S, S>> firstLastMap = getFirstLastMap();

            // get a <-- != p[m-1]
            //if(firstLastMap.get(patter).b.equals(pair.b) && slp.length(pattern) > 1) {
            long len = slp.length(pattern, true);
            if(len > 1) {
                S a = slp.get(pattern, len - 1);
                b = firstLastMap.get(patter).b;
                // update pairs!

                Pair<S, S> suffixAB = new Pair<>(a, b);
                fixSuffix(suffixAB, pattern, text);
            }
        }
    }

    private void fixSuffix(final Pair<S, S> pair, final S pattern, final S text) {

        if(pair.a != pair.b) {
            // compress the single pair (p[1],p[2])
            compressPairAll(pair);

        }
        else {
            popBlocks(prefix -> prefix.equals(pair.a), suffix -> suffix.equals(pair.a));
            W patternRhs = slp.getProduction(pattern).getRight();
            W wordRhs = slp.getProduction(text).getRight();

            long len = patternRhs.getSuffixLenPrefix(pair.b);
            S a_l = fixBlock(pair, len, pattern);
            // if t ends with a_l then remove this a_l
            if(wordRhs.getFirst().equals(a_l)) {
                wordRhs.deleteFirst();
            }
        }
    }

    private void fixPrefix(final Pair<S, S> pair, final S pattern, final S text) {

        if(pair.a != pair.b) {
            // compress the single pair (p[1],p[2])
            compressPairAll(pair);
        }
        else {
            popBlocks(prefix -> prefix.equals(pair.a), suffix -> suffix.equals(pair.a));
            W patternRhs = slp.getProduction(pattern).getRight();
            W wordRhs = slp.getProduction(text).getRight();
            long len = patternRhs.getPrefixLenPrefix(pair.a);
            S a_l = fixBlock(pair, len, pattern);
            // if t ends with a_l then remove this a_l
            if(wordRhs.getLast().equals(a_l)) {
                wordRhs.deleteLast();
            }
        }
    }

    /**
     * Test whether the recompression is complete i.e. for each axiom production p, rhs(p) = 1 holds or,
     * in case of pattern matchingAll, if for the production p of the pattern rhs(p) = 1 holds.
     * @return true if the recompression has finished.
     */
    public boolean hasFinished() {

        if(!matching) {
            boolean first = true;
            for(S left : slp.getAxioms()) {
                if(slp.length(left, first) == 1) {
                    return true;
                }
                first = false;
            }
        }

        if(matching) {
            long patternLen = slp.length(patter, true);
            if(patternLen == 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compresses all non-crossing pair of a sorted List of records. Complexity: O(|G|)
     * @param records sorted list of pair records
     */
    private void compressNonCrossingPairs(final List<GPairRecord<S, S>> records) {
        if(records.isEmpty()) {
            return ;
        }

        boolean globalCrossing = false;
        Pair<S, S> lastCrossingPair = null;
        Pair<S, S> lastCompressedPair = null;
        Iterator<GPairRecord<S, S>> iterator = records.iterator();
        GPairRecord<S, S> record;
        S letter = null;

        while(iterator.hasNext())
        {
            record = iterator.next();
            Pair<S, S> currentPair = record.pair;
            globalCrossing = record.isCrossingPair() || (lastCrossingPair != null && (lastCrossingPair.equals(currentPair)));

            if(record.isCrossingPair()) {
                lastCrossingPair = currentPair;
            }

            // skip all crossing pairs including the local pairs that are not crossing but has some other pairs equal to them are crossing
            if(!globalCrossing) {
                if(wordProperties.isPairAt(record.node, currentPair)) {
                    if(lastCompressedPair == null || !lastCompressedPair.equals(record.pair)) {
                        Pair<S, S> pair = record.pair;
                        letter = terminalAlphabet.createTerminal(phase, 1L, pair.a.getWeight() + pair.b.getWeight(), pair);
                                //signature.createLetter(record.pair);
                        lastCompressedPair = record.pair;
                    }
                    compressNonCrossingPair(record, letter);
                }
                iterator.remove();
            }
            else {
                // we need the pairs to calculate the appearance for greedy pairing!
                if(!greedyPairCompression) {
                    if(record.isCrossingPair() || !wordProperties.isPairAt(record.node, record.pair)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Return all pairs sorted!
     *
     * @return all pairs sorted
     */
    public List<GPairRecord<S, S>> getPairs(final Predicate<Pair<S, S>> predicate) {
        List<GPairRecord<S, S>> pairs = getNonCrossingPairs(predicate);
        pairs.addAll(getCrossingPairs(predicate));

        sortPairs(pairs);
        return pairs;
    }

    /**
     * Computes the number of appearances of non-terminals in words.
     * Compplexity: O(|G|)
     *
     * @return a mapping X -> appearances of X in the the word i.e. val(axiom).
     */
    private Map<S, Integer> getAppearances() {
        List<P> productions = slp.getOrderedProductions();
        Map<S, Integer> appearences = new HashMap<>(productions.size());

        for(S axiom : slp.getAxioms()) {
            appearences.put(axiom, 1);
        }

        for(P production : productions) {
            if(!appearences.containsKey(production.getLeft())) {
                appearences.put(production.getLeft(), 1);
            }

            for(S terminal : production.getRight().findAll(s -> !s.isTerminal())) {
                if(!appearences.containsKey(terminal)) {
                    appearences.put(terminal, 0);
                }

                appearences.put(terminal, appearences.get(terminal) + appearences.get(production.getLeft()));
            }
        }
        return appearences;
    }

    /**
     * Computes a mapping X -> list of pointers to X.
     * Complexity: O(|G|)
     *
     * @return a mapping X -> list of pointers to X
     */
    private Map<S, List<Node<S>>> getAllOccurrences() {
        List<P> productions = slp.getOrderedProductions();
        Map<S, List<Node<S>>> predecessorMap = new HashMap<>();
        slp.getProductions().stream().map(p -> p.getLeft()).forEach(symbol -> predecessorMap.put(symbol, new ArrayList<>()));

        // gather all occurrence
        productions.stream()
                .map(rule -> rule.getRight())
                .flatMap(word -> word.nodeStream())
                .filter(node -> !node.getElement().isTerminal())
                .forEach(node -> predecessorMap.get(node.getElement()).add(node));

        return predecessorMap;
    }

    /**
     * Compress all pairs of the form (a,x) where x is in the current alphabet without a.
     * Complexity: O(|G| + n + m)
     *
     * @param a the letter that specifies the singleton.
     */
    private void compressLeftSingleTonPairs(final S a) {
        Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction = p -> new ArrayList<>()::add;
        Predicate<S> leftPredicate = nonTerminal -> doLeftPop(nonTerminal, head -> head.isTerminal() && !head.equals(a));
        Predicate<S> rightPredicate = nonTerminal -> doRightPop(nonTerminal, tail -> tail.isTerminal() && tail.equals(a));
        pop(phase + 1, leftPredicate, rightPredicate, consumerFunction);
        List<GPairRecord<S, S>> records = getPairs(p -> p.a.equals(a) && !p.b.equals(a));
        sortPairs(records);
        compressNonCrossingPairs(records);
    }

    /**
     * Compress all pairs of the form (x,a) where x is in the current alphabet without a.
     * Complexity: O(|G| + n + m)
     *
     * @param a the letter that specifies the singleton.
     */
    private void compressRightSingleTonPairs(final S a) {
        Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction = p -> new ArrayList<>()::add;
        Predicate<S> leftPredicate = nonTerminal -> doLeftPop(nonTerminal, head -> head.isTerminal() && head.equals(a));
        Predicate<S> rightPredicate = nonTerminal -> doRightPop(nonTerminal, tail -> tail.isTerminal() && !tail.equals(a));
        pop(phase + 1, leftPredicate, rightPredicate, consumerFunction);
        List<GPairRecord<S, S>> records = getPairs(p -> !p.a.equals(a) && p.b.equals(a));
        sortPairs(records);
        compressNonCrossingPairs(records);
    }

    /**
     * Compresses a single pair (all crossing and non-crossing pairs).
     * Complexity: O(|G| + n)
     *
     * @param pair the pair that should be compressed
     */
    private void compressPairAll(final Pair<S, S> pair) {
        Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction = p -> new ArrayList<>()::add;
        Predicate<S> leftPredicate = nonTerminal -> doLeftPop(nonTerminal, head -> head.isTerminal() && head.equals(pair.b));
        Predicate<S> rightPredicate = nonTerminal -> doRightPop(nonTerminal, tail -> tail.isTerminal() && tail.equals(pair.a));
        pop(phase + 1, leftPredicate, rightPredicate, consumerFunction);
        List<GPairRecord<S, S>> records = getPairs(p -> p.equals(pair));
        sortPairs(records);
        compressNonCrossingPairs(records);
    }

    /**
     * Compresses all blocks of letters of not new introduced letters by popping the
     * prefix and the suffix of each rule X_i.
     * Complexity: O(|G| + (n+m)log(n+m))
     *
     * NOTE: This may destroy pointers to uncrossed pairs, so you have to gather pairs after this operation!
     *
     * @param phaseId the largest letter of the not new introduced letters.
     */
    private void compressBlocks(final int phaseId) {
        // 1. uncross all crossing blocks: O(|G|)
        popBlocks(head -> head.isTerminal() && head.getPhaseId() < phaseId,
                tail -> tail.isTerminal() && tail.getPhaseId() < phaseId);

        List<BlockRecord<N, S>> shortBlockRecords = new ArrayList<>();
        List<BlockRecord<N, S>> longBlockRecords = new ArrayList<>();

        // 2. get all blocks: O(|G|)
        slp.getProductions().stream().forEach(rule -> consumeBlocks(rule.getRight(), shortBlockRecords::add, longBlockRecords::add, letter -> true));

        // 3.1 sort short blocks using RadixSort: O(|G|)
        sortBlocks(shortBlockRecords);

        // 3.2 sort long blocks O((n+m) log(n+m))
        Collections.sort(longBlockRecords, blockComparator);
        List<BlockRecord<N, S>> blockRecords = mergeBlocks(shortBlockRecords, longBlockRecords);

        // compress blocks
        BlockRecord<N, S> lastRecord = null;
        S letter = null;

        // 4. compress blocks: O(|G|)
        for(BlockRecord<N, S> record : blockRecords) {

            // check when it is the correct time to introduce a fresh letter
            if(lastRecord == null || !lastRecord.equals(record)) {
                letter = terminalAlphabet.createTerminal(phase, 1L, record.node.getElement().getLength() * record.block.getLength(), record.block);
                lastRecord = record;
            }
            replaceBlock(record, letter);
        }
    }

    /**
     * Replaces a sequence of the form [c b^l d] or [c b^l b^t d] or [c bbb...bbb d]
     * by [c 9 c], where 9 is a new introduced non-terminal letter.
     * Complexity: O(|G|)
     *
     * @param record the record where the block might be.
     * @param letter the new introduced letter.
     *
     * @return returns the pointer of to the newly introduced letter
     */
    private Node<S> replaceBlock(final BlockRecord<N, S> record, final S letter) {
        Node<S> node = record.node;

        // case : [c b^l b^t d] or [c bbb...bbb d]
        if(wordProperties.isNonCompressedBlockAt(node)) {
            while(wordProperties.isNonCompressedBlockAt(node)) {
                Node<S> removal = node;
                node = node.getNext();
                removal.remove();
            }
            node.setElement(letter);
        } // case : [c b^l d]
        else if(wordProperties.isCompressedBlockAt(node)) {
            node.setElement(letter);
        }
        else if(record.block.getLength() == 1 && node.getElement().equals(record.block.getLetter())) {
            node.setElement(letter);
        }

        return node;
    }

    /**
     * Pops prefix and suffix of the form a... (...b).
     * Complexity: O(|G|)
     *
     * @param prefix specifies which letter (non-terminal or terminal etc. ) should be popped left
     * @param suffix specifies which letter (non-terminal or terminal etc. ) should be popped right
     */
    private void popBlocks(final Predicate<S> prefix, final Predicate<S> suffix) {
        // make sure to delete in the right order so we do not have to check val(X_i) = epsilon.
        Iterator<P> productions = slp.getOrderedProductions().descendingIterator();

        while (productions.hasNext()) {
            P production = productions.next();
            W right = production.getRight();

            if(!right.isEmpty()) {

                S left = production.getLeft();
                S optHead = right.findFirst(s -> !s.isEmpty()).get();
                S optTail = right.findLast(s -> !s.isEmpty()).get();

                // is not a start symbol!
                if(!slp.getAxioms().contains(left) ) {
                    S head = optHead;
                    // pop prefix
                    if(prefix.test(head) && !right.isEmpty()) {
                        // O(rule length)
                        long blockLength = right.deletePrefix(symbol -> symbol.equals(head), s -> s.getLength());

                        final S blockLetter = blockLength > 1 ? terminalAlphabet.createTerminal(head, blockLength) : head;

                        List<Node<S>> pointers = occurrences.get(production.getLeft());
                        // O(number of non-terminals)
                        for(Node<S> pointer : pointers) {
                            pointer.insertPrevious(blockLetter);
                            if (right.isEmpty()) pointer.remove();
                        }
                    }

                    S tail = optTail;
                    // pop suffix
                    if(suffix.test(tail) && !right.isEmpty()) {
                        // O(rule length)
                        long blockLength = right.deleteSuffix(symbol -> symbol.equals(tail), s -> s.getLength());

                        final S blockLetter = blockLength > 1 ? terminalAlphabet.createTerminal(tail, blockLength) : tail;

                        List<Node<S>> pointers = occurrences.get(production.getLeft());
                        // O(number of non-terminals)
                        for(Node<S> pointer : pointers) {
                            pointer.insertNext(blockLetter);
                            if (right.isEmpty()) pointer.remove();
                        }
                    }
                }
            }

            // Complexity: O(1)
            if(!slp.getAxioms().contains(production.getLeft()) && right.isEmpty()) {
                productions.remove();
                deleteProduction(production);
            }
        }
    }

    /**
     * Returns all non-crossing pairs of the grammar.
     * Complexity: O(|G|)
     *
     * @return all non-crossing pairs of the grammar.
     */
    private List<GPairRecord<S, S>> getNonCrossingPairs(final Predicate<Pair<S, S>> predicate) {
        List<GPairRecord<S, S>> recordList = new ArrayList<>();
        slp.getOrderedProductions().stream().map(p -> p.getLeft()).forEach(nonTerminal -> consumeNonCrossingPairs(nonTerminal, recordList::add, predicate));
        return recordList;
    }

    /**
     * Consumes all non-crossing pairs that satisfies the predicate in at the rhs of the rule of nonTerminal by the consumer.
     * Complexity: O(|G|)
     *
     * @param nonTerminal   the left of the rule
     * @param consumer      the consumer that consumes the pairs
     * @param predicate     the predicate that a consumed pair has to be satisfied
     */
    private void consumeNonCrossingPairs(final S nonTerminal, final Consumer<GPairRecord<S, S>> consumer, final Predicate<Pair<S, S>> predicate) {
        P production = slp.getProduction(nonTerminal);
        W right = production.getRight();

        right.nodeStream()
                .filter(node -> wordProperties.isNonCrossingPair(node))
                .map(node -> new GPairRecord<>(new Pair<>(node.getElement(), node.getNext().getElement()), node, false, appearances.get(nonTerminal)))
                .filter(record -> predicate.test(record.pair))
                .forEach(record -> consumer.accept(record));
    }

    /**
     * Consumes all crossing pairs that satisfies the predicate in at the rhs of the rule of nonTerminal by the consumer.
     * Complexity: O(|G|)
     *
     * @param nonTerminal   the left of the rule
     * @param consumer      the consumer that consumes the pairs
     * @param pairs         first and last terminals of each rhs
     * @param predicate     the predicate that a consumed pair has to be satisfied
     */
    private void consumeCrossingPairs(final S nonTerminal, final Consumer<GPairRecord<S, S>> consumer, final Pair<S, S>[] pairs, final Predicate<Pair<S, S>> predicate) {
        P production = slp.getProduction(nonTerminal);
        W right = production.getRight();

        right.nodeStream()
                .map(node -> getCrossingPair(node, pairs))
                .filter(optPair -> optPair.isPresent())
                .map(optPair -> optPair.get())
                .filter(pair -> predicate.test(pair))
                .map(pair -> new GPairRecord<>(pair, null, true, appearances.get(nonTerminal)))
                .forEach(record -> consumer.accept(record));
    }

    /**
     * Returns the crossing pair if it is still present, otherwise the optional is not present.
     * Complexity: O(1)
     *
     * @param node the pointer starting at the left of the pair
     * @param map  the a mapping X -> (first(X), last(X))
     *
     * @return the the crossing pair if it is still present, otherwise the optional is not present
     */
    private Optional<Pair<S, S>> getCrossingPair(final Node<S> node, final Pair<S, S>[] map) {
        Pair<S, S> result = null;
        if(node.hasNext()) {
            //aX ?
            if(node.getElement().isTerminal()) {
                // ab
                if(!node.getNext().getElement().isTerminal()) {
                    S a = node.getElement();
                    S b = map[node.getNext().getElement().getId()].a;
                    result = !a.equals(b) ? new Pair<S, S>(a,b) : null;
                }
            } // Ax or AX
            else {
                S a = map[node.getElement().getId()].b;
                S b = node.getNext().getElement();
                // Ab?
                if(b.isTerminal()) {
                    result = !a.equals(b) ? new Pair<S, S>(a, b) : null;
                } //AB
                else {
                    result = !a.equals(map[b.getId()].a) ? new Pair<S, S>(a, map[b.getId()].a) : null;
                }
            }
        }
        return Optional.ofNullable(result);
    }

    /**
     * Returns all crossing pairs of the slp.
     * Complexity: O(|G|).
     *
     * @return all crossing pairs of the slp.
     */
    private List<GPairRecord<S,S>> getCrossingPairs(final Predicate<Pair<S, S>> predicate) {

        // get first and last terminal for each non-terminal
        Pair<S, S>[] pairs = getFirstLastMappingArray();

        // list of records of crossing pairs
        List<GPairRecord<S,S>> recordList = new ArrayList<>();

        // scan for crossing pairs in the right order
        slp.getOrderedProductions().stream().map(p -> p.getLeft()).forEach(nonTerminal -> consumeCrossingPairs(nonTerminal, recordList::add, pairs, predicate));
        return recordList;
    }

    /**
     * Returns a mapping Map : X -> (first(X), last(X)). It may happen that first(X) = last(X).
     * Complexity: O(n + m)
     *
     * @return a mapping Map : X -> (first(X), last(X))
     */
    private Pair<S, S>[] getFirstLastMappingArray() {
        Pair<S, S>[] pairs = new Pair[maxNonTerminal+1];
        Iterator<P> productions = slp.getOrderedProductions().descendingIterator();
        while(productions.hasNext()) {
            P production = productions.next();
            S left = production.getLeft();
            W right = production.getRight();

            Pair<S, S> pair = new Pair<>(right.findFirst(s -> true).get(), right.findLast(s -> true).get());
            S a = pair.a.isTerminal() ? pair.a : pairs[pair.a.getId()].a;
            S b = pair.b.isTerminal() ? pair.b : pairs[pair.b.getId()].b;

            pairs[left.getId()] = new Pair<>(a, b);
        }
        return pairs;
    }

    /**
     * Returns a mapping Map : X -> (first(X), last(X)). It may happen that first(X) = last(X).
     * Complexity: O(n + m)
     *
     * @return a mapping Map : X -> (first(X), last(X))
     */
    private Map<S, Pair<S, S>> getFirstLastMap() {
        Map<S, Pair<S, S>> pairs = new HashMap<>();
        Iterator<P> productions = slp.getOrderedProductions().descendingIterator();
        while(productions.hasNext()) {
            P production = productions.next();
            S left = production.getLeft();
            W right = production.getRight();

            Pair<S, S> pair = new Pair<>(right.findFirst(s -> true).get(), right.findLast(s -> true).get());
            S a = pair.a.isTerminal() ? pair.a : pairs.get(pair.a).a;
            S b = pair.b.isTerminal() ? pair.b : pairs.get(pair.b).b;

            pairs.put(left, new Pair<>(a, b));
        }
        return pairs;
    }

    /**
     * Compresses <b>all</b> crossing pairs. The list crossingPairs only contains explicit pairs that are
     * crossed and exactly one entry for a crossing pair that occurs <b>only</b> crossing in G.
     * Occurrences of uncrossed pairs will be added to partitions P_j and will be compressed at some
     * iteration step.
     *
     * @param crossingPairs local uncrossed pairs that are crossed
     * @param largestId     the largest id of JezSymbol that should be compressed, to compute the amount of required partitions
     */
    private void compressCrossingPairs(final List<GPairRecord<S, S>> crossingPairs, int phase, final int largestId) {
        int highestOneBit = Integer.highestOneBit(largestId);
        int J = (IndexGenerator.getBitNumber(highestOneBit)+1) * 2;
        List<GPairRecord<S, S>>[] pairPartitions = new List[J];

        // initialize all required partitions: O(log(n + m))
        for(int i = 0; i < J; i++) {
            pairPartitions[i] = new ArrayList<>();
        }

        // put explicit occurring pairs (that are crossing) in the correct partition P_j: O(|G|)
        crossingPairs.stream()
                .filter(rec -> !rec.crossing)
                .forEach(rec -> pairPartitions[IndexGenerator.calculateIndex(rec.pair)].add(rec));

        final Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction = (pair) -> pairPartitions[IndexGenerator.calculateIndex(pair)]::add;
        for(int j = 0; j < J-1; j += 2) {
            if(!pairPartitions[j].isEmpty()) {
                final int t = j;

                //1. pop(): O(n + m)
                pop(phase,
                        nonTerminal -> doLeftPop.apply(nonTerminal, t),
                        nonTerminal -> doRightPop.apply(nonTerminal, t),
                        consumerFunction);

                //2. sort(): O(|P_j|)
                sortPairs(pairPartitions[j]);

                //3. compress(): O(|P_j|)
                compressUncrossedPairs(pairPartitions[j]);
            }

            if(!pairPartitions[j+1].isEmpty()) {
                final int t = j + 1;
                //1. pop(): O(|rules|)
                pop(phase,
                        nonTerminal -> doLeftPop.apply(nonTerminal, t),
                        nonTerminal -> doRightPop.apply(nonTerminal, t),
                        consumerFunction);

                //2. sort(): O(|P_{j+1}|)
                sortPairs(pairPartitions[j + 1]);

                //3. compress(): O(|P_{j+1}|)
                compressUncrossedPairs(pairPartitions[j + 1]);
            }
        }
    }

    /**
     * Pops all letter left that satisfy the first predicate and pops all letter right that satisfy the second predicate.
     * Furthermore all uncrossed pairs that contain only letters that are in the alphabet defined by alphabetSize
     * will be given to the consumer that may depend on the pair itself.
     *
     * @param phase      the size of the alphabet of the pairs that will be consumed
     * @param doPopLeft         the predicate that has to be satisfied to pop a letter left
     * @param doPopRight        the predicate that has to be satisfied to pop a letter right
     * @param consumerFunction  the consumer function that specifies the consuming consumer by the pair
     */
    public void pop(final int phase, final Predicate<S> doPopLeft, final Predicate<S> doPopRight,
                    final Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction) {

        // make sure to delete in the right order so we do not have to check val(X_i) = epsilon.
        Iterator<P> nonTerminals = slp.getOrderedProductions().descendingIterator();

        while (nonTerminals.hasNext()) {
            P production = nonTerminals.next();
            if(doPopLeft.test(production.getLeft())) {
                popLeft(production.getLeft(), phase, consumerFunction);
            }

            if(doPopRight.test(production.getLeft())) {
                popRight(production.getLeft(), phase, consumerFunction);
            }

            if(!slp.containsProduction(production.getLeft())) {
                nonTerminals.remove();
            }
        }
    }

    /**
     * Pop left for a specific non-terminal, and consume all created uncrossed pairs.
     *
     * @param nonTerminal       the non-terminal were the pop took place
     * @param phase      the alphabet size of the old alphabet containing no fresh letters of the phase
     * @param consumerFunction  the consumer function that returns consumers specified by the pair that should be consumed
     */
    private void popLeft(final S nonTerminal, final int phase, final Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction) {
        P production = getProduction(nonTerminal);
        S head = production.getRight().findFirst(s -> !s.isEmpty()).get();

        production.getRight().deleteFirst();
        occurrences.get(production.getLeft()).forEach(pointer -> leftReplace(head, pointer, phase, production, consumerFunction));
        if(production.getRight().isEmpty() && !slp.getAxioms().contains(production.getLeft())) {
            deleteProduction(production);
        }
    }

    /**
     * Pop right for a specific non-terminal, and consume all created uncrossed pairs.
     *
     * @param nonTerminal       the non-terminal were the pop took place
     * @param phase             the alphabet size of the old alphabet containing no fresh letters of the phase
     * @param consumerFunction  the consumer function that returns consumers specified by the pair that should be consumed
     */
    private void popRight(final S nonTerminal, final int phase, final Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction) {
        P production = getProduction(nonTerminal);
        S tail = production.getRight().findLast(s -> !s.isEmpty()).get();

        production.getRight().deleteLast();
        occurrences.get(production.getLeft()).forEach(pointer -> rightReplace(tail, pointer, phase, production, consumerFunction));
        if(production.getRight().isEmpty() && !slp.getAxioms().contains(production.getLeft())) {
            deleteProduction(production);
        }
    }

    /**
     * Test whether the right-pop should take place or not. In any case, the predicate has to hold.
     *
     * @param nonTerminal       the non-terminal for which we do the right-pop
     * @param letterPredicate   the predicate that has to hold
     * @return  true if the right-pop should take place, false otherwise
     */
    private boolean doRightPop(final S nonTerminal, final Predicate<S> letterPredicate) {
        if(!slp.containsProduction(nonTerminal) || slp.getAxioms().contains(nonTerminal)) {
            return false;
        }
        P production = getProduction(nonTerminal);
        S tail = production.getRight().findLast(s -> !s.isEmpty()).get();
        return letterPredicate.test(tail) && !production.getRight().isEmpty();
    }

    /**
     * Test whether the left-pop should take place or not. In any case, the predicate has to hold.
     *
     * @param nonTerminal       the non-terminal for which we do the left-pop
     * @param letterPredicate   the predicate that has to hold
     * @return  true if the left-pop should take place, false otherwise
     */
    private boolean doLeftPop(final S nonTerminal, final Predicate<S> letterPredicate) {
        if(!slp.containsProduction(nonTerminal) || slp.getAxioms().contains(nonTerminal)) {
            return false;
        }
        P production =  getProduction(nonTerminal);
        S head = production.getRight().findFirst(s -> !s.isEmpty()).get();
        return letterPredicate.test(head) && !production.getRight().isEmpty();
    }

    private P getProduction(final S symbol){
        return slp.getProduction(symbol);
    }

    private void deleteProduction(final S left) {
        slp.deleteProduction(left);
        occurrences.put(left, new ArrayList());
    }

    private void deleteProduction(final P production) {
        deleteProduction(production.getLeft());
    }

    /**
     * Replaces a single non-terminal X by bX for a single rule and deletes the non-terminal if the rule in the slp is equals X -> epsilon.
     * Furthermore it consumes a record if a new non crossing pair was created, the consumer is specified by the consumed pair itself and
     * is given by the consumerFunction.
     *
     * Complexity: O(1)
     *
     * @param head              the terminal b
     * @param pointer           the pointer to X
     * @param phase             the alphabet size of the old alphabet containing no fresh letters of the phase
     * @param production        the rule of the non-terminal that this method replaces
     * @param consumerFunction  the function that returns the consumer specfied by the pair that will be consumed
     */
    private void leftReplace(final S head, final Node<S> pointer, final int phase, final P production, final Function<Pair<S, S>, Consumer<GPairRecord<S, S>>> consumerFunction) {
        Node<S> p = pointer;
        p.insertPrevious(head);
        if(p.getPrev().hasPrev()) {
            p = p.getPrev().getPrev();
            gatherPair(p, phase, consumerFunction, pointer.getElement());
        }
        if(production.getRight().isEmpty()) {
            Node<S> prev = null;
            if(pointer.hasPrev()) {
                prev = pointer.getPrev();
            }
            pointer.remove();
            // maybe we are in the situation aBb and B-> "" than we have to add the pair ab!
            if(prev != null && prev.hasNext()) {
                gatherPair(prev, phase, consumerFunction, pointer.getElement());
            }
        }
    }

    /**
     * Replaces a single non-terminal X by Xb for a single rule and deletes the non-terminal if the rule in the slp is equals X -> epsilon.
     * Furthermore it consumes a record if a new non crossing pair was created, the consumer is specified by the consumed pair itself and
     * is given by the consumerFunction.
     *
     * Complexity: O(1)
     *
     * @param tail              the terminal b
     * @param pointer           the pointer to X
     * @param phase             the alphabet size of the old alphabet containing no fresh letters of the phase
     * @param production        the rule of the non-terminal that this method replaces
     * @param consumerFunction  the function that returns the consumer specfied by the pair that will be consumed
     */
    private void rightReplace(final S tail, final Node<S> pointer, final int phase, final P production, final Function<Pair<S,S>, Consumer<GPairRecord<S, S>>> consumerFunction) {
        Node<S> p = pointer;
        p.insertNext(tail);
        if(p.getNext().hasNext()) {
            p = p.getNext();
            gatherPair(p, phase, consumerFunction, pointer.getElement());
        }
        if(production.getRight().isEmpty()) {
            Node<S> prev = null;
            if(pointer.hasPrev()) {
                prev = pointer.getPrev();
            }
            pointer.remove();
            // maybe we are in the situation aBb and B-> "" than we have to add the pair ab!
            if(prev != null && prev.hasNext()) {
                gatherPair(prev, phase, consumerFunction, pointer.getElement());
            }
        }
    }

    /**
     * Consumes a pair if at the node there is a non-crossing pair consist of no fresh letters.
     * Complexity: O(1)
     *
     * @param node           the node to the possible pair
     * @param phase      the alphabet size of the old alphabet containing no fresh letters of the phase
     * @param consumerFunction  the function that returns the consumer specfied by the pair that will be consumed
     */
    private void gatherPair(final Node<S> node, final int phase, final Function<Pair<S,S>, Consumer<GPairRecord<S, S>>> consumerFunction, final S targetNonTerminal) {
        Pair<S, S> pair = new Pair<S, S>(node.getElement(), node.getNext().getElement());
        if(wordProperties.isPair(node) && pair.a.getPhaseId() < phase && pair.b.getPhaseId() < phase) {
            GPairRecord record = new GPairRecord(pair, node, false, appearances.get(targetNonTerminal));
            consumerFunction.apply(pair).accept(record);
        }
    }

    /**
     * Simple does the compressing. Here everything is already defined and we can be sure
     * that at the pointer position there is a non-crossing pair.
     * Complexity: O(1)
     *
     * @param record the record that has every information we require
     * @param symbol the fresh symbol the pair will be replaced with
     */
    private void compressNonCrossingPair(final GPairRecord<S, S> record, final S symbol) {
        Node<S> node = record.node;
        node.setElement(symbol);
        node.getNext().remove();
    }

    /**
     * Compresses uncrossed pairs contained in records if they are still 'alive'.
     * Complexity: O(|G|)
     *
     * @param records contains the uncrossed pairs
     */
    private void compressUncrossedPairs(final List<GPairRecord<S, S>> records) {
        Iterator<GPairRecord<S, S>> recordIterator = records.iterator();

        S letter = null;
        Pair pair = null;
        while(recordIterator.hasNext())
        {
            GPairRecord<S, S> record = recordIterator.next();
            // is there now a uncrossed pair at the node?
            if(wordProperties.isPairAt(record.node, record.pair)) {
                if(letter == null || !record.pair.equals(pair)) {
                    letter = terminalAlphabet.createTerminal(phase, 1L, record.pair.a.getWeight() + record.pair.b.getWeight(), record.pair);
                    pair = record.pair;
                }
                compressNonCrossingPair(record, letter);
            }
            // remove useless pointers, this may happen if a crossed pair becomes uncrossed due to pop and becomes crossed again due pop.
            recordIterator.remove();
        }
    }

    /**
     * Gathers all blocks in a rhs(p) of a production. There is one consumer for short and one for long blocks, since we can not sort long blocks using RadixSort.
     * We do not consider blocks of length = 1.
     *
     * Complexity: O(|rhs(p)|)
     * Requirement: We assume all blocks are uncrossed.
     *
     * @param word                  rhs(p)
     * @param shortBlockConsumer    consumer for short blocks
     * @param longBlockConsumer    consumer for long blocks
     * @param predicate             the predicate that has to hold to gather a certain block
     */
    private void consumeBlocks(final W word, final Consumer<BlockRecord<N, S>> shortBlockConsumer, final Consumer<BlockRecord<N, S>> longBlockConsumer, final Predicate<S> predicate) {
        consumeBlocks(word, shortBlockConsumer, longBlockConsumer, predicate, false);
    }

    private List<Long> getBlockOccurrencePositions(final S terminal) {
        Map<S, Long> weights = getNonTerminalWeights();
        List<Long> occurrences = new ArrayList<>();
        LinkedList<S> nonTerminals = new LinkedList<>();
        LinkedList<Long> lweights = new LinkedList<>();
        nonTerminals.addLast(text);
        lweights.addLast(0l);

        while(!nonTerminals.isEmpty()) {
            S nt = nonTerminals.removeFirst();
            long sum = lweights.removeFirst();

            for (S letter : getProduction(nt).getRight()) {

                if (terminal.getBlockId() == letter.getBlockId()) {
                    if(letter.getWeight() >= terminal.getWeight()) {
                        for(long i = 0; i < (letter.getWeight() - terminal.getWeight() + 1); i++) {
                            occurrences.add(sum + i);
                        }
                    }
                }

                if (letter.isTerminal()) {
                    sum += letter.getWeight();
                } else if (!letter.isTerminal()) {
                    nonTerminals.add(letter);
                    lweights.add(sum);
                    sum += weights.get(letter);
                } else {
                    throw new IllegalArgumentException("this letter is neither a terminal nor a non-terminal.");
                }
            }
        }

        return occurrences;
    }

    private List<Long> getNonBlockOccurrencePositions(final S terminal) {
        Map<S, Long> weights = getNonTerminalWeights();
        List<Long> occurrences = new ArrayList<>();
        LinkedList<S> nonTerminals = new LinkedList<>();
        LinkedList<Long> lweights = new LinkedList<>();
        nonTerminals.addLast(text);
        lweights.addLast(0l);

        while(!nonTerminals.isEmpty()) {
            S nt = nonTerminals.removeFirst();
            long sum = lweights.removeFirst();

            for (S letter : getProduction(nt).getRight()) {

                if (terminal.equals(letter)) {
                    occurrences.add(sum);
                }

                if (letter.isTerminal()) {
                    sum += letter.getWeight();
                } else if (!letter.isTerminal()) {
                    nonTerminals.add(letter);
                    lweights.add(sum);
                    sum += weights.get(letter);
                } else {
                    throw new IllegalArgumentException("this letter is neither a terminal nor a non-terminal.");
                }
            }
        }

        return occurrences;
    }

    /**
     * Returns a mapping : X -> occ where occ is the number of occurrences of terminal in val(X).
     *
     * @param terminal  the symbol which we calculate the occurrence mapping for.
     * @return  mapping : X -> occ where occ is the number of occurrences of terminal in val(X).
     */
    private Map<S, Long> getPatternOccurrence(final S  terminal) {
        Map<S, Long> occInNonTerminal = new HashMap<>();
        if(matchingBlocks) {
            Iterator<P> it = slp.getOrderedProductions().descendingIterator();
            while (it.hasNext()) {
                P production = it.next();
                long ntOcc = production.getRight().stream().map(s -> {
                    if(!s.isTerminal()) {
                        return occInNonTerminal.get(s);
                    }
                    else if(s.getBlockId() == terminal.getBlockId() && s.getWeight() >= terminal.getWeight()) {
                        return s.getWeight() - terminal.getWeight() + 1;
                    }
                    else {
                        return 0L;
                    }
                }).reduce(0L, (a,b) -> a + b);
                occInNonTerminal.put(production.getLeft(), ntOcc);
            }
        }
        else {
            Iterator<P> it = slp.getOrderedProductions().descendingIterator();
            while (it.hasNext()) {
                P production = it.next();
                long ntOcc = production.getRight().stream().map(s -> !s.isTerminal() ? occInNonTerminal.get(s) : (s.equals(terminal) ? 1L : 0L)).reduce(0L, (a,b) -> a+b);
                occInNonTerminal.put(production.getLeft(), ntOcc);
            }
        }


        return occInNonTerminal;
    }

    private Optional<Long> getBlockOccurrencePosition(final S terminal, final long k) {
        Map<S, Long> weights = getNonTerminalWeights();
        Map<S, Long> occInNonTerminal = getPatternOccurrence(terminal);

        long position = 0;
        long occ = 0;
        P production = slp.getProduction(text);
        boolean goDeeper = true;

        while(goDeeper && production != null) {
            goDeeper = false;
            for(S symbol : production.getRight()) {
                if(symbol.getBlockId() == terminal.getBlockId()) {
                    if(symbol.getWeight() >= terminal.getWeight()) {
                        if(k <= occ + (symbol.getWeight() - terminal.getWeight() + 1)) {
                            return Optional.of(position + (k - occ) - 1);
                        }
                    }
                }

                if(symbol.isTerminal()) {
                    position += symbol.getWeight();
                }
                else {
                    if(k - (occ + occInNonTerminal.get(symbol)) <= 0) {
                        production = getProduction(symbol);
                        goDeeper = true;
                        break;
                    }
                    else {
                        occ += occInNonTerminal.get(symbol);
                        position += weights.get(symbol);
                    }
                }
            }
        }

        if(occ != k) {
            return Optional.empty();
        }
        else {
            return Optional.of(position);
        }
    }

    private Optional<Long> getNonBlockOccurrencePosition(final S terminal, final long k) {
        Map<S, Long> weights = getNonTerminalWeights();
        Map<S, Long> occInNonTerminal = getPatternOccurrence(terminal);

        long position = 0;
        long occ = 0;
        P production = slp.getProduction(text);
        boolean goDeeper = true;

        while(goDeeper && production != null) {
            goDeeper = false;
            for(S symbol : production.getRight()) {
                if(symbol.equals(terminal)) {
                    occ++;
                    if(k == occ) {
                        return Optional.of(position);
                    }
                }

                if(symbol.isTerminal()) {
                    position += symbol.getWeight();
                }
                else {
                    if(k - (occ + occInNonTerminal.get(symbol)) <= 0) {
                        production = getProduction(symbol);
                        goDeeper = true;
                        break;
                    }
                    else {
                        occ += occInNonTerminal.get(symbol);
                        position += weights.get(symbol);
                    }
                }
            }
        }

        if(occ != k) {
            return Optional.empty();
        }
        else {
            return Optional.of(position);
        }
    }

    /**
     * Returns the weight of all non-terminals. The weight is equals to |val(X_i)|.
     * This could take exponential running time.
     * Complexity: O(2^(|G|))
     *
     * @return the weight of all non-terminals
     */
    private Map<S, Long> getNonTerminalWeights() {

        Map<S, Long> weights = new HashMap<>();
        Iterator<P> productions = slp.getOrderedProductions().descendingIterator();

        while(productions.hasNext()) {
            S nonTerminal = productions.next().getLeft();
            long sum = 0;
            for(S letter : getProduction(nonTerminal).getRight()) {
                if(letter.isTerminal()) {
                    sum += letter.getWeight();
                }
                else if(!letter.isTerminal()) {
                    sum += weights.get(letter);
                }
                else {
                    throw new IllegalArgumentException("this letter is neither a terminal nor a non-terminal.");
                }
            }
            weights.put(nonTerminal, sum);
        }
        return weights;
    }

    /**
     * Gathers all blocks in a rhs(p) of a production. There is one consumer for short and one for long blocks, since we can not sort long blocks using RadixSort.
     *
     * Complexity: O(|rhs(p)|)
     * Requirement: We assume all blocks are uncrossed.
     *
     * @param word                  rhs(p)
     * @param shortBlockConsumer    consumer for short blocks
     * @param longBlockConsumer     consumer for long blocks
     * @param predicate             the predicate that has to hold to gather a certain block
     * @param singleton             true => we also consider blocks of length = 1, otherwise we do not consider them
     */
    private void consumeBlocks(final W word, final Consumer<BlockRecord<N, S>> shortBlockConsumer, final Consumer<BlockRecord<N, S>> longBlockConsumer, final Predicate<S> predicate, final boolean singleton) {
        Iterator<Node<S>> iterator = word.nodeIterator();

        if(iterator.hasNext()) {
            Node<S> node = iterator.next();
            Node<S> current = node;
            S symbol = node.getElement();
            S next = symbol;
            boolean shortBlock = true;

            long blockLen = symbol.getLength();

            while(iterator.hasNext()) {
                Node<S> nextNode = iterator.next();
                next = nextNode.getElement();

                if(symbol.equals(next)) {
                    blockLen += next.getLength();
                    if(next.getLength() > 1) {
                        shortBlock = false;
                    }
                }
                else {
                    // add the block
                    if((blockLen > 1 || singleton) && predicate.test(symbol)) {
                        BlockRecord<N, S> record = new BlockRecord<>(new Block<>(symbol, blockLen), current);
                        if(shortBlock) {
                            shortBlockConsumer.accept(record);
                        }
                        else {
                            longBlockConsumer.accept(record);
                        }
                    }

                    symbol = next;
                    current = nextNode;
                    blockLen = next.getLength();
                    shortBlock = true;
                }
            }

            if((blockLen > 1 || singleton) && predicate.test(symbol)) {
                BlockRecord<N, S> record = new BlockRecord<>(new Block<>(symbol, blockLen), current);
                if(shortBlock) {
                    shortBlockConsumer.accept(record);
                }
                else {
                    longBlockConsumer.accept(record);
                }
            }
        }
    }

    private boolean allEqual(final List<MyLinkedList<S>> lists, final Function<MyLinkedList<S>, Supplier<Node<S>>> supplier) {
        if(lists.isEmpty()) {
            return true;
        }
        else {
            if(lists.stream().allMatch(list -> list.isEmpty())) {
                return true;
            }
            else if(lists.stream().anyMatch(list -> list.isEmpty())){
                return false;
            }
            else {
                S first = supplier.apply(lists.get(0)).get().getElement();
                return lists.stream().map(list -> supplier.apply(list).get().getElement()).allMatch(s -> s.equals(first));
            }
        }
    }

    private S fixBlock(final Pair<S, S> pair, final long l, final S pattern) {

        // pop the blocks containing p[1] so that we can compress these blocks
        W wordRhs = slp.getProduction(pattern).getRight();

        // get all a blocks
        List<BlockRecord<N, S>> shortBlockRecords = new ArrayList<>();
        List<BlockRecord<N, S>> longBlockRecords = new ArrayList<>();

        // get all blocks
        slp.getOrderedProductions().stream().forEach(production -> consumeBlocks(production.getRight(), shortBlockRecords::add, longBlockRecords::add, letter -> letter.equals(pair.a)));

        sortBlocks(shortBlockRecords);
        Collections.sort(longBlockRecords, blockComparator);
        List<BlockRecord<N, S>> blockRecords = mergeBlocks(shortBlockRecords, longBlockRecords);

        Iterator<BlockRecord<N, S>> iterator = blockRecords.iterator();
        BlockRecord<N, S> lastRecord = null;
        S letter = null;
        S a_l = null;
        while (iterator.hasNext()) {
            BlockRecord<N, S> record = iterator.next();
            long length = record.block.getLength();
            if(length >= l && a_l == null) {
                a_l = terminalAlphabet.createTerminal(phase, 1L, l * pair.a.getWeight(), record.block);
            }

            if(lastRecord == null || !lastRecord.equals(record)) {
                if(length < l) {
                    letter = terminalAlphabet.createTerminal(phase, 1L, record.block.getLength() * record.block.getLetter().getWeight(), record.block);
                }
                else if(length > l) {
                    letter = terminalAlphabet.createTerminal(phase, 1L, record.block.getLength() - l, record.block);
                }
                lastRecord = record;
            }

            // for m <= l
            if (length == l) {
                replaceBlock(record, a_l);
            }
            else if (length < l) {
                replaceBlock(record, letter);
            } // for m > l
            else {
                Node<S> node = replaceBlock(record, letter);
                node.insertNext(a_l);
            }
        }

        return a_l;
    }

    /**
     * The FixEndsSame from Jéz-Paper.
     *
     * @param pairs     first and last letter of each non-terminal
     * @param pattern   the non-terminal of the pattern
     * @param text      the non-terminal of the text
     */
    private void fixEndsSame(final Map<S, Pair<S, S>> pairs, final S pattern, final S text) {
        Pair<S, S> pair = pairs.get(pattern);

        popBlocks(prefix -> prefix.equals(pair.a), suffix -> suffix.equals(pair.b));
        W patterRhs = slp.getProduction(pattern).getRight();

        long l = patterRhs.deletePrefix(pair.a);
        long r = patterRhs.deleteSuffix(pair.b);
        assert l < Integer.MAX_VALUE-1;

        S a_L = terminalAlphabet.createTerminal(phase, 1L, pair.a.getWeight() * l, new Block<S>(pair.a, (int)l));
        S a_R = terminalAlphabet.createTerminal(phase, 1L, 0);

        S a_1 = pair.a;

        // special case: the pattern = a^k, we just have to do a block compression. After that the algorithm will terminate
        if(r == 0) {
            patterRhs.suspend(a_L);
            compressBlocks(phase);
            matchingBlocks = true;
        }
        else {
            patterRhs.suspend(a_L);
            patterRhs.append(a_R);
            matchingBlocks = false;
            assert pair.a == pair.b;

            List<BlockRecord<N, S>> shortBlockRecords = new ArrayList<>();
            List<BlockRecord<N, S>> longBlockRecords = new ArrayList<>();

            // get all a blocks
            slp.getProductions().stream().forEach(rule -> consumeBlocks(rule.getRight(), shortBlockRecords::add, longBlockRecords::add, letter -> letter.equals(pair.a), true));

            // sort blocks
            sortBlocks(shortBlockRecords);
            Collections.sort(longBlockRecords, blockComparator);
            List<BlockRecord<N, S>> blockRecords = mergeBlocks(shortBlockRecords, longBlockRecords);

            // cases
            if(l == r) {
                Iterator<BlockRecord<N, S>> iterator = blockRecords.iterator();
                BlockRecord<N, S> lastRecord = null;
                S letter = null;
                while (iterator.hasNext()) {
                    BlockRecord<N, S> record = iterator.next();
                    long length = record.block.getLength();
                    if(length != l && (lastRecord == null || !lastRecord.equals(record))) {
                        if(length < l) {
                            letter = terminalAlphabet.createTerminal(phase, 1L, a_1.getWeight() * length, record.block);
                        }
                        else if(length > l) {
                            letter = terminalAlphabet.createTerminal(phase, 1L, a_1.getWeight() * (length-l), record.block);
                        }

                        // similar to block compression
                        lastRecord = record;
                    }

                    if (length < l) {
                        replaceBlock(record, letter);
                    } // a_Ra_L
                    else if(length == l) {
                        Node<S> node = replaceBlock(record, a_R);
                        node.insertNext(a_L);
                    }
                    else {
                        Node<S> node = replaceBlock(record, a_R);
                        node.insertNext(letter);
                        node.insertNext(a_L);
                    }
                }
            }
            else if(l < r) {
                Iterator<BlockRecord<N, S>> iterator = blockRecords.iterator();
                BlockRecord<N, S> lastRecord = null;
                S letter = null;
                while (iterator.hasNext()) {
                    BlockRecord<N, S> record = iterator.next();
                    long length = record.block.getLength();
                    if(length != l && (lastRecord == null || !lastRecord.equals(record))) {
                        if(length < l) {
                            letter = terminalAlphabet.createTerminal(phase, 1L, a_1.getWeight() * length, record.block);
                        }
                        else if(length > l) {
                            letter = terminalAlphabet.createTerminal(phase, 1L, a_1.getWeight() * (length-l), record.block);
                        }
                        // similar to block compression
                        lastRecord = record;
                    }

                    if (length < l) {
                        replaceBlock(record, letter);
                    } // a_Ra_L
                    else if(length == l) {
                        replaceBlock(record, a_L);
                    }
                    else if(l < length && length < r) {
                        Node<S> node = replaceBlock(record, letter);
                        node.insertNext(a_L);
                    }
                    else {
                        Node<S> node = replaceBlock(record, a_R);
                        node.insertNext(a_L);
                        node.insertNext(letter);
                    }
                }
            }
            else if(l > r){
                Iterator<BlockRecord<N, S>> iterator = blockRecords.iterator();
                BlockRecord lastRecord = null;
                S letter = null;
                while (iterator.hasNext()) {
                    BlockRecord<N, S> record = iterator.next();
                    long length = record.block.getLength();
                    if(length != l && (lastRecord == null || !lastRecord.equals(record))) {
                        if(length < l) {
                            letter = terminalAlphabet.createTerminal(phase, 1L, a_1.getWeight() * length, record.block);
                        }
                        else if(length > l) {
                            letter = terminalAlphabet.createTerminal(phase, 1L, a_1.getWeight() * (length-l), record.block);
                        }

                        // similar to block compression
                        lastRecord = record;
                    }

                    if (length < r) {
                        replaceBlock(record, letter);
                    } // a_Ra_L
                    else if(r <= length && length < l) {
                        Node<S> node = replaceBlock(record, a_R);
                        node.insertNext(letter);
                    }
                    else if(length == l) { // ERROR?
                        Node<S> node = replaceBlock(record, a_R);
                        node.insertNext(a_L);
                    }
                    else if(length > l) {
                        Node<S> node = replaceBlock(record, a_R);
                        node.insertNext(a_L);
                        node.insertNext(letter);
                    }
                }
            }

            W rhsText = slp.getProduction(text).getRight();
            Pair<S, S> textPair = new Pair<>(rhsText.getFirst(), rhsText.getLast());
            if(textPair.b.equals(a_L)) {
                rhsText.deleteLast();
            }

            if(textPair.a.equals(a_R)) {
                rhsText.deleteFirst();
            }

            compressLeftSingleTonPairs(a_L);
            compressRightSingleTonPairs(a_R);
            if(r == 1 && r < l){
                compressLeftSingleTonPairs(a_1);
            }
        }
    }

    private void sortPairs(final List<GPairRecord<S, S>> pairs) {
        //Collections.sort(pairs, pairComparator);
        RecordSort.sortGPairRecord(pairs);
    }

    private void sortBlocks(final List<BlockRecord<N, S>> blocks) {
        //Collections.sort(blocks, blockComparator);
        RecordSort.sortBlockRecords(blocks);
    }

    /**
     * Merges two sorted lists together and return a sorted list containing all entries from both lists.
     * Requirement: list1 and list2 are sorted.
     *
     * @param list1 the first list
     * @param list2 the second list
     *
     * @return a sorted merged list.
     */
    private List<BlockRecord<N, S>> mergeBlocks(final List<BlockRecord<N, S>> list1, final List<BlockRecord<N, S>> list2) {
        List<BlockRecord<N, S>> list = new ArrayList<>(list1.size() + list2.size());
        Iterator<BlockRecord<N, S>> it1 = list1.iterator();
        Iterator<BlockRecord<N, S>> it2 = list2.iterator();

        BlockRecord<N, S> rec1 = null;
        BlockRecord<N, S> rec2 = null;
        while (it1.hasNext() || it2.hasNext() || rec1 != null || rec2 != null) {
            if(it1.hasNext() && rec1 == null) {
                rec1 = it1.next();
            }

            if(it2.hasNext() && rec2 == null) {
                rec2 = it2.next();
            }

            if(rec2 == null || (rec1 != null && blockComparator.compare(rec1, rec2) < 0)) {
                list.add(rec1);
                rec1 = null;
            }
            else {
                list.add(rec2);
                rec2 = null;
            }
        }

        return list;
    }
}
