package org.kaaproject.kaa.server.appenders.cdap.appender;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.util.concurrent.ListenableFuture;

import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.common.http.exception.HttpFailureException;

public class CdapLogAppenderTest {
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
    public void testAppend() throws IOException{
        LogEventPack logEventPack = getLogEventPack();
        
        Mockito.when(writer.write(Mockito.anyString(), Mockito.any(Charset.class))).thenReturn(new DummyFuture());
        
        LogDeliveryCallback callback = Mockito.mock(LogDeliveryCallback.class);
        appender.doAppend(logEventPack, callback);
        Mockito.verify(callback, Mockito.timeout(10000)).onSuccess();
    }
    
    @Test
    public void testAppendWithServerFailure() throws IOException{
        LogEventPack logEventPack = getLogEventPack();
        
        Mockito.when(writer.write(Mockito.anyString(), Mockito.any(Charset.class))).thenReturn(new DummyFuture(){
    		@Override
    		public Void get() throws InterruptedException,
    				ExecutionException {
    			throw new ExecutionException(new HttpFailureException("fail", 500));
    		}    		
        });
        
        LogDeliveryCallback callback = Mockito.mock(LogDeliveryCallback.class);
        appender.doAppend(logEventPack, callback);
        Mockito.verify(callback, Mockito.timeout(10000)).onRemoteError();
    }
    
    @Test
    public void testAppendToClosed() throws IOException{
        LogEventPack logEventPack = getLogEventPack();
        appender.close();
        LogDeliveryCallback callback = Mockito.mock(LogDeliveryCallback.class);
        appender.doAppend(logEventPack, callback);
        Mockito.verify(callback, Mockito.timeout(10000)).onInternalError();
    }

	private LogEventPack getLogEventPack() throws IOException {
		GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile theLog = new BasicEndpointProfile("test");

        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setSchema(BasicEndpointProfile.SCHEMA$.toString());
        LogSchema schema = new LogSchema(schemaDto);
        LogEvent logEvent = new LogEvent();

        logEvent.setLogData(converter.encode(theLog));
        LogEventPack logEventPack = new LogEventPack("endpointKey", 1234567l, schema, Collections.singletonList(logEvent));
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
		public Void get(long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
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
