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

package org.kaaproject.kaa.client.logging.memory;

import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.LogBucket;
import org.kaaproject.kaa.client.logging.LogRecord;
import org.kaaproject.kaa.client.logging.LogStorage;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.memory.MemBucket.MemBucketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemLogStorage implements LogStorage, LogStorageStatus {

  private static final Logger LOG = LoggerFactory.getLogger(MemLogStorage.class);

  private static final long DEFAULT_MAX_STORAGE_SIZE = 16 * 1024 * 1024;
  private static final long DEFAULT_MAX_BUCKET_SIZE = 16 * 1024;
  private static final int DEFAULT_MAX_BUCKET_RECORD_COUNT = 256;

  private final long maxStorageSize;
  private final long maxBucketSize;
  private final int maxBucketRecordCount;
  private final AtomicInteger bucketIdSeq = new AtomicInteger();
  private final Map<Integer, MemBucket> buckets;
  private volatile long consumedVolume;
  private volatile long recordCount;
  private MemBucket currentBucket;

  public MemLogStorage() {
    this(DEFAULT_MAX_BUCKET_SIZE, DEFAULT_MAX_BUCKET_RECORD_COUNT);
  }

  public MemLogStorage(long bucketSize, int bucketRecordCount) {
    this(DEFAULT_MAX_STORAGE_SIZE, bucketSize, bucketRecordCount);
  }

  /**
   * All-args constructor.
   */
  public MemLogStorage(long maxStorageSize, long bucketSize, int bucketRecordCount) {
    super();
    this.maxStorageSize = maxStorageSize;
    this.maxBucketSize = bucketSize;
    this.maxBucketRecordCount = bucketRecordCount;
    this.buckets = new LinkedHashMap<Integer, MemBucket>();
  }

  @Override
  public long getConsumedVolume() {
    LOG.debug("Consumed volume: {}", consumedVolume);
    return consumedVolume;
  }

  @Override
  public long getRecordCount() {
    LOG.debug("Record count: {}", recordCount);
    return recordCount;
  }

  @Override
  public BucketInfo addLogRecord(LogRecord record) {
    LOG.trace("Adding new log record with size {}", record.getSize());
    if (record.getSize() > maxBucketSize) {
      throw new IllegalArgumentException("Record size(" + record.getSize()
              + ") is bigger than max bucket size (" + maxBucketSize + ")!");
    }
    synchronized (buckets) {
      if (consumedVolume + record.getSize() > maxStorageSize) {
        throw new IllegalStateException("Storage is full!");
      }
      if (currentBucket == null || currentBucket.getState() != MemBucketState.FREE) {
        currentBucket = new MemBucket(bucketIdSeq.getAndIncrement(),
                maxBucketSize, maxBucketRecordCount);
        buckets.put(currentBucket.getId(), currentBucket);
      }
      if (!currentBucket.addRecord(record)) {
        LOG.trace("Current bucket is full. Creating new one.");
        currentBucket.setState(MemBucketState.FULL);
        currentBucket = new MemBucket(bucketIdSeq.getAndIncrement(),
                maxBucketSize, maxBucketRecordCount);
        buckets.put(currentBucket.getId(), currentBucket);
        currentBucket.addRecord(record);
      }
      recordCount++;
      consumedVolume += record.getSize();
    }
    LOG.trace("Added a new log record to bucket [{}]", currentBucket.getId());
    return new BucketInfo(currentBucket.getId(), currentBucket.getCount());
  }

  @Override
  public LogBucket getNextBucket() {
    LOG.trace("Getting new record block with block");

    LogBucket result = null;
    MemBucket bucketCandidate = null;
    synchronized (buckets) {
      for (MemBucket bucket : buckets.values()) {
        if (bucket.getState() == MemBucketState.FREE) {
          bucketCandidate = bucket;
        }
        if (bucket.getState() == MemBucketState.FULL) {
          bucket.setState(MemBucketState.PENDING);
          bucketCandidate = bucket;
          break;
        }
      }

      if (bucketCandidate != null) {
        consumedVolume -= bucketCandidate.getSize();
        recordCount -= bucketCandidate.getCount();
        if (bucketCandidate.getState() == MemBucketState.FREE) {
          LOG.trace("Only a bucket with state FREE found: [{}]. Changing its state to PENDING",
                  bucketCandidate.getId());
          bucketCandidate.setState(MemBucketState.PENDING);
        }
        result = new LogBucket(bucketCandidate.getId(), bucketCandidate.getRecords());
        LOG.debug("Return record block with records count: [{}]", bucketCandidate.getCount());
      }
    }
    return result;
  }

  @Override
  public void removeBucket(int id) {
    LOG.trace("Removing record block with id [{}]", id);
    synchronized (buckets) {
      if (buckets.remove(id) != null) {
        LOG.debug("Record block [{}] removed", id);
      } else {
        LOG.debug("Failed to remove record block [{}]", id);
      }
    }
  }

  @Override
  public void rollbackBucket(int id) {
    LOG.trace("Upload of record block [{}] failed", id);
    synchronized (buckets) {
      buckets.get(id).setState(MemBucketState.FULL);
      consumedVolume += buckets.get(id).getSize();
      recordCount += buckets.get(id).getCount();
    }
  }

  @Override
  public void close() {
    // automatically done by GC
  }

  @Override
  public LogStorageStatus getStatus() {
    return this;
  }

}
