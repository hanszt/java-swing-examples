package hzt;

import org.slf4j.Logger;

import java.util.function.Supplier;

public final class Loggers {

    private Loggers() {
    }

    public static void logIfInfoEnabled(Logger logger, Supplier<String> supplier) {
        if (logger.isInfoEnabled()) {
            logger.info(supplier.get());
        }
    }
}
