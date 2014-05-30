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

package org.kaaproject.kaa.server.operations.service.delta.merge;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.operations.service.delta.merge.MergeException;

public class MergeExceptionTest {

    @Test
    public void testMergeExceptionEmptyConstructor() {
        MergeException mergeException = new MergeException();

        Assert.assertNull(mergeException.getMessage());
        Assert.assertNull(mergeException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageParameter() {
        String message = "test_message";
        MergeException mergeException = new MergeException(message);

        Assert.assertEquals(message, mergeException.getMessage());
        Assert.assertNull(mergeException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithMessageAndCauseParameters() {
        String message = "test_message";
        Exception cause = new RuntimeException();
        MergeException mergeException = new MergeException(message, cause);

        Assert.assertEquals(message, mergeException.getMessage());
        Assert.assertTrue(cause == mergeException.getCause());
    }

    @Test
    public void testMergeExceptionConstructorWithCauseParameter() {
        Exception cause = new RuntimeException();
        MergeException mergeException = new MergeException(cause);

        Assert.assertEquals(cause.toString(), mergeException.getMessage());
        Assert.assertTrue(cause == mergeException.getCause());
    }
}
