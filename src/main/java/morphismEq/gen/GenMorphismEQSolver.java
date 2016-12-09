package morphismEq.gen;

import data.MyLinkedList;
import data.Node;
import grammar.gen.GenCFGOp;
import grammar.inter.*;
import morphismEq.PlandowskiPath;
import morphismEq.SpanningTree;
import morphismEq.edges.*;
import grammar.gen.GenSLPOp;
import morphismEq.Path;
import morphismEq.TreeNode;
import morphismEq.morphisms.IMorphism;
import symbol.IJezSymbol;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A generic implementation of the Solver for the equivalence problem on CFGs G. The morphism goes from N1 -> N2^*.
 * The solver generates words of the linear grammar of G. A word is a list of SLPs of type N1. These SLPs will be
 * morphed by the morphisms to SLPs of type N2.
 *
 * Requirement: The grammar has to be in weak Chomsky normal form.
 *
 * @author Benedikt Zoennchen
 *
 * These types are the unmorphed types.
 * @param <N1> the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S1> the type of the terminal and non-terminal symbols of the grammar
 * @param <W1> the type of the right-hand side of the grammar
 * @param <P1> the type of the grammar production
 * @param <C1> the type of the CFG
 * @param <Z1> the type of the SLP
 *
 * These types are the morphed types.
 * @param <N2> the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S2> the type of the terminal and non-terminal symbols of the grammar
 * @param <W2> the type of the right-hand side of the grammar
 * @param <P2> the type of the grammar production
 * @param <C2> the type of the CFG
 * @param <Z2> the type of the SLP
 */
public class GenMorphismEQSolver<
        N1, S1 extends IJezSymbol<N1>, W1 extends IReferencedWord<N1, S1>, P1 extends IProduction<N1, S1, W1>, C1 extends ICFG<N1, S1, W1, P1>, Z1 extends ISLP<N1, S1, W1, P1>,
        N2, S2 extends IJezSymbol<N2>, W2 extends IReferencedWord<N2, S2>, P2 extends IProduction<N2, S2, W2>, C2 extends ICFG<N2, S2, W2, P2>, Z2 extends ISLP<N2, S2, W2, P2>> {

    private static Logger logger = LogManager.getLogger(GenMorphismEQSolver.class);

    /**
     * G in weak Chomsky normal form
     */
    private C1 baseCFG;

    /**
     * Object for operations on the CFG of type N1.
     */
    private GenCFGOp<N1, S1, W1, P1 ,C1, Z1> cfgOp;

    /**
     * The creator that creates the CFG G.
     */
    private final ICFGCreator<N1, S1, W1, P1, C1, Z1> cfgCreator;

    /**
     * A creator factory for the creation of SLPs of type N2
     */
    private final ICFGCreatorFactory<N2, S2, W2, P2, C2, Z2> destSLPCreatorFactory;

    /**
     * A creator factory for the creation of SLPs of type N1
     */
    private final ICFGCreatorFactory<N1, S1, W1, P1, C1, Z1> srcSLPCreatorFactory;

    //private Set<P1> cfgProdutions;

    /**
     * A matrix representing parts of the linear graph i.e. we do not consider multi edges.
     * The matrix is used to create all trees of all non-terminals.
     */
    private IGrammarEdge<S1>[][] adjazenzmatrix;

    /**
     * All non-terminals that generate only one word.
     */
    private Set<S1> singletons;

    /**
     * The special non-terminal representing the sink T of the graph.
     */
    private S1 terminalNode;

    /**
     * A placeholder for the concatination of non-terminals
     */
    private S1 placeholder;

    /**
     * Number of nodes in the linear grammar graph
     */
    private int numberOfNodes;

    /**
     * Set of non-terminals of G including the sink T.
     */
    private Set<S1> nonTerminals;

    /**
     * All trees of non-terminals.
     */
    private SpanningTree<S1>[] trees;

    /**
     * All edges of G.
     */
    private Map<S1, List<IGrammarEdge<S1>>> multiEdges;

    /**
     * Object for operations on SLPs.
     */
    private final GenSLPOp<N2, S2, W2, P2, C2, Z2> slpOp;

    /**
     * All shortest words of a non-terminal represented by SLPs.
     */
    private Map<S1, Z1> shortestWords;

    /**
     * The empty word of type N2.
     */
    private Z2 emptyWord;

    /**
     * Default constructor of the solver.
     *
     * @param cfg                   the grammar G.
     * @param cfgCreator            the creator of G.
     * @param destSLPCreatorFactory factory for creators of type N2, for the creation of SLPs
     * @param slpFactoryCreator     factory for creators of type N1, for the creation of SLPs
     */
    public GenMorphismEQSolver(
            final C1 cfg,
            final ICFGCreator<N1, S1, W1, P1, C1, Z1> cfgCreator,
            final ICFGCreatorFactory<N2, S2, W2, P2, C2, Z2> destSLPCreatorFactory,
            final ICFGCreatorFactory<N1, S1, W1, P1, C1, Z1> slpFactoryCreator) {
        this.cfgCreator = cfgCreator;
        this.destSLPCreatorFactory = destSLPCreatorFactory;
        this.srcSLPCreatorFactory = slpFactoryCreator;
        this.emptyWord = destSLPCreatorFactory.create().emptyWord();
        this.cfgOp = new GenCFGOp<>();
        this.slpOp = new GenSLPOp<>();
        this.baseCFG = cfg;
        this.terminalNode = cfgCreator.createFreshNonTerminal();
        this.placeholder = cfgCreator.createFreshNonTerminal();
        this.numberOfNodes = cfgCreator.getSymbolFactory().getMaxNonterminal()+1;
        this.adjazenzmatrix = new IGrammarEdge[numberOfNodes][numberOfNodes];
        this.multiEdges = new HashMap<>();
        this.trees = new SpanningTree[numberOfNodes];
        this.nonTerminals = new HashSet<>(baseCFG.getNonTerminals());
        this.nonTerminals.add(terminalNode);
        this.init();
    }

    public boolean equivalentOnMorphisms(final IMorphism<Z1, Z2> morphism1, final IMorphism<Z1, Z2> morphism2) {
        logger.info(
                "start test set generation\n" +
                "#productions: "+baseCFG.getProductions().size()+"\n" +
                "#singletons:  "+baseCFG.getProductions().stream().filter(p-> singletons.contains(p.getLeft())).count());
        boolean result = equals(morphism1, morphism2);
        if(result) {
            logger.info("morphisms agree on the CFG.");
        }
        return result;
    }

    private boolean equals(final IMorphism<Z1, Z2> morphism1, final IMorphism<Z1, Z2> morphism2) {
        return equals(new PlandowskiPath(), morphism1, morphism2);
    }

    /**
     * Pseudo-Code
     *
     * check(Path path, int dept)
     * foreach Node v in Tree(path.getLastNode())
     *      path := getPath(S,v);
     *
     *      foreach neighbour u of v
     *          path.append((v,u))
     *          if(isValid(path))
     *              if(!check(path, u, dept+1))
     *                  return false;
     *
     * return true;
     *
     */
    private boolean equals(final PlandowskiPath<S1> plandowskiPath, final IMorphism<Z1, Z2> morphism1, final IMorphism<Z1, Z2> morphism2) {
        if(plandowskiPath.length() < 6) {
            SpanningTree<S1> tree = plandowskiPath.isEmpty() ? trees[baseCFG.getAxiom().getId()] : trees[plandowskiPath.tail().getId()];

            for(Path<S1> path : tree) {

                // 1. go inside the tree (u,v), maybe go zero steps
                plandowskiPath.addTreePath(path);

                // get v
                S1 node = path.isEmpty() ? tree.getRoot().get() : path.tail();
                int nodeId = node.getId();

                if(multiEdges.containsKey(node)) {
                    for(IGrammarEdge<S1> edge : multiEdges.get(node)) {
                        S1 neighbour = edge.getEnd();
                        int neighbourId = edge.getEnd().getId();

                        Path finalPath = new Path();
                        if(!neighbour.equals(terminalNode)) {
                            finalPath.addEdge(adjazenzmatrix[neighbourId][terminalNode.getId()]);
                        }

                        // 2. add edge
                        plandowskiPath.addEdges(adjazenzmatrix[nodeId][neighbourId]);

                        // 3. add tree
                        plandowskiPath.addTreePath(finalPath);

                        Z2 word1 = generateSLP(plandowskiPath, morphism1);
                        Z2 word2 = generateSLP(plandowskiPath, morphism2);

                        //logger.debug(plandowskiPath);
                        //logger.debug(plandowskiPath.toStringPlandowskiEdges());
                        //logger.debug(word1);
                        //logger.debug(word2);

                        boolean test = slpOp.equals(word1, word2, destSLPCreatorFactory, true, false);
                        if(!test) {
                            logger.debug(word1 + "!=" + word2);
                        }

                        // 3. remove tree
                        plandowskiPath.removeLastTreePath();

                        // go only deeper if the last plandowskiy edge do not end at the terminal node T!
                        if(!neighbour.equals(terminalNode)) {
                            // words are equals, go to the next word
                            test = test && equals(plandowskiPath, morphism1, morphism2);
                        }

                        if(!test) {
                            return test;
                        }

                        // 2. remove edge
                        plandowskiPath.removeLastEdge();
                    }
                }

                // 1. remove tree
                plandowskiPath.removeLastTreePath();
            }
        }
        return true;
    }

    /**
     * Transforms a Plandowski-path into a morphed word.
     *
     * @param plandowskiPath    the Plandowski-path
     * @param morphism          the morphism defines the morphed word
     * @return a morphed word represented by a SLP
     */
    private Z2 generateSLP(final PlandowskiPath<S1> plandowskiPath, final IMorphism<Z1, Z2> morphism) {
        MyLinkedList<S1> word = new MyLinkedList<>();
        word.add(placeholder);
        Node<S1> placeholder = word.getHead();

        for(IGrammarEdge<S1> edge : plandowskiPath) {
            edge.append(word, placeholder);
        }
        placeholder.remove();

        List<Z2> concateWord = word.stream()
                .map(node -> node.getElement())
                .map(symbol -> shortestWords.get(symbol))
                .map(slp -> morphism.apply(slp))
                .collect(Collectors.toList());

        if(concateWord.isEmpty()) {
            return emptyWord;
        }
        else {
            return slpOp.concatenate(concateWord, destSLPCreatorFactory);
        }
    }

    /**
     * Initializes the data structure i.e. all trees, SLPs representing shortest words and the grammar graph which is a multi-directed graph.
     */
    private void init() {
        // for each non-terminal N in G generate a SLP G' with distinct non-terminals such that L(G') = {w_N}.
        Map<S1, P1> baseProductions =cfgOp.getMinimalWord(baseCFG.getProductions());
        singletons = cfgOp.getSLPNonTerminals(baseCFG.getProductions());
        singletons = cfgOp.getSingletonNonTerminals(baseCFG.getProductions(), srcSLPCreatorFactory);

        shortestWords = new HashMap<>();
        for(S1 nonTerminal : baseCFG.getNonTerminals()) {
            Map<S1, P1> slpProductions = cfgCreator.copyProductions(cfgOp.deleteUseless(baseProductions, nonTerminal));
            Z1 slp = cfgCreator.createSLP(slpProductions, nonTerminal);
            shortestWords.put(nonTerminal, slp);
        }

        for(P1 production : baseCFG.getProductions()) {

            List<S1> nonTerminals = production.getRight().findAll(symbol -> !symbol.isTerminal());
            List<S1> terminals = production.getRight().findAll(symbol -> symbol.isTerminal());

            assert (nonTerminals.size() <= 0 || terminals.size() <= 0) && nonTerminals.size() <= 2 && terminals.size() <= 1;

            if(!multiEdges.containsKey(production.getLeft())) {
                multiEdges.put(production.getLeft(), new LinkedList<>());
            }

            List<IGrammarEdge<S1>> multiEdge = multiEdges.get(production.getLeft());

            if(nonTerminals.size() == 1 && terminals.size() == 0) {
                IGrammarEdge<S1> edge = new TerminalEdge(production.getLeft(), terminalNode, nonTerminals.get(0));
                adjazenzmatrix[production.getLeft().getId()][terminalNode.getId()] = edge;
                multiEdge.add(edge);

                if(!singletons.contains(nonTerminals.get(0))) {
                    edge = new ChainEdge(production.getLeft(), nonTerminals.get(0));
                    this.adjazenzmatrix[production.getLeft().getId()][nonTerminals.get(0).getId()] = edge;
                    multiEdge.add(edge);
                }
            }
            else if(nonTerminals.size() == 2 && terminals.size() == 0) {
                IGrammarEdge<S1> edge = new TwoWordEdge(production.getLeft(), terminalNode, nonTerminals.get(0), nonTerminals.get(1));
                this.adjazenzmatrix[production.getLeft().getId()][terminalNode.getId()] = edge;
                multiEdge.add(edge);

                if(!singletons.contains(nonTerminals.get(0))) {
                    edge = new RightWordEdge(production.getLeft(), nonTerminals.get(0), nonTerminals.get(1));
                    this.adjazenzmatrix[production.getLeft().getId()][nonTerminals.get(0).getId()] = edge;
                    multiEdge.add(edge);
                }

                if(!singletons.contains(nonTerminals.get(1))) {
                    edge = new LeftWordEdge(production.getLeft(), nonTerminals.get(1), nonTerminals.get(0));
                    this.adjazenzmatrix[production.getLeft().getId()][nonTerminals.get(1).getId()] = edge;
                    multiEdge.add(edge);
                }
            }
            else if(nonTerminals.size() == 0 && terminals.size() == 1) {
                IGrammarEdge<S1> edge = new TerminalEdge(production.getLeft(), terminalNode, production.getLeft());
                this.adjazenzmatrix[production.getLeft().getId()][terminalNode.getId()] = edge;
                multiEdge.add(edge);
            }
            else if(nonTerminals.size() == 0 && terminals.size() == 0) {
                IGrammarEdge<S1> edge = new EmptyWordEdge(production.getLeft(), terminalNode);
                this.adjazenzmatrix[production.getLeft().getId()][terminalNode.getId()] = new EmptyWordEdge(production.getLeft(), terminalNode);
                multiEdge.add(edge);
            }
            else {
                throw new IllegalArgumentException("The cfg is not in weak chomsky normal form.");
            }
        }

        trees = generateTrees();
    }

    /**
     * Returns all non-terminal trees.
     *
     * @return all non-terminal trees
     */
    private SpanningTree<S1>[] generateTrees() {
        SpanningTree<S1>[] trees = new SpanningTree[numberOfNodes];
        for (S1 nonTerminal : nonTerminals) {
            SpanningTree<S1> tree = generateSpanningTree(nonTerminal);
            trees[nonTerminal.getId()] = tree;
        }
        return trees;
    }

    /**
     * Generates and returns a non-terminal tree for the non-terminal.
     *
     * @param nonTerminal   the non-terminal
     * @return returns a non-terminal tree
     */
    private SpanningTree<S1> generateSpanningTree(final S1 nonTerminal) {
        boolean[] connected = new boolean[numberOfNodes];
        connected[nonTerminal.getId()] = true;
        TreeNode<S1> root = new TreeNode<>(nonTerminal);
        List<TreeNode<S1>> nodes = new ArrayList<>();
        nodes.add(root);

        SpanningTree<S1> tree = new SpanningTree<>(root, nodes, adjazenzmatrix);
        LinkedList<TreeNode<S1>> treeNodes = new LinkedList<>();
        treeNodes.add(root);

        while (!treeNodes.isEmpty()) {
            TreeNode<S1> treeNode = treeNodes.pop();

            for(S1 neighbour : getNeighbours(treeNode.get())) {
                int neighbourId = neighbour.getId();
                if(!connected[neighbourId]) {
                    TreeNode<S1> next = new TreeNode<>(neighbour);
                    treeNode.addNeighbour(next);
                    treeNodes.add(next);
                    nodes.add(next);
                    connected[neighbourId] = true;
                }
            }
        }

        return tree;
    }

    /**
     * Returns all neighbours of the grammar graph at the non-terminal node startNonTerminal.
     *
     * @param startNonTerminal  the non-terminal node
     * @return all neighbours of the grammar graph at the non-terminal node
     */
    private List<S1> getNeighbours(final S1 startNonTerminal) {
        List<S1> neighbours = new ArrayList<>();

        for(S1 endNonTerminal : nonTerminals) {
            if(adjazenzmatrix[startNonTerminal.getId()][endNonTerminal.getId()] != null) {
                neighbours.add(endNonTerminal);
            }
        }
        return neighbours;
    }
}
