package N2W.gen;

import N2W.inter.IN2WRule;
import N2W.inter.IN2WTransducer;
import N2W.inter.INestedLetter;
import grammar.gen.GenCFGOp;
import grammar.inter.*;
import morphismEq.morphisms.IMorphism;
import morphismEq.gen.GenMorphismEQSolver;
import symbol.IJezSymbol;
import symbol.INamedSymbol;
import utils.Pair;
import utils.Quadrupel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benedikt Zoennchen
 */
public abstract class GenN2WtoCFGReduction
        <I, N, A, B, C,
                Q extends INamedSymbol<A>,
                W extends INestedLetter<B>,
                G extends INamedSymbol<C>,
                R extends IN2WRule<N, A, B, C, Q, W, G>,
                M extends IN2WTransducer<N, A, B, C, Q, W, G, R>,

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
                >
{

    private final ICFGCreatorFactory<Object, S3, W3, P3, C3, Z3> cfgFactoryCreator;
    private static Logger logger = LogManager.getLogger(GenN2WtoCFGReduction.class);

    public GenN2WtoCFGReduction(final ICFGCreatorFactory<Object, S3, W3, P3, C3, Z3> factory) {
        this.cfgFactoryCreator = factory;
    }

    /**
     * Test if two nested word to word transducers are equals. So they have the equal domain and
     * for each nested word in the domain they generate the same word.
     *
     * @param n2wTransducer1
     * @param n2wTransducer2
     * @return
     */
    public boolean isEquals(final M n2wTransducer1,
                            final M n2wTransducer2,
                            final IMorphism<Z3, Z4> morphism1,
                            final IMorphism<Z3, Z4> morphism2,
                            final ICFGCreatorFactory<I, S4, W4, P4, C4, Z4> factory,
                            final ICFGCreatorFactory<Object, S3, W3, P3, C3, Z3> baseFactory) {
        /**
         * 1. Create cfg in cnf with terminals (r1,r2) and non-terminals (q1, p1, q2, p2)
         */
        logger.info("start transformation N2W into CFG");
        C3 cfg = N2WtoCFG(n2wTransducer1, n2wTransducer2);
        logger.info("transformation N2W into CFG finished");

        /**
         * 2. Make sure that all non-terminals are in an intervall [0; |N|-1]
         */
        ICFGCreator<Object, S3, W3, P3, C3, Z3> cfgCreator = baseFactory.create();
        cfg = cfgCreator.resetNonTerminals(cfg);

        GenMorphismEQSolver<Object, S3, W3, P3, C3, Z3, I, S4, W4, P4, C4, Z4> linGrammer = new GenMorphismEQSolver<>(cfg, cfgCreator, factory, baseFactory);

        return linGrammer.equivalentOnMorphisms(morphism1, morphism2);
        /*GenGrammarGraph<Object, I, S3, W3, P3, C3, Z3, S4, W4, P4, C4, Z4> grammarGraph = createInstance(n2wTransducer1, n2wTransducer2, cfgCreator);
        boolean result = grammarGraph.nonMatching((s1, s2) -> !slpOp.equals(s1, s2, factory, false, true));
        return result;*/
    }

    /**
     * Converts two top-down deterministic nested word to word transducer T1 and T2 into a context free grammar G and
     * two morphisms M1 and M2 such that L(G) is the set of all successful parallel runs of T1 and T2.
     * T1 is equivalent to T2 iff M1 is equivalent to M2 on G.
     *
     * Requirements:
     * + T1 and T2 have to be top-down deterministic
     * + T1 and T2 have to have the same alphabet
     * + T1 and T2 have to have the same domain
     *
     * Algorithm:
     *
     *
     * @param n2wTransducer1    T1
     * @param n2wTransducer2    T2
     * @return the tripel (G, M1, M2)
     */
    public C3 N2WtoCFG(
            final M n2wTransducer1,
            final M n2wTransducer2) {
        GenCFGOp<Object, S3, W3, P3, C3, Z3> genCFGOp = new GenCFGOp<>();
        GenN2WOp<N, A, B, C, Q, W , G, R, M> n2wOp = new GenN2WOp<>();
        Set<P3> productions = new HashSet<>();
        Set<S3> axioms = new HashSet<>();
        ICFGCreator<Object, S3, W3, P3, C3, Z3> cfgCreator = cfgFactoryCreator.create();
        S3 initialState = cfgCreator.createFreshNonTerminal();
        axioms.add(initialState);

        Set<R> openingRule1 = n2wTransducer1.getOpeningRules();
        Set<R> openingRule2 = n2wTransducer2.getOpeningRules();
        Set<R> closingRule1 = n2wTransducer1.getClosingRules();
        Set<R> closingRule2 = n2wTransducer2.getClosingRules();

        Set<Q> initialStates1 = n2wTransducer1.getInitialStates();
        Set<Q> initialStates2 = n2wTransducer2.getInitialStates();

        Set<Q> finalStates1 = n2wTransducer1.getFinalStates();
        Set<Q> finalStates2 = n2wTransducer2.getFinalStates();

        Set<Q> states1 = n2wTransducer1.getStates();
        Set<Q> states2 = n2wTransducer2.getStates();


        /**
         * (1) axiom rules
         */
        Map<Pair<B, G>, List<R>> grpedOpeningRules1 = openingRule1.stream().filter(r -> r.getNestedWord().isOpening()).collect(Collectors.groupingBy(r -> new Pair<B, G>(r.getNestedWord().getElement(), r.getStackSymbol())));
        Map<B, List<R>> grpedOpeningRules2 = openingRule2.stream().filter(r -> r.getNestedWord().isOpening()).collect(Collectors.groupingBy(r -> r.getNestedWord().getElement()));

        Map<Pair<B, G>, List<R>> grpedClosingRules1 = closingRule1.stream().filter(r -> r.getNestedWord().isClosing()).collect(Collectors.groupingBy(r -> new Pair<B, G>(r.getNestedWord().getElement(), r.getStackSymbol())));
        Map<Pair<B, G>, List<R>> grpedClosingRules2 = closingRule2.stream().filter(r -> r.getNestedWord().isClosing()).collect(Collectors.groupingBy(r -> new Pair<B, G>(r.getNestedWord().getElement(), r.getStackSymbol())));


        closingRule1.stream().filter(r -> finalStates1.contains(r.getEndState()));
        closingRule2.stream().filter(r -> finalStates2.contains(r.getEndState()));
        Set<S3> nonTerminals = new HashSet<>();
        Set<Quadrupel<Q, Q, Q, Q>> quadrupels = new HashSet<>();

        Set<Pair<Q,Q>> coreachables = n2wOp.getCoreachableStates(n2wTransducer1, n2wTransducer2);

        logger.info("start creation of productions containing terminals");
        for(Pair<B, G> pair : grpedOpeningRules1.keySet()) {
            List<R> opening1Rules = grpedOpeningRules1.get(pair);
            if(grpedClosingRules1.containsKey(pair)) {
                List<R> closing1Rules = grpedClosingRules1.get(pair);

                for(R opR1 : opening1Rules) {
                    for(R clR1 : closing1Rules) {
                        if(grpedOpeningRules2.containsKey(pair.a)) {
                            for(R opR2 : grpedOpeningRules2.get(pair.a)) {
                                if(grpedClosingRules2.containsKey(new Pair<>(pair.a, opR2.getStackSymbol()))) {
                                    for(R clR2 : grpedClosingRules2.get(new Pair<>(pair.a, opR2.getStackSymbol()))) {

                                        // changed!
                                        if(coreachables.contains(new Pair<>(opR1.getEndState(), opR2.getEndState()))
                                                && coreachables.contains(new Pair<>(clR1.getStartState(), clR2.getStartState()))){

                                            Quadrupel<Q, Q, Q, Q> quadrupel = new Quadrupel<>(opR1.getEndState(), clR1.getStartState(), opR2.getEndState(), clR2.getStartState());
                                            S3 t1 = cfgCreator.lookupSymbol(new Pair<R, R>(opR1, opR2), true);
                                            S3 nt = cfgCreator.lookupSymbol(quadrupel, false);
                                            S3 t2 = cfgCreator.lookupSymbol(new Pair<R, R>(clR1, clR2), true);

                                            List<S3> word = new LinkedList<>();
                                            word.add(t1);
                                            word.add(nt);
                                            word.add(t2);

                                            if(initialStates1.contains(opR1.getStartState()) && finalStates1.contains(clR1.getEndState())
                                                    && initialStates2.contains(opR2.getStartState()) && finalStates2.contains(clR2.getEndState())) {
                                                productions.add(cfgCreator.createProduction(initialState, cfgCreator.createWord(word)));
                                            }

                                            // changed!
                                            if(coreachables.contains(new Pair<>(opR1.getStartState(), opR2.getStartState()))
                                                    && coreachables.contains(new Pair<>(clR1.getEndState(), clR2.getEndState()))){

                                                S3 left = cfgCreator.lookupSymbol(new Quadrupel<>(opR1.getStartState(), clR1.getEndState(), opR2.getStartState(), clR2.getEndState()), false);
                                                productions.add(cfgCreator.createProduction(left, cfgCreator.createWord(word)));

                                                nonTerminals.add(nt);
                                                nonTerminals.add(left);
                                                quadrupels.add(quadrupel);

                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        logger.info("creation of productions containing terminals finished");

        //productions = genCFGOp.eliminateUnreachables(productions, axioms);

        logger.info("start creation of 'between productions'");

        for(Pair<Q, Q> coreach1 : coreachables) {
            for(Pair<Q, Q> coreach2 : coreachables) {
                for(Pair<Q, Q> coreach3 : coreachables) {
                    Q p1 = coreach1.a;
                    Q p2 = coreach1.b;

                    Q q1 = coreach2.a;
                    Q q2 = coreach2.b;

                    Q p1_ = coreach3.a;
                    Q p2_ = coreach3.b;

                    S3 left = cfgCreator.lookupSymbol(new Quadrupel<>(p1, q1, p2, q2), false);
                    S3 nt1 = cfgCreator.lookupSymbol(new Quadrupel<>(p1, p1_, p2, p2_), false);
                    S3 nt2 = cfgCreator.lookupSymbol(new Quadrupel<>(p1_, q1, p2_, q2), false);
                    List<S3> word = new LinkedList<>();
                    word.add(nt1);
                    word.add(nt2);
                    productions.add(cfgCreator.createProduction(left, cfgCreator.createWord(word)));
                }
            }
        }
        logger.info("creation of 'between productions' finished");


        for(Q q1 : states1) {
            for(Q q2 : states2) {
                S3 left = cfgCreator.lookupSymbol(new Quadrupel<>(q1, q1, q2, q2), false);
                productions.add(cfgCreator.createProduction(left, cfgCreator.createWord(new LinkedList<>())));
            }
        }

        logger.info("start transformation of G into wCNF");
        productions = genCFGOp.toWeakCNF(productions, axioms, cfgCreator);
        logger.info("transformation of G into wCNF finished");
        ICFGCreator<Object, S3, W3, P3, C3, Z3> creator = cfgFactoryCreator.create();
        C3 cfg = creator.createCFG(productions, axioms);
        return cfg;
    }



    /**
     * Transforms a base SLP that represents the shortest word of the CFG that was created by
     * problem reduction. This has to be implemented by the non-generic version of this class
     * since the type <I> can be everything! It can be a simple Character or a whole SLP.
     *
     * @param baseSLPProductions    the productions of the base slp
     * @param axioms                the axioms of the base slp
     * @param chooser               if the morphism is M1 then the chooser takes pair.a, otherwise pair.b
     * @param slpCreator            the creator for the SLP that represents the shortest word of the CFG
     * @return
     */
    /*public abstract Pair<Z4, Map<S3, S4>> applyMorphism(
            final Set<P3> baseSLPProductions,
            final Set<S3> axioms,
            final Function<Pair<R, R>, R> chooser,
            final ICFGCreator<I, S4, W4, P4, C4, Z4> slpCreator);*/
}
