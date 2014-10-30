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

package org.kaaproject.kaa.server.operations.service.logs.flume;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.apache.flume.EventDeliveryException;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.operations.service.logs.flume.client.FlumeClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class FlumeLogAppenderTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeLogAppenderTest.class);

    private static final String APPLICATION_ID = "application_id";
    private static final String APPENDER_ID = "appender_id";
    private static final String APPENDER_NAME = "appender_name";

    private FlumeLogAppender appender;
    private FlumeEventBuilder flumeEventBuilder;
    private FlumeClientManager flumeClientManger;

    @Before
    public void before() throws Exception {
        appender = new FlumeLogAppender();
        appender.setName(APPENDER_NAME);
        appender.setAppenderId(APPENDER_ID);

        flumeEventBuilder = mock(FlumeEventBuilder.class);
        flumeClientManger = mock(FlumeClientManager.class);

        ReflectionTestUtils.setField(appender, "flumeEventBuilder", flumeEventBuilder);
        ReflectionTestUtils.setField(appender, "flumeClientManger", flumeClientManger);
    }

    @Test
    public void initTest() {
        LOG.debug("Init test for appender name: {}, id: {}", appender.getName(), appender.getAppenderId());
        FlumeAppenderParametersDto parametersDto = new FlumeAppenderParametersDto();
        parametersDto.setBalancingType(FlumeBalancingTypeDto.ROUND_ROBIN);
        parametersDto.setHosts(Arrays.asList(new HostInfoDto("localhost", 12121, 0), new HostInfoDto("localhost", 12122, 0)));

        LogAppenderDto logAppender = new LogAppenderDto();
        logAppender.setApplicationId(APPLICATION_ID);
        logAppender.setId(APPENDER_ID);

        logAppender.setProperties(new LogAppenderParametersDto(parametersDto));
        appender.init(logAppender);
        appender.close();
    }

    @Test
    public void appendWithExceptionTest() throws EventDeliveryException {
        LogEventPack eventPack = new LogEventPack();
        doThrow(new EventDeliveryException()).when(flumeClientManger).sendEventToFlume(null);
        appender.doAppend(eventPack);
    }

    @Test
    public void appendTest() throws EventDeliveryException {
        LogEventPack eventPack = new LogEventPack();
        appender.doAppend(eventPack);
    }

    @Test
    public void appendWithEmptyClientManagerTest() throws EventDeliveryException {
        LogEventPack eventPack = new LogEventPack();
        ReflectionTestUtils.setField(appender, "flumeClientManger", null);
        appender.doAppend(eventPack);
    }
}
