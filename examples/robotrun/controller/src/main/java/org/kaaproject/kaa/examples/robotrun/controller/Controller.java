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
package org.kaaproject.kaa.examples.robotrun.controller;

import org.kaaproject.kaa.examples.robotrun.controller.configuration.LabyrinthUpdateListener;
import org.kaaproject.kaa.examples.robotrun.controller.events.EventListener;
import org.kaaproject.kaa.examples.robotrun.controller.state.StartRunState;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller implements LabyrinthUpdateListener, EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    private final Context context;
    private final ControllerCallback callback;
    private boolean started = false;

    public Controller(Context context, ControllerCallback callback) {
        super();
        this.context = context;
        this.callback = callback;
        LOG.debug("Registration of Configuration and Event listeners started");
        context.getConfigurationManager().addListener(this);
        context.getEventManager().addListener(this);
        LOG.debug("Registration of Configuration and Event listeners completed");
    }

    public void start() throws Exception {
        LOG.info("Starting controller...");        
        LOG.debug("Starting context...");
        context.start();
        LOG.info("Controller started.");
    }
    
    public void stop() {
        started = false;
        LOG.info("Stopping controller...");
        context.stop();
        context.getConfigurationManager().removeListener(this);
        context.getEventManager().removeListener(this);
        LOG.info("Controller stopped.");
        callback.onControllerStoped();
    }
    
    public boolean isStarted() {
        return started;
    }
    
    @Override
    public void onEventManagerInitComplete() {
        LOG.debug("Event manager init completed!"); 
        started = true;
        callback.onControllerStarted();
    }

    @Override
    public void onEventManagerInitFailed(String reason) {
        LOG.debug("Event manager init failed: {}", reason); 
        started = false;
        callback.onControllerStartFailed();
    }

    @Override
    public void onStartRun() {
        callback.onControllerStartRun();
        context.setState(new StartRunState());
        context.process();
    }

    @Override
    public void onLabyrinthUpdate(Labyrinth labyrinth) {
        if (labyrinth != null) {
            LOG.info("Labyrinth updated! [{},{}]", labyrinth.getWidth(), labyrinth.getHeight());
        }
        else {
            LOG.warn("Labyrinth updated! Labyrinth is null!");
        }
        context.setLabyrinth(labyrinth);
    }

    @Override
    public void onExitFound(Cell cell) {
        context.setExit(cell);
    }

    @Override
    public void onExitFoundCompeted() {
    }

    @Override
    public void onRobotLocationChanged(String key, String name, Cell cell) {
        context.getExitSearchAlgorithm().onRobotLocationChanged(key, cell);
    }
    
    public interface ControllerCallback {
        
        void onControllerStarted();
        
        void onControllerStartFailed();
        
        void onControllerStartRun();
        
        void onControllerStoped();
    }

}
