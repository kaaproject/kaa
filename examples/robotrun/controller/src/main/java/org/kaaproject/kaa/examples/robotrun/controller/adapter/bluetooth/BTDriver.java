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
package org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth;

import java.util.UUID;

/**
 * Bluetooth driver interface.
 * Used to hide specific Bluetooth initialization logic.
 * Usage example:
 * 
 * BTDriver bt = new AndroidBT();
 * bt.init(); 
 * 
 * bt.registerResponseCallback(callback);
 * 
 * //start connection process, BTDriver create ServerSocket and wait connection from slave
 * //When client connected, callback.onConnected() will be executed.
 * bt.connect(name, uuid); 
 * 
 * bt.sendCommand("k"); //send keepalive command, just send 'k' string and don't wait response.
 * 
 * //All responses comes thought callback.onMessage() 
 *  
 * other disconnect(), shutdown() calls in synchronous.
 * 
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public interface BTDriver {
    /**
     * Initialize BT Objects.
     * @throws Exception if something goes wrong.
     */
    public void init() throws Exception;
    
    /**
     * Start connect process, Create BT Server socket and wait incoming connections.
     * BTDriver request all paired devices, find all with names started with Bot,
     * get first and request UUIDs of this device. Use first UUID to connect()
     * @throws Exception if something goes wrong.
     */
    public void connect() throws Exception;
    
    /**
     * Register callback for receiving events from BTDriver
     * @param callback BTManageable
     */
    public void registerResponseCallback(BTManageable callback);
    
    /**
     * Send command to BT socket.
     * Current protocol with Robotrun support following commands:
     * 'p' - ping command, initiate ping, and when complete, return response 'p34' - where 34 distance (any number)
     * 'f' - move forward to next cell, return 'f' when complete.
     * 'b' - move backward to back cell, return 'b' when complte.
     * 'r' - turn right on 90 degree, return 'r' when complete.
     * 'l' - turn left on 90 degree, return 'l' when complete
     * 'k' - keepalive, return 'k' if health OK.
     * Possible return following responses:
     * 'u' - mean busy, return if previouse command don't complete.
     * 'i' - mean invalid command. 
     * @param command String
     * @throws Exception if something goes wrong.
     */
    public void sendCommand(String command) throws Exception;
    
    /**
     * Disconnect BT connection.
     * Close current BT Socket and IN/OUT streams.
     */
    public void disconnect();
    
    /**
     * Disconnect BT connection if opened.
     * Destroy all BT objects.
     */
    public void shutdown();
}
