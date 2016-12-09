package symbol;

/**
 * @author Benedikt Zoennchen
 */
public interface INamedSymbol<T> extends Symbol {
    T getName();
}
