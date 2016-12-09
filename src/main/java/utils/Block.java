package utils;


import symbol.Symbol;

/**
 * A block of the symbol a and the length l represents a sequence a^l.
 *
 * @author Benedikt Zoennchen
 *
 * @param <S> the type of the symbol of the block
 */
public class Block<S extends Symbol> {
    final S letter;
    final long length;

    public Block(final S letter, final long length) {
        this.length = length;
        this.letter = letter;
    }

    public S getLetter() {
        return letter;
    }

    public long getLength() {
        return length;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Block) {
            Block other = (Block)obj;
            return letter.equals(other.letter) && length == other.length;
        }
        return false;
    }

    @Override
    public String toString() {
        return "{" + letter + "/" + length + "}";
    }
}
