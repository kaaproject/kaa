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

package org.kaaproject.kaa.server.common.core.algorithms.generation;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationGenerationExceptionTest {

    @Test
    public void testConfigurationProcessingExceptionEmptyConstructor() {
        ConfigurationGenerationException configurationProcessingException = new ConfigurationGenerationException();

        Assert.assertNull(configurationProcessingException.getMessage());
        Assert.assertNull(configurationProcessingException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageParameter() {
        String message = "test_message";
        ConfigurationGenerationException configurationProcessingException = new ConfigurationGenerationException(message);

        Assert.assertEquals(message, configurationProcessingException.getMessage());
        Assert.assertNull(configurationProcessingException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageAndCauseParameters() {
        String message = "test_message";
        Exception cause = new RuntimeException();
        ConfigurationGenerationException configurationProcessingException = new ConfigurationGenerationException(message, cause);

        Assert.assertEquals(message, configurationProcessingException.getMessage());
        Assert.assertTrue(cause == configurationProcessingException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithCauseParameter() {
        Exception cause = new RuntimeException();
        ConfigurationGenerationException configurationProcessingException = new ConfigurationGenerationException(cause);

        Assert.assertEquals(cause.toString(), configurationProcessingException.getMessage());
        Assert.assertTrue(cause == configurationProcessingException.getCause());
    }
}
