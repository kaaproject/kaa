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

import org.junit.Assert;
import org.junit.Test;

public class DefaultLogUploadConfigurationTest {

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidBatchVolume() {
        DefaultLogUploadConfiguration conf =
                new DefaultLogUploadConfiguration.Builder()
                    .setBatchVolume(2 * DefaultLogUploadConfiguration.MAX_BATCH_VOLUME)
                    .setVolumeThreshold(60)
                    .setMaximumAllowedVolume(100)
                    .setLogUploadTimeout(300)
                    .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidVolumeThreshold() {
        DefaultLogUploadConfiguration conf =
                new DefaultLogUploadConfiguration.Builder()
                    .setBatchVolume(5)
                    .setVolumeThreshold(100)
                    .setMaximumAllowedVolume(60)
                    .setLogUploadTimeout(300)
                    .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidUploadTimeout() {
        DefaultLogUploadConfiguration conf =
                new DefaultLogUploadConfiguration.Builder()
                    .setBatchVolume(5)
                    .setVolumeThreshold(100)
                    .setMaximumAllowedVolume(60)
                    .setLogUploadTimeout(0)
                    .build();
    }

    @Test
    public void testDefaultLogConfiguration() {
        long batchVolume = 20;
        long volumeThreshold = 60;
        long maxAllowedVolume = 600;
        long uploadTimeout = 300;

        DefaultLogUploadConfiguration conf =
                new DefaultLogUploadConfiguration.Builder()
                    .setBatchVolume(batchVolume)
                    .setVolumeThreshold(volumeThreshold)
                    .setMaximumAllowedVolume(maxAllowedVolume)
                    .setLogUploadTimeout(uploadTimeout)
                    .build();

        Assert.assertTrue(conf.getBatchVolume() == batchVolume);
        Assert.assertTrue(conf.getVolumeThreshold() == volumeThreshold);
        Assert.assertTrue(conf.getMaximumAllowedVolume() == maxAllowedVolume);
        Assert.assertTrue(conf.getLogUploadTimeout() == uploadTimeout);
    }
}