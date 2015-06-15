package org.kaaproject.kaa.client.logging.memory;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.client.logging.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemBucket {
    private static final Logger LOG = LoggerFactory.getLogger(MemBucket.class);

    public static enum MemBucketState {
        FREE, FULL, PENDING
    }

    protected final int id;
    protected final long maxSize;
    protected final int maxRecordCount;
    private final List<LogRecord> records;

    protected long size;
    private MemBucketState state;

    public MemBucket(int id, long maxSize, int maxRecordCount) {
        super();
        this.id = id;
        this.maxSize = maxSize;
        this.maxRecordCount = maxRecordCount;
        this.records = new ArrayList<LogRecord>();
        this.state = MemBucketState.FREE;
    }

    public int getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public int getCount() {
        return records.size();
    }

    public List<LogRecord> getRecords() {
        return records;
    }

    public MemBucketState getState() {
        return state;
    }

    public void setState(MemBucketState state) {
        this.state = state;
    }

    public boolean addRecord(LogRecord record) {
        if (size + record.getSize() > maxSize) {
            LOG.trace("No space left in bucket. Current size: {}, record size: {}, max size: {}", size, record.getSize(), maxSize);
            return false;
        }
        if (getCount() + 1 > maxRecordCount) {
            LOG.trace("No space left in bucket. Current count: {}, max count: {}", getCount(), maxRecordCount);
            return false;
        }
        records.add(record);
        size += record.getSize();
        return true;
    }
}
