package utils;

/**
 * @author Benedikt Zoennchen
 */
public class Paring {

    public static int paring(final int x, final int y) {
        return y + (x+y) * (x+y + 1) / 2;
    }

    public static int[] inverse(final int p) {
        int j = (int)Math.floor(Math.sqrt(0.25 + 2 * p) - 0.5);
        int x = (j - (p - j * (j+1) / 2));

        int i  = (int) Math.floor(Math.sqrt(0.25 + 2*p) - 0.5);
        int y = p - i*(j+1)/2;

        int[] paar = new int[2];
        paar[0] = x;
        paar[1] = y;
        return paar;
    }

    public static int paring(final int... x) {
        return paring(x.length-1, x);
    }

    public static int[] inverse(final int k, final int p) {
        int[] pairs = new int[k];
        inverse(k-1, p, pairs);
        return pairs;
    }

    private static void inverse(final int k, final int p, final int... pairs) {
        if(k == 0) {
            pairs[k] = p;
        }
        else {
            int[] inverse = inverse(p);
            pairs[k] = inverse[1];
            inverse(k-1, inverse[0], pairs);
        }
    }

    private static int paring(final int k, final int... x) {
        if(k == 0) {
            return x[0];
        }
        else {
            return paring(paring(k-1, x), x[k]);
        }
    }
}
