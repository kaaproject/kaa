/**
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

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLogUploadFailoverStrategy implements LogUploadFailoverStrategy {
    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultLogUploadFailoverStrategy.class);

    private long RETRY_PERIOD_MS = 300 * 1000; // 5 minutes

    private boolean isUploadApproved = true;
    private final KaaChannelManager channelManager;

    private long nextUploadAttemptTS = 0;

    public DefaultLogUploadFailoverStrategy(KaaChannelManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Channel manager is null");
        }

        channelManager = manager;
    }

    @Override
    public boolean isUploadApproved() {
        if (!isUploadApproved && System.currentTimeMillis() >= nextUploadAttemptTS) {
            isUploadApproved = true;
        }

        return isUploadApproved;
    }

    @Override
    public void onTimeout() {
        LOG.info("Log upload timeout occurred. Switching to next Operation server");
        switchToNextServer();
    }

    @Override
    public void onFailure(LogDeliveryErrorCode code) {
        switch (code) {
        case NO_APPENDERS_CONFIGURED:
        case APPENDER_INTERNAL_ERROR:
        case REMOTE_CONNECTION_ERROR:
        case REMOTE_INTERNAL_ERROR:
            isUploadApproved = false;
            nextUploadAttemptTS = System.currentTimeMillis() + RETRY_PERIOD_MS;
            break;
        default:
            break;
        }
    }

    /**
     * Set retry period.
     *
     * @param period Times in milliseconds.
     */
    public void setRetryPeriod(long period) {
        if (period > 0) {
            RETRY_PERIOD_MS = period;
        }
    }

    private void switchToNextServer() {
        KaaDataChannel channel = channelManager.getChannelByTransportType(TransportType.LOGGING);
        if (channel != null) {
            channelManager.onServerFailed(channel.getServer());
        } else {
            LOG.error("Failed to switch Operation server. "
                    + "No channel is used for logging transport");
        }
    }
}
