package symbol;

/**
 * @author Benedikt Zoennchen
 */
public class JezSymbol<N> implements IJezSymbol<N>{

    private final long length;
    private final long weight;
    private final int phase;
    private final int id;
    private int blockId = -1;
    private final boolean terminal;
    private final N name;

    public JezSymbol(final int id, final boolean terminal, final N name, final int phase, final long length, final long weight) {
        this.id = id;
        this.name = name;
        this.terminal = terminal;
        this.length = length;
        this.weight = weight;
        this.phase = phase;
    }

    @Override
    public void setBlockId(final int blockId) {
        this.blockId = blockId;
    }

    @Override
    public boolean isEmpty() {
        return getId() == -1;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public long getWeight() {
        return weight;
    }

    @Override
    public int getPhaseId() {
        return phase;
    }

    @Override
    public int getBlockId() {
        return blockId;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public JezSymbol clone() {
        return this;
    }

    @Override
    public N getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return 29 * getId();
    }

    @Override
    public boolean equals(Object obj) {
        // equals ignore block length!
        if(obj instanceof IJezSymbol) {
            IJezSymbol other = ((JezSymbol)obj);
            return getId() == other.getId() && isTerminal() == other.isTerminal() && other.getPhaseId() == getPhaseId();
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(!isTerminal()) {
            builder.append("[");
        }
        for (int i = 0; i < length; i++) {
            builder.append(getId());
        }
        builder.append(((getName() != null) ? "("+getName()+")" : ""));
        if(!isTerminal()) {
            builder.append("]");
        }
        //builder.append("{"+weight+"}");
        return builder.toString();
    }

    @Override
    public int getId() {
        return id;
    }
}
