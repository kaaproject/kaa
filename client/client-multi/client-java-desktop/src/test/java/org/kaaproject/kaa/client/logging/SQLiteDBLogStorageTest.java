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

package org.kaaproject.kaa.client.logging;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;

public class SQLiteDBLogStorageTest extends AbstractLogStorageTest {
    private static final String DB_FILENAME = "target/test.db";
    private static File dbFile = new File(DB_FILENAME);

    @Before
    public void prepare() throws ClassNotFoundException, SQLException {
        deleteDBFile();
    }

    @Test
    public void testPersistDBState() {
        SQLiteDBLogStorage storage = getStorage();

        LogRecord record = new LogRecord();
        int insertionCount = 7;
        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }
        LogBlock beforePersist = storage.getRecordBlock(15);
        storage.close();

        storage = getStorage();
        Assert.assertEquals(insertionCount, storage.getRecordCount());
        Assert.assertEquals(insertionCount * 3, storage.getConsumedVolume());
        LogBlock afterPersist = storage.getRecordBlock(15);

        Assert.assertEquals(beforePersist.getRecords().size(), afterPersist.getRecords().size());

        storage.close();
    }

    @Test
    public void testGetBigRecordBlock() {
        SQLiteDBLogStorage storage = getStorage();

        LogRecord record = new LogRecord();
        int insertionCount = 7;
        /*
         * Size of each record is 3B
         */
        int iter = insertionCount;
        while (iter-- > 0) {
            storage.addLogRecord(record);
        }

        LogBlock logBlock = storage.getRecordBlock(8192);
        Assert.assertEquals(insertionCount, logBlock.getRecords().size());
        storage.close();
    }

    @After
    public void cleanup() {
        deleteDBFile();
    }

    @Override
    protected SQLiteDBLogStorage getStorage(long bucketSize) {
        return getStorage();
    }

    private SQLiteDBLogStorage getStorage() {
        return new SQLiteDBLogStorage(DB_FILENAME);
    }

    private void deleteDBFile() {
        dbFile.delete();
    }
}
