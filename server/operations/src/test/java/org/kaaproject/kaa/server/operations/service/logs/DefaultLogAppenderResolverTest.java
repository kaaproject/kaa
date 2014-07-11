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

package org.kaaproject.kaa.server.operations.service.logs;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.operations.service.logs.mongo.MongoDBLogAppender;

public class DefaultLogAppenderResolverTest {
    
    private static final String APPENDER_NAME = "appender";
    private static final String FAKE_NAME = "fake";
    
    private static final MongoDBLogAppender MONGO_DB_LOG_APPENDER = new MongoDBLogAppender(APPENDER_NAME);
    
    private static final Map<String, LogAppender> APPENDER_MAP = new HashMap<>();
    
    private static final DefaultLogAppenderResolver DEFAULT_LOG_APPENDER_RESOLVER = new DefaultLogAppenderResolver(APPENDER_MAP);
    
    @BeforeClass
    public static void init() {
        APPENDER_MAP.put(APPENDER_NAME, MONGO_DB_LOG_APPENDER);
    }
    
    @Test
    public void resolveTest() {        
        LogAppender logAppender = DEFAULT_LOG_APPENDER_RESOLVER.resolve(APPENDER_NAME);
        Assert.assertEquals(APPENDER_NAME, logAppender.getName());
    }
    
    @Test(expected = NullPointerException.class)
    public void resolveNoSuchAppenderTest() {        
        LogAppender logAppender = DEFAULT_LOG_APPENDER_RESOLVER.resolve(FAKE_NAME);
    }
}
