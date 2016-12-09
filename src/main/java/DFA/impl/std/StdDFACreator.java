package DFA.impl.std;

import DFA.inter.IDFACreator;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A IDFACreator for creating DFAs of the standard implementation i.e.
 *
 * Input symbol identifier type: Character
 * State identifier type: Integer
 *
 * @author Benedikt Zoennchen
 */
public class StdDFACreator implements IDFACreator<Character, Integer, StdDFASymbol, StdDFAState, StdDFARule, StdDFA> {

    /**
     * a mapping : char -> letter, to identify a letter by its element (in this case of type char).
     * We use weak references such that if the letter is no longer used, the garbage collector can delete it.
     */
    private Map<Character,WeakReference<StdDFASymbol>> baseSymbols;

    /**
     * a mapping : integer -> state, to identify a letter by its element (in this case of type char).
     * We use weak references such that if the letter is not unused, the garbage collector can delete it.
     */
    private Map<Integer,WeakReference<StdDFAState>> baseStates;

    /**
     * The id for the next fresh state.
     */
    private int maxState = 0;

    public StdDFACreator() {
        baseSymbols = new HashMap<>();
        baseStates = new HashMap<>();
    }

    @Override
    public StdDFAState createState() {
        return lookup(maxState+1);
    }

    @Override
    public StdDFAState createState(final Integer name) {
        return lookup(name);
    }

    @Override
    public StdDFASymbol createSymbol(final Character name) {
        return lookup(name);
    }

    @Override
    public StdDFARule createRule(final StdDFAState srcState, final StdDFAState destState, final StdDFASymbol symbol) {
        return new StdDFARule(srcState, destState, symbol);
    }

    @Override
    public StdDFA create(final Set<StdDFARule> rules, final StdDFAState initialState, final Set<StdDFAState> finalStates) {
        return new StdDFA(rules, initialState, finalStates);
    }

    /**
     * Creates a new named state with the given name.
     *
     * @param name name of the new state
     * @return a new named state
     */
    private StdDFASymbol lookup(final Character name) {
        WeakReference<StdDFASymbol> ref = baseSymbols.get(name);
        StdDFASymbol state = (ref!=null)?ref.get():null;
        if (state==null) {
            state = new StdDFASymbol(name);
            baseSymbols.put(name, new WeakReference<>(state));
            return state;
        } else {
            return state;
        }
    }

    /**
     * Creates a new named state with the given name.
     *
     * @param name name of the new state
     * @return a new named state
     */
    private StdDFAState lookup(final Integer name) {
        WeakReference<StdDFAState> ref = baseStates.get(name);
        StdDFAState state = (ref!=null)?ref.get():null;
        if (state==null) {
            state = new StdDFAState(name);
            baseStates.put(name, new WeakReference<>(state));
            maxState = Math.max(maxState, state.getName());
            return state;
        } else {
            return state;
        }
    }
}
