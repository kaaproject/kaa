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
                new DefaultLogUploadConfiguration(2 * DefaultLogUploadConfiguration.MAX_BATCH_VOLUME, 60, 100);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidVolumeThreshold() {
        DefaultLogUploadConfiguration conf =
                new DefaultLogUploadConfiguration(5, 100, 60);
    }

    @Test
    public void testDefaultLogConfiguration() {
        long batchVolume = 20;
        long volumeThreshold = 60;
        long maxAllowedVolume = 600;

        DefaultLogUploadConfiguration conf =
                new DefaultLogUploadConfiguration(batchVolume, volumeThreshold, maxAllowedVolume);

        Assert.assertTrue(conf.getBatchVolume() == batchVolume);
        Assert.assertTrue(conf.getVolumeThreshold() == volumeThreshold);
        Assert.assertTrue(conf.getMaximumAllowedVolume() == maxAllowedVolume);
    }
}