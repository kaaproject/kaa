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

package org.kaaproject.kaa.client.logging;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractLogStorageTest {
    private void testAddHelper(int addedN, int bucketSize, int recordCount, int expectedN) {
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        List<LogRecord> expectedList = new LinkedList<>();
        LogRecord record = new LogRecord();

        while (addedN-- > 0) {
            storage.addLogRecord(record);
        }

        while (expectedN-- > 0) {
            expectedList.add(record);
        }

        LogBucket group = storage.getNextBucket();
        List<LogRecord> actualList = group.getRecords();

        Assert.assertTrue("Expected: " + expectedList.size() + ", actual: " + actualList.size()
                , expectedList.size() == actualList.size());

        Iterator<LogRecord> expectedIt = expectedList.iterator();
        Iterator<LogRecord> actualIt = actualList.iterator();

        while (expectedIt.hasNext()) {
            LogRecord expected = expectedIt.next();
            LogRecord actual = actualIt.next();

            Assert.assertTrue(expected.getSize() == actual.getSize());
            Assert.assertArrayEquals(expected.getData(), actual.getData());
        }

        storage.close();
    }

    @Test
    public void testEmptyLogRecord() {
        long bucketSize = 3;
        int recordCount = 3;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogBucket group = storage.getNextBucket();
        Assert.assertTrue(group == null);
        storage.close();
    }

    @Test
    public void testRecordCountAndConsumedBytes() {
        long bucketSize = 3;
        int recordCount = 3;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogRecord record = new LogRecord();
        int insertionCount = 3;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        Assert.assertTrue(storage.getStatus().getRecordCount() == insertionCount);
        Assert.assertTrue(storage.getStatus().getConsumedVolume() == (insertionCount * record.getSize()));
        storage.close();
    }

    @Test
    public void testUniqueIdGeneration() {
        long bucketSize = 3;
        int recordCount = 3;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogRecord record = new LogRecord();

        int insertionCount = 3;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBucket group1 = storage.getNextBucket();
        LogBucket group2 = storage.getNextBucket();

        Assert.assertNotEquals(group1.getBucketId(), group2.getBucketId());
        storage.close();
    }

    @Test
    public void testLogRecordAdding() {
        /*
         * Size of each record is 3B
         */
        testAddHelper(1, 3, 1, 1);
        testAddHelper(4, 3, 2, 1);
        testAddHelper(3, 9, 4, 3);
        testAddHelper(5, 5, 2, 1);
    }

    @Test
    public void testGetSameLogBlock() {
        long bucketSize = 3;
        int recordCount = 3;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogRecord record = new LogRecord();

        int insertionCount = 3;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBucket group1 = storage.getNextBucket();

        storage.rollbackBucket(group1.getBucketId());

        LogBucket group2 = storage.getNextBucket();

        Assert.assertTrue("Expected: " + group1.getRecords().size() + ", actual: " + group2.getRecords().size()
                , group1.getRecords().size() == group2.getRecords().size());

        Iterator<LogRecord> expectedIt = group1.getRecords().iterator();
        Iterator<LogRecord> actualIt = group2.getRecords().iterator();

        while (expectedIt.hasNext()) {
            LogRecord expected = expectedIt.next();
            LogRecord actual = actualIt.next();

            Assert.assertTrue(expected.getSize() == actual.getSize());
            Assert.assertArrayEquals(expected.getData(), actual.getData());
        }
        storage.close();
    }

    @Test
    public void testLogRecordRemoval() {
        long bucketSize = 9;
        int recordCount = 3;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogRecord record = new LogRecord();

        int insertionCount = 7 ;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBucket removingBlock = storage.getNextBucket();

        insertionCount -= removingBlock.getRecords().size();
        storage.removeBucket(removingBlock.getBucketId());

        removingBlock = storage.getNextBucket();

        insertionCount -= removingBlock.getRecords().size();
        storage.removeBucket(removingBlock.getBucketId());

        LogBucket leftBlock = storage.getNextBucket();
        Assert.assertTrue(leftBlock.getRecords().size() == insertionCount);
        storage.close();
    }

    @Test
    public void testComplexLogRemoval() {
        long bucketSize = 9;
        int recordCount = 3;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogRecord record = new LogRecord();

        int insertionCount = 8;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBucket removingBlock1 = storage.getNextBucket();
        insertionCount -= removingBlock1.getRecords().size();

        LogBucket removingBlock2 = storage.getNextBucket();
        insertionCount -= removingBlock2.getRecords().size();

        LogBucket removingBlock3 = storage.getNextBucket();
        insertionCount -= removingBlock3.getRecords().size();

        storage.removeBucket(removingBlock2.getBucketId());
        storage.rollbackBucket(removingBlock1.getBucketId());
        insertionCount += removingBlock1.getRecords().size();

        LogBucket leftBlock1 = storage.getNextBucket();
        LogBucket leftBlock2 = storage.getNextBucket();
        int leftSize = leftBlock1.getRecords().size();
        if (leftBlock2 != null) {
            leftSize += leftBlock2.getRecords().size();
        }
        Assert.assertTrue("Ac: " + leftSize + ", ex: " + insertionCount
                , leftSize == insertionCount);
        storage.close();
    }

    @Test
    public void testLogStoreCountAndVolume() {
        long bucketSize = 9;
        int recordCount = 3;
        LogStorage storage = (LogStorage) getStorage(bucketSize, recordCount);
        LogRecord record = new LogRecord();

        int insertionCount = 9;
        int receivedCount = 0;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBucket logBlock = storage.getNextBucket();
        receivedCount = addIfNotEmpty(receivedCount, logBlock);
        Assert.assertEquals(insertionCount - receivedCount, storage.getStatus().getRecordCount());
        Assert.assertEquals((insertionCount - receivedCount) * 3, storage.getStatus().getConsumedVolume());

        logBlock = storage.getNextBucket();
        receivedCount = addIfNotEmpty(receivedCount, logBlock);
        Assert.assertEquals(insertionCount - receivedCount, storage.getStatus().getRecordCount());
        Assert.assertEquals((insertionCount - receivedCount) * 3, storage.getStatus().getConsumedVolume());

        logBlock = storage.getNextBucket();
        receivedCount = addIfNotEmpty(receivedCount, logBlock);
        Assert.assertEquals(insertionCount - receivedCount, storage.getStatus().getRecordCount());
        Assert.assertEquals((insertionCount - receivedCount) * 3, storage.getStatus().getConsumedVolume());

        storage.rollbackBucket(logBlock.getBucketId());
        receivedCount -= logBlock.getRecords().size();
        Assert.assertEquals(insertionCount - receivedCount, storage.getStatus().getRecordCount());
        Assert.assertEquals((insertionCount - receivedCount) * 3, storage.getStatus().getConsumedVolume());
        storage.close();
    }

    private int addIfNotEmpty(int count, LogBucket logBlock) {
        if (logBlock != null && logBlock.getRecords().size() > 0) {
            count += logBlock.getRecords().size();
        }
        return count;
    }

    protected abstract Object getStorage(long bucketSize, int recordCount);
}
