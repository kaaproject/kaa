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

import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.ErrorCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveBackwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.MoveForwardCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.PingCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.StateCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.TurnCallback;

/**
 * Drivable Interface to control movements of Kaa Robotrun
 * Use between Robotrun controller and Robot Adapter or Robot Emulator.
 * All movement methods initiate appropriate actions, Action completeness 
 * notified using appropriate Callback. 
 * In case error happened, it notified using ErrorCallback.
 * 
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public interface Drivable {
    
    /**
     * Send Turn command to Robot
     * @param direction TurnDirection, possible LEFT or RIGHT.
     * @param withColebration - boolean, true - switch on calibration during
     */
    public void turn(TurnDirection direction, boolean withCalibration);
    
    /**
     * Send Move forward command to Robot.
     * @param withColebration - boolean, true - switch on calibration during movement to forward
     */
    public void moveForward(boolean withCalibration);
    
    /**
     * Send Move backward command to Robot.
     */
    public void moveBackward();
    
    /**
     * Initiate wall existing measurement on forward direction.
     */
    public void ping();
    
    /**
     * Register Turn completion callback
     * @param callback TurnCallback interface.
     */
    public void registerTurnCallback(TurnCallback callback);
    
    /**
     * Register Move Forward completion callback
     * @param callback MoveForwardCallback interface.
     */
    public void registerMoveForwardCallback(MoveForwardCallback callback);
    
    /**
     * Register Move Backward completion callback
     * @param callback MoveBackwardCallback interface.
     */
    public void registerMoveBackwardCallback(MoveBackwardCallback callback);
    
    /**
     * Register Ping completion callback
     * @param callback PingCallback interface.
     */
    public void registerPingCallback(PingCallback callback);
    
    /**
     * Register Error callback
     * @param callback ErrorCallback interface.
     */
    public void registerErrorCallback(ErrorCallback callback);
    
    public void start() throws Exception;
    
    public void shutdown();
    
    public void registerStateCallback(StateCallback callback);
}
