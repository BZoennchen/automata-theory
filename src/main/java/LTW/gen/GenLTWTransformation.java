package LTW.gen;

import LTW.inter.ILTWRule;
import grammar.impl.*;
import grammar.inter.*;
import LTW.inter.ILTW;
import LTW.inter.ILTWCreator;
import LTW.inter.IRankedSymbol;
import N2W.gen.GenN2WtoCFGReduction;
import N2W.inter.IN2WCreator;
import N2W.inter.IN2WRule;
import N2W.inter.IN2WTransducer;
import N2W.inter.INestedLetter;
import morphismEq.morphisms.IMorphism;
import symbol.IJezSymbol;
import symbol.INamedSymbol;
import utils.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The class that implements the equivalence test for LTWs i.e. the transformation of LTWs
 * into partial normal form, reducing it to dN2Ws, reducing these dN2Ws to a grammar of
 * successful runs and solving the morphism equivalence problem on this grammar.
 *
 * Note: The current grammar and the mapping of a state to a non-terminal will be updated when
 * ever the LTW the algorithm working with changes i.e. see createGrammar.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output alphabet
 * @param <A> the type of the identifier of the ranked symbol
 * @param <B> the type of the identifier of the state of the LTW
 * @param <F> the type of the ranked symbol
 * @param <Q> the type of the states of the LTW
 * @param <R> the type of the rules of the LTW
 * @param <L> the type of the LTW
 *
 * @param <Q2> the type of the state of the nested word-to-word transducer
 * @param <W2> the type of the nested letter of the nested word-to-word transducer
 * @param <G2> the type of the stack symbol of the nested word-to-word transducer
 * @param <R2> the type of the rule of the nested word-to-word transducer
 * @param <M2> the type of the nested word-to-word transducer
 *
 * the type the alphabet of the grammar of successful parallel runs is Object
 *            (since we have quadrupel of states as non-terminals and tuples of N2W rules as terminals).
 * @param <S3> the type of the symbols of the grammar (the identifier which defines the alphabet is object)
 * @param <W3> the type of the word of the grammar of successful runs (i.e. the right-hand side)
 * @param <P3> the type of the grammar production of the grammar of successful runs
 * @param <C3> the type of the CFG of the grammar of successful runs
 * @param <Z3> the type of the SLP of the grammar of successful runs
 *
 * @param <I> the type of the alphabet of the morphed grammar (in our case the type of the output alphabets of the LTW)
 * @param <S4> the type of the symbols of the morphed grammar
 * @param <W4> the type of the word of the morphed grammar (i.e. the right-hand side)
 * @param <P4> the type of the grammar production of the morphed grammar
 * @param <C4> the type of the CFG of the morphed grammar
 * @param <Z4> the type of the SLP of the morphed grammar
 */
public abstract class GenLTWTransformation<I, N, A, B,
        F extends IRankedSymbol<A>,
        Q extends INamedSymbol<B>,
        R extends ILTWRule<N, A, B, F, Q>,
        L extends ILTW<N, A, B, F, Q, R>,

        Q2 extends INamedSymbol<Pair<R, Integer>>,
        W2 extends INestedLetter<F>,
        G2 extends INamedSymbol<Pair<R, Integer>>,
        R2 extends IN2WRule<SLP<N>, Pair<R, Integer>, F, Pair<R, Integer>, Q2, W2, G2>,
        M2 extends IN2WTransducer<SLP<N>, Pair<R, Integer>, F, Pair<R, Integer>, Q2, W2, G2, R2>,

        S3 extends IJezSymbol<Object>,
        W3 extends IReferencedWord<Object, S3>,
        P3 extends IProduction<Object, S3, W3>,
        C3 extends ICFG<Object, S3, W3, P3>,
        Z3 extends ISLP<Object, S3, W3, P3>,

        S4 extends IJezSymbol<I>,
        W4 extends IReferencedWord<I, S4>,
        P4 extends IProduction<I, S4, W4>,
        C4 extends ICFG<I, S4, W4, P4>,
        Z4 extends ISLP<I, S4, W4, P4>
        > {

    /**
     * A logger for debugging.
     */
    private static Logger logger = LogManager.getLogger(GenLTWTransformation.class);

    /**
     * A mapping : Q -> N that maps a state of the LTW to its non-terminal of the grammar of the LTW.
     */
    private Map<Q, IJezSymbol<N>> ltwToCfgMap;

    /**
     * The grammar of the LTW.
     */
    private CFG<N> cfg;

    /**
     * Creator factory for creators creating SLPs of output words of the LTW.
     */
    private final CFGCreatorFactory<N> slpCreatorFactory;

    /**
     * Creator factory for creators creating grammars of the type of the image of the morphisms.
     */
    private final ICFGCreatorFactory<I, S4, W4, P4, C4, Z4> cfgCreatorFactory;

    /**
     * Creator factory for creators creating grammars of the type of the domain of the morphism
     */
    private final ICFGCreatorFactory<Object, S3, W3, P3, C3, Z3> baseSLPCreatorFactory;

    /**
     * The creator for creating and manipulating output words.
     */
    private CFGCreator<N> cfgCreator;

    /**
     * The morphism that maps (r_1,r_2) to the output word of r_1
     */
    private IMorphism<Z3, Z4> morphism1;

    /**
     * The morphism that maps (r_1,r_2) to the output word of r_2
     */
    private IMorphism<Z3, Z4> morphism2;

    /**
     * The LTW creator that creates the LTWs the class work with. This is important since we the algorithm has to introduce fresh states!
     */
    private ILTWCreator<N, A, B, F, Q, R, L> ltwCreator;

    /**
     * The object that transform the LTW interpreting it as STW into an N2W.
     */
    private GenSTWtoN2W<N, A, B, F, Q, R ,L, Q2, W2, G2, R2, M2> ltwToN2W;

    /**
     * A mapping : Q -> word i.e. q |-> u, such that q is periodic of period u.
     */
    private Map<Q, SLP<N>> periodicStates;

    /**
     * The set of states that the algorithm identified as being not periodic.
     */
    private Set<Q> nonPeriodicStates;

    /**
     * The creator for creating N2Ws.
     */
    private IN2WCreator<SLP<N>, Pair<R, Integer>, F, Pair<R, Integer>, Q2, W2, G2, R2, M2> n2wCreator;

    /**
     * The object that transform two N2Ws into the grammar of successful runs of the N2Ws.
     */
    private GenN2WtoCFGReduction<I, SLP<N>, Pair<R, Integer>, F, Pair<R, Integer>, Q2, W2, G2, R2, M2, S3, W3, P3, C3, Z3, S4, W4, P4, C4, Z4> n2wToCfg;

    /**
     * Basic operations for LTWs.
     */
    private GenLTWOp<N, A, B, F, Q, R, L> genLTWOp;

    /**
     * The default constructor for the LTW equivalence test.
     *
     * @param morphism1     the morphism that maps (r_1,r_2) to the output word of r_1
     * @param morphism2     the morphism that maps (r_1,r_2) to the output word of r_2
     * @param ltwCreator    a linear tree to word transducer creator for introducing new rules and states, this has to be the creator that creates the ltw
     * @param ltwToN2W      a sequential tree to word transducer to nested word to word transducer converter
     * @param n2wCreator    a nested word to word transducer creator to build the STW's (fresh creator)
     * @param n2wToCfg      a nested word to word transducer to context-free grammar converter
     * @param factory       a factory creator for creating grammars of the successful runs of two N2Ws
     */
    public GenLTWTransformation(
            final IMorphism<Z3, Z4> morphism1,
            final IMorphism<Z3, Z4> morphism2,
            final ILTWCreator<N, A, B, F, Q, R, L> ltwCreator,
            final GenSTWtoN2W<N, A, B, F, Q, R, L, Q2, W2, G2, R2, M2> ltwToN2W,
            final IN2WCreator<SLP<N>, Pair<R, Integer>, F, Pair<R, Integer>, Q2, W2, G2, R2, M2> n2wCreator,
            final GenN2WtoCFGReduction<I, SLP<N>, Pair<R, Integer>, F, Pair<R, Integer>, Q2, W2, G2, R2, M2, S3, W3, P3, C3, Z3, S4, W4, P4, C4, Z4> n2wToCfg,
            final ICFGCreatorFactory<I, S4, W4, P4, C4, Z4> factory,
            final ICFGCreatorFactory<Object, S3, W3, P3, C3, Z3> baseFactory) {
        this.genLTWOp = new GenLTWOp<>();
        this.ltwCreator = ltwCreator;
        this.morphism1 = morphism1;
        this.morphism2 = morphism2;
        this.slpCreatorFactory = new CFGCreatorFactory<>();
        this.baseSLPCreatorFactory = baseFactory;
        this.cfgCreator = slpCreatorFactory.create();
        this.cfgCreatorFactory = factory;
        this.ltwToN2W = ltwToN2W;
        this.n2wCreator = n2wCreator;
        this.n2wToCfg = n2wToCfg;
    }

    /**
     * Tests whether two LTWs are equivalent i.e. if their domain are the same and that
     * for the same input they produce the same output.
     *
     * @param ltw1  the first LTW
     * @param ltw2  the second LTW
     * @return true => the LTWs are equivalent, otherwise false
     */
    public boolean isEquals(final L ltw1, final L ltw2) {

        if(!isEqualLTWDomain(ltw1, ltw2)) {
            logger.info("dom(" + ltw1 + ") != dom(" + ltw2 + ")");
            return false;
        }

        logger.info("dom(" + ltw1 + ") = dom(" + ltw2 + ")");

        if(genLTWOp.isSameOrdered(ltw1, ltw2)) {
            logger.info(ltw1 + " and " + ltw2 + " are same-ordered.");
            return isEqualSTWImage(ltw1, ltw2);
        }
        else {
            logger.info(ltw1 + " and " + ltw2 + " aren't same-ordered.");
            logger.info("start transformation into partial normal form of " + ltw1);
            L pnfLTW1 = toPartialNormalForm(ltw1);
            logger.info("start transformation into partial normal form of " + ltw2);
            L pnfLTW2 = toPartialNormalForm(ltw2);

            if(genLTWOp.isSameOrdered(pnfLTW1, pnfLTW2)) {
                logger.info(pnfLTW1 + " and " + pnfLTW2 + " are same-ordered.");
                return isEqualSTWImage(pnfLTW1, pnfLTW2);
            }
            else {
                logger.info(pnfLTW1 + " and " + pnfLTW2 + " aren't same-ordered.");
            }
        }

        return false;
    }

    /**
     * Tests wether two LTWs have the same domain.
     *
     * @param ltw1  the first LTW
     * @param ltw2  the second LTW
     * @return true => the LTWs have the same domain, otherwise false
     */
    public boolean isEqualLTWDomain(final L ltw1, final L ltw2) {
        return genLTWOp.isEqualLTWDomain(ltw1, ltw2, ltwCreator);
    }

    public CFG<N> toCFG(final L ltw, final Map<Q, IJezSymbol<N>> ltwToCfgMap) {
        return genLTWOp.toCFG(ltw, ltwToCfgMap, cfgCreator);
    }

    /**
     * Tests whether two STWs or two same-ordered LTWs produce the same output with the same input.
     *
     * @param stw1  the first LTW
     * @param stw2  the second LTW
     * @return true => the two same-ordered LTWs produce the same output, otherwise false
     */
    public boolean isEqualSTWImage(
            final L stw1,
            final L stw2) {

        M2 n2w1 = ltwToN2W.STWtoN2W(stw1, n2wCreator);
        M2 n2w2 = ltwToN2W.STWtoN2W(stw2, n2wCreator);
        return n2wToCfg.isEquals(n2w1, n2w2, morphism1, morphism2, cfgCreatorFactory, baseSLPCreatorFactory);

    }

    /**
     * Transforms a LTW into partial normal form. The original LTW will be destroyed.
     *
     * @param m the LTW
     * @return a LTW in partial normal form that is equivalent to m
     */
    public L toPartialNormalForm(final L m) {
        this.ltwToCfgMap = new HashMap<>();
        this.periodicStates = new HashMap<>();
        this.nonPeriodicStates = new HashSet<>();
        L resultLTW = m;

        // build grammar for all L_q's
        resultLTW = ltwCreator.createLTW(m.getRules(), m.getInitialStates());
        createGrammar(resultLTW);
        // test if q is quasi-periodic if so, add rules and change the ltw, otherwise do nothing
        Set<Q> states = new HashSet<>(resultLTW.getStates());
        logger.info(resultLTW);



        while (!states.isEmpty()) {
            Q q = states.iterator().next();
            states.remove(q);

            if(resultLTW.getStates().contains(q)) {
                // is q quasi-periodic on the left?
                Pair<L, Boolean> pair = eliminateQuasiPeriodicity(resultLTW, q, null, true);
                resultLTW = pair.a;
                createGrammar(resultLTW);

                if(!pair.b) {
                    // if not is q quasi-periodic on the right?
                    pair = eliminateQuasiPeriodicity(resultLTW, q, null, false);
                    resultLTW = pair.a;
                    createGrammar(resultLTW);

                    if(!pair.b) {
                        nonPeriodicStates.add(q);
                    }
                }
            }
            else {
                logger.info(q + " state was deleted during the process.");
            }
        }

        logger.info("reorder empty languages.");
        resultLTW = reorderErasingLanguages(resultLTW);
        createGrammar(resultLTW);
        // copy rules since we will change the ltw during the iteration.
        Set<Pair<Q, F>> ruleIdentifier = new HashSet<>();

        for(R rule : resultLTW.getRules().stream().filter(r -> !ruleIdentifier.contains(new Pair<>(r.getSrcState(), r.getSymbol()))).collect(Collectors.toSet())) {
            ruleIdentifier.add(new Pair<>(rule.getSrcState(), rule.getSymbol()));
            for (int i = rule.getDestStates().size() - 2; i >= 0; i--) {
                SLP<N> u = rule.getOutputWords().get(i + 1);
                if (u.length() > 0) {
                    // the ltw changed so we have to reorder the rule
                    Q q = rule.getDestStates().get(i);
                    if (!nonPeriodicStates.contains(q)) {
                        Pair<L, Boolean> pair = eliminateQuasiPeriodicity(resultLTW, q, u, true);
                        resultLTW = pair.a;
                        if(pair.b) {
                            createGrammar(resultLTW);
                        }
                    }
                }
            }
        }
        logger.info("LTW M_1 was build and has no proper quasi-periodic qu's!");


        logger.info("reorder periodic languages.");
        resultLTW = reorderPeriodicLanguages(resultLTW);
        createGrammar(resultLTW);

        return resultLTW;
    }

    private void createGrammar(final L ltw) {
        cfgCreator = new CFGCreator<>();
        ltwToCfgMap = new HashMap<>();
        cfg = genLTWOp.toCFG(ltw, ltwToCfgMap, cfgCreator);
    }

    /**
     * Re-orders maximal periodic sequences in the rules of the LTW according to its permutation.
     *
     * @param ltw the LTW
     */
    private L reorderPeriodicLanguages(final L ltw) {
        CFGOp<N> cfgOp = new CFGOp<>();
        SLPOp<N> slpOp = new SLPOp<>();
        Set<R> newLtwRules = new HashSet<>();

        for(R rule : ltw.getRules()) {
            Map<Q, Integer> permutation = new HashMap<>();
            for(int i = 0; i < rule.getDestStates().size(); i++) {
                permutation.put(rule.getDestStates().get(i), rule.getInputPermutation().apply(i));
            }

            Comparator<Pair<Q, Integer>> stateComparator = (o1, o2) -> {
                int p = rule.getInputPermutation().apply(o1.b) - rule.getInputPermutation().apply(o2.b);
                assert p != 0;
                if(p < 0) {
                    return -1;
                }
                else if(p > 0) {
                    return 1;
                }
                else {
                    return 0;
                }
            };

            List<Q> newStates = new ArrayList<>();


            List<List<Pair<Q, Integer>>> maxLists = new ArrayList<>();
            //List<Pair<Q, Integer>> maxStateList = new ArrayList<>();
            List<Pair<Q, Integer>> stateList = new ArrayList<>();

            // search the largest part such that Lq_i, ... , Lq_j are periodic with the same periodic and u_i = ... = u_j-1 = empty.
            SLP<N> period1 = null;
            for(int i = 0; i < rule.getDestStates().size(); i++) {

                if(period1 == null && rule.getOutputWords().get(i+1).length() == 0 && isPeriodic(ltw, rule.getDestStates().get(i))) {
                    period1 = cfgOp.getMinimalNonEmptyWord(cfg.getProductions(), ltwToCfgMap.get(rule.getDestStates().get(i)), cfgCreator).orElse(cfgCreator.emptyWord());
                }
                else if(period1 != null) {
                    if(rule.getOutputWords().get(i).length() == 0 && isPeriodic(ltw, rule.getDestStates().get(i))) {
                        SLP<N> period2 = cfgOp.getMinimalNonEmptyWord(cfg.getProductions(), ltwToCfgMap.get(rule.getDestStates().get(i)), cfgCreator).orElse(cfgCreator.emptyWord());
                        if(!slpOp.equals(period1, period2, slpCreatorFactory, true, false)) {
                            maxLists.add(stateList);
                            stateList = new ArrayList<>();
                            period1 = null;
                        }
                    }
                    else {
                        maxLists.add(stateList);
                        stateList = new ArrayList<>();
                        period1 = null;
                    }
                }
                else {
                    period1 = null;
                }

                stateList.add(new Pair<>(rule.getDestStates().get(i), i));
            }

            if(!stateList.isEmpty()) {
                maxLists.add(stateList);
            }

            int[] per = new int[rule.getDestStates().size()];
            for(int i = 0; i < maxLists.size(); i++) {
                Collections.sort(maxLists.get(i), stateComparator);
                for(int j = 0; j < maxLists.get(i).size(); j++) {
                    newStates.add(maxLists.get(i).get(j).a);
                }
            }

            for(int i = 0; i < newStates.size(); i++) {
                per[i] = permutation.get(newStates.get(i));
            }

            newLtwRules.add(ltwCreator.createRule(rule.getSrcState(), rule.getSymbol(), newStates, rule.getOutputWords(), i -> per[i]));
        }

        L resultLtw = ltwCreator.createLTW(newLtwRules, ltw.getInitialStates());
        logger.info(resultLtw);
        return resultLtw;
    }

    /**
     * Re-orders erasing states of the LTW.
     *
     * @param ltw the LTW
     */
    private L reorderErasingLanguages(final L ltw) {
        CFGOp<N> cfgOp = new CFGOp<>();
        SLPOp<N> slpOp = new SLPOp();
        Set<R> newLtwRules = new HashSet<>();

        for(R rule : ltw.getRules()) {
            Map<Q, Integer> permutation = new HashMap<>();
            for(int i = 0; i < rule.getDestStates().size(); i++) {
                permutation.put(rule.getDestStates().get(i), rule.getInputPermutation().apply(i));
            }

            Comparator<Pair<Q, Integer>> stateComparator = (o1, o2) -> {
                int p = rule.getInputPermutation().apply(o1.b) - rule.getInputPermutation().apply(o2.b);
                assert p != 0;
                if(p < 0) {
                    return -1;
                }
                else if(p > 0) {
                    return 1;
                }
                else {
                    return 0;
                }
            };

            LinkedList<Pair<Q, Integer>> emptWordLanguages = new LinkedList<>();
            LinkedList<Pair<Q, Integer>> nonEmptyWordLanguages = new LinkedList<>();
            Set<Q> emptyState = new HashSet<>();
            List<SLP<N>> newOutputWords = new ArrayList<>(rule.getOutputWords().size());


            for(int i = 0; i < rule.getDestStates().size(); i++) {
                Q destState = rule.getDestStates().get(i);
                if(cfgOp.isEmptyWord(cfg.getProductions(), ltwToCfgMap.get(destState))) {
                    emptWordLanguages.add(new Pair<>(destState, i));
                    emptyState.add(destState);
                }
                else {
                    nonEmptyWordLanguages.add(new Pair<>(destState, i));
                }
            }

            List<Q> newStates = new ArrayList<>(rule.getDestStates().size());


            if(!emptWordLanguages.isEmpty()) {
                int j = 0;
                newOutputWords.add(rule.getOutputWords().get(0));
                for(int i = 0; i < rule.getDestStates().size(); i++) {
                    Q q = rule.getDestStates().get(i);
                    if(emptyState.contains(q)) {
                        newOutputWords.set(j, slpOp.concatenate(newOutputWords.get(j), rule.getOutputWords().get(i+1), slpCreatorFactory));
                    }
                    else {
                        j++;
                        newOutputWords.add(rule.getOutputWords().get(i+1));
                        newStates.add(q);
                    }
                }

                Collections.sort(emptWordLanguages, stateComparator);

                for(Pair<Q, Integer> empty : emptWordLanguages) {
                    newStates.add(empty.a);
                }

                for(int i = newOutputWords.size(); i < rule.getOutputWords().size(); i++) {
                    newOutputWords.add(cfgCreator.emptyWord());
                }
                // build new permutation

                int[] per = new int[newStates.size()];
                for(int i = 0; i < newStates.size(); i++) {
                    per[i] = permutation.get(newStates.get(i));
                }

                newLtwRules.add(ltwCreator.createRule(rule.getSrcState(), rule.getSymbol(), newStates, newOutputWords, i -> per[i]));
            }
            else {
                newLtwRules.add(rule);
            }
        }

        L resultLtw = ltwCreator.createLTW(newLtwRules, ltw.getInitialStates());
        logger.info(resultLtw);
        return resultLtw;
    }

    /**
     * Tests the state q (or occurrences qh) to be quasi-periodic and transform the LTW into a equivalent LTW that does no longer contain
     * q i.e. one less quasi-periodic state or removes qh such that qh is no longer quasi-periodic
     *
     * @param ltw   the LTW
     * @param q     the state we test and transform
     * @param h     if h is not null this means we are interested in qh occurrences
     * @param left  left = true, we test for quasi-periodic on the left, otherwise we test for quasi-periodic on the right
     * @return  true if the state was quasi-periodic, otherwise false
     */
    private Pair<L, Boolean> eliminateQuasiPeriodicity(
            final L ltw,
            final Q q,
            final SLP<N> h,
            final boolean left) {

        CFGOp<N> cfgOp = new CFGOp<>();
        SLPOp<N> slpOp = new SLPOp<>();

        boolean quasiPeriodic = false;
        if(left && h != null) {
            if(nonPeriodicStates.contains(q)) {
                return new Pair<>(ltw, false);
            }

            // we already calculated the period of q, therefore we only have to check if u is a prefix of the period or u and the period are of the same period
            if(periodicStates.containsKey(q)) {
                SLP<N> period = periodicStates.get(q);
                if(slpOp.isMultiPrefixOf(period, h, slpCreatorFactory)) {
                    quasiPeriodic = true;
                }
            }
        }

        Set<R> ltwRules = new HashSet<>(ltw.getRules());
        Set<Q> initialState = new HashSet<>(ltw.getInitialStates());



        logger.info(" we do some work here.");

        Set<R> productionsT = new HashSet<>();
        Set<R> productionsM = new HashSet<>();
        Set<R> productionsMWithAxioms = new HashSet<>();
        Set<R> productionsTWithAxioms = new HashSet<>();

        /**
         * The empty word to use in rules like u_0q_1q_2q_3...
         */
        SLP<N> emptyWordSLP = cfgCreator.emptyWord();

        /**
         * The function that maps: q -> q^e
         */
        Map<Q, Q> stateMap = new HashMap<>();

        /**
         * The functions that maps: q' -> s'(q, q') or s''(q, q') for all q' accessible from q.
         */
        Map<Q, Long> shifts = mockShift(q, ltw, left);

        /**
         * s'(qu, q') = s'(q, q') + |u| or s''(uq, q') = |u| + s''(q, q')
         */
        if(h != null) {
            SLP<N> hSlp = cfgOp.getMinimalWord(h.getProductions(), h.getAxiom(), false, cfgCreator);
            long hLength = hSlp.length();
            for(Q key : shifts.keySet()) {
                shifts.put(key, shifts.get(key) + hLength);
            }
        }

        /**
         * introduce a new state q^e for q
         */
        stateMap.put(q, ltwCreator.createFreshState());

        /**
         * introduce a new state q'^e for each q' accessible from q
         */
        for(Q p : shifts.keySet()) {
            // maybe q is accessible from q!
            if(!stateMap.containsKey(p)) {
                stateMap.put(p, ltwCreator.createFreshState());
            }
        }

        /**
         * get all rules that are accessible from q
         */
        Set<R> rules = ltwRules.stream().filter(r -> shifts.keySet().contains(r.getSrcState())).collect(Collectors.toSet());

        /**
         * compute w_q from the paper
         */
        SLP<N> w_q = cfgOp.getMinimalWord(cfg.getProductions(), ltwToCfgMap.get(q), true, cfgCreator);

        Q q0 = ltwCreator.createFreshState();

        /**
         * add a new rule containing q'^e described in the ltw-paper
         */
        for(R rule : rules) {
            SLP<N> u;
            /*if(cfgOp.isNullable(cfg.getProductions(), cfg.getAxioms())) {
                u = emptyWordSLP;
            }
            else {*/
                u = createEarliestWord(rule, shifts.get(rule.getSrcState()), left, cfg, new CFGCreatorFactory<>());
            //}

            // construct output words u, epsilon, epsilon, ... or ... epsilon, epsilon u
            int j = 0;
            if(!left) {
                j = rule.getOutputWords().size()-1;
            }
            List<SLP<N>> outputWords = new ArrayList<>();
            for(int i = 0; i < rule.getOutputWords().size(); i++) {
                if(i == j) {
                    outputWords.add(u);
                }
                else {
                    outputWords.add(emptyWordSLP);
                }
            }

            // create new rule q'e(f) -> ...
            R newRule = ltwCreator.createRule(
                    stateMap.get(rule.getSrcState()),
                    rule.getSymbol(),
                    rule.getDestStates().stream().map(p -> stateMap.get(p)).collect(Collectors.toList()),
                    outputWords,
                    rule.getInputPermutation());

            // add the new rule to M^q and the old one to T^q
            productionsM.add(newRule);
            productionsMWithAxioms.add(newRule);
            productionsT.add(rule);


            /**
             * this is for the axiom ax^e = w_q q^e since we use another definition of LTWs (i.e. we have initial states):
             *
             * we introduce a new state q_0 (the only initial state) and for each rule r: q(f) -> u_0 q_1(x_1) ... q_n(x_n) u_n
             * we take the corresponding new created rule q'e -> ... and copy this rule add but w_q or append w_q at the end.
             * This copied rule is a axiom rule! We have to do this to avoid cycles with w_q!
             */
            if(rule.getSrcState().equals(q)) {
                // copy output words
                List<SLP<N>> axiomOutputWords = newRule.getOutputWords().stream().map(slp -> cfgCreator.createSLP(cfgCreator.copyProductions(slp.getSLPProductions()), slp.getAxiom())).collect(Collectors.toList());
                if(left) {
                    int index = 0;
                    if(h == null) {
                        axiomOutputWords.set(index, slpOp.concatenate(w_q, axiomOutputWords.get(index), slpCreatorFactory));
                    }
                    else {
                        List<SLP<N>> words = new LinkedList<>();
                        words.add(w_q);
                        words.add(h);
                        words.add(axiomOutputWords.get(index));
                        axiomOutputWords.set(index, slpOp.concatenate(words, slpCreatorFactory));
                    }
                }
                else {
                    int index = axiomOutputWords.size()-1;
                    if(h == null) {
                        axiomOutputWords.set(index, slpOp.concatenate(axiomOutputWords.get(index), w_q, slpCreatorFactory));
                    }
                    else {
                        List<SLP<N>> words = new LinkedList<>();
                        words.add(axiomOutputWords.get(index));
                        words.add(h);
                        words.add(w_q);
                        axiomOutputWords.set(index, slpOp.concatenate(words, slpCreatorFactory));
                    }
                }

                R axiomRule = ltwCreator.createRule(
                        q0,
                        newRule.getSymbol(),
                        newRule.getDestStates().stream().collect(Collectors.toList()),
                        axiomOutputWords,
                        newRule.getInputPermutation());

                // don't take these rules into the new ltw if q is indeed quasi-periodic.
                productionsMWithAxioms.add(axiomRule);
            }
        }


        // the axiom of T^{qu} is q(x_i)u, otherwise T^q is just the LTW with initial state q.
        L ltwT_q;
        if(h != null) {
            Q initialStateT = ltwCreator.createFreshState();
            for(R rule : rules) {
                if(rule.getSrcState().equals(q)) {
                    List<SLP<N>> axiomOutputWords = rule.getOutputWords().stream().map(slp -> cfgCreator.createSLP(cfgCreator.copyProductions(slp.getSLPProductions()), slp.getAxiom())).collect(Collectors.toList());
                    axiomOutputWords.set(axiomOutputWords.size() - 1, slpOp.concatenate(axiomOutputWords.get(axiomOutputWords.size()-1), h, slpCreatorFactory));

                    R axiomRule = ltwCreator.createRule(
                            initialStateT,
                            rule.getSymbol(),
                            rule.getDestStates().stream().collect(Collectors.toList()),
                            axiomOutputWords,
                            rule.getInputPermutation());

                    productionsT.add(axiomRule);
                }
            }

            ltwT_q = ltwCreator.createLTW(productionsT, initialStateT);
        }
        else {
            ltwT_q = ltwCreator.createLTW(productionsT, q);
        }


        //Set<R> allNewRules = new HashSet<>(ltwRules);
        //allNewRules.addAll(productionsT);
        L ltwM_q = ltwCreator.createLTW(productionsM, stateMap.get(q));

        assert genLTWOp.isEqualLTWDomain(ltwT_q, ltwM_q, ltwCreator);


        // test if M_q == T_q, if so replace q by lcp(q)stateMap.get(q) but only if the quasi periodic test does not fail, otherwise undo everything!
        if(quasiPeriodic || isPeriodic(ltwM_q, stateMap.get(q))) {
            logger.info("L_" + q0 + "/" + q + " is peridic.");
            ltwM_q = ltwCreator.createLTW(productionsMWithAxioms, q0);

            // if this is the case q is quasi-periodic
            if(quasiPeriodic || isEqualSTWImage(ltwM_q, ltwT_q)) {
                logger.info("M_q equals T_q => " + q + " is quasi-periodic on the " + (left ? "left" : "right"));

                // remember the period of the states
                SLP<N> period = getPeriod(ltwM_q, stateMap.get(q));
                for(Q p : stateMap.values()) {
                    periodicStates.put(p, period);
                }


                // remove all old accessibles
                //ltwRules.removeAll(productionsT);
                //states.removeAll(shifts.keySet());

                // calculate should be lcp(q) or lcp(qu) or lcs(q) or lcs(qu)
                SLP<N> lcpQ = cfgOp.getMinimalWord(cfg.getProductions(), ltwToCfgMap.get(q), true, cfgCreator);
                if(h != null) {
                    lcpQ = slpOp.concatenate(lcpQ, h, slpCreatorFactory);
                    /*if(left) {
                        lcpQ = slpOp.concatenate(lcpQ, h, slpCreatorFactory);
                    }
                    else {
                        lcpQ = slpOp.concatenate(h, lcpQ, slpCreatorFactory);
                    }*/
                }

                /**
                 * Replace each occurrence of q(x_i) or q(x_i)u by lcp(q)q^e(x_i) or lcp(qu)q^e(x_i) or q^e(x_i)lcs(q) or q^e(x_i)lcs(qu)
                 */
                for(R rule : ltwRules) {
                    for(int i = 0; i < rule.getDestStates().size(); i++) {
                        if(rule.getDestStates().get(i).equals(q)) {
                            if(left) {
                                // we spot q^e(x_i)u and replace this by lcp(qu)q^e(x_i)
                                if(h != null && slpOp.equals(rule.getOutputWords().get(i+1), h, slpCreatorFactory, true, false)) {
                                    SLP<N> oldWord = rule.getOutputWords().get(i);
                                    SLP<N> newWord = slpOp.concatenate(oldWord, lcpQ, slpCreatorFactory);
                                    // replace q(x_i)u by lcp(qu)q^e(x_i)
                                    rule.getDestStates().set(i, stateMap.get(q));
                                    rule.getOutputWords().set(i, newWord);
                                    rule.getOutputWords().set(i+1, cfgCreator.emptyWord());
                                }
                                else {
                                    SLP<N> oldWord = rule.getOutputWords().get(i);
                                    SLP<N> newWord = slpOp.concatenate(oldWord, lcpQ, slpCreatorFactory);
                                    // replace q(x_i) by lcp(q)q^e(x_i)
                                    rule.getDestStates().set(i, stateMap.get(q));
                                    rule.getOutputWords().set(i, newWord);
                                }
                            }
                            else {
                                // we spot q(x_i)u and replace this by q^e(x_i)lcs(qu)
                                if(h != null && slpOp.equals(rule.getOutputWords().get(i+1), h, slpCreatorFactory, true, false)) {
                                    // replace q(x_i)u by q^e(x_i)lcp(qu)
                                    rule.getDestStates().set(i, stateMap.get(q));
                                    rule.getOutputWords().set(i+1, lcpQ);
                                }
                                else {
                                    SLP<N> oldWord = rule.getOutputWords().get(i+1);
                                    SLP<N> newWord = slpOp.concatenate(lcpQ, oldWord, slpCreatorFactory);

                                    // replace q(x_i) by q^e(x_i)lcs(q)
                                    rule.getDestStates().set(i, stateMap.get(q));
                                    rule.getOutputWords().set(i+1, newWord);
                                }
                            }
                        }
                    }
                }

                // add new accessibles
                ltwRules.addAll(productionsM);

                // we replace q, so we can delete all rules of the form q(f) -> ...
                Set<R> newLtwRules = new HashSet<>(ltwRules);
                /*for(R rule : ltwRules) {
                    if(!rule.getSrcState().equals(q)) {
                        newLtwRules.add(rule);
                    }
                }*/

                //shifts.keySet().forEach(oldQ -> states.add(stateMap.get(oldQ)));

                // remove q, since q is already fixed!
                //states.remove(stateMap.get(q));

                // replace old initial states
                Set<Q> newInitialStates = new HashSet<>();
                Set<Q> replacedStates = shifts.keySet();
                for(Q init : initialState) {
                    if(replacedStates.contains(init)) {
                        newInitialStates.add(stateMap.get(init));
                    }
                    else {
                        newInitialStates.add(init);
                    }
                }

                initialState = newInitialStates;

                // filter all non-reachables
                L resultLTW = ltwCreator.createLTW(newLtwRules, initialState);
                Set<Q> reachables = genLTWOp.getReachable(resultLTW);
                newLtwRules = resultLTW.getRules().stream().filter(rule -> reachables.contains(rule.getSrcState())).collect(Collectors.toSet());
                resultLTW = ltwCreator.createLTW(newLtwRules, initialState);
                logger.info(resultLTW);

                // create grammar for all L_q again.
                return new Pair<>(resultLTW, true);
            }
            else {
                logger.info("M_q not equals T_q => " + q + " is not quasi-periodic on the " + (left ? "left" : "right"));
                return new Pair<>(ltw, false);
            }
        }
        else {
            logger.info("L_"+q0+"/"+ q +" is not periodic");
            return new Pair<>(ltw, false);
        }
    }

    /**
     * Computes the period of a state if the state is periodic otherwise the result is invalid.
     *
     * Requirement: the state has to be periodic
     *
     * @param ltw   the LTW
     * @param q     the state of the LTW that is periodic
     * @return the period of a state
     */
    protected SLP<N> getPeriod(final L ltw, final Q q) {
        SLPOp<N> slpOp = new SLPOp<>();
        CFGOp<N> cfgOp = new CFGOp<>();
        Map<Q, IJezSymbol<N>> ltwToCfg = new HashMap<>();
        CFG<N> tmpCFG = genLTWOp.toCFG(ltw, ltwToCfg, cfgCreator);
        CFG<N> qCFG = cfgCreator.copy(cfgCreator.createCFG(tmpCFG.getProductions(), ltwToCfg.get(q)));
        Optional<SLP<N>> optionalSlp = cfgOp.getMinimalNonEmptyWord(qCFG.getProductions(), ltwToCfg.get(q), cfgCreator);
        return optionalSlp.orElse(cfgCreator.emptyWord());
    }

    /**
     * Tests whether the state q of the LTW is periodic.
     *
     * @param ltw   the LTW
     * @param q     the state we test
     * @return true => the q is periodic, otherwise false
     */
    protected boolean isPeriodic(final L ltw, final Q q) {
        SLPOp<N> slpOp = new SLPOp<>();
        CFGOp<N> cfgOp = new CFGOp<>();
        Map<Q, IJezSymbol<N>> ltwToCfg = new HashMap<>();
        CFG<N> tmpCFG = genLTWOp.toCFG(ltw, ltwToCfg, cfgCreator);
        CFG<N> qCFG = cfgCreator.copy(cfgCreator.createCFG(tmpCFG.getProductions(), ltwToCfg.get(q)));
        Optional<SLP<N>> optionalSlp = cfgOp.getMinimalNonEmptyWord(qCFG.getProductions(), ltwToCfg.get(q), cfgCreator);

        // there is a shortest non-emoty word
        if(optionalSlp.isPresent()) {
            SLP<N> aSlp = optionalSlp.get();
            return slpOp.hasSamePeriod(qCFG, aSlp, cfgCreator, slpCreatorFactory);
        } // the language of the cfg does only generate the empty word and therefore the language is trivial periodic
        else {
            return true;
        }
    }

    /**
     * Calculates all shifts s(q', q_x) where q_x is accessible from q'.
     *
     * @param state the state q'
     * @param ltw   the LTW
     * @param left  left = true => compute the left shift, otherwise compute teh right shift
     * @return all shifts s(q', q_x) where q_x is accessible from q'.
     */
    private Map<Q, Long> mockShift(final Q state, final L ltw, final boolean left) {
        Map<Q, Long> accessibleShifts = new HashMap<>();
        Set<Q> added = new HashSet<>();
        added.add(state);
        //s(q,q) = 0
        accessibleShifts.put(state, 0L);
        Map<Q, List<R>> ruleMap = ltw.getRulesBySrcState();

        while (!added.isEmpty()) {
            HashSet newAdded = new HashSet();
            for (Q q : added) {
                List<R> rules = ruleMap.get(q);
                logger.info("mock Shift: " + q);
                for (R r : rules) {
                    // compute all s'(q, q_i) or s''(q, q_i) for r
                    List<Long> mockShifts = mockShift(r, left);
                    for (int i = 0; i < mockShifts.size(); i++) {
                        long minShift = Long.MAX_VALUE;
                        if (accessibleShifts.containsKey(r.getDestStates().get(i))) {
                            minShift = accessibleShifts.get(r.getDestStates().get(i));
                        } else {
                            newAdded.add(r.getDestStates().get(i));
                        }

                        if (state.equals(r.getDestStates().get(i))) {
                            // s(q, q) = 0
                            accessibleShifts.put(r.getDestStates().get(i), 0L);
                        } // s(q, q_i) for r := q(f) -> u_0 q_1(x_1) ... q_n(x_n) s(q, q_i) = |u_i lcp(q_{+1}) ... u_n|
                        else if (state.equals(r.getSrcState())) {
                            accessibleShifts.put(r.getDestStates().get(i), Math.min(minShift, mockShifts.get(i)));
                        } else {
                            // s(q_1, q_3) = s(q_1, q_2) + s(q_2, q_3)
                            accessibleShifts.put(r.getDestStates().get(i), Math.min(minShift, mockShifts.get(i) + accessibleShifts.get(r.getSrcState())));
                        }
                    }
                }
            }
            added = newAdded;
        }

        return accessibleShifts;
    }

    /**
     * let the rule be r: q,f -> u_0 q_1 ... q_n u_n, then this method calculates all shifts
     * s'(q, q_i) or s''(q, q_i) for all i in [n].
     *
     * @param rule  a rule of the LTW
     * @param left  true => we assume quasi-periodicity on the left, otherwise on the right
     * @return a function i -> s'(q, q_i) or i -> s''(q, q_i)
     */
    private List<Long> mockShift(final R rule, final boolean left) {
        CFGOp<N> cfgOp = new CFGOp<>();
        Q srcState = rule.getSrcState();
        int n = rule.getDestStates().size();
        List<Long> shifts = new ArrayList<>(n);
        List<Long> wordLengths = new ArrayList<>(n+1);
        List<Long> stateLengths = new ArrayList<>(n);

        // compute the smallest words of L_{q_i}
        for(int i = 0; i < n; i++) {
            Q q = rule.getDestStates().get(i);

            // s'(q, q) = 0
            if(q.equals(rule.getSrcState())) {
                stateLengths.add(0L);
            }
            else {
                IJezSymbol<N> axiom = ltwToCfgMap.get(q);
                SLP<N> shortestWord = cfgOp.getMinimalWord(cfg.getProductions(), axiom, true, cfgCreator);
                stateLengths.add(shortestWord.length());
            }

        }

        // compute all |u_i|
        for(int i = 0; i < n+1; i++) {
            SLP<N> u = rule.getOutputWords().get(i);
            wordLengths.add(u.length());
        }

        for(int i = 0; i < n; i++) {
            long sum = 0;
            if(left) {
                for(int j = i+1; j < n; j++) {
                    sum +=  wordLengths.get(j);
                    sum += stateLengths.get(j);
                }
                sum += wordLengths.get(n);

            }
            else {
                sum += wordLengths.get(i);
                for(int j = i-1; j >= 0; j--) {
                    sum +=  wordLengths.get(j);
                    sum += stateLengths.get(j);
                }
            }
            // s'(q, q) = 0
            if(rule.getDestStates().get(i).equals(rule.getSrcState())) {
                shifts.add(0L);
            }
            else {
                shifts.add(sum);
            }
        }

        return shifts;
    }

    /**
     * Creates the word u from a production p as follows, here we assume left == true:
     *
     * let p := X -> u_0 X_1 u_1 ... u_{n-1} X_n u_n where all u_i and X_i are non-terminals.
     * Each u_i represents a output word of a rule of an ltw in a SLP representation.
     * u := shift_shift(short(X)^{-1}u_0 short(X_i) u_1 ... u_{n-1} short(X_n)), where short(X_i) is the shortest word
     * generated by X_i.
     * so first we delete the prefix of short(X)^{-1} from u_0 short(X_i) u_1 ... u_{n-1} short(X_n)
     * and after that we shift to the left by shift.
     *
     * In case left == false we delete the suffix short(X)^{-1} from u_0 short(X_i) u_1 ... u_{n-1} short(X_n)
     * and after that we shift to the right by shift.
     *
     * Constrains: We assume that each SLP for each word u_i has distinct non-terminal symbols.
     *
     * @param shift         the amount of shift to the left or right
     * @param left          true => we shift to the left, otherwise we shift to the right
     * @param cfg           the cfg representing the output language of the ltw
     * @return an earliest word u that deletes quasi-periodicity.
     */
    private SLP<N> createEarliestWord(
            final R rule,
            final long shift,
            final boolean left,
            final CFG<N> cfg,
            final CFGCreatorFactory<N> factory) {

        CFGOp<N> cfgOp = new CFGOp<>();
        SLPOp<N> slpOp = new SLPOp();

        IJezSymbol<N> nonTerminal = ltwToCfgMap.get(rule.getSrcState());
        List<SLP<N>> shortestWord = new LinkedList<>();
        for(int i = 0; i < rule.getDestStates().size(); i++) {
            // u_i
            SLP<N> wordSLP = rule.getOutputWords().get(i);

            // w_i
            SLP<N> stateSLP = cfgOp.getMinimalWord(cfg.getProductions(), ltwToCfgMap.get(rule.getDestStates().get(i)), true, cfgCreator);

            // add u_iw_i
            shortestWord.add(wordSLP);
            shortestWord.add(stateSLP);
        }
        // add u_n
        shortestWord.add(rule.getOutputWords().get(rule.getDestStates().size()));

        // this should be u = u_0lcp(q_1)...lcp(q_n)u_n or u_0lcs(q_1)...lcs(q_n)u_n if the state was quasi-periodic
        SLP<N> u = slpOp.concatenate(shortestWord, factory);

        // compute |w'|
        SLP<N> w = cfgOp.getMinimalWord(cfg.getProductions(), nonTerminal, false, cfgCreator);
        long length = w.length();

        //compute u': delete the prefix |w'| or the suffix from u' (depends on left == true or left == false)
        SLP<N> uPrefixDeleted = slpOp.delete(u, length, left, cfgCreator);

        // finally shift u' by s'(q, q') or s''(q, q'') (depends on left == true or left == false)
        SLP<N> uShifted = slpOp.shift(uPrefixDeleted, shift, left, cfgCreator);

        return uShifted;
    }

}
