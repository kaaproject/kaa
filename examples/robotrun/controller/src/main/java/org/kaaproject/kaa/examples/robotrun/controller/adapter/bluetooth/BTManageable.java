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

/**
 * Bluetooth managing callback interface.
 * Used to transmit events from BT in callback.
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public interface BTManageable {
    
    /**
     * Invokes when BT socket connect complete.
     */
    public void onConected(String deviceName);
    
    /**
     * Invokes when BT socket disconnects.
     */
    public void onDisconnected();
    
    /**
     * Invoke on error. 
     * @param error Exception 
     */
    public void onError(Exception error);
    
    /**
     * Invokes on some string read from BT socket.
     * @param msg String
     */
    public void onMessage(String msg);
}
