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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SQLiteDBLogStorageTest {
    private static File dbFile = new File("test.db");

    @BeforeClass
    public static void prepareDB() throws ClassNotFoundException, SQLException {
        deleteDBFile();
    }

    @Test
    public void testConstructor() throws IOException {
        SQLiteDBLogStorage storage = new SQLiteDBLogStorage();

        storage.addLogRecord(new LogRecord());
        storage.addLogRecord(new LogRecord());

        Assert.assertEquals(2, storage.getRecordCount());

        storage.close();
    }

    @AfterClass
    public static void cleanupDB() {
        deleteDBFile();
    }

    private static void deleteDBFile() {
        dbFile.delete();
    }
}
