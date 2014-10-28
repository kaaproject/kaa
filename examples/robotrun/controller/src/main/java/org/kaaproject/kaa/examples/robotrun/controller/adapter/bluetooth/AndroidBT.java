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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

/**
 * Android Bluetooth initialization and handling class.
 * Initialize Android Bluetooth.
 * Create BT Server socket and accept one incoming connection.
 * Handle read/write operations.
 * 
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public abstract class AndroidBT extends BroadcastReceiver implements BTDriver {
    
    /** BT read buffer size */
    private static final int BUFFER_SIZE = 256;
    
    /** Loging TAG */
    protected static final String TAG = "RobotrunAndroidBT";

    /** Constant from which all robots names should start */
    private static final String ROBOTRUN_BLUETOOTH_PREFIX = "BOT";
    
    /** Adapter object */
    private BluetoothAdapter mBluetoothAdapter;
    
    /** BT Socket initialized after server socket accept incoming connection */
    private BluetoothSocket socket;
    
    /** callback interface to operate with upcoming classes */
    private BTManageable callback;
    
    /** boolean set to true after BT initialized */
    private boolean btInited;
    
    /** indicate BT connected or not */
    private boolean btConnected;
    
    /** BT reader object */
    private BTReader reader;
    
    /** BT writer object */
    private BTWriter writer;
    
    /** Thread executor, use FixedThread executor with 2 threads */
    protected final ExecutorService executor;
    
    private final Context context;
    
    private String pendingDiscoveryDeviceName;
    
    
    /**
     * BT writing class. 
     */
    protected class BTWriter {

        private final OutputStream out;
        
        protected BTWriter(OutputStream out) {
            this.out = out;
        }
        
        protected void send(String command) {
            try {
                out.write(command.getBytes());
                Log.i(TAG,"BT: SEND: "+command);
            } catch (IOException e) {
                Log.e(TAG, "BT: error BTReader",e);
                onDisconnect(e);
            }
        }
        
        protected void shutdown() {
            try {
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "BT: error shutdown() BTWriter",e);
            }
        }
    }
    
    /**
     * BT Reader Class.
     * In own thread blocks on reading from input stream.
     */
    protected class BTReader implements Runnable {

        private final InputStream in;
        
        private boolean operate = true;
        
        private byte[] buffer;
        
        protected BTReader(InputStream in) {
            this.in = in;
            buffer = new byte[BUFFER_SIZE];
        }
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            while(operate) {
                try {
                    int c = in.read(buffer);
                    if (c > 0) {
                        String msg = new String(buffer, 0, c);
                        Log.v(TAG,"BT: RX: "+msg);
                        getCallback().onMessage(msg);
                    } else {
                        Log.e(TAG, "BT: connection closed.");
                        onDisconnect(new BTException("EOF read, connection closed"));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "BT: error BTReader",e);
                    onDisconnect(e);
                }
            }
        }
        
        
        protected void shutdown() {
            operate = false;
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "BT: error shutdown() BTReader",e);
                }
            }
        }
        
    }
    
    /**
     * Default constrcutor.
     */
    public AndroidBT(Context context) {
        btInited = false;
        btConnected = false;
        mBluetoothAdapter = null;
        socket = null;
        executor = Executors.newFixedThreadPool(2);
        this.context = context;
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#init()
     */
    @Override
    public void init() throws Exception {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "No Bluetooth adapter.");
            throw new BTException("No Bluetooth adapter.");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth adapter is Disabled.");
            throw new BTException("Bluetooth adapter is Disabled");
        }
        Log.i(TAG, "BluetoothAdapter initialized sucessfully");
        btInited = true;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#connect()
     */
    @Override
    public void connect() throws Exception {
        if (!btInited) {
            Log.e(TAG, "AndroidBT not initialized.");
            throw new BTException("AndroidBT not initialized");
        }
        String name = "";
        UUID uuid = null;
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.v(TAG,"Start looking for paired robot, see "+devices.size()+" devices");
        for(BluetoothDevice device : devices) {
            Log.i(TAG,"Device "+device.getName()+";");
            if (device.getName().startsWith(ROBOTRUN_BLUETOOTH_PREFIX)) {
                name = device.getName();
                ParcelUuid[] uuids = BluetoothNativeHelper.getUuids(device);
                if (uuids == null || uuids.length == 0) {
                    pendingDiscoveryDeviceName = name;
                    IntentFilter filter = new IntentFilter(BluetoothNativeHelper.ACTION_UUID);
                    context.registerReceiver(this, filter);                    
                    if (!BluetoothNativeHelper.fetchUuidsWithSdp(device)) {
                        pendingDiscoveryDeviceName = null;
                        context.unregisterReceiver(this);
                        throw new BTException("Error initializing BT, can't fetch device uuids.");
                    }
                }
                else {
                    uuid = uuids[0].getUuid();
                    Log.i(TAG,"Device "+device.getName()+" found, UUID "+uuid.toString());
                    connect(device, uuid);
                }
                break;
            }
        }
        if (name == null || name.trim().equals("")) {
            throw new BTException("Error initializing BT, necessary device not found.");
        }
    }
    
    protected abstract void connect(BluetoothDevice device, UUID uuid) throws IOException;

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#registerResponseCallback(org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTManageable)
     */
    @Override
    public void registerResponseCallback(BTManageable callback) {
        this.setCallback(callback);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#sendCommand(java.lang.String)
     */
    @Override
    public void sendCommand(String command) throws Exception {
        if (!btConnected) {
            throw new BTException("BT not connected");
        }
        if (writer == null) {
            throw new BTException("BT connection incomplete");
        }
        writer.send(command);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#disconnect()
     */
    @Override
    public void disconnect() {
        Log.i(TAG, "BT: Disconnect initiated...");
        
        closeBTConnection();
        
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#shutdown()
     */
    @Override
    public void shutdown() {
        
        Log.i(TAG, "BluetoothAdapter shutdown() ....");
        
        closeBTConnection();
        
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e1) {
                Log.e(TAG, "BT: error shutdown()",e1);
            }
        }
    }

    /**
     * Callback getter.
     * @return BTManageable the callback
     */
    public BTManageable getCallback() {
        return callback;
    }

    /**
     * Callback setter.
     * @param callback BTManageable
     */
    public void setCallback(BTManageable callback) {
        this.callback = callback;
    }

    /**
     * Socket getter,
     * @return BluetoothSocket the socket
     */
    public BluetoothSocket getSocket() {
        return socket;
    }

    /**
     * Called in case of BT disconnection detected
     * @param e Exception of disconnect error, possible null.
     */
    private void onDisconnect(Exception e) {
        Log.i(TAG, "BT: disconnecting...");
        
        closeBTConnection();
        
        if (getCallback() != null) {
            getCallback().onDisconnected();
        }
    }

    /**
     * Close current BT connection.
     */
    private void closeBTConnection() {
        
        btConnected = false;
        
        if (reader != null) {
            reader.shutdown();
            reader = null;
        }
        
        if (writer != null) {
            writer.shutdown();
            writer = null;
        }
        
        if (getSocket() != null) {
            try {
                getSocket().close();
            } catch (IOException e1) {
                Log.e(TAG, "BT: Error closing socket",e1);
            } finally {
                socket = null;
            }
        }
        Log.v(TAG, "BT: disconnected.");
    }
    
    /**
     * Socket setter.
     * On new socket create reader/writer and switch Adapter to connected state.
     * @param socket BluetoothSocket socket to set
     * @throws IOException 
     */
    protected void setSocket(BluetoothSocket socket) throws IOException {
        this.socket = socket;
        Log.i(TAG, "BT: connecting ....");
        Log.i(TAG, "BT: connecting with "+socket.getRemoteDevice().getName());
        writer = new BTWriter(socket.getOutputStream());
        
        reader = new BTReader(socket.getInputStream());
        executor.execute(reader);
        
        btConnected = true;
        
        Log.i(TAG, "BT: connected");
        
        if (getCallback() != null) {
            getCallback().onConected(socket.getRemoteDevice().getName());
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(BluetoothNativeHelper.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (pendingDiscoveryDeviceName != null && pendingDiscoveryDeviceName.equals(device.getName())) {
                context.unregisterReceiver(this);
                pendingDiscoveryDeviceName = null;
                ParcelUuid[] uuids = BluetoothNativeHelper.getUuids(device);
                if (uuids != null && uuids.length > 0) {
                    UUID uuid = uuids[0].getUuid();
                    try {
                        connect(device, uuid);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to connect to device " + device.getName(), e);
                        getCallback().onError(e);
                    }
                }
                else {
                    Log.e(TAG, "Failed to connect to device " + device.getName() + " uuids are empty");
                    getCallback().onError(new BTException("Failed to connect to device " + device.getName() + " "
                            + "uuids are empty"));
                }
            }
        }
    }

    /**
     * @return the mBluetoothAdapter
     */
    protected BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * @return the reader
     */
    protected BTReader getReader() {
        return reader;
    }

    /**
     * @return the writer
     */
    protected BTWriter getWriter() {
        return writer;
    }

    /**
     * @param reader the reader to set
     */
    protected void setReader(BTReader reader) {
        this.reader = reader;
    }

    /**
     * @param writer the writer to set
     */
    protected void setWriter(BTWriter writer) {
        this.writer = writer;
    }
    
    public static class BluetoothNativeHelper {
        
        static final String ACTION_UUID = "android.bluetooth.device.action.UUID";
        
        static Method getUuids;
        
        static Method fetchUuidsWithSdp;
        
        static Method createRfcommSocket;
        
        static {
            try {
                getUuids = BluetoothDevice.class.getMethod("getUuids");
                fetchUuidsWithSdp = BluetoothDevice.class.getMethod("fetchUuidsWithSdp");
                createRfcommSocket = BluetoothDevice.class.getMethod("createRfcommSocket", new Class[] {int.class});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public static ParcelUuid[] getUuids(BluetoothDevice device) {
            try {
                return (ParcelUuid[]) getUuids.invoke(device);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } 
        }
        
        public static boolean fetchUuidsWithSdp(BluetoothDevice device) {
            try {
                return (boolean) fetchUuidsWithSdp.invoke(device);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } 
        }
        
        public static BluetoothSocket createRfcommSocket(BluetoothDevice device, int channel) throws Exception {
            return (BluetoothSocket) createRfcommSocket.invoke(device, channel);
        }
        
    }

}
