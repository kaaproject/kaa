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
package org.kaaproject.kaa.examples.robotrun.controller.robot;

import java.util.LinkedList;

import org.kaaproject.kaa.examples.robotrun.controller.Context;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveForwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.OperationStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PingCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PongStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.TurnCallback;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRobotManager implements RobotManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRobotManager.class);
    private final Drivable drivable;

    private Cell currentPos;
    private Direction currentDirection;

    private volatile boolean progress = false;
    private volatile boolean stopped = false;

    private boolean turnClockwise = true;

    private Cell targetPos;
    private TurnDirection turnDirection;

    public DefaultRobotManager(Cell position, Direction direction, Drivable drivable) {
        this.currentPos = position;
        this.currentDirection = direction;
        this.drivable = drivable;
    }

    @Override
    public Cell getPosition() {
        return currentPos;
    }

    @Override
    public Direction getDirection() {
        return currentDirection;
    }

    @Override
    public synchronized void move(Cell target, RobotMoveListener listener) {
        if (stopped){
            return;
        }
        checkProgress();

        if (!Context.isNeighbors(currentPos, target)) {
            LOG.warn("Can't move from {} to {}", currentPos, target);
            throw new IllegalArgumentException("Cell is to far!");
        }

        this.targetPos = target;

        doMove(listener);
    }

    private void doMove(final RobotMoveListener listener) {
        if (stopped){
            return;
        }
        if (currentPos.equals(targetPos)) {
            clearProgress();
            listener.onMoveSuccess();
        } else {
            Direction targetDirection = getMoveDirection(currentPos, targetPos);
            if (currentDirection != targetDirection) {
                TurnDirection turn = getTurnDirection(currentDirection, targetDirection);
                final Direction pendingDirection = getDirection(currentDirection, turn);
                drivable.registerTurnCallback(new TurnCallback() {
                    @Override
                    public void complete(OperationStatus status) {
                        if (status == OperationStatus.SUCESSFULL) {
                            LOG.info("Turned robot from {} to {}", currentDirection, pendingDirection);
                            currentDirection = pendingDirection;
                        } else {
                            LOG.warn("Failed to turn robot from {} to {}", currentDirection, pendingDirection);
                        }
                        doMove(listener);
                    }
                });
                //TODO set to use properly turn with calibration
                drivable.turn(turn, true);
            } else {
                final Cell pendingPos = targetPos;
                drivable.registerMoveForwardCallback(new MoveForwardCallback() {
                    @Override
                    public void complete(OperationStatus status) {
                        if (status == OperationStatus.SUCESSFULL) {
                            LOG.info("Moved robot from {} to {}", currentPos, pendingPos);
                            currentPos = pendingPos;
                        } else {
                            LOG.warn("Failed to move robot from {} to {}", currentPos, pendingPos);
                        }
                        doMove(listener);
                    }
                });
                //TODO set to use properly turn with calibration
                drivable.moveForward(true);
            }
        }
    }

    @Override
    public synchronized void scan(RobotScanListener listener) {
        if (stopped) {
            return;
        }
        checkProgress();
        doScan(listener, getUndiscoveredDirections());
    }

    private LinkedList<Direction> getUndiscoveredDirections() {
        LinkedList<Direction> undiscoveredDirections = new LinkedList<>();
        Direction [] directionValues = Direction.values();

        for (int directionCounter = 0, index = currentDirection.ordinal();
                directionCounter < directionValues.length; ++directionCounter)
        {
            if (index < 0 || index >= directionValues.length) {
                index = (turnClockwise ? 0 : (directionValues.length - 1));
            }

            if (currentPos.getBorder(directionValues[index]) == BorderType.UNKNOWN) {
                undiscoveredDirections.add(directionValues[index]);
            }

            index = (turnClockwise ? ++index : --index);
        }

        return undiscoveredDirections;
    }

    private void doScan(final RobotScanListener listener, final LinkedList<Direction> undiscoveredDirections) {
        if (stopped) {
            return;
        }

        if (undiscoveredDirections.size() > 0) {
            turnDirection = guessTurnDirection(currentDirection, undiscoveredDirections.peek());

            if (turnDirection == null) {
                drivable.registerPingCallback(new PingCallback() {
                    @Override
                    public void pong(PongStatus status) {
                        BorderType borderType = (status == PongStatus.EMPTY ? BorderType.FREE : BorderType.SOLID);
                        LOG.info("Scanned in cell {} with direction {} -> {}", currentPos, currentDirection, borderType);
                        listener.onScan(currentDirection, borderType);

                        if (undiscoveredDirections.size() > 1) {
                            undiscoveredDirections.poll();
                            doScan(listener, undiscoveredDirections);
                        } else {
                            turnClockwise = !turnClockwise;
                            LOG.debug("Change turn direction to {}", (turnClockwise ? "RIGHT" : "LEFT"));
                            clearProgress();
                            listener.onScanComplete();
                        }
                    }
                });
                drivable.ping();
            } else {
                drivable.registerTurnCallback(new TurnCallback() {
                    @Override
                    public void complete(OperationStatus status) {
                        if (status == OperationStatus.SUCESSFULL) {
                            LOG.info("Turned robot from {} to {}", currentDirection, turnDirection);
                            currentDirection = getDirection(currentDirection, turnDirection);
                        } else {
                            LOG.warn("Failed to turn robot from {} to {}", currentDirection, turnDirection);
                        }

                        if (undiscoveredDirections.peek() == currentDirection) {
                            doScan(listener, undiscoveredDirections);
                        } else {
                            drivable.turn(turnDirection, true);
                        }
                    }
                });
                drivable.turn(turnDirection, true);
            }
        }
    }

    private TurnDirection guessTurnDirection(Direction current, Direction target) {
        TurnDirection td = null;
        if (current != target) {
            int turnCount = 1;
            td = (turnClockwise ? TurnDirection.RIGHT : TurnDirection.LEFT);
            Direction d = current;
            while (target != (d = getDirection(d, td))) {
                ++turnCount;
            }

            if (turnCount >= 2) {
                td = (turnClockwise ? TurnDirection.LEFT : TurnDirection.RIGHT);
                turnClockwise = !turnClockwise;
                LOG.debug("Find better way, so direction has changed to {}"
                                        , (turnClockwise ? "RIGHT" : "LEFT"));
            }
        }
        return td;
    }

    private void clearProgress() {
        progress = false;
    }

    private void checkProgress() {
        if (progress) {
            throw new IllegalStateException("Robot is busy!");
        } else {
            progress = true;
        }
    }

    private Direction getDirection(Direction currentDirection, TurnDirection turn) {
        switch (currentDirection) {
        case NORTH:
            return turn == TurnDirection.LEFT ? Direction.WEST : Direction.EAST;
        case WEST:
            return turn == TurnDirection.LEFT ? Direction.SOUTH : Direction.NORTH;
        case SOUTH:
            return turn == TurnDirection.LEFT ? Direction.EAST : Direction.WEST;
        case EAST:
            return turn == TurnDirection.LEFT ? Direction.NORTH : Direction.SOUTH;
        default:
            return null;
        }
    }

    private TurnDirection getTurnDirection(Direction currentDirection, Direction targetDirection) {
        if ((currentDirection == Direction.NORTH && targetDirection == Direction.EAST)
                || (currentDirection == Direction.EAST && targetDirection == Direction.SOUTH)
                || (currentDirection == Direction.SOUTH && targetDirection == Direction.WEST)
                || (currentDirection == Direction.WEST && targetDirection == Direction.NORTH)) {
            return TurnDirection.RIGHT;
        } else {
            return TurnDirection.LEFT;
        }
    }

    private Direction getMoveDirection(Cell from, Cell to) {
        if (from.getX() == to.getX()) {
            return from.getY() - to.getY() > 0 ? Direction.NORTH : Direction.SOUTH;
        } else {
            return from.getX() - to.getX() > 0 ? Direction.WEST : Direction.EAST;
        }
    }

    @Override
    public void stop() {
        LOG.info("Stopping Robot Manager...");
        stopped = true;
        drivable.shutdown();
    }

}
