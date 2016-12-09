package grammar.impl;

import grammar.inter.IJezSymbolFactory;
import symbol.IJezSymbol;
import symbol.JezSymbol;
import utils.Block;
import utils.Pair;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A JezSymbolFactory has two tasks. First of all it contains non-terminal and terminal IJezSymbols identified by
 * its name i.e. a terminal and a non-terminal alphabet. Therefore we can use the factory to get a jez symbols for certain names. The second task is
 * to support a changing phase alphabet of jez symbol of the last and the current jez phase. This will be used by the recompression algorithm only.
 *
 * @author Benedikt Zoennchen
 *
 * @param <T> the type of terminal and non-terminal symbols.
 */
public class JezSymbolFactory<T> implements IJezSymbolFactory<T, IJezSymbol<T>, JezWord<T>, Production<T>> {

    private List<IJezSymbol<T>> terminalAlphabet;
    private List<IJezSymbol<T>> phaseAlphabet;
    private List<IJezSymbol<T>> nonTerminalAlphabet;
    private HashMap<T,WeakReference<IJezSymbol<T>>> baseSymbols;
    private long phase;

    /**
     * The constructor with an non-terminal alphabet containing jez-symbols of id [maxNonTerminal]
     * and an empty terminal alphabet.
     *
     * @param maxNonTerminal
     */
    public JezSymbolFactory(final int maxNonTerminal) {
        this();

        for(int i = 0; i <= maxNonTerminal; i++) {
            createFreshNonTerminal();
        }
    }

    /**
     * The default constructor creating empty alphabets.
     */
    public JezSymbolFactory() {
        terminalAlphabet = new ArrayList<>();
        phaseAlphabet = new ArrayList<>();
        nonTerminalAlphabet = new ArrayList<>();
        baseSymbols = new HashMap<>();
        phase = 0;
    }

    @Override
    public void nextPhase() {
        phase++;
        phaseAlphabet = new ArrayList<>();
    }

    @Override
    public IJezSymbol<T> makeSymbol() {
        return createTerminal(0, 1L, 1L);
    }

    @Override
    public IJezSymbol<T> makeSymbol(final T name, final boolean terminal) {
        return lookup(name, terminal);
    }

    @Override
    public IJezSymbol<T> createFreshNonTerminal() {
        JezSymbol jezSymbol = new JezSymbol(nonTerminalAlphabet.size(), false, null, 0, 1L, 1L);
        nonTerminalAlphabet.add(jezSymbol);
        return jezSymbol;
    }

    @Override
    public IJezSymbol<T> createTerminal(T name, int phase, long length, long weight) {
        return createTerminal(true, null, phase, length, weight);
    }

    @Override
    public IJezSymbol<T> createTerminal(int phase, long length, long weight, final Pair<IJezSymbol<T>, IJezSymbol<T>> pair) {
        IJezSymbol<T> symbol = createTerminal(phase, length, weight);
        return symbol;
    }

    @Override
    public IJezSymbol<T> createTerminal(int phase, long length, long weight, final Block<IJezSymbol<T>> block) {
        IJezSymbol<T> symbol = createTerminal(phase, length, weight);
        symbol.setBlockId(block.getLetter().getId());
        return symbol;
    }

    @Override
    public IJezSymbol<T> createTerminal(int phase, long length, long weight, final IJezSymbol<T> letter) {
        IJezSymbol<T> symbol = createTerminal(phase, length, weight);
        return symbol;
    }


    @Override
    public IJezSymbol<T> createTerminal(int phase, long length, long weight) {
        return createTerminal(null, phase, length, weight);
    }


    @Override
    public IJezSymbol<T> createTerminal(final IJezSymbol<T> symbol, long length) {
        return new JezSymbol<>(symbol.getId(), symbol.isTerminal(), symbol.getName(), symbol.getPhaseId(), length, symbol.getWeight());
    }

    @Override
    public int getMax(int phase) {
        return getAlphabet(phase).size();
    }

    public List<IJezSymbol<T>> getAlphabet(final int phase) {
        if(phase != this.phase) {
            throw new IllegalArgumentException("unsynchonized phase.");
        }
        return phaseAlphabet;
    }

    private IJezSymbol<T> createTerminal(final boolean terminal, final T name, int phase, long length, long weight) {
        if(terminal && phase != this.phase) {
            throw new IllegalArgumentException("unsynchonized phase.");
        }

        List<IJezSymbol<T>> alphabet;
        if(terminal) {
            alphabet = getAlphabet(phase);
        }
        else {
            alphabet = nonTerminalAlphabet;
        }
        JezSymbol jezSymbol = new JezSymbol(alphabet.size(), terminal, name, phase, length, weight);
        alphabet.add(jezSymbol);
        return jezSymbol;
    }

    private IJezSymbol<T> createNonTerminal(final boolean terminal, final T name, int phase, long length, long weight) {
        if(phase != 0) {
            new IllegalArgumentException("your not allowed to createTerminal non-terminals in a phase != 0.");
        }
        JezSymbol jezSymbol = new JezSymbol(nonTerminalAlphabet.size(), terminal, name, phase, length, weight);
        nonTerminalAlphabet.add(jezSymbol);
        return jezSymbol;
    }

    @Override
    public int getMaxNonterminal() {
        return nonTerminalAlphabet.size();
    }

    @Override
    public List<IJezSymbol<T>> getNonTerminals() {
        return Collections.unmodifiableList(nonTerminalAlphabet);
    }

    /**
     * Creates a new named state with the given name.
     *
     * @param name name of the new state
     * @return a new named state
     */
    @SuppressWarnings("unchecked")
    private IJezSymbol<T> lookup(T name, boolean terminal) {
        WeakReference<IJezSymbol<T>> ref = baseSymbols.get(name); //lookup if we know this state

        // this cast is safe because if we find a NamedState for
        // the name, its type parameter is the type of the name
        IJezSymbol<T> s = (ref!=null)?ref.get():null;
        if (s==null) {
            if(terminal) {
                s = createTerminal(terminal, name, 0, 1L, 1L);
            }
            else {
                s = createNonTerminal(terminal, name, 0, 1L, 1L);
            }
            baseSymbols.put(name, new WeakReference<>(s));
            return s;
        } else {
            return s;
        }
    }
}
