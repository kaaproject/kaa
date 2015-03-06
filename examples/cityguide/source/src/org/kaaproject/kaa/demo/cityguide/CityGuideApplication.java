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
 * The Class CityGuideApplication.
 * Implementation of base {@link Application} class. Performs initialization of 
 * application resources including initialization of Kaa client. Handles Kaa client lifecycle.
 * Stores reference to actual endpoint configuration. Receives configuration updates from Kaa cluster.
 * Manages endpoint profile object, notifies Kaa cluster about profile updates.
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
         * Create empty city guide profile object based on user defined profile
         * schema.
         */
        mProfile = new CityGuideProfile();

        /*
         * Initialize Kaa client using android context.
         */
        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(
                this);
        mClient = Kaa.newClient(kaaClientContext,
                new SimpleKaaClientStateListener() {

                    /*
                     * Implement onStarted callback to get notified when Kaa
                     * client is operational. Obtain city guide configuration
                     * from Kaa. Notify UI components about Kaa started to start
                     * using configuration.
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
         * Set configuration storage file to persist configuration.
         */
        mClient.setConfigurationStorage(new SimpleConfigurationStorage(
                kaaClientContext, "cityGuideConfig.data"));

        /*
         * Set configuration listener to get notified about configuration
         * updates from Kaa cluster. Update configuration object and notify UI
         * components to start using updated configuration.
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
         * Set profile container used by Kaa client to get actual profile
         * object.
         */
        mClient.setProfileContainer(new ProfileContainer() {
            @Override
            public CityGuideProfile getProfile() {
                return mProfile;
            }
        });

        /*
         * Start Kaa client workflow.
         */
        mClient.start();
    }

    public void pause() {

        /*
         * Suspend Kaa client. Release all network connections and application
         * resources. Suspend all Kaa client tasks.
         */
        mClient.pause();
    }

    public void resume() {

        /*
         * Resume Kaa client. Restore Kaa client workflow. Resume all Kaa client
         * tasks.
         */
        mClient.resume();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        /*
         * Stop Kaa client. Release all network connections and application
         * resources. Shutdown all Kaa client tasks.
         */
        mClient.stop();
        mKaaStarted = false;
    }

    public void updateLocation(String area, String city) {

        /*
         * Update city guide profile object and notify Kaa client about profile
         * update.
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
