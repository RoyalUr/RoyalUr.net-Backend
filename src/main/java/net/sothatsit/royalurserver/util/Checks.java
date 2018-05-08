package net.sothatsit.royalurserver.util;

import java.util.function.*;

public class Checks {

    public static void ensureNonNull(Object argument, String argName) {
        ensure(argument != null, argName + " cannot be null");
    }

    public static void ensureSingleDigit(int argument, String argName) {
        ensure(argument >= 0 && argument <= 9, argName + " must be a single digit number (0 to 9 inclusive)");
    }

    public static void ensure(boolean expression, String message) {
        if(!expression)
            throw new IllegalArgumentException(message);
    }

    public static void ensureStateNonNull(Object state, String stateName) {
        ensureState(state != null, stateName + " is null");
    }

    public static void ensureState(boolean expression, String message) {
        if(!expression)
            throw new IllegalStateException(message);
    }

    public static <A, B, R> R detailThrown(BiFunction<A, B, R> function, A arg1, B arg2, String detail) {
        return detailThrown(() -> {
            return function.apply(arg1, arg2);
        }, detail);
    }

    public static <A, R> R detailThrown(Function<A, R> function, A argument, String detail) {
        return detailThrown(() -> {
            return function.apply(argument);
        }, detail);
    }

    public static <R> R detailThrown(Supplier<R> function, String detail) {
        Checks.ensureNonNull(function, "function");
        Checks.ensureNonNull(detail, "detail");

        try {
            return function.get();
        } catch(RuntimeException exception) {
            throw ExceptionDetailer.detail(exception, detail);
        }
    }

    public static <A, B> void detailThrown(BiConsumer<A, B> function, A arg1, B arg2, String detail) {
        detailThrown(() -> {
            function.accept(arg1, arg2);
        }, detail);
    }

    public static <A> void detailThrown(Consumer<A> function, A argument, String detail) {
        detailThrown(() -> {
            function.accept(argument);
        }, detail);
    }

    public static void detailThrown(Runnable function, String detail) {
        Checks.ensureNonNull(function, "function");
        Checks.ensureNonNull(detail, "detail");

        try {
            function.run();
        } catch(RuntimeException exception) {
            throw ExceptionDetailer.detail(exception, detail);
        }
    }
}
