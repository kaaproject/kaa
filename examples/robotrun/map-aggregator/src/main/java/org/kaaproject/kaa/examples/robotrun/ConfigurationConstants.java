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

package org.kaaproject.kaa.examples.robotrun;

public interface ConfigurationConstants {

    public static final String CONFIG_TXN_EVENT_MAX = "txnEventMax";
    public static final long DEFAULT_TXN_EVENT_MAX = 10;
 
    public static final String CONFIG_KAA_REST_HOST = "kaa.rest.host";
    public static final String DEFAULT_KAA_REST_HOST = "localhost";
    
    public static final String CONFIG_KAA_REST_PORT = "kaa.rest.port";
    public static final int DEFAULT_KAA_REST_PORT = 8080;
    
    public static final String CONFIG_KAA_REST_USER = "kaa.rest.user";
    public static final String CONFIG_KAA_REST_PASSWORD = "kaa.rest.password";
    
    public static final String CONFIG_SINK_HOST = "sink.host";
    public static final String CONFIG_SINK_PORT = "sink.port";

}
