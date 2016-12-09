package symbol;

/**
 * @author Benedikt Zoennchen
 */
public interface IJezSymbol<N> extends NaturalSymbol {
    boolean isEmpty();

    long getLength();

    long getWeight();

    int getPhaseId();

    int getBlockId();

    void setBlockId(final int blockId);

    boolean isTerminal();

    N getName();

    IJezSymbol<N> clone();

    int getId();
}
