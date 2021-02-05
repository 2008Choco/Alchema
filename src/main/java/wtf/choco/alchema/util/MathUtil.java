package wtf.choco.alchema.util;

import com.google.common.base.Preconditions;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.NotNull;

/**
 * A series of utilities pertaining to mathematical operations.
 *
 * @author Parker Hawke - Choco
 */
public final class MathUtil {

    private MathUtil() { }

    /**
     * Generate a random number between a minimum and maximum value (inclusive).
     *
     * @param random the random instance to use
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     *
     * @return the randomly generated number
     */
    public static int generateNumberBetween(@NotNull Random random, int min, int max) {
        Preconditions.checkArgument(min <= max, "min (%s) must be <= max (%s)", min, max);
        return (min == max) ? min : min + (random.nextInt(max - min) + 1);
    }

    /**
     * Generate a random number between a minimum and maximum value (inclusive).
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     *
     * @return the randomly generated number
     */
    public static int generateNumberBetween(int min, int max) {
        return generateNumberBetween(ThreadLocalRandom.current(), min, max);
    }

}
