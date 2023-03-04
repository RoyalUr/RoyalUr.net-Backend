package net.royalur.backend.util;

/**
 * Random math helper functions.
 */
public class MathHelper {

    /**
     * Calculate the power of {@code base} to the power of {@code exponent}.
     * @param base The base of the exponential.
     * @param exponent The exponent  of the exponential.
     * @return The value of base to the power of exponent.
     */
    public static long power(long base, int exponent) {
        long res = 1;
        long sq = base;
        while (exponent > 0) {
            if (exponent % 2 == 1) {
                res *= sq;
            }
            sq = sq * sq;
            exponent /= 2;
        }
        return res;
    }
}
