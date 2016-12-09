package utils;


import data.Node;
import symbol.IJezSymbol;

/**
 * A block record combines a block with a pointer, pointing to the position of the block occurrence.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the symbol identifier
 * @param <S> the type of the symbol
 */
public class BlockRecord<N, S extends IJezSymbol<? extends N>> {
    public final Block<S> block;
    public final Node<S> node;

    public BlockRecord(final Block<S> block, final Node<S> node) {
        this.block = block;
        this.node = node;
    }

    public boolean equals(final PairRecord record) {
        if(record == null) {
            return false;
        }
        return block.equals(record.pair);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BlockRecord) {
            return block.equals(((BlockRecord)obj).block);
        }
        return false;
    }

    @Override
    public String toString() {
        return block.toString() + "->" + node;
    }
}
