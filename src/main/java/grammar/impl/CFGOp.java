package grammar.impl;

import grammar.gen.GenCFGOp;
import symbol.IJezSymbol;

/**
 * The standard implementation of GenCFGOp.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class CFGOp<N> extends GenCFGOp<N, IJezSymbol<N>, JezWord<N>, Production<N>, CFG<N>, SLP<N>> {}
