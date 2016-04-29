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

package org.kaaproject.kaa.client.logging.memory;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.LogRecord;

import java.util.List;

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

    @Test
    public void shrinkToSizeTest() {
        MemBucket bucket = new MemBucket(1, 100, 100);
        addNRecordsToBucket(bucket, 10);
        List<LogRecord> overSize = bucket.shrinkToSize(10, 4);
        Assert.assertEquals(3, bucket.getCount());
        Assert.assertEquals(9, bucket.getSize());
        Assert.assertEquals(7, overSize.size());

        bucket = new MemBucket(1, 100, 100);
        addNRecordsToBucket(bucket, 10);
        overSize = bucket.shrinkToSize(10, 2);
        Assert.assertEquals(2, bucket.getCount());
        Assert.assertEquals(6, bucket.getSize());
        Assert.assertEquals(8, overSize.size());

        overSize = bucket.shrinkToSize(400, 400);
        Assert.assertEquals(0, overSize.size());
    }

    private void addNRecordsToBucket(MemBucket bucket, int n) {
        while (n-- > 0) {
            bucket.addRecord(new LogRecord());
        }
    }
}
