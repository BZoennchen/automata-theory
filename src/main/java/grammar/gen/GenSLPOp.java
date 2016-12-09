package grammar.gen;

import grammar.inter.*;
import morphismEq.gen.GenMorphismEQSolver;
import morphismEq.morphisms.PeriodicMorphism;
import symbol.IJezSymbol;
import utils.Triple;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * We separate special operations on SLP and put them into a extra class i.e. GenSLPOp.
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
public class GenSLPOp<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>, P extends IProduction<N, S, W>, C extends ICFG<N, S, W, P>, Z extends ISLP<N, S, W, P>> {

    private static Logger logger = LogManager.getLogger(GenSLPOp.class);

    /**
     * Let G be the CFG and G' be the SLP. The method checks whether L(G) is of the same period as L(G').
     *
     * @param cfg               the CFG
     * @param slp               the SLP
     * @param cfgCreator        the creator that crates the CFG and the SLP (this is important!)
     * @param creatorFactory    a factory for the creation of fresh grammars
     * @return true if the CFG and the SLP share the same period, otherwise false
     */
    public boolean hasSamePeriod(
            final C cfg,
            final Z slp,
            final ICFGCreator<N, S, W, P, C, Z> cfgCreator,
            final ICFGCreatorFactory<N, S, W, P, C, Z> creatorFactory) {

        if(!slp.isSingleton()) {
            throw new IllegalArgumentException("Requirement: SLP is a singleton SLP.");
        }

        GenCFGOp<N, S, W, P, C, Z> cfgGenOp = new GenCFGOp<N, S, W, P, C, Z>();

        Set<P> wCNFCFG = cfgGenOp.toWeakCNF(cfg.getProductions(), cfg.getAxioms(), cfgCreator);
        C wCFG = cfgCreator.createCFG(wCNFCFG, cfg.getAxioms());

        Map<S, P> wCNFSLP = cfgGenOp.toCNF(slp.getSLPProductions(), slp.getAxiom(), cfgCreator);
        Z wSLP = cfgCreator.createSLP(wCNFSLP, slp.getAxioms());

        // make sure that non-terminals are in an interval [0;|N|-1]
        ICFGCreator<N, S, W, P, C, Z> cfgCreator1 = creatorFactory.create();
        wCFG = cfgCreator1.resetNonTerminals(wCFG);
        wSLP = cfgCreator1.resetNonTerminals(wSLP);

        Z leftSLP = cfgCreator1.freshNonTerminals(wSLP, new HashMap<>());
        Z rightSLP = cfgCreator1.freshNonTerminals(wSLP, new HashMap<>());

        // create the grammar S' S_1 S''
        Set<P> cfgProductions = new HashSet<>(wCFG.getProductions());
        cfgProductions.addAll(leftSLP.getProductions());
        cfgProductions.addAll(rightSLP.getProductions());

        S axiom = cfgCreator1.createFreshNonTerminal();
        S middle = cfgCreator1.createFreshNonTerminal();

        for(S oldAxiom : wCFG.getAxioms()) {
            cfgProductions.add(cfgCreator1.createProduction(axiom, leftSLP.getAxiom(), middle));
            cfgProductions.add(cfgCreator1.createProduction(middle, oldAxiom, rightSLP.getAxiom()));
        }

        C finalCFG = cfgCreator1.createCFG(cfgProductions, axiom);

        GenMorphismEQSolver<N, S, W, P, C, Z, N, S, W, P, C, Z> linGrammer = new GenMorphismEQSolver<>(finalCFG, cfgCreator1, creatorFactory, creatorFactory);
        PeriodicMorphism periodicMorphismLeft = new PeriodicMorphism(leftSLP, creatorFactory);
        PeriodicMorphism periodicMorphismRight = new PeriodicMorphism(rightSLP, creatorFactory);
        return linGrammer.equivalentOnMorphisms(periodicMorphismLeft, periodicMorphismRight);
    }

    /**
     * Computes all positions of all occurrences of pattern in text. Since there can be exponential many (in size of the grammars)
     * occurrences this may require exponential time.
     *
     * Complexity: exponential
     * Requirement: SLPs text and pattern are singletons.
     *
     * @param text      SLP of the text
     * @param pattern   SLP of the pattern
     * @param factory   creator factory for renaming non-terminals
     * @return  a list of all positions of all occurrences of the pattern in the text
     */
    public List<Long> matchingAll(final Z text, final Z pattern, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        if(!text.isSingleton() || !pattern.isSingleton()) {
            throw new IllegalArgumentException("the matchingAll operation requires two singleton SLPs.");
        }

        Triple<Z, Set<S>, Set<S>> triple = merge(text, pattern, factory, true, false);
        GenJez<N, S, W, P> jez = new GenJez(triple.a, triple.b.iterator().next(), triple.c.iterator().next(), true, factory);
        jez.execute();
        List<Long> occurrences = jez.getPatternOccurrencePositions();
        return occurrences;
    }

    /**
     * Computes the position of the k-th occurrence of the pattern in the text.
     *
     * Complexity:  O((n+m)^2log(n+m)), where n is the size of the grammar of text and m is the site of the grammar of the pattern.
     * Requirement: SLPs text and pattern are singletons.
     *
     * @param text      SLP of the text
     * @param pattern   SLP of the pattern
     * @param k         the number of occurrence we are asking for
     * @param factory   creator factory for renaming non-terminals
     * @return the position (starting from 0) of the k-th occurrence of the pattern in the text, if there is such an occurrence
     */
    public Optional<Long> matching(final Z text, final Z pattern, final long k, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        if(!text.isSingleton() || !pattern.isSingleton()) {
            throw new IllegalArgumentException("the matchingAll operation requires two singleton SLPs.");
        }

        Triple<Z, Set<S>, Set<S>> triple = merge(text, pattern, factory, true, false);
        GenJez<N, S, W, P> jez;
        jez = new GenJez(triple.a, triple.b.iterator().next(), triple.c.iterator().next(), true, factory);
        jez.execute();

        return jez.getPatternOccurrencePosition(k);
    }

    /**
     * Checks if prefix is a prefix of u.
     *
     * Complexity:  O((n+m)^2log(n+m)), where n is the size of the grammar of text and m is the site of the grammar of the pattern.
     * Requirement: SLPs u and prefix are singletons.
     *
     * @param u         a fully compressed word
     * @param prefix    a fully compressed word
     * @param factory   creator factory for renaming non-terminals
     * @return true if prefix is a prefix of u.
     */
    public boolean isPrefixOf(final Z u, final Z prefix, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        if(u.length() >= prefix.length()) {
            Optional<Long> optMatching = matching(u, prefix, 1, factory);
            if(optMatching.isPresent()) {
                return optMatching.get() == 0;
            }
        }
        return false;
    }

    /**
     * Test whether prefix is a prefix of u or prefix = (u)^* w such that w is a prefix of u.
     *
     * @param u         a fully compressed word
     * @param prefix    a fully compressed word
     * @param factory   creator factory for renaming non-terminals
     * @return true if prefix is a prefix of u or prefix = (u)^* w such that w is a prefix of u, otherwise false.
     */
    public boolean isMultiPrefixOf(final Z u, final Z prefix, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        long lenU = u.length();
        long lenPrefix = prefix.length();
        boolean result = true;

        if(u.length() >= prefix.length()) {
            Optional<Long> optMatching = matching(u, prefix, 1, factory);
            result = optMatching.isPresent() && optMatching.get() == 0;
        }
        else {
            ICFGCreator<N, S, W, P, C, Z> cfgCreator = factory.create(prefix);
            long rest = lenPrefix % lenU;

            Z period = delete(prefix, rest, false, cfgCreator);
            Z restPrefix = delete(prefix, lenPrefix - rest, true, cfgCreator);
            if(rest != 0) {
                Optional<Long> optMatching = matching(u, restPrefix, 1, factory);
                result = optMatching.isPresent() && optMatching.get() == 0;
            }

            result = result && hasSamePeriod(period, u, factory);
        }
        return result;
    }

    /**
     * Checks if u and v share the same period.
     *
     * @param u         a fully compressed word
     * @param v         a fully compressed word
     * @param factory   creator factory for renaming non-terminals
     * @return true if u and v share the same period, otherwise false.
     */
    public boolean hasSamePeriod(final Z u, final Z v, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        if(u.length() == 0 && v.length() == 0) {
            return true;
        }

        if(u.length() == 0 || v.length() == 0) {
            return false;
        }

        Z uv = concatenate(u, v, factory);
        Z vu = concatenate(v, u, factory);
        return equals(uv, vu, factory);
    }

    /**
     * Returns a copy of the slp that is equal to the word shifted by m.
     *
     * Requirement: the SLP is a singleton set and m >= 0.
     *
     * @param slp           the original SLP
     * @param m             the amount of shift
     * @param cfgCreator    the creator of the SLP
     * @return a copy of the slp that is equal to the word shifted by m
     */
    public Z shift(final Z slp, final long m, final boolean left, final ICFGCreator<N, S, W, P, C, Z> cfgCreator) {
        if(!slp.isSingleton()) {
            throw new IllegalArgumentException("for this operation the slp has to be a singleton.");
        }

        if(m < 0) {
            throw new IllegalArgumentException("u cannot delete negative amount of terminals.");
        }

        Map<S, P> newProductions = cfgCreator.copyProductions(slp.getSLPProductions());
        GenCFGOp<N, S, W, P, C, Z> cfgOp = new GenCFGOp<>();

        if(slp.length() == 0 || m == 0) {
            return slp;
        }

        long n = m % slp.length();
        Set<S> newAxioms = new HashSet<>();

        long len = 0;

        S axiom = slp.getAxiom();
        S current = axiom;
        S next = null;

        if(!left) {
            n = slp.length() - n;
        }

        if(n == 0) {
            return cfgCreator.createSLP(newProductions, slp.getAxioms());
        }

        LinkedList<S> leftWord = new LinkedList<>();
        LinkedList<S> rightWord = new LinkedList<>();

        while(len < n) {
            LinkedList<S> tmpRightWord = new LinkedList<>();
            P production = newProductions.get(current);

            for(S symbol : production.getRight()) {
                if(next == null && (symbol.isTerminal() && len+1 <= n || !symbol.isTerminal() && len + slp.length(symbol) <= n)) {
                    leftWord.add(symbol);
                    if(symbol.isTerminal()) {
                        len++;
                    }
                    else {
                        len += slp.length(symbol);
                    }
                } // we are done
                else if(next == null && !symbol.isTerminal()) {
                    next = symbol;
                    if(len == n) {
                        tmpRightWord.add(symbol);
                    }
                }
                else {
                    tmpRightWord.add(symbol);
                }
            }

            Iterator<S> descendingIterator = tmpRightWord.descendingIterator();
            while (descendingIterator.hasNext()) {
                rightWord.addFirst(descendingIterator.next());
            }
            current = next;
            next = null;
        }

        // delete the old axiom rule
        newProductions.remove(slp.getAxiom());

        rightWord.addAll(leftWord);
        newAxioms.add(slp.getAxiom());

        // add the new axiom
        newProductions.put(slp.getAxiom(), cfgCreator.createProduction(slp.getAxiom(), cfgCreator.createWord(rightWord)));

        return cfgCreator.createSLP(cfgOp.toCNF(newProductions, slp.getAxiom(), cfgCreator), newAxioms);
    }

    /**
     * Returns a copy of the slp that is equal to the word delete the first/last m terminal symbols from the left/right.
     *
     * Requirement: the SLP is a singleton set and m >= 0.
     *
     * @param slp           the original SLP
     * @param m             the amount of shift
     * @param cfgCreator    the creator of the SLP
     * @return a copy of the slp that is equal to the word shifted by m
     */
    public Z delete(final Z slp, final long m, final boolean fromLeft, final ICFGCreator<N, S, W, P, C, Z> cfgCreator) {
        if(!slp.isSingleton()) {
            throw new IllegalArgumentException("for this operation the slp has to be a singleton.");
        }

        if(m < 0) {
            throw new IllegalArgumentException("u cannot delete negative amount of terminals.");
        }

        GenCFGOp<N, S, W, P, C, Z> cfgOp = new GenCFGOp<>();
        Map<S, P> newProductions = cfgCreator.copyProductions(slp.getSLPProductions());

        if(m == 0) {
            return slp;
        }

        if(slp.length() <= m) {
            return cfgCreator.emptyWord();
        }

        long n;
        if(fromLeft) {
            n = m;
        }
        else {
            n = slp.length() - m;
        }

        long len = 0;

        S axiom = slp.getAxiom();
        S current = axiom;
        S next = null;


        LinkedList<S> leftWord = new LinkedList<>();
        LinkedList<S> rightWord = new LinkedList<>();

        while(len < n) {
            LinkedList<S> tmpRightWord = new LinkedList<>();
            P production = newProductions.get(current);

            for(S symbol : production.getRight()) {
                if(next == null && (symbol.isTerminal() && len+1 <= n || !symbol.isTerminal() && len + slp.length(symbol) <= n)) {
                    leftWord.add(symbol);
                    if(symbol.isTerminal()) {
                        len++;
                    }
                    else {
                        len += slp.length(symbol);
                    }
                } // we are done
                else if(next == null && !symbol.isTerminal()) {
                    next = symbol;
                    if(len == n) {
                        tmpRightWord.add(symbol);
                    }
                }
                else {
                    tmpRightWord.add(symbol);
                }
            }

            Iterator<S> descendingIterator = tmpRightWord.descendingIterator();
            while (descendingIterator.hasNext()) {
                rightWord.addFirst(descendingIterator.next());
            }
            current = next;
            next = null;
        }

        // delete the old axiom rule
        newProductions.remove(slp.getAxiom());

        //rightWord.addAll(leftWord);
        Set<S> newAxioms = new HashSet<>();
        newAxioms.add(slp.getAxiom());

        // add the new axiom
        if(fromLeft) {
            newProductions.put(slp.getAxiom(), cfgCreator.createProduction(slp.getAxiom(), cfgCreator.createWord(rightWord)));
        }
        else {
            newProductions.put(slp.getAxiom(), cfgCreator.createProduction(slp.getAxiom(), cfgCreator.createWord(leftWord)));
        }

        return cfgCreator.createSLP(cfgOp.toCNF(newProductions, slp.getAxiom(), cfgCreator), newAxioms);
    }

    /**
     * Test whether two singleton SLP i.e. two SLP-compressed words are equal without destroying them and the SLP can share common non-terminals.
     *
     * Requirement: both SLPs are singleton sets
     *
     * @param slp1                  the first SLP-compressed word
     * @param slp2                  the second SLP-compressed word
     * @param factory               a creator factory for create fresh grammars
     * @return true => the two SLP-compressed words are equal, false => they are not equal
     */
    public boolean equals(final Z slp1, final Z slp2, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        return equals(slp1, slp2, factory, true, false);
    }

    /**
     * Test whether two singleton SLP i.e. two SLP-compressed words are equal.
     *
     * Requirement: both SLPs are singleton sets, if distinctNonTerminals is true then the SLP share no common non-terminals.
     *
     * @param slp1                  the first SLP-compressed word
     * @param slp2                  the second SLP-compressed word
     * @param factory               a creator factory for create fresh grammars
     * @param immutable             true => this method will not destroy the SLP-compressed words (the arguments), othewise the method destroys the SLPs.
     * @param distinctNonTerminals  true => the method assumes the SLPs does not share any common non-terminals, otherwise the method rename non-terminals
     * @return true => the two SLP-compressed words are equal, false => they are not equal
     */
    public boolean equals(final Z slp1, final Z slp2, final ICFGCreatorFactory<N, S, W, P, C, Z> factory, final boolean immutable, final boolean distinctNonTerminals) {
        if(slp1.isSingleton() && slp2.isSingleton()) {
            Triple<Z, Set<S>, Set<S>> triple = merge(slp1, slp2, factory, immutable, distinctNonTerminals);
            GenJez<? extends N, S, W, P> jez = new GenJez(triple.a, factory);
            return jez.isEquals();
        }
        else {
            throw new IllegalArgumentException("this method requires two singleton slp's.");
        }
    }

    /**
     * Merges the grammar of two SLPs such that the languages are merged i.e. let G be the grammar of the first SLP and
     * G' be the grammar of the second SLP. This method creates a new SLP G'' such that L(G'') = L(G) union L(G').
     *
     * @param slp1                  the first SLP
     * @param slp2                  the second SLP
     * @param factory               the a creator factory to get access to a create for introducing fresh non-terminals
     * @param immutable             true => the new SLP will be created due to copies of the old ones, otherwise no copies will be created
     * @param distinctNonTerminals  true => no renaming of the non-terminals of the SLPs, otherwise we rename non-terminals to be sure that the new SLP is well-defined
     * @return a grammar of two SLPs representing the union of the two SLPs
     */
    public Triple<Z, Set<S>, Set<S>> merge(final Z slp1, final Z slp2, final ICFGCreatorFactory<N, S, W, P, C, Z> factory, final boolean immutable, final boolean distinctNonTerminals) {

        ICFGCreator<N, S, W, P, C, Z> cfgCreator = factory.create(slp1, slp1);

        Z freshSLP1 = slp1;
        Z freshSLP2 = slp2;

        if(immutable) {
            freshSLP1 = cfgCreator.copy(freshSLP1);
            freshSLP2 = cfgCreator.copy(freshSLP2);
        }

        Set<S> allAxioms = new HashSet<>();
        Map<S, P> allProductions = new HashMap<>();

        // we assume all non-terminals are distinct so its not necessary to rename non-terminals.
        if(!distinctNonTerminals) {
            freshSLP1 = cfgCreator.freshNonTerminals(freshSLP1, new HashMap<S, S>());
            freshSLP2 = cfgCreator.freshNonTerminals(freshSLP2, new HashMap<S, S>());
        }

        allAxioms.addAll(freshSLP1.getAxioms());
        allAxioms.addAll(freshSLP2.getAxioms());
        allProductions.putAll(freshSLP1.getSLPProductions());
        allProductions.putAll(freshSLP2.getSLPProductions());
        Z finalSLP = cfgCreator.createSLP(allProductions, allAxioms);

        return new Triple<>(finalSLP, freshSLP1.getAxioms(), freshSLP2.getAxioms());
    }

    /**
     * Tests whether all words defined by the SLP are equals.
     *
     * @param slp the SLP that defines a set of words
     * @return true if all words defined by the SLP are equals, otherwise false
     */
    public boolean equal(final Z slp, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        GenJez<N, S, W, P> jez = new GenJez(slp, factory);
        return jez.isEquals();
    }

    /**
     * Let non-terminals equals [A,B,C,...], then this SLP generate the word w_Aw_Bw_C... where w_A = value(A).
     * All non-terminals has to have a rule in the original SLP.
     *
     * @param nonTerminals non-terminals of the original SLP, there has to be a rule for all these non-terminals.
     * @return an SLP that generates the concatenation of the values of non-terminals of the original SLP.
     */
    public Z concatenate(final Z slp, final ICFGCreator<? extends N, S, W, P, C, Z> cfgCreator, final List<S> nonTerminals) {
        List<List<S>> words = new ArrayList<>();
        words.add(nonTerminals);
        return concatenateWords(slp, cfgCreator, words);
    }

    /**
     * Concatenates two SLPs i.e. if the SLP represents the words w and u the new SLP represents the word wu.
     * Note: The SLPs will not be copied so if another method changes the SLP the original SLPs will be changed as well!
     *
     * Requirement: All SLPs represent singleton sets.
     *
     * @param slp1      the first SLP representing w
     * @param slp2      the second SLP representing u
     * @param factory   a factory to create fresh SLPs
     * @return a SLP representing the concatenated words
     */
    public Z concatenate(final Z slp1, Z slp2, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {
        List<Z> list = new ArrayList<>();
        list.add(slp1);
        list.add(slp2);
        return concatenate(list, factory);
    }

    /**
     * Concatenates a list of SLPs i.e. if the SLP represents the words u_1, u_2, u_3 and so on, the new SLP represents the word u_1u_2u_3....
     * Note: The SLPs will not be copied so if another method changes the SLP the original SLPs will be changed as well!
     *
     * Requirement: All SLPs represent singleton sets.
     *
     * @param slpList   a list of SLP representing words u_1,u_2,u_3 and so on.
     * @param factory   a factory to create fresh SLPs
     * @return a SLP representing the concatenated words u_1u_2u_3...
     */
    public Z concatenate(final List<Z> slpList, final ICFGCreatorFactory<N, S, W, P, C, Z> factory) {

        if(slpList.stream().anyMatch(s -> !s.isSingleton())) {
            throw new IllegalArgumentException("concatination is only supported for singleton slps.");
        }

        if(slpList.isEmpty()) {
            throw new IllegalArgumentException("there is no slp to concatinate.");
        }

        if(slpList.size() == 1) {
            return slpList.get(0);
        }

        ICFGCreator<? extends N, S, W, P, C, Z> cfgCreator = factory.create(slpList);

        boolean distinct = isDistinctNonTerminals(slpList);
        List<Z> slpPairList = slpList.stream()
                .map(s -> distinct ? s : cfgCreator.freshNonTerminals(s, new HashMap<>()))
                .collect(Collectors.toList());

        S axiom = cfgCreator.createFreshNonTerminal();
        P production = cfgCreator.createProduction(axiom, cfgCreator.createWord(slpPairList.stream().map(slp -> slp.getAxiom()).collect(Collectors.toList())));

        Map<S, P> slpProductions = new HashMap<>();
        slpProductions.put(axiom, production);
        if(production.getRight().isEmpty()) {
            logger.info("stop");
        }

        slpPairList.forEach(slp -> slpProductions.putAll(slp.getSLPProductions()));

        return cfgCreator.createSLP(slpProductions, axiom);
    }

    /**
     * Tests whether a list of SLP do not share a single common non-terminal.
     *
     * @param slps  a list of SLPs
     * @return true => the SLPs do not share a common non-terminal, otherwise false
     */
    private boolean isDistinctNonTerminals(final List<Z> slps) {
        Set<S> nonTerminals = new HashSet<>();

        for(Z slp : slps) {
            final Set<S> slpNonTerminals = slp.getNonTerminals();
            int size = nonTerminals.size();
            nonTerminals.addAll(slpNonTerminals);

            assert slpNonTerminals.size() <= nonTerminals.size();
            if(nonTerminals.size() - size != slpNonTerminals.size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an new SLP, without destroying the old SLP, that represent the words in the word list.
     * This word has to be defined by non-terminals of the origin SLP.
     *
     * Example:
     * Assume the SLP looks like: S -> AB, A -> a, B -> CaD, C -> c, D -> d than a List of words w1 = ACD, w2 = SD would
     * result in an SLP:
     * S -> AB, A -> a, B -> CaD, C -> c, D -> d
     * X_1 -> ACD
     * X_2 -> SD
     * where X_1 and X_2 are axioms of the new SLP.
     *
     *
     * @param slp               the base SLP
     * @param cfgCreator        the object that creates words and productions
     * @param words             the list of words
     * @param freshNonTerminals the list of fresh non-terminals, these has to be distinct from all non-terminals in the origin SLP
     * @return                  a new SLP that represents the words in the word list.
     */
    public Z concatenateWords(final Z slp, final ICFGCreator<? extends N, S, W, P, C, Z> cfgCreator, final List<List<S>> words, final List<S> freshNonTerminals) {
        Map<S, P> productionSet = cfgCreator.copyProductions(slp.getProductions()).stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p));
        Set<S> axioms = new HashSet<>();

        if(words.size() != freshNonTerminals.size()) {
            throw new IllegalArgumentException("there has to be as many fresh non-terminals as words to concatenate.");
        }

        Iterator<List<S>> itWords = words.iterator();
        Iterator<S> itFreshNonTerminals = freshNonTerminals.iterator();

        while (itWords.hasNext() && itFreshNonTerminals.hasNext()) {
            List<S> word = itWords.next();
            S axiom = itFreshNonTerminals.next();
            W right = cfgCreator.createWord(word);
            axioms.add(axiom);
            productionSet.put(axiom, cfgCreator.createProduction(axiom, right));
        }

        Z concatSLP = cfgCreator.createSLP(productionSet, axioms);

        return concatSLP;
    }

    /**
     * Construct a SLP representing words that are the concatenation of the list of words defined by one list in words.
     *
     * Requirement: Each non-terminal has to be contained in the set of non-terminals of the base slp.
     *
     * @param slp           the base SLP
     * @param cfgCreator    the creator of the base SLP
     * @param words         the words defined by lists of non-terminals of the base SLP
     * @return a SLP representing words that are the concatination of the list of words defined by one list in words
     */
    public Z concatenateWords(final Z slp, final ICFGCreator<? extends N, S, W, P, C, Z> cfgCreator, final List<List<S>> words) {
        List<S> freshNonTerminals = new LinkedList<>();
        words.forEach(w -> freshNonTerminals.add(cfgCreator.createFreshNonTerminal()));
        return concatenateWords(slp, cfgCreator, words, freshNonTerminals);
    }
}
