/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * An KaaUncaughtExceptionHandler class provides method to handle exceptions
 * in that threads which don't contain own exceptoin handler
 *
 * @author Oleksandr Didukh
 *
 */
public class KaaUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(KaaUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        LOG.error("Thread [name: {}, id: {}] uncaught exception: ", thread.getName(), thread.getId(), exception);
    }
}
