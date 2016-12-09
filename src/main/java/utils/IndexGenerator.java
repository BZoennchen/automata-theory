package utils;

import symbol.IJezSymbol;
/**
 * @author Benedikt Zoennchen
 *
 * Helper class for realizing the parition and the correct indexing of the Jéz-Paper.
 */
public class IndexGenerator {

    /**
     * Computes the index of the partition of the pair (see the Jez-Paper).
     *
     * @param pair  the pair
     * @param <S>   the type of the symbol
     * @return the index of the partition of the pair
     */
    public static <S extends IJezSymbol> int calculateIndex(final Pair<S, S> pair){
        int result = (pair.a.getId() ^ pair.b.getId());

        // i-th is is different, this is O(1)
        int lowestOneBit = Integer.lowestOneBit(result);
        int count = getBitNumber(lowestOneBit);
        int index = 2 * count;

        // left has 1 and right has 0
        //logger.info("bit:"+(pair.a.getId() >> (lowestOneBit-1) & 1) + ", " + pair);
        int shift = (pair.a.getId() >> count);
        index = index + ((shift & 1) == 0 ? 0 : 1);
        return index;
    }

    /**
     * Tests whether the letter is in the left partition of index j (see the Jez-Paper).
     *
     * @param j         the index of the partition
     * @param letter    the letter we test
     * @param <S>       the type of the symbol
     * @return true => the letter is in the left partition of index j, otherwise false
     */
    public static <S extends IJezSymbol> boolean isInLeftPartition(final int j, final S letter) {
        int i = j / 2;

        int shift = letter.getId() >> i;
        int partition = shift & 1;

        return j % 2 == 0 ? partition == 0 : partition == 1;
    }

    /**
     * Tests whether the letter is in the right partition of index j (see the Jez-Paper).
     *
     * @param j         the index of the partition
     * @param letter    the letter we test
     * @param <S>       the type of the symbol
     * @return true => the letter is in the right partition of index j, otherwise false
     */
    public static <S extends IJezSymbol> boolean isInRightPartition(final int j, final S letter) {
        int i = j / 2;

        int shift = letter.getId() >> i;
        int partition = shift & 1;

        return j % 2 == 0 ? partition == 1 : partition == 0;
    }

    /**
     * Computes the position of the highest bit of i, i has to be a positive integer.
     *
     * Requirement: i is a positive integer.
     *
     * @param i an Integer
     * @return the position of the highest bit of i
     */
    public static int getBitNumber(int i) {
        int count = 0;
        while (i != 1) {
            i /= 2;
            count++;
        }
        return count;
    }
}
