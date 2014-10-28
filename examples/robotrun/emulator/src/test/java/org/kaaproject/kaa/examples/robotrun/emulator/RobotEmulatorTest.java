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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.examples.robotrun.controller.robot.TurnDirection;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.ErrorCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveForwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.OperationStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PingCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PongStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.StateCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.TurnCallback;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.LabyrinthGenerator;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinthGenerator;

/**
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class RobotEmulatorTest {

    private final static int LABYRINTH_WIDTH = 7;
    private final static int LABYRINTH_HEIGHT = 7;

    private Labyrinth labyrinth;

    private Cell initialPosition;

    private int initialX;
    private int initialY;
    private int exitX;
    private int exitY;

    private Direction initialDirection;

    private boolean OK = false;

    private static final Random rnd = new Random();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        generatePosition();
        if ((initialX >= LABYRINTH_WIDTH)
                || (initialY >= LABYRINTH_HEIGHT)) {
            fail("Error generation initial position");
        }
        LabyrinthGenerator generator = new BasicLabyrinthGenerator(LABYRINTH_WIDTH, LABYRINTH_HEIGHT);
        labyrinth = generator.generate(initialX, initialY, exitX, exitY);
        assertNotNull(labyrinth);
        initialPosition = labyrinth.getCell(initialX, initialY);

    }

    /**
     *
     */
    private void generatePosition() {
        initialX = rnd.nextInt(LABYRINTH_WIDTH -1)+1;
        initialY = rnd.nextInt(LABYRINTH_HEIGHT -1)+1;
        int initialD = rnd.nextInt(4);
        if (initialD == 0) {
            setInitialDirection(Direction.NORTH);
        } else if (initialD == 1) {
            setInitialDirection(Direction.SOUTH);
        } else if (initialD == 2) {
            setInitialDirection(Direction.WEST);
        } else if (initialD == 3) {
            setInitialDirection(Direction.EAST);
        } else {
            setInitialDirection(Direction.NORTH);
        }
        int side = rnd.nextInt(4);
        if (side == 0) {
            //NORTH side
            exitY = 0;
            exitX = rnd.nextInt(LABYRINTH_WIDTH);
        } else if (side == 1) {
            //SOUTH side
            exitY = LABYRINTH_HEIGHT - 1;
            exitX = rnd.nextInt(LABYRINTH_WIDTH);
        } else if (side == 2) {
            //WEST side
            exitX = 0;
            exitY = rnd.nextInt(LABYRINTH_HEIGHT);
        } else {
            //EAST side
            exitX = LABYRINTH_HEIGHT - 1;
            exitY = rnd.nextInt(LABYRINTH_HEIGHT);
        }
    }

    /**
     * @return the initialDirection
     */
    public Direction getInitialDirection() {
        return initialDirection;
    }

    /**
     * @param initialDirection the initialDirection to set
     */
    public void setInitialDirection(Direction initialDirection) {
        this.initialDirection = initialDirection;
    }

    /**
     * @return the initialPosition
     */
    public Cell getInitialPosition() {
        return initialPosition;
    }

    /**
     * @param initialPosition the initialPosition to set
     */
    public void setInitialPosition(Cell initialPosition) {
        this.initialPosition = initialPosition;
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator#RobotEmulator(org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth, org.kaaproject.kaa.examples.robotrun.labyrinth.Cell, org.kaaproject.kaa.examples.robotrun.labyrinth.Direction, java.util.Properties)}.
     */
    @Test
    public void testRobotEmulatorDefault() {
        RobotEmulator emu = new RobotEmulator(labyrinth, getInitialPosition(), getInitialDirection(), null);
        assertNotNull(emu);
        final Object obj = new Object();
        OK = false;
        emu.registerStateCallback(new StateCallback() {

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onConnected(String deviceName) {
                synchronized (obj) {
                    OK = true;
                    obj.notify();
                }
            }
        });

        try {
            emu.start();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        synchronized (obj) {
            try {
                obj.wait(2000);
                assertEquals(true, OK);
            } catch (InterruptedException e) {
                fail("Creator wait failed");
            }
        }
        assertEquals(labyrinth, emu.getLabyrinth());
        assertEquals(getInitialPosition(), emu.getCurrentPosition());
        assertEquals(getInitialDirection(), emu.getCurrentDirection());
        assertEquals(RobotEmulator.DEFAULT_COMMAND_TIMEOUT, emu.getCommandTimeout());
        assertEquals(RobotEmulator.DEFAULT_COMMAND_TIMEOUT_DEVIATION, emu.getTimeoutDeviation());
        assertEquals(RobotEmulator.DEFAULT_PING_TIMEOUT, emu.getPingTimeout());
        emu.shutdown();
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator#RobotEmulator(org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth, org.kaaproject.kaa.examples.robotrun.labyrinth.Cell, org.kaaproject.kaa.examples.robotrun.labyrinth.Direction, java.util.Properties)}.
     */
    @Test
    public void testRobotEmulatorProps() {
        Properties props = new Properties();
        String commTimeout = "1001";
        props.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT, commTimeout );
        String commTimeoutDev = "101";
        props.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION, commTimeoutDev );
        String pingTimeout = "203";
        props.put(RobotEmulator.PROPERTY_NAME_PING_TIMEOUT, pingTimeout );

        RobotEmulator emu = new RobotEmulator(labyrinth, getInitialPosition(), getInitialDirection(), props);
        assertNotNull(emu);
        final Object obj = new Object();
        OK = false;
        emu.registerStateCallback(new StateCallback() {

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onConnected(String deviceName) {
                synchronized (obj) {
                    OK = true;
                    obj.notify();
                }
            }
        });

        try {
            emu.start();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        synchronized (obj) {
            try {
                obj.wait(2000);
                assertEquals(true, OK);
            } catch (InterruptedException e) {
                fail("Creator wait failed");
            }
        }
        assertEquals(labyrinth, emu.getLabyrinth());
        assertEquals(getInitialPosition(), emu.getCurrentPosition());
        assertEquals(getInitialDirection(), emu.getCurrentDirection());
        assertEquals(Long.parseLong(commTimeout), emu.getCommandTimeout());
        assertEquals(Integer.parseInt(commTimeoutDev), emu.getTimeoutDeviation());
        assertEquals(Integer.parseInt(pingTimeout), emu.getPingTimeout());
        emu.shutdown();
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator#turn(org.kaaproject.kaa.examples.robotrun.controller.robot.TurnDirection)}.
     */
    @Test
    public void testTurn() {
        setInitialDirection(Direction.NORTH);
        RobotEmulator emu = new RobotEmulator(labyrinth, getInitialPosition(), getInitialDirection(), null);
        assertNotNull(emu);

        final Object obj = new Object();
        OK = false;
        emu.registerStateCallback(new StateCallback() {

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onConnected(String deviceName) {
                synchronized (obj) {
                    OK = true;
                    obj.notify();
                }
            }
        });

        try {
            emu.start();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        synchronized (obj) {
            try {
                obj.wait(2000);
                assertEquals(true, OK);
            } catch (InterruptedException e) {
                fail("Creator wait failed");
            }
        }

        emu.registerErrorCallback(new ErrorCallback() {
            @Override
            public void error(Exception exception) {
                exception.printStackTrace();
                fail(exception.toString());
            }
        });



        final Object objSync = new Object();
        emu.registerTurnCallback(new TurnCallback() {

            @Override
            public void complete(OperationStatus status) {
                if (status == OperationStatus.FAILED) {
                    fail("Turn command failed");
                }
                synchronized (objSync) {
                    objSync.notify();
                }
            }
        });

        emu.turn(TurnDirection.RIGHT, false); //NORTH after right turn should face to EAST
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.EAST, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        emu.turn(TurnDirection.RIGHT, false); //EAST after right turn should face to SOUTH
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.SOUTH, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        emu.turn(TurnDirection.RIGHT, false); //SOUTH after right turn should face to WEST
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.WEST, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        emu.turn(TurnDirection.RIGHT, false); //WEST after right turn should face to NORTH
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.NORTH, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        //Test LEFT turn
        emu.turn(TurnDirection.LEFT, false); //NORTH after left turn should face to WEST
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.WEST, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        emu.turn(TurnDirection.LEFT, false); //WEST after left turn should face to SOUTH
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.SOUTH, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        emu.turn(TurnDirection.LEFT, false); //SOUTH after left turn should face to EAST
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.EAST, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        emu.turn(TurnDirection.LEFT, false); //EAST after left turn should face to NORTH
        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(Direction.NORTH, emu.getCurrentDirection());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator#moveForward()}.
     */
    @Test
    public void testMove() {
        RobotEmulator emu = new RobotEmulator(labyrinth, getInitialPosition(), getInitialDirection(), null);
        assertNotNull(emu);
        final Object obj = new Object();
        OK = false;
        emu.registerStateCallback(new StateCallback() {

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onConnected(String deviceName) {
                synchronized (obj) {
                    OK = true;
                    obj.notify();
                }
            }
        });

        try {
            emu.start();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        synchronized (obj) {
            try {
                obj.wait(2000);
                assertEquals(true, OK);
            } catch (InterruptedException e) {
                fail("Creator wait failed");
            }
        }
        emu.registerErrorCallback(new ErrorCallback() {
            @Override
            public void error(Exception exception) {
                exception.printStackTrace();
                fail(exception.toString());
            }
        });

        final Object objSync = new Object();

        emu.registerMoveForwardCallback(new MoveForwardCallback() {

            @Override
            public void complete(OperationStatus status) {
                if (status == OperationStatus.FAILED) {
                    fail("MoveForward command failed");
                }
                synchronized (objSync) {
                    objSync.notify();
                }
            }
        });

        Cell posBefore = emu.getCurrentPosition();
        Cell posAfter = getReadyForMoveForward(emu);

        emu.moveForward(false);

        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(posAfter.getX(), emu.getCurrentPosition().getX());
                assertEquals(posAfter.getY(), emu.getCurrentPosition().getY());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        emu.registerMoveForwardCallback(new MoveForwardCallback() {

            @Override
            public void complete(OperationStatus status) {
                if (status == OperationStatus.FAILED) {
                    fail("MoveForward command failed");
                }
                synchronized (objSync) {
                    objSync.notify();
                }
            }
        });

        emu.moveBackward();

        synchronized (objSync) {
            try {
                objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                assertEquals(posBefore.getX(), emu.getCurrentPosition().getX());
                assertEquals(posBefore.getY(), emu.getCurrentPosition().getY());
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

    }

    /**
     *
     */
    private Cell getReadyForMoveForward(RobotEmulator emu) {
        int i = 0;
        while(emu.getCurrentPosition().getBorder(emu.getCurrentDirection()) != BorderType.FREE) {
            if (i >= 4) {
                fail("Error find SOLID border in position X="+emu.getCurrentPosition().getX()+" Y="+emu.getCurrentPosition().getY());
            }
            i++;

            final Object objSync = new Object();

            emu.registerTurnCallback(new TurnCallback() {

                @Override
                public void complete(OperationStatus status) {
                    if (status == OperationStatus.FAILED) {
                        fail("Turn command failed");
                    }
                    synchronized (objSync) {
                        objSync.notify();
                    }

                }
            });

            emu.turn(TurnDirection.RIGHT, false);
            synchronized (objSync) {
                try {
                    objSync.wait(emu.getCommandTimeout() + emu.getTimeoutDeviation());
                } catch (InterruptedException e) {
                    fail(e.toString());
                }
            }
        }

        switch (emu.getCurrentDirection()) {
        case NORTH:
            return emu.getLabyrinth().getCell(emu.getCurrentPosition().getX(), emu.getCurrentPosition().getY() - 1);
        case SOUTH:
            return emu.getLabyrinth().getCell(emu.getCurrentPosition().getX(), emu.getCurrentPosition().getY() + 1);
        case WEST:
            return emu.getLabyrinth().getCell(emu.getCurrentPosition().getX() -1, emu.getCurrentPosition().getY());
        case EAST:
            return emu.getLabyrinth().getCell(emu.getCurrentPosition().getX() +1, emu.getCurrentPosition().getY());
        }
        return null;
    }



    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator#ping()}.
     */
    @Test
    public void testPing() {
        final RobotEmulator emu = new RobotEmulator(labyrinth, getInitialPosition(), getInitialDirection(), null);
        assertNotNull(emu);
        emu.registerErrorCallback(new ErrorCallback() {
            @Override
            public void error(Exception exception) {
                exception.printStackTrace();
                fail(exception.toString());
            }
        });

        final Object objSync = new Object();

        emu.registerPingCallback(new PingCallback() {

            @Override
            public void pong(PongStatus status) {

                if (status == PongStatus.EMPTY) {
                    assertEquals(BorderType.FREE, emu.getCurrentPosition().getBorder(emu.getCurrentDirection()));
                } else if (status == PongStatus.WALL) {
                    assertEquals(BorderType.SOLID, emu.getCurrentPosition().getBorder(emu.getCurrentDirection()));
                } else {
                    fail("Ping test failed");
                }

                synchronized (objSync) {
                    objSync.notify();
                }

            }
        });
        emu.ping();

    }

}
