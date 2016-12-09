package N2W.impl.std;

import N2W.inter.IN2WCreator;
import symbol.StdState;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * N2W creator for the standard implementation.
 *
 * @author Benedikt Zoennchen
 */
public class StdCreator implements IN2WCreator<
        List<StdNestedWord>,
        Integer,
        Character,
        Integer,
        StdState,
        StdNestedWord,
        StdStackSymbol,
        StdN2WRule,
        StdN2W> {


    private StateFactory stateFactory;

    public StdCreator() {
        this.stateFactory = new StateFactory();
    }

    @Override
    public StdState createFreshState() {
        return this.stateFactory.createFresh();
    }

    @Override
    public StdState createState(Integer name) {
        return this.stateFactory.createState(name);
    }

    @Override
    public StdNestedWord createNestedLetter(Character element, boolean opening) {
        return new StdNestedWord(element, opening);
    }

    @Override
    public StdStackSymbol createStackSymbol(Integer element) {
        return new StdStackSymbol(element);
    }

    @Override
    public StdN2WRule createRule(StdState srcState, StdState destState, StdNestedWord nestedLetter, StdStackSymbol stackSymbol, List<StdNestedWord> outputWord) {
        return new StdN2WRule(srcState, destState, nestedLetter, outputWord, stackSymbol);
    }

    @Override
    public StdN2W createTransducer(Set<StdN2WRule> rules, Set<StdState> initialStates, Set<StdState> finalStates) {
        return new StdN2W(rules, initialStates, finalStates);
    }

    private class StateFactory {
        private Map<Integer,WeakReference<StdState>> baseSymbols;
        private int counter = 0;

        public StateFactory() {
            baseSymbols = new HashMap<>();
        }

        public StdState createFresh() {
            while (baseSymbols.containsKey(counter)) {
                counter++;
            }
            return lookup(counter);
        }

        public StdState createState(final Integer name) {
            return lookup(name);
        }

        /**
         * Creates a new named state with the given name.
         *
         * @param name name of the new state
         * @return a new named state
         */
        @SuppressWarnings("unchecked")
        private StdState lookup(final Integer name) {
            WeakReference<StdState> ref = baseSymbols.get(name); //lookup if we know this state

            // this cast is safe because if we find a NamedState for
            // the name, its type parameter is the type of the name
            StdState state = (ref!=null)?ref.get():null;
            if (state==null) {
                state = new StdState(name);
                baseSymbols.put(name, new WeakReference<>(state));
                return state;
            } else {
                return state;
            }
        }
    }
}
