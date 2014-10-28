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

package org.kaaproject.kaa.examples.robotrun.visualization.emulator;

import java.util.Properties;

import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.examples.robotrun.controller.Launcher;
import org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatorLauncher extends Launcher {

    private static final Logger LOG = LoggerFactory.getLogger(EmulatorLauncher.class);

    public EmulatorLauncher(LauncherCallback callback, Labyrinth labyrinth, 
            Cell startPosition, 
            Direction startDirection, 
            String name, 
            Properties robotProperties) throws Exception {
        super(new KaaDesktop(), callback,
                new RobotEmulator(labyrinth, startPosition, startDirection, robotProperties), 
                startPosition.getX(), startPosition.getY(), startDirection, name);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }
    
    
    
}
