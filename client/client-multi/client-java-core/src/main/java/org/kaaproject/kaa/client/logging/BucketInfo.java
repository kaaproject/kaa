package org.kaaproject.kaa.client.logging;

/**
 * <p>Describes a unique log bucket.</p>
 *
 * <p>By uniqueness it means that any of log records in a bucket is not repeated in any other log bucket.</p>
 *
 * <p><b>Note:</b>The id should be unique across all available log buckets.</p>
 */
public class BucketInfo {
    private final int bucketId;
    private final int logCount;

    /**
     * Constructs the {@link BucketInfo} object which contains a useful information about a log bucket.
     *
     * @param bucketId The id of a bucket. <b>Note:</b>The id should be unique across all available log buckets.
     * @param logCount The number of logs the bucket contains.
     */
    public BucketInfo(int bucketId, int logCount) {
        this.bucketId = bucketId;
        this.logCount = logCount;
    }

    /**
     * <p>Returns the id of a bucket.</p>
     *
     * <p><b>Note:</b>The id should be unique across all available log buckets.</p>
     *
     * @return The id of a bucket.
     */
    public int getBucketId() {
        return bucketId;
    }

    /**
     * @return The number of logs a bucket contains.
     */
    public int getLogCount() {
        return logCount;
    }

}
