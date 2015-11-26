package org.kaaproject.kaa.server.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * An ExceptionHandlerUtil class provides method to handle exceptions
 * in that threads which don't contain own exceptoin handler
 *
 * @author Oleksandr Didukh
 *
 */
public class ExceptionHandlerUtil implements Thread.UncaughtExceptionHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerUtil.class);

    public ExceptionHandlerUtil() {
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        LOG.error("Thread [name: {}, id: {}] uncaught exception: ", thread.getName(), thread.getId(), exception);
    }
}
