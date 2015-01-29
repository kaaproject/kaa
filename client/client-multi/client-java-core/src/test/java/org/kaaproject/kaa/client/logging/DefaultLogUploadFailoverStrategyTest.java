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

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.mockito.Mockito;

public class DefaultLogUploadFailoverStrategyTest {

    @Test(expected=IllegalArgumentException.class)
    public void testBadParams() {
        DefaultLogUploadFailoverStrategy strategy =
                new DefaultLogUploadFailoverStrategy(null);
    }

    @Test
    public void testSwitchServerFailure() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        DefaultLogUploadFailoverStrategy strategy =
                new DefaultLogUploadFailoverStrategy(channelManager);

        strategy.onTimeout();

        Mockito.verify(channelManager, Mockito.times(0)).onServerFailed(Mockito.any(TransportConnectionInfo.class));
    }

    @Test
    public void testSwitchServerSuccess() {
        TransportConnectionInfo serverInfo = Mockito.mock(TransportConnectionInfo.class);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getServer()).thenReturn(serverInfo);

        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        Mockito.when(channelManager.getChannelByTransportType(TransportType.LOGGING)).thenReturn(channel);

        DefaultLogUploadFailoverStrategy strategy =
                new DefaultLogUploadFailoverStrategy(channelManager);

        strategy.onTimeout();

        Mockito.verify(channelManager, Mockito.times(1)).onServerFailed(serverInfo);
    }

    @Test
    public void testLogUploadFailure() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        DefaultLogUploadFailoverStrategy strategy =
                new DefaultLogUploadFailoverStrategy(channelManager);

        Assert.assertTrue(strategy.isUploadApproved());

        strategy.onFailure(LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);

        Assert.assertFalse(strategy.isUploadApproved());
    }

    @Test
    public void testRetry() {
        long retryPeriod = 2 * 1000; // ms
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        DefaultLogUploadFailoverStrategy strategy =
                new DefaultLogUploadFailoverStrategy(channelManager);
        strategy.setRetryPeriod(retryPeriod);
        strategy.onFailure(LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);

        Assert.assertFalse(strategy.isUploadApproved());

        try {
            Thread.sleep(retryPeriod / 2);
            Assert.assertFalse(strategy.isUploadApproved());
            Thread.sleep(retryPeriod / 2);
            Assert.assertTrue(strategy.isUploadApproved());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }
}
