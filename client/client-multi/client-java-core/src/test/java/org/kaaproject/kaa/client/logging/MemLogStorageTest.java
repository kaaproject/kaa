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

package org.kaaproject.kaa.client.logging;


import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.memory.MemLogStorage;

public class MemLogStorageTest extends AbstractLogStorageTest {
    @Override
    protected Object getStorage(long bucketSize, int recordCount) {
        return new MemLogStorage(bucketSize, recordCount);
    }

    @Test
    public void testRemovalWithBucketShrinking() {
        long bucketSize = 10;
        int recordCount = 4;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogRecord record = new LogRecord();

        int insertionCount = 12;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        long maxSize = 6;
        int maxCount = 3;
        LogBlock logBlock = storage.getRecordBlock(maxSize, maxCount);
        Assert.assertTrue(logBlock.getRecords().size() <= maxCount);
        Assert.assertTrue(getLogBlockSize(logBlock) <= maxSize);
        Assert.assertEquals(insertionCount - logBlock.getRecords().size(), ((LogStorageStatus) storage).getRecordCount());
    }

    private long getLogBlockSize(LogBlock logBlock) {
        long size = 0;
        for (LogRecord record : logBlock.getRecords()) {
            size += record.getSize();
        }
        return size;
    }
}
