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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MongoDataLoader {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDataLoader.class);

    public static final String DATA_FILE = "mongo.data";
    public static final String COLLECTION_NAME_LINE = "#";

    private static DBCollection currentCollection = null;

    public static void loadData() throws IOException {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(DATA_FILE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String jsonLine="";
        while ((jsonLine = reader.readLine()) != null) {
            if (StringUtils.isNotBlank(jsonLine)) {
                String currentLine = jsonLine.trim();
                if (jsonLine.startsWith(COLLECTION_NAME_LINE)) {
                    setCollectionFromName(currentLine);
                } else {
                    currentCollection.insert((DBObject) JSON.parse(jsonLine), WriteConcern.ACKNOWLEDGED);
                }
            }
        }
        input.close();
        LOG.info("Load data finished.");
    }

    private static void setCollectionFromName(String line) {
        int idx = line.indexOf(COLLECTION_NAME_LINE);
        if (idx != -1) {
            String collectionName = line.substring(++idx, line.length()).trim();
            if (StringUtils.isNotEmpty(collectionName)) {
                LOG.info("Loading data into " + collectionName + " collection");
                currentCollection = MongoDBTestRunner.getDB().getCollectionFromString(collectionName);
            } else {
                new RuntimeException("Incorrect collection name:" + collectionName
                        + ". Please write collection name in correct format: # collectionName");
            }
        } else {
            throw new RuntimeException("Incorrect format of data file. Please write collection name in correct format: # collectionName");
        }
    }

    public static void clearDBData() {
        DB db = MongoDBTestRunner.getDB();
        if (db != null) {
            db.dropDatabase();
        }
    }
}
