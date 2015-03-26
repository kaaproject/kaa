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
