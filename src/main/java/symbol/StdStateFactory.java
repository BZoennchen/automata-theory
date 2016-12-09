package symbol;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bzoennchen on 23.12.15.
 */
public class StdStateFactory extends StateFactory<Integer> {
    private int counter = 0;

    protected Map<Integer,WeakReference<StdState>> baseSymbols;

    public StdStateFactory() {
        baseSymbols = new HashMap<>();
    }

    public StdState createFreshState() {
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
    protected StdState lookup(final Integer name) {
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
