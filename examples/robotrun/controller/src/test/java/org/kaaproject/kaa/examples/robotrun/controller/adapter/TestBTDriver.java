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

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver;
import org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTManageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class TestBTDriver implements BTDriver {

    private static final Logger LOG = LoggerFactory.getLogger(TestBTDriver.class);
    
    private boolean constrcutorInvoked = false;
    
    private boolean setCallbackInvoked = false;
    
    private boolean initInvoked = false;
    
    private boolean connectInvoked = false;
    
    private boolean shutdownInvoked = false;
    
    private BTManageable callback;
    
    private ExecutorService executor;
    /**
     * @return the constrcutorInvoked
     */
    public boolean isConstrcutorInvoked() {
        return constrcutorInvoked;
    }

    /**
     * 
     */
    public TestBTDriver() {
        constrcutorInvoked = true;
        executor = Executors.newCachedThreadPool();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#init()
     */
    @Override
    public void init() throws Exception {
        initInvoked = true;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#connect(java.lang.String, java.util.UUID)
     */
    @Override
    public void connect() throws Exception {
        connectInvoked = true;
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    callback.onConected("BOT");
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#registerResponseCallback(org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTManageable)
     */
    @Override
    public void registerResponseCallback(BTManageable callback) {
        setCallbackInvoked = true;
        this.callback = callback;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#sendCommand(java.lang.String)
     */
    @Override
    public void sendCommand(final String command) throws Exception {
        LOG.info("sendCommand: "+command);
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    if (command.equals("l")) {
                        callback.onMessage("l");
                    } else if (command.equals("r")) {
                            callback.onMessage("r");
                    } else if (command.equals("f")) {
                        callback.onMessage("f");
                    } else if (command.equals("b")) {
                        callback.onMessage("b");
                    } else if (command.equals("p")) {
                        callback.onMessage("p");
                        callback.onMessage("340");
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        });

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#disconnect()
     */
    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.BTDriver#shutdown()
     */
    @Override
    public void shutdown() {
        shutdownInvoked = true;
        executor.shutdown();
    }

    /**
     * @return the setCallbackInvoked
     */
    public boolean isSetCallbackInvoked() {
        return setCallbackInvoked;
    }

    /**
     * @return the initInvoked
     */
    public boolean isInitInvoked() {
        return initInvoked;
    }

    /**
     * @return the connectInvoked
     */
    public boolean isConnectInvoked() {
        return connectInvoked;
    }

}
