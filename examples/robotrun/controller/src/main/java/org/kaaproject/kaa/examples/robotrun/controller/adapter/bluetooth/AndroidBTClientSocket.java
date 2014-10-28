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
package org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * @author Andrey Panasenko
 *
 */
public class AndroidBTClientSocket extends AndroidBT {

    /**
     * Client socket handle Class.
     * In own thread blocks in connect call
     *
     */
    protected class BTClientSocket implements Runnable {

        private BluetoothDevice device;
        private UUID uuid;
        
        protected BTClientSocket(BluetoothDevice device, UUID uuid) {
            this.device = device;
            this.uuid = uuid;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            BluetoothSocket socket;
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid);
                Log.i(TAG,"BT: canceling discovery...");
                boolean result = getmBluetoothAdapter().cancelDiscovery();
                Log.i(TAG,"BT: cancel discovery result: " + result);
                socket.connect();
                Log.i(TAG,"BT: connected using service record.");
                setSocket(socket);
            } catch (IOException e) {
                Log.e(TAG,"BT: connect failed: " + e.getMessage());
                try {
                    socket = BluetoothNativeHelper.createRfcommSocket(device, 1);
                    socket.connect();
                    Log.i(TAG,"BT: connected using channel 1.");
                    setSocket(socket);
                } catch (Exception e1) {
                    if (getCallback() != null) {
                        getCallback().onError(e1);
                    }
                }
            }
        }
        
    }
    
    /**
     * @param context
     */
    public AndroidBTClientSocket(Context context) {
        super(context);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.AndroidBT#connect(java.lang.String, java.util.UUID)
     */
    @Override
    protected void connect(BluetoothDevice device, UUID uuid) throws IOException {
        BTClientSocket client = new BTClientSocket(device, uuid);
        executor.execute(client);
    }

}
