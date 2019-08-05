package net.sothatsit.royalurserver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Adds detail to messages logged to console.
 *
 * @author Paddy Lamont
 */
public class Logging {

    private static final SimpleDateFormat defaultDateFormatter = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    private static final Formatter defaultLogFormatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();

            Date date = new Date(record.getMillis());

            builder.append(defaultDateFormatter.format(date));
            builder.append(' ');
            builder.append(record.getLevel());
            builder.append(' ');
            builder.append(record.getLoggerName());
            builder.append(": ");
            builder.append(record.getMessage());
            builder.append('\n');

            if(record.getThrown() != null) {
                builder.append(format(record.getThrown()));
            }

            return builder.toString();
        }

        private String format(Throwable throwable) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            throwable.printStackTrace(printWriter);

            return stringWriter.toString();
        }
    };

    /**
     * @return The Logger {@param name} with a custom log formatter.
     */
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);

        for(Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                String formatted = getFormatter().format(record);

                if(record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    System.err.print(formatted);
                } else {
                    System.out.print(formatted);
                }
            }

            public void flush() {}
            public void close() throws SecurityException {}
        };

        handler.setFormatter(defaultLogFormatter);

        logger.addHandler(handler);
        logger.setUseParentHandlers(false);

        return logger;
    }
}
