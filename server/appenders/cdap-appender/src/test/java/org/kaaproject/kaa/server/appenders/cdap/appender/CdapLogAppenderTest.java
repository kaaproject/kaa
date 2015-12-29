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
package org.kaaproject.kaa.server.appenders.cdap.appender;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.common.http.exception.HttpFailureException;

import com.google.common.util.concurrent.ListenableFuture;

public class CdapLogAppenderTest {
    private static final int _10SEC = 10000;
    LogAppender appender;
    StreamWriter writer;

    @Before
    public void beforeTest() throws IOException {
        appender = new CdapLogAppender();
        writer = Mockito.mock(StreamWriter.class);
        ReflectionTestUtils.setField(appender, "header", Arrays.asList(LogHeaderStructureDto.values()));
        ReflectionTestUtils.setField(appender, "streamWriter", writer);
    }

    @Test
    public void testAppend() throws IOException {
        BaseLogEventPack logEventPack = getLogEventPack();

        Mockito.when(writer.write(Mockito.anyString(), Mockito.any(Charset.class))).thenReturn(new DummyFuture());

        LogDeliveryCallback callback = Mockito.mock(LogDeliveryCallback.class);
        appender.doAppend(logEventPack, callback);
        Mockito.verify(callback, Mockito.timeout(_10SEC)).onSuccess();
    }

    @Test
    public void testAppendWithServerFailure() throws IOException {
        BaseLogEventPack logEventPack = getLogEventPack();

        Mockito.when(writer.write(Mockito.anyString(), Mockito.any(Charset.class))).thenReturn(new DummyFuture() {
            @Override
            public Void get() throws InterruptedException, ExecutionException {
                throw new ExecutionException(new HttpFailureException("fail", 500));
            }
        });

        LogDeliveryCallback callback = Mockito.mock(LogDeliveryCallback.class);
        appender.doAppend(logEventPack, callback);
        Mockito.verify(callback, Mockito.timeout(10000)).onRemoteError();
    }

    @Test
    public void testAppendToClosed() throws IOException {
        BaseLogEventPack logEventPack = getLogEventPack();
        appender.close();
        LogDeliveryCallback callback = Mockito.mock(LogDeliveryCallback.class);
        appender.doAppend(logEventPack, callback);
        Mockito.verify(callback, Mockito.timeout(10000)).onInternalError();
    }

    private BaseLogEventPack getLogEventPack() throws IOException {
        GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile theLog = new BasicEndpointProfile("test");

        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setSchema(BasicEndpointProfile.SCHEMA$.toString());
        LogSchema schema = new LogSchema(schemaDto);
        LogEvent logEvent = new LogEvent();

        logEvent.setLogData(converter.encode(theLog));
        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", "endpointKey", 1, "", null, null);
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, 1234567l, schema.getVersion(), Collections.singletonList(logEvent));
        logEventPack.setLogSchema(schema);
        return logEventPack;
    }

    private static class DummyFuture implements ListenableFuture<Void> {
        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            listener.run();
        }
    }
}
