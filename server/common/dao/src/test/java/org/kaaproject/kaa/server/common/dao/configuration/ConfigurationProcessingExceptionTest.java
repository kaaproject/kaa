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

package org.kaaproject.kaa.server.common.dao.configuration;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationProcessingExceptionTest {

    @Test
    public void testConfigurationProcessingExceptionEmptyConstructor() {
        ConfigurationProcessingException configurationProcessingException = new ConfigurationProcessingException();

        Assert.assertNull(configurationProcessingException.getMessage());
        Assert.assertNull(configurationProcessingException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageParameter() {
        String message = "test_message";
        ConfigurationProcessingException configurationProcessingException = new ConfigurationProcessingException(message);

        Assert.assertEquals(message, configurationProcessingException.getMessage());
        Assert.assertNull(configurationProcessingException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageAndCauseParameters() {
        String message = "test_message";
        Exception cause = new RuntimeException();
        ConfigurationProcessingException configurationProcessingException = new ConfigurationProcessingException(message, cause);

        Assert.assertEquals(message, configurationProcessingException.getMessage());
        Assert.assertTrue(cause == configurationProcessingException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithCauseParameter() {
        Exception cause = new RuntimeException();
        ConfigurationProcessingException configurationProcessingException = new ConfigurationProcessingException(cause);

        Assert.assertEquals(cause.toString(), configurationProcessingException.getMessage());
        Assert.assertTrue(cause == configurationProcessingException.getCause());
    }
}
