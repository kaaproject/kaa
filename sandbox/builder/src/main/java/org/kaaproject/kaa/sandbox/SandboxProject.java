/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.sandbox;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SandboxProject extends Project {

    private static final Logger logger = LoggerFactory.getLogger(SandboxProject.class);
    
    @Override
    public void log(Task task, String message, int msgLevel) {
        log(message, null, msgLevel);
    }

    @Override
    public void log(Task task, String message, Throwable throwable, int msgLevel) {
        log(message, throwable, msgLevel);
    }

    @Override
    public void log(Target target, String message, int msgLevel) {
        log(message, null, msgLevel);
    }

    @Override
    public void log(Target target, String message, Throwable throwable,
            int msgLevel) {
        log(message, throwable, msgLevel);
    }
 
    @Override
    public void log(String message, Throwable throwable, int msgLevel) {
        super.log(message, throwable, msgLevel);
        switch (msgLevel) {
        case MSG_ERR:
            if (throwable != null) {
                logger.error(message, throwable);
            }
            else {
                logger.error(message);
            }
            break;
        case MSG_WARN:
            if (throwable != null) {
                logger.warn(message, throwable);
            }
            else {
                logger.warn(message);
            }
            break;
        case MSG_INFO:
            if (throwable != null) {
                logger.info(message, throwable);
            }
            else {
                logger.info(message);
            }
            break;
        case MSG_VERBOSE:
            if (throwable != null) {
                logger.debug(message, throwable);
            }
            else {
                logger.debug(message);
            }
            break;
        case MSG_DEBUG:
            if (throwable != null) {
                logger.trace(message, throwable);
            }
            else {
                logger.trace(message);
            }
            break;
        }
    }


}
