package org.kaaproject.kaa.client.logging;

/**
 * <p>Interface for a log collector.</p>
 *
 * <p>Defines callback for log delivery.</p>
 */
public interface LogDeliveryListener {

    /**
     * Handles success of log delivery
     * @param bucketInfo the bucketInfo
     */
    void onLogDeliverySuccess(BucketInfo bucketInfo);

    /**
     * Handles failure of log delivery
     * @param bucketInfo the bucketInfo
     */
    void onLogDeliveryFailure(BucketInfo bucketInfo);

    /**
     * Handles timeout of log delivery
     * @param bucketInfo the bucketInfo
     */
    void onLogDeliveryTimeout(BucketInfo bucketInfo);
}
