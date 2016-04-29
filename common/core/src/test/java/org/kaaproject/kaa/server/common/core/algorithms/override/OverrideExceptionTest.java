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

package org.kaaproject.kaa.server.common.core.algorithms.override;

import org.junit.Assert;
import org.junit.Test;

public class OverrideExceptionTest {

    @Test
    public void testMergeExceptionEmptyConstructor() {
        OverrideException mergeException = new OverrideException();

        Assert.assertNull(mergeException.getMessage());
        Assert.assertNull(mergeException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageParameter() {
        String message = "test_message";
        OverrideException mergeException = new OverrideException(message);

        Assert.assertEquals(message, mergeException.getMessage());
        Assert.assertNull(mergeException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageAndCauseParameters() {
        String message = "test_message";
        Exception cause = new RuntimeException();
        OverrideException mergeException = new OverrideException(message, cause);

        Assert.assertEquals(message, mergeException.getMessage());
        Assert.assertTrue(cause == mergeException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithCauseParameter() {
        Exception cause = new RuntimeException();
        OverrideException mergeException = new OverrideException(cause);

        Assert.assertEquals(cause.toString(), mergeException.getMessage());
        Assert.assertTrue(cause == mergeException.getCause());
    }
}
