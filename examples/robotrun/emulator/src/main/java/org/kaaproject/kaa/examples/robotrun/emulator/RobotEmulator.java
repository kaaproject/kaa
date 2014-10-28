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
package org.kaaproject.kaa.examples.robotrun.emulator;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable;
import org.kaaproject.kaa.examples.robotrun.controller.robot.TurnDirection;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.ErrorCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveBackwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveForwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.OperationCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.OperationStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PingCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PongStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.StateCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.TurnCallback;
import org.kaaproject.kaa.examples.robotrun.emulator.commands.MoveBackward;
import org.kaaproject.kaa.examples.robotrun.emulator.commands.MoveForward;
import org.kaaproject.kaa.examples.robotrun.emulator.commands.MovementCollision;
import org.kaaproject.kaa.examples.robotrun.emulator.commands.Ping;
import org.kaaproject.kaa.examples.robotrun.emulator.commands.Turn;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RobotEmulator Class.
 * Simulates robot actions.
 * Typical initialization:
 *      Properties props = new Properties();
 *      String commTimeout = "1001";
 *      props.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT, commTimeout );
 *      String commTimeoutDev = "101";
 *      props.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION, commTimeoutDev );
 *      String pingTimeout = "203";
 *      props.put(RobotEmulator.PROPERTY_NAME_PING_TIMEOUT, pingTimeout );
 *
 *      RobotEmulator emu = new RobotEmulator(labyrinth, initialPosition, initialDirection, props);
 *
 * Properties props = new Properties(); may not be used, default values in msec:
 *      RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT             1000
 *      RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION   100
 *      RobotEmulator.PROPERTY_NAME_PING_TIMEOUT                200
 *
 * PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION - is used to randomize real command complete time.
 *
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class RobotEmulator implements Drivable {
    
    private static final Logger LOG = LoggerFactory.getLogger(RobotEmulator.class);

    public static final String PROPERTY_NAME_COMMAND_TIMEOUT           = "COMMAND_TIMEOUT";
    public static final String PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION = "COMMAND_TIMEOUT_DEVIATION";
    public static final String PROPERTY_NAME_PING_TIMEOUT              = "PING_TIMEOUT";
    public static final long DEFAULT_COMMAND_TIMEOUT = 1000;
    public static final int DEFAULT_COMMAND_TIMEOUT_DEVIATION = 100;
    public static final long DEFAULT_PING_TIMEOUT = 200;

    private long currentCommandTimeout = DEFAULT_COMMAND_TIMEOUT;

    private int currentTimeoutDeviation = DEFAULT_COMMAND_TIMEOUT_DEVIATION;

    private long pingTimeout = DEFAULT_PING_TIMEOUT;

    private TurnCallback turnCallback;

    private MoveForwardCallback mForwardCallback;

    private MoveBackwardCallback mBackwardCallback;

    private PingCallback pingCallback;

    private ErrorCallback errorCallabck;
    
    private StateCallback stateCallback;

    private final Labyrinth labyrinth;

    private Cell currentPosition;

    private Direction currentDirection;

    private ExecutorService executor;

    private static Random rnd = new Random();

    /**
     * Default constructor.
     * @param labyrinth - Labyrinth, externally generated labyrinth
     * @param initialPosition - Cell, initial position of robot in labyrinth
     * @param initialDirection - Direction, direction where robot faced.
     * @param props - Properties.
     */
    public RobotEmulator(Labyrinth labyrinth, Cell initialPosition, Direction initialDirection, Properties props) {
        this.labyrinth = labyrinth;
        currentPosition = initialPosition;
        currentDirection = initialDirection;
        executor = Executors.newFixedThreadPool(1);
        if (props != null) {
            if (props.containsKey(PROPERTY_NAME_COMMAND_TIMEOUT)) {
                setCommandTimeout(Long.parseLong(props.getProperty(PROPERTY_NAME_COMMAND_TIMEOUT)));
            }
            if (props.containsKey(PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION)) {
                currentTimeoutDeviation = Integer.parseInt(props.getProperty(PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION));
            }
            if (props.containsKey(PROPERTY_NAME_PING_TIMEOUT)) {
                pingTimeout = Long.parseLong(props.getProperty(PROPERTY_NAME_PING_TIMEOUT));
            }
        }
    }

    @Override
    public void start() throws Exception {
        LOG.info("Starting...");
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if (stateCallback != null) {
                        stateCallback.onConnected("BOT_EMU_"+rnd.nextInt(100));
                    }
                } catch (InterruptedException e) {}
            }
        });
    }
    
    /**
     * Shutdown Emulator.
     */
    @Override
    public void shutdown() {
        LOG.info("Shutdown initiated.");
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
    
            } finally {
                executor = null;
            }
        }
        if (stateCallback != null) {
            stateCallback.onDisconnected();
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#turn(org.kaaproject.kaa.examples.robotrun.controller.robot.TurnDirection)
     */
    @Override
    public void turn(final TurnDirection direction, boolean withCalibration) {
        Turn turn = new Turn(getTimeout(), new OperationCallback() {

            @Override
            public void complete(OperationStatus status) {
                if (status == OperationStatus.SUCESSFULL) {
                    updateRobotDirection(direction);
                }
                if (turnCallback != null) {
                    turnCallback.complete(status);
                }
            }


        });
        executor.execute(turn);
    }



    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#moveForward()
     */
    @Override
    public void moveForward(boolean withCalibration) {
        MoveForward move = new MoveForward(getTimeout(), new OperationCallback( ) {

            @Override
            public void complete(OperationStatus status) {
                OperationStatus realStatus = status;
                if (status == OperationStatus.SUCESSFULL) {
                    try {
                        updatePositionForward();
                    } catch (MovementCollision e) {
                        realStatus = OperationStatus.FAILED;
                        if (errorCallabck != null) {
                            errorCallabck.error(e);
                        }
                    }
                }
                if (mForwardCallback != null) {
                    mForwardCallback.complete(realStatus);
                }
            }


        });
        executor.execute(move);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#moveBackward()
     */
    @Override
    public void moveBackward() {
        MoveBackward move = new MoveBackward(getTimeout(), new OperationCallback( ) {

            @Override
            public void complete(OperationStatus status) {
                OperationStatus realStatus = status;
                if (status == OperationStatus.SUCESSFULL) {
                    try {
                        updatePositionBackward();
                    } catch (MovementCollision e) {
                        realStatus = OperationStatus.FAILED;
                        if (errorCallabck != null) {
                            errorCallabck.error(e);
                        }
                    }
                }
                if (mBackwardCallback != null) {
                    mBackwardCallback.complete(realStatus);
                }
            }

        });
        executor.execute(move);

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#ping()
     */
    @Override
    public void ping() {
        Ping ping  = new Ping(getPingTimeout(), new OperationCallback() {

            @Override
            public void complete(OperationStatus status) {
                PongStatus pongStatus = PongStatus.EMPTY;
                if (getCurrentPosition().getBorder(getCurrentDirection()) == BorderType.SOLID) {
                    pongStatus = PongStatus.WALL;
                }
                if (pingCallback != null) {
                    pingCallback.pong(pongStatus);
                }
            }
        });
        executor.execute(ping);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#registerTurnCallback(org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.TurnCallback)
     */
    @Override
    public void registerTurnCallback(TurnCallback callback) {
        this.turnCallback = callback;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#registerMoveForwardCallback(org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveForwardCallback)
     */
    @Override
    public void registerMoveForwardCallback(MoveForwardCallback callback) {
        mForwardCallback = callback;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#registerMoveBackwardCallback(org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveBackwardCallback)
     */
    @Override
    public void registerMoveBackwardCallback(MoveBackwardCallback callback) {
        mBackwardCallback = callback;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#registerPingCallback(org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PingCallback)
     */
    @Override
    public void registerPingCallback(PingCallback callback) {
        pingCallback = callback;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#registerErrorCallback(org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.ErrorCallback)
     */
    @Override
    public void registerErrorCallback(ErrorCallback callback) {
        errorCallabck = callback;
    }

    /**
     * Update robot current direction according to specified turn directions.
     * @param direction TurnDirection
     */
    private void updateRobotDirection(TurnDirection direction) {
        switch (currentDirection) {
        case NORTH:
            if (direction == TurnDirection.LEFT) {
                currentDirection = Direction.WEST;
            } else if (direction == TurnDirection.RIGHT) {
                currentDirection = Direction.EAST;
            }
            break;
        case SOUTH:
            if (direction == TurnDirection.LEFT) {
                currentDirection = Direction.EAST;
            } else if (direction == TurnDirection.RIGHT) {
                currentDirection = Direction.WEST;
            }
            break;
        case WEST:
            if (direction == TurnDirection.LEFT) {
                currentDirection = Direction.SOUTH;
            } else if (direction == TurnDirection.RIGHT) {
                currentDirection = Direction.NORTH;
            }
            break;
        case EAST:
            if (direction == TurnDirection.LEFT) {
                currentDirection = Direction.NORTH;
            } else if (direction == TurnDirection.RIGHT) {
                currentDirection = Direction.SOUTH;
            }
            break;
        default:
            break;
        }
    }

    /**
     * Update current robot position on one Cell forward, check wall behind before movement
     * @throws If wall exist.
     */
    private void updatePositionForward() throws MovementCollision {
        if (getCurrentPosition().getBorder(getCurrentDirection()) == BorderType.SOLID) {
            throw new MovementCollision("Wall is set in "+
                            getCurrentDirection().toString()+
                            " direction from X="+getCurrentPosition().getX()+
                            " Y="+getCurrentPosition().getY());
        }
        if (currentPosition.isExit() && getCurrentDirection().equals(currentPosition.getExitDirection())) {
            currentPosition = null;
        } else {
            switch (getCurrentDirection()) {
            case NORTH:
                if(getCurrentPosition().getY() > 0) {
                    Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX(), getCurrentPosition().getY() - 1);
                    currentPosition = newPosition;
                } else {
                    throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                            getCurrentDirection().toString()+
                            " X="+getCurrentPosition().getX()+
                            " Y="+getCurrentPosition().getY());
                }
                break;
            case SOUTH:
                if(getLabyrinth().getHeight() > (getCurrentPosition().getY()+1)) {
                    Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX(), getCurrentPosition().getY() + 1);
                    currentPosition = newPosition;
                } else {
                    throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                            getCurrentDirection().toString()+
                            " X="+getCurrentPosition().getX()+
                            " Y="+getCurrentPosition().getY());
                }
                break;
            case WEST:
                if(getCurrentPosition().getX() > 0) {
                    Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX() - 1, getCurrentPosition().getY());
                    currentPosition = newPosition;
                } else {
                    throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                            getCurrentDirection().toString()+
                            " X="+getCurrentPosition().getX()+
                            " Y="+getCurrentPosition().getY());
                }
                break;
            case EAST:
                if(getLabyrinth().getWidth() > (getCurrentPosition().getX()+1)) {
                    Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX() + 1, getCurrentPosition().getY());
                    currentPosition = newPosition;
                } else {
                    throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                            getCurrentDirection().toString()+
                            " X="+getCurrentPosition().getX()+
                            " Y="+getCurrentPosition().getY());
                }
                break;
            default:
                break;
            }
        }

    }

    /**
     * Update current robot position on one Cell backward, check wall behind before movement
     * @throws If wall exist.
     */
    private void updatePositionBackward() throws MovementCollision {
        if (getCurrentPosition().getBorder(getOpositeDirection(getCurrentDirection())) == BorderType.SOLID) {
            throw new MovementCollision("Wall is set in "+
                            getCurrentDirection().toString()+
                            " direction from X="+getCurrentPosition().getX()+
                            " Y="+getCurrentPosition().getY());
        }
        switch (getCurrentDirection()) {
        case SOUTH:
            if(getCurrentPosition().getY() > 0) {
                Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX(), getCurrentPosition().getY() - 1);
                currentPosition = newPosition;
            } else {
                throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                        getCurrentDirection().toString()+
                        " X="+getCurrentPosition().getX()+
                        " Y="+getCurrentPosition().getY());
            }
            break;
        case NORTH:
            if(getLabyrinth().getHeight() > (getCurrentPosition().getY()+1)) {
                Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX(), getCurrentPosition().getY() + 1);
                currentPosition = newPosition;
            } else {
                throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                        getCurrentDirection().toString()+
                        " X="+getCurrentPosition().getX()+
                        " Y="+getCurrentPosition().getY());
            }
            break;
        case EAST:
            if(getCurrentPosition().getX() > 0) {
                Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX() - 1, getCurrentPosition().getY());
                currentPosition = newPosition;
            } else {
                throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                        getCurrentDirection().toString()+
                        " X="+getCurrentPosition().getX()+
                        " Y="+getCurrentPosition().getY());
            }
            break;
        case WEST:
            if(getLabyrinth().getWidth() > (getCurrentPosition().getX()+1)) {
                Cell newPosition = getLabyrinth().getCell(getCurrentPosition().getX() + 1, getCurrentPosition().getY());
                currentPosition = newPosition;
            } else {
                throw new MovementCollision("Labyrinth error, can't move to outside of Labyrinth: direction="+
                        getCurrentDirection().toString()+
                        " X="+getCurrentPosition().getX()+
                        " Y="+getCurrentPosition().getY());
            }
            break;
        default:
            break;
        }

    }

    /**
     * Return Direction of opposite to specified
     * @param direction Direction
     * @return opposite Direction
     */
    private Direction getOpositeDirection(Direction direction) {
        switch (direction) {
        case NORTH:
            return Direction.SOUTH;
        case SOUTH:
            return Direction.NORTH;
        case WEST:
            return Direction.EAST;
        case EAST:
            return Direction.WEST;
        }
        return Direction.SOUTH;
    }

    /**
     * Get command emulation timeout.
     * @return long
     */
    private long getTimeout() {
        int deviation = rnd.nextInt(getTimeoutDeviation());
        if (getCommandTimeout() > getTimeoutDeviation()) {
            long timeout = getCommandTimeout() - (getTimeoutDeviation()/2) + deviation;
            return timeout;
        }
        return getCommandTimeout();
    }

    /**
     * CommandTimeout getter.
     * @return currentCommandTimeout
     */
    public final long getCommandTimeout() {
        return currentCommandTimeout;
    }

    /**
     * TimeoutDeviation getter
     * @return currentTimeoutDeviation
     */
    public final int getTimeoutDeviation() {
        return currentTimeoutDeviation;
    }

    /**
     * Labyrinth getter.
     * @return Labyrinth
     */
    public Labyrinth getLabyrinth() {
        return labyrinth;
    }

    /**
     * CurrentPosition getter.
     * @return Cell
     */
    public Cell getCurrentPosition() {
        return currentPosition;
    }

    /**
     * CurrentDirection getter.
     * @return Direction
     */
    public Direction getCurrentDirection() {
        return currentDirection;
    }

    /**
     * CommandTimeout setter
     * @param commandTimeout
     */
    public void setCommandTimeout(long commandTimeout) {
        this.currentCommandTimeout = commandTimeout;
    }

    /**
     * TimeoutDeviation setter.
     * @param timeoutDeviation
     */
    public void setTimeoutDeviation(int timeoutDeviation) {
        this.currentTimeoutDeviation = timeoutDeviation;
    }

    /**
     * Ping timeout getter.
     * @return long pingTimeout
     */
    public long getPingTimeout() {
        return pingTimeout;
    }

    /**
     * Ping timeout setter.
     * @param pingTimeout long
     */
    public void setPingTimeout(long pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable#registerStateCallback(org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.StateCallback)
     */
    @Override
    public void registerStateCallback(StateCallback callback) {
        stateCallback = callback;
    }

}
