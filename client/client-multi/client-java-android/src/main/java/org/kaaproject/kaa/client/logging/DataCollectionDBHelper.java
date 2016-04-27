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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataCollectionDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DataCollectionDBHelper";

    public DataCollectionDBHelper(Context context, String name) {
        super(context, name, null, PersistentLogStorageConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(TAG, "Database is being created");
        sqLiteDatabase.execSQL(PersistentLogStorageConstants.KAA_CREATE_LOG_TABLE);
        sqLiteDatabase.execSQL(PersistentLogStorageConstants.KAA_CREATE_INFO_TABLE);
        sqLiteDatabase.execSQL(PersistentLogStorageConstants.KAA_CREATE_BUCKET_ID_INDEX);
        Log.i(TAG, "Database with its tables and indices was successfully created");
    }

    /*
     * This method is called when database is upgraded like modifying
     * the table structure, adding constraints to database etc.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.i(TAG, "Database was upgraded. Dropping its contents");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PersistentLogStorageConstants.LOG_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PersistentLogStorageConstants.STORAGE_INFO_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
