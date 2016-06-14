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

package org.kaaproject.kaa.server.control;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;

/**
 * The Class ControlServerLogAppenderIT.
 */
public class ControlServerLogAppenderIT extends AbstractTestControlServer {

    /**
     * Gets the log appenders by application token test.
     *
     * @return the log appenders by application token test
     * @throws Exception the exception
     */
    @Test
    public void getLogAppendersByApplicationTokenTest() throws Exception {
        LogAppenderDto appenderDto = createLogAppender(null, null);
        List<LogAppenderDto> found = client.getLogAppendersByAppToken(appenderDto.getApplicationToken());
        Assert.assertEquals(1, found.size());
    }

    /**
     * Gets the log appenders by id test.
     *
     * @return the log appenders by id test
     * @throws Exception the exception
     */
    @Test
    public void getLogAppendersByIdTest() throws Exception {
        LogAppenderDto appenderDto = createLogAppender(null, null);
        LogAppenderDto found = client.getLogAppender(appenderDto.getId());
        Assert.assertEquals(appenderDto, found);
    }

    /**
     * Delete log appender test.
     *
     * @throws Exception the exception
     */
    @Test
    public void deleteLogAppenderTest() throws Exception {
        final LogAppenderDto appenderDto = createLogAppender(null, null);
        client.deleteLogAppender(appenderDto.getId());
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getLogAppender(appenderDto.getId());
            }
        });
    }
}
