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

package org.kaaproject.kaa.examples.robotrun.controller.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.apache.avro.Schema;
import org.junit.Test;
import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.common.CommonArray;
import org.kaaproject.kaa.client.common.CommonEnum;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.common.CommonValue;
import org.kaaproject.kaa.client.common.DefaultCommonFactory;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationManager;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.mockito.Mockito;

public class DefaultConfigurationManagerTest {

    private final DefaultCommonFactory commonFactory = new DefaultCommonFactory();

    private DefaultConfigurationManager createConfigManager() {
        AbstractKaaClient client = Mockito.mock(AbstractKaaClient.class);
        ConfigurationManager configManager = Mockito.mock(ConfigurationManager.class);
        Mockito.when(client.getConfigurationManager()).thenReturn(configManager);

        return new DefaultConfigurationManager(client);
    }

    private CommonValue createBorderRecord(int x, int y, BorderType type) {
        CommonValue b_x = commonFactory.createCommonValue(x);
        CommonValue b_y = commonFactory.createCommonValue(y);
        CommonEnum  b_type_ = commonFactory.createCommonEnum((Schema)null, type.name());
        CommonValue b_type  = commonFactory.createCommonValue(b_type_);

        CommonRecord b_ = commonFactory.createCommonRecord((Schema)null);
        b_.setField("x", b_x);
        b_.setField("y", b_y);
        b_.setField("type", b_type);

        return commonFactory.createCommonValue(b_);
    }

    private CommonRecord createSampleLabyrinthConfig() {
        CommonArray hBordersArray_ = commonFactory.createCommonArray((Schema)null
                , Arrays.asList(createBorderRecord(1, 1, BorderType.FREE)
                                , createBorderRecord(1, 2, BorderType.SOLID)
                                ));
        CommonArray vBordersArray_ = commonFactory.createCommonArray((Schema)null
                , Arrays.asList(createBorderRecord(1, 1, BorderType.FREE)
                                , createBorderRecord(2, 1, BorderType.SOLID)));

        CommonValue hBordersArray = commonFactory.createCommonValue(hBordersArray_);
        CommonValue vBordersArray = commonFactory.createCommonValue(vBordersArray_);

        CommonRecord labRecord = commonFactory.createCommonRecord((Schema)null);
        labRecord.setField("hBorders", hBordersArray);
        labRecord.setField("vBorders", vBordersArray);
        return labRecord;
    }

    @Test(expected = IllegalArgumentException.class)
    public void badInitializationTest() {
        new DefaultConfigurationManager(null);
    }

    @Test
    public void initializationTest() {
        AbstractKaaClient client = Mockito.mock(AbstractKaaClient.class);
        ConfigurationManager configManager = Mockito.mock(ConfigurationManager.class);
        Mockito.when(client.getConfigurationManager()).thenReturn(configManager);

        DefaultConfigurationManager manager = new DefaultConfigurationManager(client);

        assertNotNull(manager);
        Mockito.verify(configManager, Mockito.times(1)).subscribeForConfigurationUpdates(manager);
        Mockito.verify(configManager, Mockito.times(1)).getConfiguration();
        assertNotNull(manager.getLabyrinth());
    }

    @Test
    public void loadLabyrinthTest() {
        DefaultConfigurationManager manager = createConfigManager();

        manager.onConfigurationUpdated(createSampleLabyrinthConfig());

        Labyrinth labyrinth = manager.getLabyrinth();

        assertNotNull(labyrinth);
        assertFalse(labyrinth.isEmpty());
        assertEquals(2, labyrinth.getWidth());
        assertEquals(2, labyrinth.getHeight());
    }

    @Test
    public void testSubscribtion() {
        DefaultConfigurationManager manager = createConfigManager();

        LabyrinthUpdateListener listener = Mockito.mock(LabyrinthUpdateListener.class);
        manager.onConfigurationUpdated(createSampleLabyrinthConfig());

        manager.addListener(listener);
        manager.addListener(listener);
        manager.onConfigurationUpdated(createSampleLabyrinthConfig());

        manager.removeListener(listener);
        manager.onConfigurationUpdated(createSampleLabyrinthConfig());

        Mockito.verify(listener, Mockito.times(1)).onLabyrinthUpdate(Mockito.any(Labyrinth.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testBadStartPosition() {
        DefaultConfigurationManager manager = createConfigManager();
        manager.getStartPosition();
    }

    @Test(expected = IllegalStateException.class)
    public void testBadStartDirection() {
        DefaultConfigurationManager manager = createConfigManager();
        manager.getStartDirection();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBadStartPosition() {
        DefaultConfigurationManager manager = createConfigManager();
        manager.onConfigurationUpdated(createSampleLabyrinthConfig());
        manager.setStartPosition(Integer.MAX_VALUE, Integer.MIN_VALUE, Direction.EAST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullStartDirection() {
        DefaultConfigurationManager manager = createConfigManager();
        manager.onConfigurationUpdated(createSampleLabyrinthConfig());
        manager.setStartPosition(0, 0, null);
    }

    @Test
    public void testSetStartPosition() {
        DefaultConfigurationManager manager = createConfigManager();
        manager.onConfigurationUpdated(createSampleLabyrinthConfig());
        manager.setStartPosition(0, 1, Direction.EAST);

        Cell startCell = manager.getStartPosition();
        assertEquals(0, startCell.getX());
        assertEquals(1, startCell.getY());
        assertEquals(Direction.EAST, manager.getStartDirection());
    }

}
