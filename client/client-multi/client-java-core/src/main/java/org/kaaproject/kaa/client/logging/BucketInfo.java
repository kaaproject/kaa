package org.kaaproject.kaa.client.logging;

public class BucketInfo {

    private int bucketId;
    private int logCount;

    public BucketInfo() {}

    public BucketInfo(int bucketId, int logCount) {
        this.bucketId = bucketId;
        this.logCount = logCount;
    }

    public int getBucketId() {
        return bucketId;
    }

    public int getLogCount() {
        return logCount;
    }

}
