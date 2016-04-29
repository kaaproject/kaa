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

package org.kaaproject.kaa.server.operations.service.filter;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.operations.service.filter.el.GenericRecordPropertyAccessor;
import org.springframework.expression.AccessException;

public class GenericRecordPropertyAccessorTest {

    @Test
    public void canWriteTest() throws AccessException {
        GenericRecordPropertyAccessor accessor = new GenericRecordPropertyAccessor();
        Assert.assertFalse(accessor.canWrite(null, null, null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void writeTest() throws AccessException {
        GenericRecordPropertyAccessor accessor = new GenericRecordPropertyAccessor();
        accessor.write(null, null, null, null);
    }
}
