package org.kaaproject.kaa.client.logging.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.kaaproject.kaa.client.logging.LogBlock;
import org.kaaproject.kaa.client.logging.LogRecord;
import org.kaaproject.kaa.client.logging.LogStorage;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemLogStorage implements LogStorage, LogStorageStatus {

    private static final Logger LOG = LoggerFactory.getLogger(MemLogStorage.class);

    private static final long DEFAULT_MAX_STORAGE_SIZE = 16 * 1024 * 1024;
    private static final long DEFAULT_MAX_BUCKET_SIZE = 16 * 1024;
    private static final int DEFAULT_MAX_BUCKET_RECORD_COUNT = 256;
    private static final Random RANDOM = new Random();

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
            for(MemBucket value : buckets.values()){
                //TODO: check bucket state;
                volume += value.getSize();
            }
        }
        LOG.trace("Calculated consumed volume {}", volume);
        return volume;
    }

    @Override
    public long getRecordCount() {
        long count = 0;
        LOG.trace("Calculating record count");
        synchronized (buckets) {
            for(MemBucket value : buckets.values()){
                //TODO: check bucket state;
                count += value.getCount();
            }
        }
        LOG.trace("Calculated record count {}", count);
        return count;
    }

    @Override
    public void addLogRecord(LogRecord record) {
        LOG.trace("Adding new log record with size {}", record.getSize());
        if(record.getSize() > maxBucketSize){
            throw new IllegalArgumentException("Record size(" + record.getSize() + ") is bigger then max bucket size (" + maxBucketSize + ")!");
        }
        synchronized (buckets) {
            if(currentBucket == null){
                currentBucket = new MemBucket(bucketIdSeq.getAndIncrement());
                buckets.put(currentBucket.getId(), currentBucket);
            }
        }
    }

    @Override
    public LogStorageStatus getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LogBlock getRecordBlock(long blockSize, int batchCount) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeRecordBlock(int id) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyUploadFailed(int id) {
        // TODO Auto-generated method stub
    }

}
