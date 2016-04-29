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

package org.kaaproject.kaa.server.common;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Environment provides ability to log environment state.
 */
public class Environment {
    
    public static final String SERVER_HOME_DIR = "server_home_dir";
    
    private static final String DEFAULT_SERVER_HOME_DIR = "."; 

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

    /** The Constant SYSTEM_PROPERTIES. */
    private static final List<String> SYSTEM_PROPERTIES = Arrays.asList("java.version", "java.vendor", "java.home",
            "java.class.path", "java.library.path", "java.io.tmpdir", "java.compiler", "os.name", "os.arch", "os.version",
            "user.name", "user.home", "user.dir", SERVER_HOME_DIR);

    /**
     * Instantiates a new environment.
     */
    private Environment(){
    }

    /**
     * Logs environment state using {@link Logger}
     */
    public static void logState(){
        LOG.info("Kaa version: {}, commit: {}", Version.PROJECT_VERSION, Version.COMMIT_HASH);
        for(String property : SYSTEM_PROPERTIES){
            LOG.info("{} : {}", property, System.getProperty(property, "N/A"));
        }
    };
    
    public static String getServerHomeDir() {
        return System.getProperty(SERVER_HOME_DIR, DEFAULT_SERVER_HOME_DIR);
    }
}
