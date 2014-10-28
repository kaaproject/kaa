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

package org.kaaproject.kaa.examples.robotrun.controller.state;

import java.util.List;

import org.kaaproject.kaa.examples.robotrun.controller.Context;
import org.kaaproject.kaa.examples.robotrun.controller.events.EventManager;
import org.kaaproject.kaa.examples.robotrun.controller.events.MoveEventListener;
import org.kaaproject.kaa.examples.robotrun.controller.events.Reason;
import org.kaaproject.kaa.examples.robotrun.controller.robot.RobotManager;
import org.kaaproject.kaa.examples.robotrun.controller.robot.RobotMoveListener;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderUpdate;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunawayState implements ControllerState {
    private static final Logger LOG = LoggerFactory
            .getLogger(RunawayState.class);

    private List<Cell>   runawayPath;
    private Cell         exitCell;
    private EventManager eventManager;
    private RobotManager robotManager;
    private volatile Context context;
    private volatile boolean stopped = false;

    private RunawayRobotMoveListener robotListener;
    private RunawayMoveEventListener eventListener;

    @Override
    public void process(Context context) {
        runawayPath  = context.getRunawayPath();
        eventManager = context.getEventManager();
        robotManager = context.getRobotManager();
        exitCell     = context.getExit();
        this.context = context;
        runaway();
    }

    private void runaway() {
        if (runawayPath != null) {
            if (!runawayPath.isEmpty()) {
                LOG.info("Moving to the exit.");
                robotListener = new RunawayRobotMoveListener();
                eventListener = new RunawayMoveEventListener();
            }
        }
        tryMove();
    }

    private Cell getOuterExitCell(final Cell exitCell) {
        if (!exitCell.isExit()) {
            throw new IllegalArgumentException("Not an exit cell");
        }
        return new Cell() {
            @Override
            public BorderUpdate setBorder(Direction side, BorderType border) {
                return null;
            }

            @Override
            public boolean isExit() {
                return false;
            }

            @Override
            public boolean isDiscovered() {
                return true;
            }

            @Override
            public int getY() {
                switch (exitCell.getExitDirection()) {
                case NORTH:
                    return -1;
                case SOUTH:
                    return exitCell.getY() + 1;
                default:
                    return exitCell.getY();
                }
            }

            @Override
            public int getX() {
                switch (exitCell.getExitDirection()) {
                case WEST:
                    return -1;
                case EAST:
                    return exitCell.getX() + 1;
                default:
                    return exitCell.getX();
                }
            }

            @Override
            public BorderType getBorder(Direction side) {
                return BorderType.FREE;
            }

            @Override
            public Direction getExitDirection() {
                return null;
            }

            @Override
            public String toString() {
                return "ExitCell [ " + getX() + ", " + getY() + " ]";
            }

            @Override
            public boolean isDeadEnd() {
                return false;
            }

            @Override
            public void markDeadEnd() {
            }
        };
    }

    private void doExit() {
        while (!exitCell.isDiscovered() && !stopped) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
        LOG.info("Exiting labyrint from cell [{}] isExit = {}", exitCell, exitCell.isExit());
        Cell outerExitCell = getOuterExitCell(exitCell);
        robotManager.move(outerExitCell, new RobotMoveListener() {

            @Override
            public void onMoveSuccess() {
                LOG.info("Exited labyrinth!");
                context.getEventManager().exitFound(context.getCell());
                Thread stopThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                            context.stop();
                        } catch (InterruptedException e) {}
                    }
                    
                });
                stopThread.start();
            }

            @Override
            public void onMoveFailure() {
                LOG.error("Failed to exit the labyrinth!");
                tryMove();
            }
        });
    }

    private void tryMove() {
        if (stopped)
            return;
        if (runawayPath != null && !runawayPath.isEmpty()) {
            Cell target = runawayPath.get(0);
            LOG.info("Going to request move to [{}]", target);
            eventManager.requestMove(target, eventListener);
        } else {
            doExit();
        }
    }

    private class RunawayRobotMoveListener implements RobotMoveListener {
        @Override
        public void onMoveSuccess() {
            context.setCell(robotManager.getPosition());
            context.getEventManager().reportLocation(
                    context.getCell());
            runawayPath.remove(0);
            tryMove();
        }

        @Override
        public void onMoveFailure() {
            LOG.error("Move failed.");
            tryMove();
        }
    }

    private class RunawayMoveEventListener implements MoveEventListener {
        @Override
        public void onMoveAllowed() {
            LOG.debug("Move allowed to {}", runawayPath.get(0));
            robotManager.move(runawayPath.get(0), robotListener);
        }

        @Override
        public void onMoveForbidden(Reason reason) {
            LOG.warn("Move forbidden to {} due to {}!", runawayPath.get(0), reason);
            tryMove();
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }
}
