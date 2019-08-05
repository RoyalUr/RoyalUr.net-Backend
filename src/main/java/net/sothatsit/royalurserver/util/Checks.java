package net.sothatsit.royalurserver.util;

import java.util.function.*;

/**
 * Various checks to allow more robust error checking.
 *
 * @author Paddy Lamont
 */
public class Checks {

    /**
     * Ensure that {@param array} and all of its contents are non-null.
     *
     * @throws IllegalArgumentException if {@param array} or any of its contents are null.
     */
    public static <T> void ensureArrayNonNull(T[] array, String argName) {
        ensureNonNull(array, argName);

        String message = "The values within " + argName + " cannot be null";
        for (T value : array) {
            ensure(value != null, message);
        }
    }

    /**
     * Ensure that {@param argument} is not null.
     *
     * @throws IllegalArgumentException if {@param argument} is null.
     */
    public static void ensureNonNull(Object argument, String argName) {
        ensure(argument != null, argName + " cannot be null");
    }

    /**
     * Ensure that {@param argument} is between 0 and 9, inclusive.
     *
     * @throws IllegalArgumentException if {@param argument} is not a single digit.
     */
    public static void ensureSingleDigit(int argument, String argName) {
        ensure(
            argument >= 0 && argument <= 9,
            argName + " must be a single digit number (0 to 9 inclusive)"
        );
    }

    /**
     * Ensure that {@param expression} is true.
     *
     * @throws IllegalArgumentException if {@param expression} is false.
     */
    public static void ensure(boolean expression, String message) {
        if(!expression)
            throw new IllegalArgumentException(message);
    }

    /**
     * Ensure that {@param expression} is true.
     *
     * @throws IllegalStateException if {@param expression} is false.
     */
    public static void ensureState(boolean expression, String message) {
        if(!expression)
            throw new IllegalStateException(message);
    }

    /**
     * Calls {@param function}, adding additional detail {@param detail} to any exception thrown.
     */
    public static <A, B, R> R detailThrown(BiFunction<A, B, R> function, A arg1, B arg2, String detail) {
        return detailThrown(() -> {
            return function.apply(arg1, arg2);
        }, detail);
    }

    /**
     * Calls {@param function}, adding additional detail {@param detail} to any exception thrown.
     */
    public static <A, R> R detailThrown(Function<A, R> function, A argument, String detail) {
        return detailThrown(() -> {
            return function.apply(argument);
        }, detail);
    }

    /**
     * Calls {@param function}, adding additional detail {@param detail} to any exception thrown.
     */
    public static <R> R detailThrown(Supplier<R> function, String detail) {
        Checks.ensureNonNull(function, "function");
        Checks.ensureNonNull(detail, "detail");

        try {
            return function.get();
        } catch(RuntimeException exception) {
            throw ExceptionDetailer.detail(exception, detail);
        }
    }

    /**
     * Calls {@param function}, adding additional detail {@param detail} to any exception thrown.
     */
    public static <A, B> void detailThrown(BiConsumer<A, B> function, A arg1, B arg2, String detail) {
        detailThrown(() -> {
            function.accept(arg1, arg2);
        }, detail);
    }

    /**
     * Calls {@param function}, adding additional detail {@param detail} to any exception thrown.
     */
    public static <A> void detailThrown(Consumer<A> function, A argument, String detail) {
        detailThrown(() -> {
            function.accept(argument);
        }, detail);
    }

    /**
     * Calls {@param function}, adding additional detail {@param detail} to any exception thrown.
     */
    public static void detailThrown(Runnable function, String detail) {
        Checks.ensureNonNull(function, "function");
        Checks.ensureNonNull(detail, "detail");

        try {
            function.run();
        } catch (RuntimeException exception) {
            throw ExceptionDetailer.detail(exception, detail);
        }
    }
}
