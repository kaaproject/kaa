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
package org.kaaproject.kaa.examples.robotrun.controller.state;

import org.kaaproject.kaa.examples.robotrun.controller.Context;
import org.kaaproject.kaa.examples.robotrun.controller.robot.DefaultRobotManager;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StartRunState implements ControllerState {
    private static final Logger LOG = LoggerFactory.getLogger(StartRunState.class);
    
    private volatile boolean stopped = false;
    
    @Override
    public void process(Context context) {
        Labyrinth labyrinth = context.getConfigurationManager().getLabyrinth();
        Cell cell = context.getConfigurationManager().getStartPosition();
        Direction direction = context.getConfigurationManager().getStartDirection();
        context.setLabyrinth(labyrinth);
        context.setCell(cell);
        context.setRobotManager(new DefaultRobotManager(cell, direction, context.getDrivable()));
        LOG.debug("Labyrinth {}, Cell {}", labyrinth, cell);
        context.setState(new ExitSearchState());
        context.getEventManager().reportLocation(context.getCell());
        context.process();
    }

    @Override
    public void stop() {
        stopped = true;
    }
}