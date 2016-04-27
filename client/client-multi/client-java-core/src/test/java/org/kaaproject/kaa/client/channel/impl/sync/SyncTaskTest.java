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

package org.kaaproject.kaa.client.channel.impl.sync;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.DefaultChannelManager;
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncTaskTest {

    public static final Logger LOG = LoggerFactory // NOSONAR
            .getLogger(DefaultChannelManager.class);

    @Test
    public void mergeTest() {
        SyncTask task1 = new SyncTask(TransportType.CONFIGURATION, true, false);
        SyncTask task2 = new SyncTask(TransportType.NOTIFICATION, false, false);

        SyncTask merged = SyncTask.merge(task1, Collections.singletonList(task2));
        LOG.info(merged.toString());
        
        Assert.assertEquals(2, merged.getTypes().size());
        Assert.assertFalse(merged.isAckOnly());
        Assert.assertFalse(merged.isAll());
    }

    @Test
    public void mergeAcksTest() {
        SyncTask task1 = new SyncTask(TransportType.CONFIGURATION, true, false);
        SyncTask task2 = new SyncTask(TransportType.NOTIFICATION, true, false);

        SyncTask merged = SyncTask.merge(task1, Collections.singletonList(task2));
        LOG.info(merged.toString());
        
        Assert.assertEquals(2, merged.getTypes().size());
        Assert.assertTrue(merged.isAckOnly());
        Assert.assertFalse(merged.isAll());
    }

    @Test
    public void mergeAllTest() {
        SyncTask task1 = new SyncTask(TransportType.CONFIGURATION, true, false);
        SyncTask task2 = new SyncTask(TransportType.NOTIFICATION, false, true);

        SyncTask merged = SyncTask.merge(task1, Collections.singletonList(task2));
        LOG.info(merged.toString());
        
        Assert.assertEquals(2, merged.getTypes().size());
        Assert.assertFalse(merged.isAckOnly());
        Assert.assertTrue(merged.isAll());
    }
}
