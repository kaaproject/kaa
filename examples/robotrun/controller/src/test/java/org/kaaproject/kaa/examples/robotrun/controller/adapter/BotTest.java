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
package org.kaaproject.kaa.examples.robotrun.controller.adapter;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.examples.robotrun.controller.robot.TurnDirection;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.ErrorCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveBackwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveForwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.OperationStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PingCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PongStatus;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.TurnCallback;

/**
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class BotTest {
    
    private boolean OK = false;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.controller.adapter.Bot#Bot(java.lang.String)}.
     */
    @Test
    public void testBot() {
        try {
            Bot bot = new Bot(new TestBTDriver());
            assertNotNull(bot);
            assertNotNull(bot.getDriver());
            assertNotNull(bot.getExecutor());
            TestBTDriver testBot = (TestBTDriver)bot.getDriver();
            if (!testBot.isConstrcutorInvoked()) {
                fail("BTDriver constrcutor invoke failed");
            }
            if (!testBot.isInitInvoked()) {
                fail("BTDriver init() invoke failed");
            }
            if (!testBot.isSetCallbackInvoked()) {
                fail("BTDriver set callback invoke failed");
            }
            
            bot.start();
            
            if (!testBot.isConnectInvoked()) {
                fail("BTDriver connect invoke failed");
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.controller.adapter.Bot#shutdown()}.
     */
    @Test
    public void testShutdown() {
        try {
            Bot bot = new Bot(new TestBTDriver());
            assertNotNull(bot);
            assertNotNull(bot.getExecutor());
            
            bot.start();
            
            bot.shutdown();
            if (!bot.getExecutor().isShutdown()) {
                fail("Bot shutdown failed");
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.controller.adapter.Bot#turn(org.kaaproject.kaa.examples.robotrun.controller.robot.TurnDirection)}.
     */
    @Test
    public void testTurn() {
        try {
            final Bot bot = new Bot(new TestBTDriver());
            assertNotNull(bot);
            assertNotNull(bot.getExecutor());
            
            final Object objSync = new Object();
            
            OK = false;
            
            bot.registerErrorCallback(new ErrorCallback() {
                
                @Override
                public void error(Exception exception) {
                    fail(exception.toString());
                }
            });
            
            bot.registerTurnCallback(new TurnCallback() {
                
                @Override
                public void complete(OperationStatus status) {
                    if (status != OperationStatus.SUCESSFULL) {
                        fail("Turn command failed");
                    }
                    OK = true;
                    synchronized (objSync) {
                        objSync.notify();
                    }
                }
            });
            
            bot.start();
            
            bot.turn(TurnDirection.LEFT, false);
            bot.turn(TurnDirection.RIGHT, false);
            
            synchronized (objSync) {
                objSync.wait(2000);
            }
            if(!OK) {
                fail("Turn left failed");
            }
            OK = false;
            synchronized (objSync) {
                objSync.wait(2000);
            }
            if(!OK) {
                fail("Turn right failed");
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.controller.adapter.Bot#moveForward()}.
     */
    @Test
    public void testMoveForward() {
        try {
            final Bot bot = new Bot(new TestBTDriver());
            assertNotNull(bot);
            assertNotNull(bot.getExecutor());
            
            final Object objSync = new Object();
            
            OK = false;
            
            bot.registerErrorCallback(new ErrorCallback() {
                
                @Override
                public void error(Exception exception) {
                    fail(exception.toString());
                }
            });
            
            bot.registerMoveForwardCallback(new MoveForwardCallback() {
                
                @Override
                public void complete(OperationStatus status) {
                    if (status != OperationStatus.SUCESSFULL) {
                        fail("Move forward command failed");
                    }
                    OK = true;
                    synchronized (objSync) {
                        objSync.notify();
                    }
                    
                }
            });
            
            bot.start();
            
            bot.moveForward(false);
            
            synchronized (objSync) {
                objSync.wait(2000);
            }
            if(!OK) {
                fail("Move forward failed");
            }
            
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.controller.adapter.Bot#moveBackward()}.
     */
    @Test
    public void testMoveBackward() {
        try {
            final Bot bot = new Bot(new TestBTDriver());
            assertNotNull(bot);
            assertNotNull(bot.getExecutor());
            
            final Object objSync = new Object();
            
            OK = false;
            
            bot.registerErrorCallback(new ErrorCallback() {
                
                @Override
                public void error(Exception exception) {
                    fail(exception.toString());
                }
            });
            
            bot.registerMoveBackwardCallback(new MoveBackwardCallback() {
                
                @Override
                public void complete(OperationStatus status) {
                    if (status != OperationStatus.SUCESSFULL) {
                        fail("Move backward command failed");
                    }
                    OK = true;
                    synchronized (objSync) {
                        objSync.notify();
                    }
                    
                }
            });
            
            bot.start();
            
            bot.moveBackward();
            
            synchronized (objSync) {
                objSync.wait(2000);
            }
            if(!OK) {
                fail("Move backward failed");
            }
            
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.examples.robotrun.controller.adapter.Bot#ping()}.
     */
    @Test
    public void testPing() {
        try {
            final Bot bot = new Bot(new TestBTDriver());
            assertNotNull(bot);
            assertNotNull(bot.getExecutor());
            
            final Object objSync = new Object();
            
            OK = false;
            
            bot.registerErrorCallback(new ErrorCallback() {
                
                @Override
                public void error(Exception exception) {
                    fail(exception.toString());
                }
            });
            
            bot.registerPingCallback(new PingCallback() {
                
                @Override
                public void pong(PongStatus status) {
                    if (status == PongStatus.WALL) {
                        fail("Pong status failed");
                    }
                    OK = true;
                }
            });
            
            bot.start();
            
            bot.ping();
            synchronized (objSync) {
                objSync.wait(2000);
            }
            if(!OK) {
                fail("Ping failed");
            }
            
        } catch (Exception e) {
            fail(e.toString());
        }
    }

}
