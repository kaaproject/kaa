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

import java.io.IOException;
import java.util.UUID;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class AndroidBTServerSocket extends AndroidBT {

    /** ServerSocket object, leave while waiteing incoming connection for accept, after this become null */
    private BluetoothServerSocket mmServerSocket;

    /**
     * Server socket handle Class.
     * In own thread blocks in accept call, waiting for incoming connection. 
     *
     */
    protected class BTServerSocket implements Runnable {

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            BluetoothSocket localSocket = null;
            try {
                localSocket = mmServerSocket.accept();
                setSocket(localSocket);
                mmServerSocket.close();
                mmServerSocket = null;
            } catch (IOException e) {
                if (getCallback() != null) {
                    getCallback().onError(e);
                }
            }
        }
        
    }
    
    /**
     * @param context
     */
    public AndroidBTServerSocket(Context context) {
        super(context);
        mmServerSocket = null;
    }

    protected void connect(BluetoothDevice device, UUID uuid) throws IOException {
        Log.i(TAG, "BluetoothAdapter connecting with Name: "+device.getName());
        mmServerSocket = getmBluetoothAdapter().listenUsingRfcommWithServiceRecord(device.getName(), uuid);
        BTServerSocket btSSocket = new BTServerSocket();
        executor.execute(btSSocket);
    }
    
    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.AndroidBT#shutdown()
     */
    @Override
    public void shutdown() {
        
        Log.i(TAG, "BluetoothAdapter ServerSocket shutdown() ....");
        super.shutdown();
        try {
            if (mmServerSocket != null) {
                mmServerSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "BT: error shutdown()",e);
        } finally {
            mmServerSocket = null;
        }
    }
}
