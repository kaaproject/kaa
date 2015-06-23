package org.kaaproject.kaa.client.logging.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.kaaproject.kaa.client.logging.LogBlock;
import org.kaaproject.kaa.client.logging.LogRecord;
import org.kaaproject.kaa.client.logging.LogStorage;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.memory.MemBucket.MemBucketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemLogStorage implements LogStorage, LogStorageStatus {

    private static final Logger LOG = LoggerFactory.getLogger(MemLogStorage.class);

    private static final long DEFAULT_MAX_STORAGE_SIZE = 16 * 1024 * 1024;
    private static final long DEFAULT_MAX_BUCKET_SIZE = 16 * 1024;
    private static final int DEFAULT_MAX_BUCKET_RECORD_COUNT = 256;

    private final long maxStorageSize;
    private long maxBucketSize;
    private int maxBucketRecordCount;

    private final AtomicInteger bucketIdSeq = new AtomicInteger();
    private MemBucket currentBucket;
    private final Map<Integer, MemBucket> buckets;
    
    public MemLogStorage() {
        this(DEFAULT_MAX_BUCKET_SIZE, DEFAULT_MAX_BUCKET_RECORD_COUNT);
    }
    
    public MemLogStorage(long bucketSize, int bucketRecordCount) {
        this(DEFAULT_MAX_STORAGE_SIZE, bucketSize, bucketRecordCount);
    }

    public MemLogStorage(long maxStorageSize, long bucketSize, int bucketRecordCount) {
        super();
        this.maxStorageSize = maxStorageSize;
        this.maxBucketSize = bucketSize;
        this.maxBucketRecordCount = bucketRecordCount;
        this.buckets = new HashMap<Integer, MemBucket>();
    }

    @Override
    public long getConsumedVolume() {
        long volume = 0;
        LOG.trace("Calculating consumed volume");
        synchronized (buckets) {
            for(MemBucket bucket : buckets.values()){
                if(bucket.getState() != MemBucketState.PENDING){
                    volume += bucket.getSize();
                }
            }
        }
        LOG.debug("Calculated consumed volume {}", volume);
        return volume;
    }

    @Override
    public long getRecordCount() {
        long count = 0;
        LOG.trace("Calculating record count");
        synchronized (buckets) {
            for(MemBucket bucket : buckets.values()){
                if(bucket.getState() != MemBucketState.PENDING){
                    count += bucket.getCount();
                }
            }
        }
        LOG.debug("Calculated record count {}", count);
        return count;
    }

    @Override
    public void addLogRecord(LogRecord record) {
        LOG.trace("Adding new log record with size {}", record.getSize());
        if(record.getSize() > maxBucketSize){
            throw new IllegalArgumentException("Record size(" + record.getSize() + ") is bigger than max bucket size (" + maxBucketSize + ")!");
        }
        if(getConsumedVolume() + record.getSize() > maxStorageSize){
            throw new IllegalStateException("Storage is full!");
        }
        synchronized (buckets) {
            if(currentBucket == null){
                currentBucket = new MemBucket(bucketIdSeq.getAndIncrement(), maxBucketSize, maxBucketRecordCount);
                buckets.put(currentBucket.getId(), currentBucket);
            }
            if(!currentBucket.addRecord(record)){
                LOG.trace("Current bucket is full. Creating new one.");
                currentBucket.setState(MemBucketState.FULL);
                currentBucket = new MemBucket(bucketIdSeq.getAndIncrement(), maxBucketSize, maxBucketRecordCount);
                buckets.put(currentBucket.getId(), currentBucket);
                currentBucket.addRecord(record);
            }
        }
        LOG.trace("Added new log record to bucket [{}]", currentBucket.getId());
    }

    @Override
    public LogBlock getRecordBlock(long blockSize, int batchCount) {
        LOG.trace("Getting new record block with block size = {} and count = {}", blockSize, batchCount);
        if(blockSize > maxBucketSize || batchCount > maxBucketRecordCount){
            //TODO: add support of block resize
            LOG.warn("Resize of record block is not supported yet");
        }
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
        }

        LogBlock result = null;
        if (bucketCandidate != null) {
            if (bucketCandidate.getSize() <= blockSize && bucketCandidate.getCount() <= batchCount) {
                result = new LogBlock(bucketCandidate.getId(), bucketCandidate.getRecords());
                LOG.debug("Return record block [{}]", result);
            } else {
                LOG.debug("Shrinking bucket {} to new size: [{}] and count: [{}]", bucketCandidate, blockSize, batchCount);
                List<LogRecord> overSized = bucketCandidate.shrinkToSize(blockSize, batchCount);
                result = new LogBlock(bucketCandidate.getId(), bucketCandidate.getRecords());
                for (LogRecord record : overSized) {
                    addLogRecord(record);
                }
            }
        }
        return result;
    }

    @Override
    public void removeRecordBlock(int id) {
        LOG.trace("Removing record block with id [{}]", id);
        synchronized (buckets) {
            if(buckets.remove(id) != null){
                LOG.debug("Record block [{}] removed", id);
            }else{
                LOG.debug("Failed to remove record block [{}]", id);
            }
        }
    }

    @Override
    public void notifyUploadFailed(int id) {
        LOG.trace("Upload of record block [{}] failed", id);
        synchronized (buckets) {
            buckets.get(id).setState(MemBucketState.FULL);
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
