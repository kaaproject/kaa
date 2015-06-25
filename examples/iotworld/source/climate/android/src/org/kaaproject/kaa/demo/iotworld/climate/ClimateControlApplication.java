/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.iotworld.climate;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.KaaStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import de.greenrobot.event.EventBus;

public class ClimateControlApplication extends Application {

    private static final Logger LOG = LoggerFactory
            .getLogger(ClimateControlApplication.class);
    
    private EventBus mEventBus;
    private KaaClient mClient;
    private ClimateController mController;
    
    private boolean mKaaStarted = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mEventBus = new EventBus();
        
        /*
         * Initialize the Kaa client using the Android context.
         */
        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(
                this);
        
        mClient = Kaa.newClient(kaaClientContext,
                new SimpleKaaClientStateListener() {

                    /*
                     * Implement the onStarted callback to get notified when the Kaa
                     * client is operational.
                     */
                    @Override
                    public void onStarted() {
                        mKaaStarted = true;
                        mEventBus.post(new KaaStartedEvent());
                        LOG.info("Kaa client started");
                    }

                    /*
                     * Implement the onResume callback to notify remote devices  
                     * of a local device availability.
                     */
                    @Override
                    public void onResume() {
                        LOG.info("Kaa client resumed");
                    }
                });
        
        mController = new ClimateController(this, mClient, mEventBus);
        
        /*
         * Start the Kaa client workflow.
         */
        mClient.start();
    }
    
    public void pause() {

        /*
         * Suspend the Kaa client. Release all network connections and application
         * resources. Suspend all the Kaa client tasks.
         */
        mClient.pause();
    }

    public void resume() {

        /*
         * Resume the Kaa client. Restore the Kaa client workflow. 
         * Resume all the Kaa client tasks.
         */
        mClient.resume();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        /*
         * Stop the Kaa client. Release all network connections and application
         * resources. Shut down all the Kaa client tasks.
         */
        mClient.stop();
        mKaaStarted = false;
    }
    
    public boolean isKaaStarted() {
        return mKaaStarted;
    }

    public EventBus getEventBus() {
        return mEventBus;
    }
    
    public ClimateController getController() {
        return mController;
    }
    
}
