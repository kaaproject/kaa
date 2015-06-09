package org.kaaproject.kaa.client.logging.memory;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.client.logging.LogRecord;

public class MemBucket {

    public static enum MemBucketState {
        FREE, FULL, PENDING
    }

    protected final int id;
    private final List<LogRecord> records;
    private MemBucketState state;

    public MemBucket(int id) {
        super();
        this.id = id;
        this.records = new ArrayList<LogRecord>();
        this.state = MemBucketState.FREE;
    }

    public int getId() {
        return id;
    }

    public long getSize() {
        long result = 0L;
        for (LogRecord record : records) {
            result += record.getSize();
        }
        return result;
    }

    public int getCount() {
        return records.size();
    }

    public MemBucketState getState() {
        return state;
    }
}
