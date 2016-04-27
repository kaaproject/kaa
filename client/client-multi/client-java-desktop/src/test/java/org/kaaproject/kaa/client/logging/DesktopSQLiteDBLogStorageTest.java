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

import org.junit.After;
import org.junit.Before;
import java.io.File;
import java.sql.SQLException;

public class DesktopSQLiteDBLogStorageTest extends AbstractPersistentLogStorageTest {
    private static final String DB_FILENAME = "test.db";
    private static File dbFile = new File(DB_FILENAME);

    @Before
    public void prepare() throws ClassNotFoundException, SQLException {
        deleteDBFile();
    }

    @After
    public void cleanup() {
        deleteDBFile();
    }

    @Override
    protected DesktopSQLiteDBLogStorage getStorage(long bucketSize, int recordCount) {
        return new DesktopSQLiteDBLogStorage(DB_FILENAME, bucketSize, recordCount);
    }

    private void deleteDBFile() {
        dbFile.delete();
    }
}
