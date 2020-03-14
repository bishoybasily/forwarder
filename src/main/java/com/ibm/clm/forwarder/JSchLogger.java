package com.ibm.clm.forwarder;

import com.jcraft.jsch.Logger;
import org.apache.logging.log4j.Level;

/**
 * @author bishoybasily
 * @since 3/14/20
 */
public class JSchLogger implements Logger {

    private org.apache.logging.log4j.Logger logger;

    public JSchLogger(org.apache.logging.log4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isEnabled(int level) {
        return logger.isEnabled(map(level));
    }

    @Override
    public void log(int level, String message) {
        logger.log(map(level), message);
    }

    private Level map(int level) {
        switch (level) {
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            case FATAL:
                return Level.FATAL;
        }
        throw new IllegalArgumentException("Unknown logging level");
    }

}
