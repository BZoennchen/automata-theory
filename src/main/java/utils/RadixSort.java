package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A RadixSort implementation to sort lists.
 *
 * @author Benedikt Zoennchen
 */
public class RadixSort {

    /**
     * Sorts a list containing n elements of max{f.apply(el)} = n^c for some constant c according to exp.
     * exp is in [1;c].
     *
     * Complexity: O(n + c + c*n) = O(c*n)
     *
     * @param list          the list that will be sorted
     * @param f             a function for the sorting that maps an element of type T to an Integer value
     * @param exp           the result of n^x = exp is in [1;c]
     * @param <T>           the type of the elements of the list
     */
    private static <T> void radixSort(final List<T> list, int exp, final Function<T, Integer> f) {

        List<T> out = new ArrayList<>(list);

        int[] count = new int[list.size()];
        int n = list.size();

        for(int i = 0; i < count.length; i++) {
            count[f.apply(out.get(i))/exp % n]++;
        }

        for(int i = 1; i < count.length; i++) {
            count[i] += count[i-1];
        }

        for(int i = n - 1; i >= 0; i--) {
            int index = f.apply(out.get(i)) / exp % n;
            list.set(count[index] - 1, out.get(i));
            count[index]--;
        }

    }

    /**
     * Sorts a list containing n elements of f.apply(el) max n^c for some constant c.
     *
     * @param list          the list
     * @param f             a function for the sorting that maps an element of type T to an Integer value
     * @param c             max{f.apply(el) = n^c}
     * @param descending    true => sort descending order, otherwise ascending order
     * @param <T>           the type of the elements of the list
     */
    public static <T> void radixSort(final List<T> list, final Function<T, Integer> f, final int c, final boolean descending) {
        int n = list.size();
        int exp = 1; // n^0
        for(int i = 0; i <= c; i++) {
            radixSort(list, exp, f);
            exp = exp * n; // n^1, n^2, ..., n^c
        }

        if(descending) {
            List<T> tmp = new ArrayList<>(list);
            for(int i = 0; i < tmp.size(); i++) {
                list.set(i, tmp.get((tmp.size()-1)-i));
            }
        }
    }

    /**
     * Sorts a list containing n elements of f.apply(el) max n^c for some constant c.
     *
     * Complexity: O(n + c + c*n) = O(c*n)
     * @param list          the list that will be sorted
     * @param f             a function for the sorting that maps an element of type T to an Integer value
     * @param descending    true => sort descending order, otherwise ascending order
     * @param <T>           the type of the elements of the list
     */
    public static <T> void radixSort(final List<T> list, final Function<T, Integer> f, final boolean descending) {
        if(list.size() <= 1) {
            return;
        }

        int max = 0;
        for(int i = 0; i < list.size(); i++) {
            max = Math.max(max, f.apply(list.get(i)));
        }
        // n^x - 1 = max => x = log_n(max+1), logb(x) = loga(x) / loga(b).
        //int c = (int)Math.ceil(Math.log(max+1) / Math.log(list.size()));
        int c = findLog(max + 1, list.size());
        radixSort(list, f, c, descending);
    }

    /**
     * Sorts a list in ascending order by using the function f.
     *
     * @param list  the list that will be sorted
     * @param f     a function for the sorting that maps an element of type T to an Integer value
     * @param <T>   the type of the elements of the list
     */
    public static <T> void radixSort(final List<T> list, final Function<T, Integer> f) {
        radixSort(list, f, false);
    }

    /**
     * Splits the list by using the splitter function and sorts each sub list separately and merges all lists again together.
     *
     * Complexity: O(c_1 * n_1 + c_2 * n_2 + ... + c_k + n_k), where n_1 + n_2 + ... + n_k = n and c_i < c => O(c*n)
     *
     *
     * @param list a partial sorted list
     * @param splitter a function which gives us the elements that are already sorted
     * @param f a second function for sorting the partial sorted elements
     * @param descending true => sort descending order, otherwise ascending order
     * @param <T> the type of objects that should be sorted
     */
    public static <T> void radixSort(final List<T> list, final Function<T, Integer> splitter, final Function<T, Integer> f, final boolean descending) {
        if(list.size() <= 1) {
            return;
        }

        int mark = splitter.apply(list.get(0));
        List<T> out = new ArrayList<>(list);

        list.clear();
        List<T> split = new ArrayList<>();

        for(int i = 0; i < out.size(); i++) {
            if(mark != splitter.apply(out.get(i))) {
                mark = splitter.apply(out.get(i));
                // O(c_i*n_i)
                radixSort(split, f, descending);
                // O(n_i)
                list.addAll(split);
                // O(n_i)
                split = new ArrayList<>(list.size());
            }
            split.add(out.get(i));
        }

        radixSort(split, f, descending);
        // O(n_i)
        list.addAll(split);
    }

    /**
     * Splits the list by using the splitter function and sorts each sub list separately and merges all lists again together.
     * Merges each sub list in ascending order.
     *
     * Complexity: O(c_1 * n_1 + c_2 * n_2 + ... + c_k + n_k), where n_1 + n_2 + ... + n_k = n and c_i < c
     * => O(c*n)
     *
     * @param list a partial sorted list
     * @param splitter a function which gives us the elements that are already sorted
     * @param f a second function for sorting the partial sorted elements
     * @param <T> the type of objects that should be sorted
     */
    public static <T> void radixSort(final List<T> list, final Function<T, Integer> splitter, final Function<T, Integer> f) {
        radixSort(list, splitter, f, false);
    }

    /**
     * Computes c such that c is the smallest value such that n^c >= max.
     *
     * Complexity: O(c)
     *
     * @param max   the max value
     * @param n     the base
     * @return c such that c is the smallest value such that n^c >= max.
     */
    public static int findLog(final int max, final int n) {
        if(n <= 1) {
            return 0;
        }

        int c = 0;
        int exp = 1;
        while (exp < max) {
            exp = exp * n;
            c++;
        }

        return c;
    }
}
