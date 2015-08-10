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

package org.kaaproject.kaa.server.appenders.flume.appender;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.flume.appender.client.FlumeClientManager;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeConfig;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeEventFormat;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeNode;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeNodes;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.mockito.Mockito;
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
    private FlumeClientManager<?> flumeClientManger;

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
    public void initTest() throws IOException {
        LOG.debug("Init test for appender name: {}, id: {}",
                appender.getName(), appender.getAppenderId());

        LogAppenderDto logAppender = new LogAppenderDto();
        logAppender.setApplicationId(APPLICATION_ID);
        logAppender.setId(APPENDER_ID);

        FlumeNodes nodes = FlumeNodes
                .newBuilder()
                .setFlumeNodes(
                        Arrays.asList(new FlumeNode("localhost", 12121),
                                new FlumeNode("localhost", 12122))).build();
        
        FlumeConfig flumeConfig = FlumeConfig.newBuilder().setFlumeEventFormat(FlumeEventFormat.RECORDS_CONTAINER).setHostsBalancing(nodes).setExecutorThreadPoolSize(2).setCallbackThreadPoolSize(2).setClientsThreadPoolSize(2).build();
        
        AvroByteArrayConverter<FlumeConfig> converter = new AvroByteArrayConverter<>(FlumeConfig.class);
        byte[] rawConfiguration = converter.toByteArray(flumeConfig);
        
        logAppender.setRawConfiguration(rawConfiguration);

        appender.init(logAppender);
        appender.close();
    }

    @Test
    public void appendWithExceptionTest() throws EventDeliveryException {
        LogEventPack eventPack = new LogEventPack();
        Mockito.when(flumeEventBuilder.generateEvents(Mockito.any(LogEventPack.class), Mockito.any(RecordHeader.class), Mockito.anyString())).thenReturn(Collections.singletonList(Mockito.mock(Event.class)));
        doThrow(new EventDeliveryException()).when(flumeClientManger).sendEventsToFlume(Mockito.anyList());
        TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
        appender.doAppend(eventPack, callback);
        Assert.assertTrue(callback.connectionError);
    }

    @Test
    public void appendTest() throws EventDeliveryException {
        LogEventPack eventPack = new LogEventPack();
        Mockito.when(flumeEventBuilder.generateEvents(Mockito.any(LogEventPack.class), Mockito.any(RecordHeader.class), Mockito.anyString())).thenReturn(Collections.singletonList(Mockito.mock(Event.class)));
        TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
        appender.doAppend(eventPack, callback);
        Assert.assertTrue(callback.success);

    }

    @Test
    public void appendWithEmptyClientManagerTest() throws EventDeliveryException {
        LogEventPack eventPack = new LogEventPack();
        ReflectionTestUtils.setField(appender, "flumeClientManger", null);
        TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
        appender.doAppend(eventPack, callback);
        Assert.assertTrue(callback.internallError);
    }
    
    private static class TestLogDeliveryCallback implements LogDeliveryCallback{

    	private volatile boolean success;
    	private volatile boolean internallError;
    	private volatile boolean connectionError;
    	private volatile boolean remoteError;
    	
		@Override
		public void onSuccess() {
			success = true;
		}

		@Override
		public void onInternalError() {
			internallError = true;
		}

		@Override
		public void onConnectionError() {
			connectionError = true;
		}

		@Override
		public void onRemoteError() {
			remoteError = true;
		}
    	
    }
}
