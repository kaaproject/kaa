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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation for {@link LogStorage} and {@link LogStorageStatus}
 */
public class MemoryLogStorage implements LogStorage, LogStorageStatus {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryLogStorage.class);
    private static final int MAX_STORAGE_SIZE = 1024 * 1024;

    private final static Random RANDOM = new Random();
    
    private class Bucket {
        private final int id;
        private final LinkedList<LogRecord> records; //NOSONAR

        private final long maxBucketSize;
        private long consumedSize;
        private boolean isUsed;

        public Bucket(long bucketSize) {
            id = RANDOM.nextInt();
            records = new LinkedList<>();
            maxBucketSize = bucketSize;
            consumedSize = 0;
            isUsed = false;
        }

        public List<LogRecord> getRecords() {
            return records;
        }

        public int getId() {
            return id;
        }

        public boolean tryPushRecord(LogRecord rec) {
            long newConsumedSize = consumedSize + rec.getSize();

            if (maxBucketSize >= newConsumedSize) {
                records.add(rec);
                consumedSize = newConsumedSize;
                return true;
            }

            return false;
        }

        public LogRecord popRecord() {
            LogRecord record = records.pop();
            consumedSize -= record.getSize();
            return record;
        }

        public boolean isUsed() {
            return isUsed;
        }

        public void setUsage(boolean state) {
            isUsed = state;
        }

        public long getConsumedSize() {
            return consumedSize;
        }
    }

    private long maxStorageSize = MAX_STORAGE_SIZE;
    private long maxBucketSize;
    private Bucket currentBucket;
    private final List<Bucket> buckets;

    private long consumedSize;
    private long recordCount;

    public MemoryLogStorage(long bucketSize) {
        maxBucketSize = bucketSize;
        consumedSize = 0;

        buckets = new LinkedList<>();
        initBucketList();
    }

    @Override
    public void addLogRecord(LogRecord record) {
        synchronized (buckets) {
            if(this.getConsumedVolume() + record.getSize() > maxStorageSize){
                removeOldestRecord(MAX_STORAGE_SIZE);
            }
            
            if (buckets.isEmpty()) {
                initBucketList();
            }

            if (currentBucket.isUsed() || !currentBucket.tryPushRecord(record)) {
                Bucket newBucket = new Bucket(maxBucketSize);
                buckets.add(newBucket);
                currentBucket = newBucket;
                currentBucket.tryPushRecord(record);
            }

            consumedSize += record.getSize();
            ++recordCount;

            LOG.trace("Added new log record, records: {}", recordCount);
        }
    }

    @Override
    public long getConsumedVolume() {
        return consumedSize;
    }

    @Override
    public long getRecordCount() {
        return recordCount;
    }

    @Override
    public LogBlock getRecordBlock(long blockSize) {
        LogBlock logBlock = null;

        synchronized (buckets) {
            if (!buckets.isEmpty()) {
                if (maxBucketSize != blockSize) {
                    resize(blockSize);
                }

                for (Bucket bucket : buckets) {
                    if (!bucket.isUsed()) {
                        bucket.setUsage(true);
                        logBlock = new LogBlock(bucket.getId(), bucket.getRecords());
                        LOG.trace("Formed log block with id: {}, records: {}"
                                , logBlock.getBlockId(),logBlock.getRecords().size());
                        break;
                    }
                }
            }
        }

        return logBlock;
    }

    @Override
    public void removeRecordBlock(int id) {
        synchronized (buckets) {
            LOG.trace("Removing log group by id: {}", id);

            if (!buckets.isEmpty()) {
                boolean isFound = false;
                Iterator<Bucket> it = buckets.iterator();

                while (it.hasNext()) {
                    Bucket bucket = it.next();
                    if (bucket.getId() == id && bucket.isUsed()) {
                        consumedSize -= bucket.getConsumedSize();
                        recordCount -= bucket.getRecords().size();
                        isFound = true;
                        LOG.trace("Successfully removed log group by id {}, removed: {}, in storage: {}"
                                , bucket.getId(), bucket.getRecords().size(), recordCount);
                        it.remove();
                        break;
                    }
                }

                if (!isFound) {
                    LOG.warn("Failed to remove log group: unknown id {}, records: {}", id);
                }
            }
        }
    }

    private void removeOldestRecord(long maximumAllowedVolume) {
        synchronized (buckets) {
            if (!buckets.isEmpty()) {
                long currentRecordCount = recordCount;
                LOG.info("Removing oldest log records up to {}B", maximumAllowedVolume);

                Iterator<Bucket> it = buckets.iterator();

                while (it.hasNext()) {
                    Bucket bucket = it.next();

                    if (!bucket.isUsed()) {
                        long newConsumedSize = consumedSize - bucket.getConsumedSize();
                        if (newConsumedSize > maximumAllowedVolume) {
                            consumedSize = newConsumedSize;
                            recordCount -= bucket.getRecords().size();
                            it.remove();
                        } else {
                            while (consumedSize > maximumAllowedVolume) {
                                consumedSize -= bucket.popRecord().getSize();
                                --recordCount;
                            }
                        }
                    }
                }

                LOG.info("{} log records was forcibly removed", (currentRecordCount - recordCount));
                resize(maxBucketSize);
            }
        }
    }

    @Override
    public void notifyUploadFailed(int id) {
        LOG.warn("Failed to upload log group with id {}. Try to send them later", id);

        Iterator<Bucket> it = buckets.iterator();

        while (it.hasNext()) {
            Bucket bucket = it.next();

            if (bucket.getId() == id && bucket.isUsed) {
                bucket.setUsage(false);
                break;
            }
        }

        resize(maxBucketSize);
    }

    private void resize(long newBucketSize) {
        LOG.info("Resizing storage. CurBlockSize: {}, newBlockSize: {}", maxBucketSize, newBucketSize);

        Iterator<Bucket> bucketIt = buckets.iterator();

        List<Bucket> resizedBuckets = new LinkedList<>();
        Bucket resizedBucket = new Bucket(newBucketSize);

        while (bucketIt.hasNext()) {
            Bucket bucket = bucketIt.next();
            if (!bucket.isUsed()) {
                Iterator<LogRecord> recordIt = bucket.getRecords().iterator();

                while (recordIt.hasNext()) {
                    LogRecord record = recordIt.next();
                    boolean isPushed = resizedBucket.tryPushRecord(record);
                    if (!isPushed) {
                        resizedBuckets.add(resizedBucket);
                        resizedBucket = new Bucket(newBucketSize);
                        //FIXME: Bucket max size may be less than record size
                        resizedBucket.tryPushRecord(record);
                    }
                }

                bucketIt.remove();
            }
        }

        if (resizedBucket.getConsumedSize() > 0) {
            resizedBuckets.add(resizedBucket);
        }

        if (!resizedBuckets.isEmpty()) {
            buckets.addAll(resizedBuckets);
        }

        maxBucketSize = newBucketSize;
    }

    private void initBucketList() {
        currentBucket = new Bucket(maxBucketSize);
        buckets.add(currentBucket);
    }

    @Override
    public LogStorageStatus getStatus() {
        return this;
    }

    public void setMaxStorageSize(long maxStorageSize) {
        this.maxStorageSize = maxStorageSize;
    }
}
