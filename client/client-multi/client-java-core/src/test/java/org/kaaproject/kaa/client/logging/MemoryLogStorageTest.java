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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class MemoryLogStorageTest {

    private void testAddHelper(int addedN, int blockSize, int expectedN) {
        MemoryLogStorage storage = new MemoryLogStorage(blockSize);
        List<LogRecord> expectedList = new LinkedList<>();
        LogRecord record = new LogRecord();

        while (addedN-- > 0) {
            storage.addLogRecord(record);
        }

        while (expectedN-- > 0) {
            expectedList.add(record);
        }

        LogBlock group = storage.getRecordBlock(blockSize);
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
    }

    @Test
    public void testEmptyLogRecord() {
        long bucketSize = 3;
        MemoryLogStorage storage = new MemoryLogStorage(bucketSize);
        LogBlock group = storage.getRecordBlock(5);
        Assert.assertTrue(group == null);
    }

    @Test
    public void testRecordCountAndConsumedBytes() {
        long bucketSize = 3;
        MemoryLogStorage storage = new MemoryLogStorage(bucketSize);
        LogRecord record = new LogRecord();
        int insertionCount = 3;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        Assert.assertTrue(storage.getRecordCount() == insertionCount);
        Assert.assertTrue(storage.getConsumedVolume() == (insertionCount * record.getSize()));
    }

    @Test
    public void testUniqueIdGeneration() {
        long bucketSize = 3;
        MemoryLogStorage storage = new MemoryLogStorage(bucketSize);
        LogRecord record = new LogRecord();

        int insertionCount = 3;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBlock group1 = storage.getRecordBlock(6);
        LogBlock group2 = storage.getRecordBlock(6);

        Assert.assertNotEquals(group1.getBlockId(), group2.getBlockId());
    }

    @Test
    public void testLogRecordAdding() {
        /*
         * Size of each record is 3B
         */
        testAddHelper(1, 3, 1);
        testAddHelper(4, 3, 1);
        testAddHelper(3, 9, 3);
        testAddHelper(5, 5, 1);
    }

    @Test
    public void testGetSameLogBlock() {
        long bucketSize = 3;
        MemoryLogStorage storage = new MemoryLogStorage(bucketSize);
        LogRecord record = new LogRecord();

        int insertionCount = 3;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBlock group1 = storage.getRecordBlock(7);

        storage.notifyUploadFailed(group1.getBlockId());

        LogBlock group2 = storage.getRecordBlock(7);

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
    }

    @Test
    public void testLogRecordRemoval() {
        long bucketSize = 3;
        MemoryLogStorage storage = new MemoryLogStorage(bucketSize);
        LogRecord record = new LogRecord();

        int insertionCount = 7;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBlock removingBlock = storage.getRecordBlock(7);

        insertionCount -= removingBlock.getRecords().size();
        storage.removeRecordBlock(removingBlock.getBlockId());

        removingBlock = storage.getRecordBlock(9);

        insertionCount -= removingBlock.getRecords().size();
        storage.removeRecordBlock(removingBlock.getBlockId());

        LogBlock leftBlock = storage.getRecordBlock(50);
        Assert.assertTrue(leftBlock.getRecords().size() == insertionCount);
    }

    @Test
    public void testComplexLogRemoval() {
        long bucketSize = 3;
        MemoryLogStorage storage = new MemoryLogStorage(bucketSize);
        LogRecord record = new LogRecord();

        int insertionCount = 8;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBlock removingBlock1 = storage.getRecordBlock(7);
        insertionCount -= removingBlock1.getRecords().size();

        LogBlock removingBlock2 = storage.getRecordBlock(9);
        insertionCount -= removingBlock2.getRecords().size();

        LogBlock removingBlock3 = storage.getRecordBlock(6);
        insertionCount -= removingBlock3.getRecords().size();

        storage.removeRecordBlock(removingBlock2.getBlockId());
        storage.notifyUploadFailed(removingBlock1.getBlockId());
        insertionCount += removingBlock1.getRecords().size();

        LogBlock leftBlock = storage.getRecordBlock(50);
        Assert.assertTrue("Ac: " + leftBlock.getRecords().size() + ", ex: " + insertionCount
                , leftBlock.getRecords().size() == insertionCount);
    }

    @Test
    public void testOldestRecordRemoval() {
        long bucketSize = 3;
        MemoryLogStorage storage = new MemoryLogStorage(bucketSize);
        LogRecord record = new LogRecord();

        int insertionCount = 8;

        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        long maxAllowedSize = insertionCount * record.getSize() - 4;
        storage.removeOldestRecord(maxAllowedSize);

        LogBlock block = storage.getRecordBlock(50);

        Assert.assertTrue("Ac: " + block.getRecords().size() + ", ex: " + (maxAllowedSize / record.getSize())
                , block.getRecords().size() == (maxAllowedSize / record.getSize()));
    }
}