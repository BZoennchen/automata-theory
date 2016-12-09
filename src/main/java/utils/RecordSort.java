package utils;

import symbol.IJezSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * The class for sorting records via RadixSort.
 *
 * @author Benedikt Zoennchen
 */
public class RecordSort {

    public static <N, S extends IJezSymbol<? extends N>> void sortBlocks(final List<Block<S>> list) {
        RadixSort.radixSort(list, block -> block.getLetter().getId());
        RadixSort.radixSort(list, block -> block.getLetter().getId(), block -> Math.toIntExact(block.getLength()), true);
    }

    public static <N, S extends IJezSymbol<? extends N>> void sortBlockRecords(final List<BlockRecord<N, S>> list) {
        RadixSort.radixSort(list, record -> record.block.getLetter().getId());
        RadixSort.radixSort(list, record -> record.block.getLetter().getId(), record -> Math.toIntExact(record.block.getLength()), true);
    }

    public static <N, S extends IJezSymbol<? extends N>, T extends IJezSymbol<N>> void sortPairs(final List<Pair<S, T>> list) {
        RadixSort.radixSort(list, pair -> pair.a.getId());
        RadixSort.radixSort(list, pair -> pair.a.getId(), pair -> pair.b.getId());
    }

    public static <N, S extends IJezSymbol<? extends N>, T extends IJezSymbol<N>> void sortPairRecords(final List<PairRecord<S, T>> list) {
        RadixSort.radixSort(list, record -> record.pair.a.getId());
        RadixSort.radixSort(list, record -> record.pair.a.getId(), record -> record.pair.b.getId());
    }

    public static <N, S extends IJezSymbol<? extends N>, T extends IJezSymbol<? extends N>> void sortGPairRecord(final List<GPairRecord<S, T>> list) {
        RadixSort.radixSort(list, record -> record.pair.a.getId());
        RadixSort.radixSort(list, record -> record.pair.a.getId(), record -> record.pair.b.getId());

        if(list.size() <= 1) {
            return;
        }

        Pair<S, T> mark = list.get(0).pair;
        List<GPairRecord<S, T>> out = new ArrayList<>(list);
        list.clear();

        List<GPairRecord<S, T>> split0 = new ArrayList<>(out.size());
        List<GPairRecord<S, T>> split1 = new ArrayList<>(out.size());

        for(int i = 0; i < out.size(); i++) {
            GPairRecord<S, T> record = out.get(i);

            if(!mark.equals(record.pair)) {
                mark = out.get(i).pair;
                // O(n_i)
                list.addAll(split0);
                list.addAll(split1);
                // O(n_i)
                split0.clear();
                split1.clear();
            }

            if(record.isCrossingPair()) {
                split0.add(record);
            }
            else {
                split1.add(record);
            }
        }

        list.addAll(split0);
        list.addAll(split1);
    }
}
