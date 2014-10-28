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

import java.util.List;

import org.kaaproject.kaa.examples.robotrun.controller.configuration.ConfigurationManager;
import org.kaaproject.kaa.examples.robotrun.controller.events.EventManager;
import org.kaaproject.kaa.examples.robotrun.controller.log.LogManager;
import org.kaaproject.kaa.examples.robotrun.controller.path.ExitSearchAlgorithm;
import org.kaaproject.kaa.examples.robotrun.controller.path.PathSearchAlgorithm;
import org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable;
import org.kaaproject.kaa.examples.robotrun.controller.robot.RobotManager;
import org.kaaproject.kaa.examples.robotrun.controller.state.ControllerState;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);

    private ConfigurationManager configurationManager;
    private EventManager eventManager;
    private LogManager logManager;
    private RobotManager robotManager;
    private Drivable drivable;
    private ExitSearchAlgorithm exitSearchAlgorithm;
    private PathSearchAlgorithm pathSearchAlgorithm;

    private volatile ControllerState state;

    private volatile Labyrinth labyrinth;
    private volatile Cell cell;
    private volatile Cell exit;
    private volatile List<Cell> runawayPath;

    public void process(){
        if(state != null){
            LOG.debug("Processing {} state", state.getClass().getName());
            state.process(this);
        }else{
            LOG.error("State is not set!");
            throw new IllegalStateException("State is not set!");
        }
    }
    
    public void start() throws Exception {
        eventManager.init();
        if (drivable != null) {
            drivable.start();
        }
    }
    
    public void stop() {
        LOG.info("Stopping context...");
        if(state != null){
            state.stop();
            state = null;
        }
        if (robotManager != null) {
            robotManager.stop();
        }
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void setConfigurationManager(
            ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public Drivable getDrivable() {
        return drivable;
    }
    
    public void setDrivable(Drivable drivable) {
        this.drivable = drivable;
    }

    public RobotManager getRobotManager() {
        return robotManager;
    }
    
    public void setRobotManager(RobotManager robotManager) {
        this.robotManager = robotManager;
    }

    public ExitSearchAlgorithm getExitSearchAlgorithm() {
        return exitSearchAlgorithm;
    }

    public void setExitSearchAlgorithm(ExitSearchAlgorithm exitSearchAlgorithm) {
        this.exitSearchAlgorithm = exitSearchAlgorithm;
    }

    public ControllerState getState() {
        return state;
    }

    public void setState(ControllerState state) {
        this.state = state;
    }

    public Labyrinth getLabyrinth() {
        return labyrinth;
    }

    public void setLabyrinth(Labyrinth labyrinth) {
        this.labyrinth = labyrinth;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Cell getExit() {
        return exit;
    }

    public void setExit(Cell exit) {
        this.exit = exit;
    }

    public List<Cell> getRunawayPath() {
        return runawayPath;
    }

    public void setRunawayPath(List<Cell> runawayPath) {
        this.runawayPath = runawayPath;
    }

    public PathSearchAlgorithm getPathSearchAlgorithm() {
        return pathSearchAlgorithm;
    }

    public void setPathSearchAlgorithm(PathSearchAlgorithm pathSearchAlgorithm) {
        this.pathSearchAlgorithm = pathSearchAlgorithm;
    }

    public static boolean isNeighbors(Cell c1, Cell c2) {
        return (Math.abs(c1.getX() - c2.getX()) + Math.abs(c1.getY() - c2.getY())) == 1;
    }
    
    public static Direction getDirection(Cell source, Cell neighbor){
        if(source.getX() < neighbor.getX()){
            return Direction.EAST;
        }else if (source.getX() > neighbor.getX()){
            return Direction.WEST;
        }else if (source.getY() < neighbor.getY()){
            return Direction.SOUTH;
        }else{
            return Direction.NORTH;
        }
    }
}
