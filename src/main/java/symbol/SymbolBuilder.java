package symbol;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by bzoennchen on 29.11.15.
 */
public class SymbolBuilder extends ISymbolBuilder {

    /**Counter for anonymous states.*/
    private static int anonCount = 0;

    /**
     * Stores the references to known named states. <br>
     * Named states have a name of arbitrary type which is used as hash key.<br>
     * They are stored separately from other states to avoid a mix-up of states and their names,
     * since states can be used as names of states, too.
     */
    private static HashMap<Object,WeakReference<IJezSymbol<?>>> namedStateCache = new HashMap<Object,WeakReference<IJezSymbol<?>>>();


    @Override
    public JezSymbol<Integer> makeState() {

        while (namedStateCache.containsKey(new Integer(anonCount))){
            anonCount++;
        }

        final Integer id = new Integer(anonCount);
        anonCount++;
        return new JezSymbol<>(id, false, id, 0, 1L, 1L);
    }

    @Override
    public <T> JezSymbol<T> makeState(T name, boolean terminal) {
        return create(name, terminal);
    }


    /**
     * Creates a new named state with the given name.
     *
     * @param <T> type of the name of a state
     * @param name name of the new state
     * @return a new named state
     */
    @SuppressWarnings("unchecked")
    private <T> JezSymbol<T> create(T name, boolean terminal) {
        WeakReference<IJezSymbol<?>> ref = namedStateCache.get(name); //lookup if we know this state

        // this cast is safe because if we find a NamedState for
        // the name, its type parameter is the type of the name
        JezSymbol<T> s = (ref!=null)?(JezSymbol<T>)ref.get():null;
        if (s==null) {
            anonCount++;
            s = new JezSymbol<T>(anonCount, terminal, name, 0, 1L, 1L);
            namedStateCache.put(name, new WeakReference<IJezSymbol<?>>(s));
            return s;
        } else {
            return s;
        }
    }

}
