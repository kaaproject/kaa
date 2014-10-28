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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatorProcess {
    
    private static final Logger LOG = LoggerFactory.getLogger(EmulatorProcess.class);
    
    private static final String OUT_FILE = "emulator.out";
    
    private static final String EMULATOR_ID_PREFIX = "roboemulator";
    
    private ProcessBuilder processBuilder; 
    private Process process;
    private File outFile;
    private PrintWriter emulatorStdout;
    private PrintWriter output;
    private boolean running = false;
    private boolean inited = false;
    private Integer id;
    private Thread processWaiter;
    
    private EmulatorCallback callback;
    
    public EmulatorProcess (Integer id, 
            File labyrinthFile, 
            Cell startCell, 
            Direction startDirection, 
            Properties robotProperties,
            int debugPort,
            EmulatorCallback callback) {
        
        this.id = id;
        this.callback = callback;
        
        String workingDir = System.getProperty("user.dir");
        
        File emulatorWorkingDir = new File(new File(workingDir), EMULATOR_ID_PREFIX+id);
        if (!emulatorWorkingDir.exists()) {
            emulatorWorkingDir.mkdirs();
        }
        else {
            try {
                FileUtils.cleanDirectory(emulatorWorkingDir);
            } catch (IOException e) {
                LOG.error("Unable to clean emulator directory {}", emulatorWorkingDir);
                throw new RuntimeException("Unable to clean emulator directory!");
            } 
        }
        
        outFile = new File(emulatorWorkingDir, OUT_FILE);
        try {
            emulatorStdout = new PrintWriter(outFile);
        } catch (IOException e) {
            LOG.error("Unable to create emulator stdout {}", outFile);
            throw new RuntimeException("Unable to create emulator stdout!");
        }
        
        String separator = System.getProperty("path.separator");
        String classPath = System.getProperty("java.class.path");
        String[] classPathUnits = classPath.split(separator);
        StringBuilder normalizedClassPath = new StringBuilder();
        for (int i=0;i<classPathUnits.length;i++) {
            File classPathUnitFile = new File(classPathUnits[i]);
            if (classPathUnitFile.exists()) {
                if (i>0) {
                    normalizedClassPath.append(separator);
                }
                normalizedClassPath.append(classPathUnitFile.getAbsolutePath());
            }
        }
        
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(normalizedClassPath.toString());
        if (debugPort > -1) {
            command.add("-Xdebug");
            command.add("-Xrunjdwp:server=y,transport=dt_socket,address="+debugPort+",suspend=n");
        }
        command.add(EmulatorMain.class.getName());
        command.add(labyrinthFile.getAbsolutePath());
        command.add(""+startCell.getX());
        command.add(""+startCell.getY());
        command.add(startDirection.name());
        command.add(""+id);
        
        if (robotProperties != null) {
            for (Object key : robotProperties.keySet()) {
                String val = robotProperties.getProperty(""+key);
                command.add(key+"="+val);
            }
        }

        StringBuilder emulatorStartCommand = new StringBuilder();
        for (String arg : command) {
            emulatorStartCommand.append(arg+" ");
        }
        
        emulatorStdout.println("Starting emulator using the following command:");
        emulatorStdout.println(emulatorStartCommand.toString());

        processBuilder = new ProcessBuilder(command);
        processBuilder.directory(emulatorWorkingDir);
        processBuilder.redirectErrorStream(true);
    }
    
    public void start() {
        try {
            process = processBuilder.start();
            running = true;
            output = new PrintWriter(process.getOutputStream());
            handleStream(process.getInputStream());
            
            processWaiter = new Thread(new Runnable() {
                public void run() {
                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {}
                    fireProcessStopped();
                }
            }, "Process waiter for " + id);
            processWaiter.start();
            
        } catch (IOException e) {
            LOG.error("Unable to start emulator process!", e);
            throw new RuntimeException("Unable to start emulator process!");
        }
    }
    
    private void fireProcessStopped() {
        running = false;
        process = null;
        if (callback != null) {
            callback.onEmulatorStopped(id);
        }
    }
    
    private void handleStream(final InputStream input) {
        Runnable streamHandler = new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String line = null;
                    while (running) {
                        line = reader.readLine();
                        if (line != null) {
                            emulatorStdout.println(line);
                            emulatorStdout.flush();
                            if (!inited && line.contains("Launcher started!")) {
                                inited = true;
                                callback.onEmulatorInitCompleted(id);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOG.error("Unexpected exception in handleStream: ", e);
                }
                finally {
                    if (emulatorStdout != null) {
                        emulatorStdout.close();
                    }
                }
            }
        };

        Thread handleStreamThread = new Thread(streamHandler, "Output handler for emulator " + id);
        handleStreamThread.start();
    }
    
    public void sendShutdown() {
        if (process != null && running) {
            if (output != null) {
                output.println("shutdown");
                output.flush();
            }
        }
    }
    
    public void terminate() {
        if (process != null && running) {
            process.destroy();
        }
    }
    
    public static interface EmulatorCallback {
        void onEmulatorInitCompleted(Integer id);
        void onEmulatorStopped(Integer id);
    }
    
}
