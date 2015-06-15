/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.client.logging.memory;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.LogRecord;

public class MemBucketTest {

    public void addLogRecordTestHelper(int maxSize, int maxRecordCount) {
        MemBucket bucket = new MemBucket(1, maxSize, maxRecordCount);

        int curSize = 0;
        int curRecordCount = 0;

        LogRecord record = new LogRecord();

        while (curSize + record.getSize() <= maxSize && curRecordCount < maxRecordCount) {
            Assert.assertTrue(bucket.addRecord(record));
            curRecordCount++;
            curSize += 3;
        }

        Assert.assertFalse(bucket.addRecord(record));
    }

    @Test
    public void addLogRecordTest() {
        addLogRecordTestHelper(10, 2);
        addLogRecordTestHelper(14, 10);
        addLogRecordTestHelper(2, 10);
        addLogRecordTestHelper(10, 1);
    }

}
