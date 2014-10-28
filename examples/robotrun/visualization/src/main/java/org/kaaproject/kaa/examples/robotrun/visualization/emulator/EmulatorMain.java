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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.kaaproject.kaa.examples.robotrun.controller.Launcher.LauncherCallback;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatorMain implements LauncherCallback {
    
    private static final Logger LOG = LoggerFactory.getLogger(EmulatorMain.class);

    public static void main(String[] args) {
        new EmulatorMain(args);
    }
    
    private EmulatorLauncher launcher;
    private boolean started = false;
    
    public EmulatorMain(String[] args) {
        
        Thread outputHandlerThread = new Thread(new OutputHandler(), "Output handler thread");
        outputHandlerThread.start();
        
        File labyrinthFile;
        int startX = -1;
        int startY = -1;
        
        Labyrinth labyrinth;
        Cell startCell;
        Direction startDirection = null;
        Integer id = null;
        
        if (args.length < 5) {
            LOG.error("Insufficient arguments!");
            throw new RuntimeException("Insufficient arguments!");
        }
        try {
            labyrinthFile = new File(args[0]);
            if (!labyrinthFile.exists() || !labyrinthFile.isFile()) {
                LOG.error("Labyrinth file [{}] doesn't exists or not a file!", args[0]);
                throw new RuntimeException("Invalid labyrinth file!");
            }
            try {
                startX = Integer.valueOf(args[1]);
                startY = Integer.valueOf(args[2]);
            }
            catch (Exception e) {}
            if (startX < 0) {
                LOG.error("Invalid startX [{}]!", args[1]);
                throw new RuntimeException("Invalid startX!");
            }
            if (startY < 0) {
                LOG.error("Invalid startY [{}]!", args[2]);
                throw new RuntimeException("Invalid startY!");
            }
            try {
                startDirection = Direction.valueOf(args[3].toUpperCase());
            }
            catch (Exception e) {}
            if (startDirection == null) {
                LOG.error("Unknown start direction [{}]!", args[3]);
                throw new RuntimeException("Unknown start direction!");
            }
            
            try {
                id = Integer.valueOf(args[4]);
            }
            catch (Exception e) {}
            
            if (id == null) {
                LOG.error("Invalid id [{}]!", args[4]);
                throw new RuntimeException("Invalid id!");
            }
            
            FileInputStream fis = new FileInputStream(labyrinthFile);
            try {
                labyrinth = BasicLabyrinth.load(fis);
            }
            catch (Exception e) {
                LOG.error("Unable to load labyrinth from file [{}]!", labyrinthFile);
                throw new RuntimeException("Unable to load labyrinth from file!");
            }
            startCell = labyrinth.getCell(startX, startY);
        }
        catch (Exception e) {
            LOG.error("Unable to parse arguments!", e);
            throw new RuntimeException("Unable to parse arguments!");
        }
        
        LOG.info("Starting Robotun emulator with the following parameters:");
        LOG.info("labyrinthFile: [{}]", labyrinthFile);
        LOG.info("startX: [{}]", startX);
        LOG.info("startY: [{}]", startY);
        LOG.info("startDirection: [{}]", startDirection);
        LOG.info("id: [{}]", id);
        
        Properties robotProperties = readRobotProperties(args);
        
        try {
            launcher = new EmulatorLauncher(this, labyrinth, startCell, startDirection, "emulator"+id, robotProperties);
        } catch (Exception e) {
            LOG.error("Failed to construct emulator launcher!", e);
            throw new RuntimeException("Failed to construct emulator launcher!");
        }
        launcher.start();
    }
    
    private Properties readRobotProperties(String[] args) {
        Properties props = new Properties();
        for (String arg : args) {
            if (arg != null && arg.length()>0) {
                String[] params = arg.split("=");
                if (params != null && params.length==2) {
                    String name = params[0];
                    String value = params[1];
                    if (name != null && value != null && value.length()>0) {
                        LOG.info("Robot property {} = {}", name, value);
                        props.put(name, value);
                    }
                }
            }
        }
        return props;
    }
    
    class OutputHandler implements Runnable {

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            try {
                while ((line=reader.readLine()) != null) {
                    LOG.info("Received command: " + line);
                    if (line.equals("shutdown")) {
                        LOG.info("Recevied shutdown command!");
                        if (launcher != null && started) {
                            launcher.stop();
                        }
                        else {
                            System.exit(0);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("Error reading console!", e);
                System.exit(-1); 
            }
            
        }
        
    }

    @Override
    public void onLauncherStarted(String entity) {
        started = true;
        LOG.info("Launcher started!");
    }

    @Override
    public void onLauncherStartRun() {
        LOG.info("Launcher Start Run!");
    }

    @Override
    public void onLauncherStopped() {
        started = false;
        System.exit(0);
    }

    @Override
    public void onLauncherError(Exception e) {
        LOG.info("Launcher Error!", e);
    }

}
