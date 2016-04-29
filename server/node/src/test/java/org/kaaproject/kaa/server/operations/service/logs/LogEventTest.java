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

package org.kaaproject.kaa.server.operations.service.logs;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;

public class LogEventTest {
    
    private static final byte[] logData = {1, 2, 3};
    private static final byte[] fakeLogData = {1, 2, 4};
    
    @Test
    public void basicLogEventTest() {
        LogEvent logEvent = new LogEvent();
        
        Assert.assertNull(logEvent.getLogData());
        
        logEvent.setLogData(logData);
        
        Assert.assertEquals(logData, logEvent.getLogData());
        
        Assert.assertNotEquals(fakeLogData, logEvent.getLogData());
    }
}
