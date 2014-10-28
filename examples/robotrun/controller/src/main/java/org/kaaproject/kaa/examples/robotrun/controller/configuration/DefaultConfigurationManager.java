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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.LabyrinthConverter;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinthConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigurationManager implements ConfigurationManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigurationManager.class);

    private final Set<LabyrinthUpdateListener>    listeners = Collections.synchronizedSet(new HashSet<LabyrinthUpdateListener>());
    private Labyrinth labyrinth;
    private final LabyrinthConverter labyrinthConverter;

    private int startX = 0;
    private int startY = 0;
    private Direction startDirection = Direction.SOUTH;
    private volatile boolean startPositionInitialized = false;

    public DefaultConfigurationManager(KaaClient client) {
        if (client == null) {
            LOG.error("Can not instantiate ConfigurationManager: Kaa client is null");
            throw new IllegalArgumentException("Kaa client is null");
        }
        labyrinthConverter = new BasicLabyrinthConverter();

        CommonRecord currentConfig = client.getConfigurationManager().getConfiguration();
        if (currentConfig != null) {
            onConfigurationUpdated(currentConfig);
        } else {
            labyrinth = new BasicLabyrinth(0, 0);
        }
        client.getConfigurationManager().subscribeForConfigurationUpdates(this);
    }

    @Override
    public synchronized void onConfigurationUpdated(CommonRecord rootConfiguration) {
        LOG.info("onConfigurationUpdated: Received new labyrinth configuration rootConfiguration="
                + "{}", rootConfiguration != null ? rootConfiguration : "null" );
        labyrinth = labyrinthConverter.convert(labyrinth, rootConfiguration);
        LOG.info("onConfigurationUpdated: Received new labyrinth configuration, labyrinth="
                + "{}", labyrinth != null ? labyrinth : "null" );
        notifyListeners();
    }

    @Override
    public synchronized Labyrinth getLabyrinth() {
        return labyrinth;
    }

    @Override
    public boolean addListener(LabyrinthUpdateListener listener) {
        boolean result = false;
        synchronized (listeners) {
            result = listeners.add(listener);
        }
        LOG.info("{} labyrinth update listener {}", (result ? "Added" : "Failed to add"), listener);
        return result;
    }

    @Override
    public boolean removeListener(LabyrinthUpdateListener listener) {
        boolean result = false;
        synchronized (listeners) {
            result = listeners.remove(listener);
        }
        LOG.info("{} labyrinth update listener {}", (result ? "Removed" : "Failed to remove"), listener);
        return result;
    }

    private void notifyListeners() {
        synchronized (listeners) {
            Iterator<LabyrinthUpdateListener> i = listeners.iterator();
            while (i.hasNext()) {
                i.next().onLabyrinthUpdate(labyrinth);
            }
        }
    }

    @Override
    public synchronized Cell getStartPosition() {
        if (!startPositionInitialized) {
            throw new IllegalStateException("Start position was not initialized yet");
        }
        if (startX >= labyrinth.getWidth() || startY >= labyrinth.getHeight() || startX < 0 || startY < 0) {
            LOG.error("Cell coordinates [x: {}, y: {}] are not in labyrinth [{}] bounds", startX, startY, labyrinth);
            throw new IllegalStateException("Start position is out of labyrinth");
        }
        return labyrinth.getCell(startX, startY);
    }

    @Override
    public synchronized void setStartPosition(int x, int y, Direction direction) {
        if (x >= labyrinth.getWidth() || y >= labyrinth.getHeight() || direction == null) {
            LOG.error("Can not set initial position. Either start position ({},{}) "
                    + "or direction '{}' is invalid",x, y, direction);
            throw new IllegalArgumentException("Unresolved direction");
        }

        this.startX = x;
        this.startY = y;
        this.startDirection = direction;

        LOG.info("Provided initial position: [x: {}, y: {}]. Direction is {}"
                                , this.startX, this.startY, this.startDirection);

        startPositionInitialized = true;
    }

    @Override
    public synchronized Direction getStartDirection() {
        if (!startPositionInitialized) {
            throw new IllegalStateException("Start position was not initialized yet");
        }
        return startDirection;
    }

}
