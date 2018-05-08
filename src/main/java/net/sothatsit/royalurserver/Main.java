package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.network.Server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.*;

public class Main {

    public static final int DEFAULT_PORT = 9113;

    public static void main(String[] args) {
        RoyalUr game = new RoyalUr();
        Logger logger = Logger.getLogger("Main");
        Server server = new Server(DEFAULT_PORT, game);

        int retryCount = 0;

        while(true) {
            try {
                server.start();
                break;
            } catch(Exception exception) {
                if(retryCount < 10) {
                    retryCount += 1;
                    try {
                        Thread.sleep(retryCount * 1000);
                    } catch (InterruptedException ignored) {}
                    continue;
                }

                logger.log(Level.SEVERE, "error starting server", exception);
                System.exit(1);
                return;
            }
        }

        try(Scanner scanner = new Scanner(System.in)) {
            while(true) {
                String input = scanner.nextLine();

                if(input.equalsIgnoreCase("stop")) {
                    game.shutdown();
                    server.shutdown();
                    break;
                }

                game.onConsoleInput(input);
            }
        }
    }

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
