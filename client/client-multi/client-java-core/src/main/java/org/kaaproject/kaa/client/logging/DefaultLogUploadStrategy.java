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

import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 */
public class DefaultLogUploadStrategy implements LogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogUploadStrategy.class);

    private static final int DEFAULT_UPLOAD_TIMEOUT = 2 * 60;
    private static final int DEFAULT_UPLOAD_CHECK_PERIOD = 30;
    private static final int DEFAULT_RETRY_PERIOD = 5 * 60;
    private static final int DEFAULT_UPLOAD_VOLUME_THRESHOLD = 8 * 1024;
    private static final int DEFAULT_UPLOAD_COUNT_THRESHOLD = 64;
    private static final int DEFAULT_BATCH_SIZE = 8 * 1024;
    private static final int DEFAULT_BATCH_COUNT = 256;

    private int timeout = DEFAULT_UPLOAD_TIMEOUT;
    private int uploadCheckPeriod = DEFAULT_UPLOAD_CHECK_PERIOD;
    private int retryPeriod = DEFAULT_RETRY_PERIOD;
    private int volumeThreshold = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
    private int countThreshold = DEFAULT_UPLOAD_COUNT_THRESHOLD;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int batchCount = DEFAULT_BATCH_COUNT;
    

    @Override
    public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;
        if (status.getConsumedVolume() >= volumeThreshold) {
            LOG.info("Need to upload logs - current size: {}, max: {}", status.getConsumedVolume(), volumeThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
        }else if (status.getRecordCount() >= countThreshold) {
            LOG.info("Need to upload logs - current size: {}, max: {}", status.getRecordCount(), countThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
        }
        return decision;
    }
    
    @Override
    public long getBatchSize() {
        return batchSize;
    }

    @Override
    public int getBatchCount() {
        return batchCount;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void onTimeout(LogFailoverCommand controller) {
        controller.switchAccessPoint();
    }

    @Override
    public void onFailure(LogFailoverCommand controller, LogDeliveryErrorCode code) {
        switch (code) {
        case NO_APPENDERS_CONFIGURED:
        case APPENDER_INTERNAL_ERROR:
        case REMOTE_CONNECTION_ERROR:
        case REMOTE_INTERNAL_ERROR:
            controller.retryLogUpload(retryPeriod);
            break;
        default:
            break;
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setRetryPeriod(int retryPeriod) {
        this.retryPeriod = retryPeriod;
    }

    public void setVolumeThreshold(int volumeThreshold) {
        this.volumeThreshold = volumeThreshold;
    }

    public void setCountThreshold(int countThreshold) {
        this.countThreshold = countThreshold;
    }

    public void setBatch(int batch) {
        this.batchSize = batch;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public int getUploadCheckPeriod() {
        return uploadCheckPeriod;
    }

    public void setUploadCheckPeriod(int uploadCheckPeriod) {
        this.uploadCheckPeriod = uploadCheckPeriod;
    }
}