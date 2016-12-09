package symbol;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Benedikt Zoennchen
 */
public class StateFactory<T> {
    protected Map<T,WeakReference<INamedSymbol<T>>> baseSymbols;

    public StateFactory() {
        baseSymbols = new HashMap<>();
    }

    public INamedSymbol<T> createState(final T name) {
        return lookup(name);
    }

    /**
     * Creates a new named state with the given name.
     *
     * @param name name of the new state
     * @return a new named state
     */
    @SuppressWarnings("unchecked")
    protected INamedSymbol<T> lookup(final T name) {
        WeakReference<INamedSymbol<T>> ref = baseSymbols.get(name); //lookup if we know this state

        // this cast is safe because if we find a NamedState for
        // the name, its type parameter is the type of the name
        INamedSymbol<T> state = (ref!=null)?ref.get():null;
        if (state==null) {
            state = new GenState<>(name);
            baseSymbols.put(name, new WeakReference<>(state));
            return state;
        } else {
            return state;
        }
    }
}
