/*
 * Copyright 2014-2016 CyberVision, Inc.
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

public class DefaultLogUploadStrategyTest {

    class TestLogStorageStatus implements LogStorageStatus {
        private final long consumedVolume;
        private final long recordCount;

        TestLogStorageStatus(long consumedVolume, long recordCount) {
            this.consumedVolume = consumedVolume;
            this.recordCount = recordCount;
        }

        @Override
        public long getConsumedVolume() {
            return consumedVolume;
        }

        @Override
        public long getRecordCount() {
            return recordCount;
        }
    };

    @Test
    public void testNOOPDecision() {
        DefaultLogUploadStrategy strategy = new DefaultLogUploadStrategy();
        strategy.setBatch(20);
        strategy.setVolumeThreshold(60);
        strategy.setTimeout(300);
        TestLogStorageStatus status = new TestLogStorageStatus(30, 3);

        Assert.assertEquals(LogUploadStrategyDecision.NOOP, strategy.isUploadNeeded(status));
    }

    @Test
    public void testUpdateDecision() {
        DefaultLogUploadStrategy strategy = new DefaultLogUploadStrategy();
        strategy.setBatch(20);
        strategy.setVolumeThreshold(60);
        strategy.setTimeout(300);

        TestLogStorageStatus status = new TestLogStorageStatus(60, 3);

        Assert.assertEquals(LogUploadStrategyDecision.UPLOAD, strategy.isUploadNeeded(status));

        status = new TestLogStorageStatus(70, 3);
        Assert.assertEquals(LogUploadStrategyDecision.UPLOAD, strategy.isUploadNeeded(status));
    }
}