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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinth;
import org.kaaproject.kaa.examples.robotrun.visualization.emulator.EmulatorProcess.EmulatorCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatorManager implements EmulatorCallback {
    
    private static final Logger LOG = LoggerFactory.getLogger(EmulatorManager.class);
    
    private static final String LABYRINTH_FILE = "labyrinth.data";
    
    private static final int startDebugPort = 33500;

    private Map<Integer,EmulatorProcess> emulators = new HashMap<>();
    private Set<Integer> emulatorIds = new HashSet<>();
    
    private boolean enableDebug;
    
    private EmulatorManagerCallback callback;
    
    private Object shutdownMonitor = new Object();
    
    public EmulatorManager(EmulatorManagerCallback callback, boolean enableDebug) {
        this.callback = callback;
        this.enableDebug = enableDebug;
    }
    
    public static void saveLabyrinth(Labyrinth labyrinth) {
        File labyrinthFile = new File(LABYRINTH_FILE);
        try {
            if (labyrinthFile.exists()) {
                labyrinthFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(labyrinthFile);
            labyrinth.store(fos);
            fos.flush();
            fos.close();
        }
        catch (IOException e) {
            LOG.error("Unable to save labyrinth to file [{}]!", labyrinthFile);
        }
    }
    
    public static Labyrinth loadLabyrinth() {
        Labyrinth labyrinth = null;
        File labyrinthFile = new File(LABYRINTH_FILE);
        if (labyrinthFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(labyrinthFile);
                labyrinth = BasicLabyrinth.load(fis);
            }
            catch (Exception e) {
                LOG.error("Unable to load labyrinth from file [{}]!", labyrinthFile);
            }
        }
        return labyrinth;
    }
    
    public void setupEmulators(Map<Cell, Direction> robots, Properties robotProperties) throws IOException {
        emulators.clear();
        emulatorIds.clear();
        
        File labyrinthFile = new File(LABYRINTH_FILE);
        
        for (Cell cell : robots.keySet()) {
            Direction direction = robots.get(cell);
            Integer id = (emulators.size()+1);
            int debugPort = -1;
            if (enableDebug) {
                debugPort = startDebugPort + emulators.size();
            }
            EmulatorProcess process = new EmulatorProcess(id, labyrinthFile, cell, direction, robotProperties, debugPort, this);
            emulators.put(id, process);
            emulatorIds.add(id);
        }
    }
    
    public void startEmulators() {
        synchronized (emulators) {
            for (EmulatorProcess process : emulators.values()) {
                process.start();
            }
        }
    }
    
    public void stopEmulators() {
        if (isStarted()) {
            Thread t = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            synchronized (emulators) {
                                for (EmulatorProcess process : emulators.values()) {
                                    process.sendShutdown();
                                }
                            }
                            synchronized (shutdownMonitor) {
                                while (isStarted()) {
                                    try {
                                        shutdownMonitor.wait();
                                    } catch (InterruptedException e) {}
                                }
                            }
                        }                        
            }, "Emulator shutdown thread");
            t.start();
            try {
                t.join(5000);
            } catch (InterruptedException e) {}
            if (isStarted()) {
                terminateEmulators();
            }
        }
    }
    
    private void terminateEmulators() {
        synchronized (emulators) {
            for (EmulatorProcess process : emulators.values()) {
                process.terminate();
            }
        }
    }
    
    public boolean isStarted() {
        return !emulators.isEmpty();
    }

    @Override
    public void onEmulatorInitCompleted(Integer id) {
        emulatorIds.remove(id);
        if (emulatorIds.isEmpty()) {
            callback.onEmulatorsInitCompleted();
        }
    }

    @Override
    public void onEmulatorStopped(Integer id) {
        synchronized (emulators) {
            emulators.remove(id);
        }
        if (emulators.isEmpty()) {
            synchronized (shutdownMonitor) {
                shutdownMonitor.notifyAll();
            }
            callback.onEmulatorsStopped();
        }
    }

    public static interface EmulatorManagerCallback {
        
        void onEmulatorsInitCompleted();
        
        void onEmulatorsStopped();
        
    }

    
}
