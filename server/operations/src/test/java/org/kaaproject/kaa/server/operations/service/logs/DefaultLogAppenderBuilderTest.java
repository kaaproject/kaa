package org.kaaproject.kaa.server.operations.service.logs;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;

public class DefaultLogAppenderBuilderTest {

    LogAppenderBuilder builder;

    @Before
    public void before() {
        builder = new DefaultLogAppenderBuilder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAppenderConfig() throws ReflectiveOperationException {
        builder.getAppender(null);
    }

    @Test(expected = ReflectiveOperationException.class)
    public void testGetNotExistingAppender() throws ReflectiveOperationException {
        LogAppenderDto dto = new LogAppenderDto();
        dto.setAppenderClassName("not.existing.class.name");
        builder.getAppender(dto);
    }

    @Test(expected = ReflectiveOperationException.class)
    public void testGetProtectedAppender() throws ReflectiveOperationException {
        LogAppenderDto dto = new LogAppenderDto();
        dto.setAppenderClassName(TestPrivateAppender.class.getName());
        builder.getAppender(dto);
    }

    @Test
    public void testGetPublicAppender() throws ReflectiveOperationException {
        LogAppenderDto dto = new LogAppenderDto();
        dto.setAppenderClassName(TestPublicAppender.class.getName());
        builder.getAppender(dto);
    }

    public static class TestPrivateAppender extends TestPublicAppender {
        private TestPrivateAppender() {
            super();
        }
    }

    public static class TestPublicAppender implements LogAppender {
        public TestPublicAppender() {
            super();
        }

        @Override
        public void setName(String name) {
            // TODO Auto-generated method stub
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setAppenderId(String appenderId) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getAppenderId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setApplicationToken(String applicationToken) {
            // TODO Auto-generated method stub

        }

        @Override
        public void init(LogAppenderDto appender) {
            // TODO Auto-generated method stub

        }

        @Override
        public void doAppend(LogEventPack logEventPack, LogDeliveryCallback callback) {
            // TODO Auto-generated method stub

        }

        @Override
        public void close() {
            // TODO Auto-generated method stub

        }

		@Override
		public boolean isSchemaVersionSupported(int version) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isDeliveryConfirmationRequired() {
			// TODO Auto-generated method stub
			return false;
		}
    }
}
