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

import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.examples.robotrun.controller.Context;
import org.kaaproject.kaa.examples.robotrun.controller.events.MoveEventListener;
import org.kaaproject.kaa.examples.robotrun.controller.events.Reason;
import org.kaaproject.kaa.examples.robotrun.controller.path.ManhattanDistanceHeuristic;
import org.kaaproject.kaa.examples.robotrun.controller.path.PathSearchAlgorithm;
import org.kaaproject.kaa.examples.robotrun.controller.robot.RobotManager;
import org.kaaproject.kaa.examples.robotrun.controller.robot.RobotMoveListener;
import org.kaaproject.kaa.examples.robotrun.controller.robot.RobotScanListener;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderUpdate;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExitSearchState implements ControllerState {
    private static final Logger LOG = LoggerFactory.getLogger(ExitSearchState.class);
    private volatile Context context;
    private BorderUpdate update;
    private Map<String, RobotPosition> robotPositions;
    private RobotManager robot;
    private volatile boolean stopped = false;

    @Override
    public void process(Context context) {
        this.context = context;
        this.robot = context.getRobotManager();
        scan();
    }

    private void scan() {
        if (stopped) {
            return;
        }

        if(tryExit()){
            return;
        };

        LOG.debug("Get Robot positions");
        robotPositions = context.getEventManager().getRobotPositions();
        LOG.debug("Scan current cell");
        if (!context.getCell().isDiscovered()) {
            robot.scan(new RobotScanListener() {

                @Override
                public void onScan(Direction direction, BorderType borderType) {
                    LOG.info("Scan result received. Direction: {} borderType: {}", direction, borderType);
                    BorderUpdate tmp = context.getCell().setBorder(direction, borderType);
                    if (update == null) {
                        update = tmp;
                    } else {
                        update.add(tmp);
                    }
                }

                @Override
                public void onScanFailure() {
                    // TODO: Handle
                    LOG.warn("Scan failure!");
                }

                @Override
                public void onScanComplete() {
                    onCellScanComplete();
                }
            });
        } else {
            onCellScanComplete();
        }
    }

    private boolean tryExit() {
        if (context.getExit() != null) {
            LOG.debug("Exit already found by other robots!");
            List<Cell> path = getExitPath(context.getExit());
            if (path != null) {
                LOG.info("Exit path found!");
                context.setRunawayPath(path);
                context.setState(new RunawayState());
                context.process();
                return true;
            } else {
                LOG.debug("Exit path is unknown yet!");
            }
        }
        return false;
    }

    private void onCellScanComplete() {
        if (stopped) {
            return;
        }
        boolean exit = context.getCell().isExit();
        LOG.info("Scan completed. IsExit: {}, Updates: {}", exit, update);
        if (update != null) {
            context.getLogManager().reportBorders(update);
            update = null;
        }
        if (exit) {
            context.setExit(context.getCell());
            context.getEventManager().exitFound(context.getCell());
            startRunaway();
        } else {
            tryMove();
        }
    }

    private List<Cell> getExitPath(Cell exitCell) {
        PathSearchAlgorithm algorithm = context.getPathSearchAlgorithm();
        algorithm.setHeuristic(new ManhattanDistanceHeuristic(exitCell));
        return algorithm.getPath(context.getCell(), exitCell, context.getLabyrinth());
    }

    private void startRunaway() {
        context.setState(new RunawayState());
        context.process();
    }

    private void tryMove() {
        if (stopped) {
            return;
        }

        Cell nextCell;
        boolean nextCellIsSame = false;
        do {
            if(tryExit()){
                return;
            }
            boolean deadEnd = context.getCell().isDeadEnd();
            nextCell = context.getExitSearchAlgorithm().getNextCell(context.getLabyrinth(), context.getCell(),
                    robotPositions);
            if (!deadEnd && context.getCell().isDeadEnd()) {
                context.getEventManager().reportLocation(context.getCell());
            }
            nextCellIsSame = nextCell.equals(context.getCell());
            if (nextCellIsSame) {
                try {
                    LOG.warn("Calculated next cell is same {}, sleeping 200 ms", nextCell);
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
        } while (nextCellIsSame && !stopped);

        if (stopped) {
            return;
        }

        final Cell next = nextCell;
        LOG.info("Calculated next target cell {}", next);
        context.getEventManager().requestMove(next, new MoveEventListener() {

            @Override
            public void onMoveForbidden(Reason reason) {
                LOG.debug("Move forbidden to {}", next);
                tryMove();
            }

            @Override
            public void onMoveAllowed() {
                LOG.debug("Move allowed to {}", next);
                context.getRobotManager().move(next, new RobotMoveListener() {

                    @Override
                    public void onMoveSuccess() {
                        context.setCell(robot.getPosition());
                        context.getEventManager().reportLocation(context.getCell());
                        scan();
                    }

                    @Override
                    public void onMoveFailure() {
                        LOG.warn("Move failure!");
                        // TODO: Handle
                    }
                });
            }
        });
    }

    @Override
    public void stop() {
        stopped = true;
    }
}
