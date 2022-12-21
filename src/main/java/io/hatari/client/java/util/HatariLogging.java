package io.hatari.client.java.util;

import java.util.logging.*;

/**
 * HatariLogging is a wrapper around a logging module and provides, well, logging for the Keen Java SDK.
 * Logging is disabled by default so as not to clutter up your development experience.
 *
 * @author Ebot Tabi
 * @since 1.0.0
 */
public class HatariLogging {

    private static final Logger LOGGER;

    static {
        LOGGER = Logger.getLogger(HatariLogging.class.getName());
        LOGGER.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
        disableLogging();
    }

    public static void log(String msg) {
        LOGGER.log(Level.FINER, msg);
    }

    /**
     * Call this to enable logging.
     */
    public static void enableLogging() {
        setLogLevel(Level.FINER);
    }

    /**
     * Call this to disable logging.
     */
    public static void disableLogging() {
        setLogLevel(Level.OFF);
    }

    private static void setLogLevel(Level newLevel) {
        LOGGER.setLevel(newLevel);
        for (Handler handler : LOGGER.getHandlers()) {
            handler.setLevel(newLevel);
        }
    }
}