package utils;

/**
 * A simple counter object used for the data structure introduced by Hopcroft.
 *
 * @author Benedikt Zoennchen
 */
public class Counter {

    /**
     * The value of the counter.
     */
    private int count;

    /**
     * Default constructor, initializes the counter with zero.
     */
    public Counter() {
        count = 0;
    }

    /**
     * Constructor, initializes the counter with init.
     *
     * @param init the initial value
     */
    public Counter(final int init) {
        count = init;
    }

    /**
     * increments the counter by 1.
     */
    public void inc() {
        count++;
    }

    /**
     * decrements the counter by 1.
     */
    public void dec() {
        count--;
    }

    /**
     * Tests whether the counter is zero.
     *
     * @return true => the counter is zero, otherwise false
     */
    public boolean isZero() {
        return count == 0;
    }
}
