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

package org.kaaproject.kaa.server.control;

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;

import java.io.IOException;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;

public class ControlServerLogAppenderIT extends AbstractTestControlServer {

    @Test
    public void getLogAppendersByApplicationIdTest() throws Exception {
        LogAppenderDto appenderDto = createLogAppender(null, null);
        List<LogAppenderDto> found = client.getLogAppenders(appenderDto.getApplicationId());
        Assert.assertEquals(1, found.size());
    }

    @Test
    public void getLogAppendersByIdTest() throws Exception {
        LogAppenderDto appenderDto = createLogAppender(null, null);
        LogAppenderDto found = client.getLogAppender(appenderDto.getId());
        Assert.assertEquals(appenderDto, found);
    }

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
