package symbol;

/**
 * @author Benedikt Zoennchen
 */
public abstract class ISymbolBuilder {

    private static ISymbolBuilder instance = null;

    /**
     * Initially sets a non-standard state factory.
     * @param factory the state factory to use.
     */
    public static void init(ISymbolBuilder factory){
        assert(instance == null);
        instance = factory;
    }

    /**
     * Returns the state factory instance.
     * @return the state factory instance
     */
    public static ISymbolBuilder getStateFactory(){
        if (instance == null) instance = new SymbolBuilder();
        return instance;
    }

    public abstract <T> JezSymbol<T> makeState(T name, boolean terminal);

    public abstract JezSymbol<?> makeState();

}
