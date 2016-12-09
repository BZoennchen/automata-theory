package utils;


import symbol.IJezSymbol;

import java.util.Comparator;

/**
 * Compares block records by comparing blocks.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the symbol identifier
 * @param <S> the type of the symbol
 */
public class BlockRecordComparator<N, S extends IJezSymbol<N>> implements Comparator<BlockRecord<N, S>> {

    private BlockComparator<N, S> blockComparator;

    public BlockRecordComparator() {
        this.blockComparator = new BlockComparator<>();
    }

    @Override
    public int compare(BlockRecord<N, S> o1, BlockRecord<N, S> o2) {
        return blockComparator.compare(o1.block, o2.block);
    }
}
