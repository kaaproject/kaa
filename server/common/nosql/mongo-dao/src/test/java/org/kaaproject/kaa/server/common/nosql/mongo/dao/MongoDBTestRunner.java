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
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MongoDBTestRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBTestRunner.class);

    private static final String MONGO_HOST = "localhost";
    private static final int DEFAULT_PORT = 27717;
    private static final String DB_NAME = "kaa";

    private static MongodExecutable mongoDBExec;
    private static MongodProcess mongod;
    private static MongoClient mongo;

    public static void setUp() throws Exception {
        setUp(DEFAULT_PORT);
    }

    public static void setUp(int port) throws Exception {
        LOG.info("Embedded MongoDB server started on " + port + " port and " + MONGO_HOST + " host.");
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongoDBExec = runtime.prepare(createMongodConfig(port));
        mongod = mongoDBExec.start();
        mongo = new MongoClient(MONGO_HOST, port);
    }

    protected static IMongodConfig createMongodConfig(int port) throws Exception {
        return createMongodConfigBuilder(port).build();
    }

    protected static MongodConfigBuilder createMongodConfigBuilder(int port) throws Exception {
        return new MongodConfigBuilder()
                .version(Version.Main.V2_4)
                .net(new Net(port, Network.localhostIsIPv6()));
    }

    public static void tearDown() throws Exception {
        mongod.stop();
        mongoDBExec.stop();
    }

    public static MongoClient getMongo() {
        return mongo;
    }

    public static DB getDB() {
        DB db = null;
        if (mongo != null) {
            db = mongo.getDB(DB_NAME);
        }
        return db;
    }
}
