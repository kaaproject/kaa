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

package org.kaaproject.kaa.demo.cityguide;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.demo.cityguide.event.ConfigurationUpdated;
import org.kaaproject.kaa.demo.cityguide.event.KaaStarted;
import org.kaaproject.kaa.demo.cityguide.image.ImageLoader;
import org.kaaproject.kaa.demo.cityguide.profile.CityGuideProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import de.greenrobot.event.EventBus;

/**
 * The implementation of the base {@link Application} class. Performs initialization of 
 * the application resources including initialization of the Kaa client. Handles the Kaa client lifecycle.
 * Stores a reference to the actual endpoint configuration. Receives configuration updates from the Kaa cluster.
 * Manages the endpoint profile object, notifies the Kaa cluster of the profile updates.
 */
public class CityGuideApplication extends Application {

    private static final Logger LOG = LoggerFactory
            .getLogger(CityGuideApplication.class);

    private EventBus mEventBus;
    private ImageLoader mImageLoader;
    private KaaClient mClient;
    private CityGuideConfig mConfig;
    private CityGuideProfile mProfile;

    private boolean mKaaStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = new EventBus();
        mImageLoader = new ImageLoader(this);

        /*
         * Create an empty city guide profile object based on the user-defined profile
         * schema.
         */
        mProfile = new CityGuideProfile();

        /*
         * Initialize the Kaa client using the Android context.
         */
        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(
                this);
        mClient = Kaa.newClient(kaaClientContext,
                new SimpleKaaClientStateListener() {

                    /*
                     * Implement the onStarted callback to get notified as soon as the Kaa
                     * client is operational. Obtain the city guide configuration
                     * from Kaa. Notify UI components when Kaa is started to start
                     * using the configuration.
                     */
                    @Override
                    public void onStarted() {
                        mConfig = mClient.getConfiguration();
                        mKaaStarted = true;
                        mEventBus.post(new KaaStarted());
                        LOG.info("Kaa client started");
                    }
                });

        /*
         * Set a configuration storage file to persist configuration.
         */
        mClient.setConfigurationStorage(new SimpleConfigurationStorage(
                kaaClientContext, "cityGuideConfig.data"));

        /*
         * Set a configuration listener to get notified about configuration
         * updates from the Kaa cluster. Update configuration object and notify UI
         * components to start using the updated configuration.
         */
        mClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(CityGuideConfig config) {
                mConfig = config;
                mEventBus.post(new ConfigurationUpdated());
                LOG.info("Configuration updated!");
            }
        });

        /*
         * Set a profile container used by the Kaa client to obtain the actual profile
         * object.
         */
        mClient.setProfileContainer(new ProfileContainer() {
            @Override
            public CityGuideProfile getProfile() {
                return mProfile;
            }
        });

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

    public void updateLocation(String area, String city) {

        /*
         * Update the city guide profile object and 
         * notify the Kaa client about the profile update.
         */
        mProfile.setArea(area);
        mProfile.setCity(city);
        mClient.updateProfile();
    }

    public CityGuideConfig getCityGuideConfiguration() {
        return mConfig;
    }

    public CityGuideProfile getCityGuideProfile() {
        return mProfile;
    }

    public boolean isKaaStarted() {
        return mKaaStarted;
    }

    public EventBus getEventBus() {
        return mEventBus;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}
