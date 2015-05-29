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
package org.kaaproject.kaa.examples.powerplant;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.PersistTo;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.Stale;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ViewConsumer {
    private static final String DEFAULT_DESIGN = "dev_total_zone_view";
    private static final String DEFAULT_VIEW = "total_zone_view_dev";
    private static final String DEFAULT_CLUSTER_IP = "127.0.0.1";
    private static final String DEFAULT_DB_NAME = "powerplant_kaa_test";
    private static final long UPDATE_CHECK_TIME = 300L;
    private static final int MAX_CACHE_SIZE = 4000;
    private static final int INITIAL_CAPACITY = 3600;
    private static final long TTL_SECONDS = 300;
    private static Cache<String, JsonDocument> documentCache;

    private static String design = null;
    private static String view = null;

    private static Bucket bucket;

    public static void main(String[] args) {
        System.out.println("Usage: java -jar JarName.jar clusterIP DBName design view");

        String clusterIP = null;
        String dbName = null;

        if (args.length != 4) {
            System.out.println("Using default options:");
            clusterIP = DEFAULT_CLUSTER_IP;
            dbName = DEFAULT_DB_NAME;
            design = DEFAULT_DESIGN;
            view = DEFAULT_VIEW;
        } else {
            clusterIP = args[0];
            dbName = args[1];
            design = args[2];
            view = args[3];
        }

        System.out.println("Cluster IP: " + DEFAULT_CLUSTER_IP);
        System.out.println("DB name: " + DEFAULT_DB_NAME);
        System.out.println("Design: " + DEFAULT_DESIGN);
        System.out.println("View: " + DEFAULT_VIEW);

        // Create a cluster reference
        CouchbaseCluster cluster = CouchbaseCluster.create(clusterIP);
        documentCache = CacheBuilder
                .newBuilder()
                .initialCapacity(INITIAL_CAPACITY)
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(TTL_SECONDS, TimeUnit.SECONDS)
                .removalListener(new DocumentRemovalListener())
                .build();

        bucket = cluster.openBucket(dbName);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Worker());

        try {
            System.out.println("Press Enter to exit");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
        bucket.close();
        cluster.disconnect();
    }


    private static class Worker implements Runnable {
        private volatile int latestTs = 0;

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(UPDATE_CHECK_TIME);
                    JsonArray startKey = JsonArray.create().add(latestTs);
                    // get the latest data
                    ViewResult latestData = bucket.query(ViewQuery.from(DEFAULT_DESIGN, DEFAULT_VIEW).stale(Stale.FALSE).groupLevel(2).descending().limit(1));

                    Iterator<ViewRow> iterator = latestData.iterator();
                    if (!iterator.hasNext()) { // no data was added yet
                        continue;
                    }

                    JsonArray latestRow = (JsonArray) iterator.next().key();
                    int curLatestTs = latestRow.getInt(0);

                    if (curLatestTs >= latestTs) {
                        latestTs = curLatestTs;
                        uploadData();
                    }
                }
            } catch (InterruptedException e) {
                // thread was interrupted
                e.printStackTrace();
            }
        }

        private void uploadData() {
            JsonArray startKey = JsonArray.create().add(latestTs);

            ViewResult result = bucket.query(ViewQuery.from(design, view).stale(Stale.FALSE).groupLevel(2).startKey(startKey));

            Map<Integer, JsonObject> updatesMap = new HashMap<>();

            for (ViewRow row : result) {
                JsonArray key = (JsonArray) row.key();
                JsonObject value = (JsonObject) row.value();

                int tsInSeconds = key.getInt(0);

                JsonObject update = updatesMap.get(tsInSeconds);

                if (update == null) {
                    update = JsonObject.empty();
                    update.put("zones", JsonArray.empty());
                    update.put("ts", tsInSeconds);
                    updatesMap.put(tsInSeconds, update);
                }

                JsonObject zoneUpdate = JsonObject.empty();
                zoneUpdate.put("zoneId", key.getInt(1));
                zoneUpdate.put("sum", value.getDouble("sum"));
                zoneUpdate.put("count", value.getInt("count"));
                update.getArray("zones").add(zoneUpdate);
            }

            for (Map.Entry<Integer, JsonObject> entry : updatesMap.entrySet()) {
                JsonDocument doc = JsonDocument.create("totals_" + entry.getKey(), entry.getValue());
                System.out.println("Updating for totals_" + entry.getKey());
                bucket.upsert(doc);
                if (documentCache.getIfPresent(doc.id()) == null) {
                    System.out.println("Putting into cache: " + doc.id());
                    documentCache.put(doc.id(), doc);
                }
            }
        }
    }

    private static class DocumentRemovalListener implements RemovalListener<String, JsonDocument> {
        @Override
        public void onRemoval(RemovalNotification<String, JsonDocument> removalNotification) {
            System.out.println("Removing document with id: " + removalNotification.getValue().id());
            bucket.remove(removalNotification.getValue().id(), PersistTo.MASTER);
        }
    }
}
