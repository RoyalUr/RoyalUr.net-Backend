package net.sothatsit.royalurserver.util;

public abstract class ExceptionDetailer {

    private static class DetailException extends Exception {
        public DetailException(String info) {
            super(info);
        }
    }

    public abstract <E extends Exception> E detail(E exception);

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

    public static <E extends Exception> E detail(E exception, String detail) {
        Checks.ensureNonNull(exception, "exception");
        Checks.ensureNonNull(detail, "detail");

        exception.addSuppressed(new DetailException(detail));

        return exception;
    }
}
