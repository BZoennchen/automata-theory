package symbol;

/**
 * @author Benedikt Zoennchen
 */
public interface NestedSymbol <S extends Symbol> extends Symbol {
    enum Brackets {
        Open, Close;
    }

    S getSymbol();

    Brackets getBrackets();
}
