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
        dto.setPluginClassName("not.existing.class.name");
        builder.getAppender(dto);
    }

    @Test(expected = ReflectiveOperationException.class)
    public void testGetProtectedAppender() throws ReflectiveOperationException {
        LogAppenderDto dto = new LogAppenderDto();
        dto.setPluginClassName(TestPrivateAppender.class.getName());
        builder.getAppender(dto);
    }

    @Test
    public void testGetPublicAppender() throws ReflectiveOperationException {
        LogAppenderDto dto = new LogAppenderDto();
        dto.setPluginClassName(TestPublicAppender.class.getName());
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
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setAppenderId(String appenderId) {
        }

        @Override
        public String getAppenderId() {
            return null;
        }

        @Override
        public void setApplicationToken(String applicationToken) {
        }

        @Override
        public void init(LogAppenderDto appender) {
        }

        @Override
        public void doAppend(LogEventPack logEventPack, LogDeliveryCallback callback) {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isSchemaVersionSupported(int version) {
            return true;
        }

        @Override
        public boolean isDeliveryConfirmationRequired() {
            return false;
        }
    }
}
