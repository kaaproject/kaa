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

package org.kaaproject.kaa.server.common;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {

    private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

    private static final List<String> systemProperties = Arrays.asList("java.version", "java.vendor", "java.home",
            "java.class.path", "java.library.path", "java.io.tmpdir", "java.compiler", "os.name", "os.arch", "os.version",
            "user.name", "user.home", "user.dir");

    public static void logState(){
        LOG.info("Kaa version: {}, commit: {}", Version.PROJECT_VERSION, Version.COMMIT_HASH);
        for(String property : systemProperties){
            LOG.info("{} : {}", property, System.getProperty(property, "N/A"));
        }
    };
}
