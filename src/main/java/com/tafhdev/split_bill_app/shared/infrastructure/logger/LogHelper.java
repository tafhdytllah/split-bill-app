package com.tafhdev.split_bill_app.shared.infrastructure.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogHelper {

    private static final Logger log =
            LoggerFactory.getLogger(LogHelper.class);

    private LogHelper() {
    }

    public static void info(String message, Object... args) {
        log.info(message, args);
    }

    public static void debug(String message, Object... args) {
        log.debug(message, args);
    }

    public static void warn(String message, Object... args) {
        log.warn(message, args);
    }

    public static void error(String message, Object... args) {
        log.error(message, args);
    }
}
