package net.sothatsit.royalurserver.util;

/**
 * Adds details to exceptions to allow easier debugging.
 *
 * @author Paddy Lamont
 */
public abstract class ExceptionDetailer {

    private static class DetailException extends Exception {
        public DetailException(String info) {
            super(info);
        }
    }

    /**
     * @return The exception {@param exception} detailed with additional information.
     */
    public abstract <E extends Exception> E detail(E exception);

    /**
     * @return An ExceptionDetailer that adds information about where an object was constructed.
     */
    public static ExceptionDetailer constructorDetailer() {
        final DetailException constructorStackTrace = new DetailException("Object constructed at");

        return new ExceptionDetailer() {
            @Override
            public <E extends Exception> E detail(E exception) {
                Checks.ensureNonNull(exception, "exception");

                try {
                    exception.addSuppressed(constructorStackTrace);

                    return exception;
                } catch(Exception e) {
                    new Exception("Exception appending info to exception", e).printStackTrace();

                    constructorStackTrace.printStackTrace();

                    return exception;
                }
            }
        };
    }

    /**
     * @return The exception {@param exception} detailed with the additional information {@param detail}.
     */
    public static <E extends Exception> E detail(E exception, String detail) {
        Checks.ensureNonNull(exception, "exception");
        Checks.ensureNonNull(detail, "detail");

        exception.addSuppressed(new DetailException(detail));

        return exception;
    }
}
