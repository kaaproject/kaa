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

package org.kaaproject.kaa.demo.cellmonitor;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.logging.LogFailoverCommand;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.demo.cellmonitor.event.CellLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.GpsLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.LogSent;
import org.kaaproject.kaa.demo.cellmonitor.event.SignalStrengthChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import de.greenrobot.event.EventBus;

/**
 * The implementation of the base {@link Application} class. Performs initialization of the
 * application resources including initialization of the Kaa client. Handles the Kaa client lifecycle.
 * Implements and registers a listener to monitor a mobile cell location and the signal strength.
 * Implements and registers a listener to monitor a phone gps location.
 * Sends cell monitor log records to the Kaa cluster via the Kaa client.
 */
public class CellMonitorApplication extends Application {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(CellMonitorApplication.class);

    public static final int UNDEFINED = -1;

    private EventBus mEventBus;
    private TelephonyManager mTelephonyManager;
    private CellMonitorPhoneStateListener mCellMonitorPhoneStateListener;
    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;
    
    private LocationManager mLocationManager;
    private GpsLocationListener mGpsLocationListener;
    private Location mGpsLocation;
    
    private int mSentLogCount = 0;
    private long mLastLogTime = 0;
    
    private KaaClient mClient;
    private boolean mKaaStarted = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = new EventBus();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mCellMonitorPhoneStateListener = new CellMonitorPhoneStateListener();
        
        mLocationManager = (LocationManager)  getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, false);        
        mGpsLocation = mLocationManager.getLastKnownLocation(bestProvider);
        mGpsLocationListener = new GpsLocationListener();
        
        /*
         * Initialize the Kaa client using the Android context.
         */
        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(
                this);
        mClient = Kaa.newClient(kaaClientContext,
                new SimpleKaaClientStateListener() {

            /*
             * Implement the onStarted callback to get notified as soon as 
             * the Kaa client is operational. 
             */
            @Override
            public void onStarted() {
                mKaaStarted = true;
                LOG.info("Kaa client started");
            }
        });
        
        /*
         * Define a log upload strategy used by the Kaa client for logs delivery.
         */
        mClient.setLogUploadStrategy(new LogUploadStrategy() {
            
            @Override
            public void onTimeout(LogFailoverCommand logFailoverCommand) {
                LOG.error("Unable to send logs within defined timeout!");
            }
            
            @Override
            public void onFailure(LogFailoverCommand logFailoverCommand, LogDeliveryErrorCode logDeliveryErrorCode) {
                LOG.error("Unable to send logs, error code: " + logDeliveryErrorCode);
                logFailoverCommand.retryLogUpload(10);
            }
            
            @Override
            public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus logStorageStatus) {
                return logStorageStatus.getRecordCount() > 0 ? 
                        LogUploadStrategyDecision.UPLOAD : LogUploadStrategyDecision.NOOP;
            }
            
            @Override
            public int getTimeout() {
                return 100;
            }
            
            @Override
            public long getBatchSize() {
                return 8 * 1024;
            }
        });
        
        /*
         * Start the Kaa client workflow.
         */
        mClient.start();
        
    }
    
    public void pause() {
        mTelephonyManager.listen(mCellMonitorPhoneStateListener,
                PhoneStateListener.LISTEN_NONE);
        mLocationManager.removeUpdates(mGpsLocationListener);
        
        /*
         * Suspend the Kaa client. Release all network connections and application
         * resources. Suspend all the Kaa client tasks.
         */
        mClient.pause();
    }
    
    public void resume() {
        mTelephonyManager.listen(mCellMonitorPhoneStateListener,
                PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, false);        
        mLocationManager.requestLocationUpdates(bestProvider, 0, 0, mGpsLocationListener);
        
        /*
         * Resume the Kaa client. Restore the Kaa client workflow. Resume all the Kaa client
         * tasks.
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
    
    private void sendLog() {
        if (mKaaStarted) {
            
            mSentLogCount++;
            mLastLogTime = System.currentTimeMillis();

            /*
             * Create an instance of a cell monitor log record and populate it with the latest values.
             */
            CellMonitorLog cellMonitorLog = new CellMonitorLog();
            cellMonitorLog.setLogTime(mLastLogTime);
            cellMonitorLog.setNetworkOperatorCode(Integer.valueOf(mTelephonyManager.getNetworkOperator()));
            cellMonitorLog.setNetworkOperatorName(mTelephonyManager.getNetworkOperatorName());
            
            int cid = UNDEFINED;
            int lac = UNDEFINED;
            
            if (mCellLocation != null && mCellLocation instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation)mCellLocation;
                cid = gsmCellLocation.getCid();
                lac = gsmCellLocation.getLac();
            }
            
            cellMonitorLog.setGsmCellId(cid);
            cellMonitorLog.setGsmLac(lac);
            
            int gsmSignalStrength = UNDEFINED;
            
            if (mSignalStrength != null) {
                gsmSignalStrength = mSignalStrength.getGsmSignalStrength();
            }
            cellMonitorLog.setSignalStrength(gsmSignalStrength);
            
            org.kaaproject.kaa.demo.cellmonitor.Location phoneLocation =
                    new org.kaaproject.kaa.demo.cellmonitor.Location();
            phoneLocation.setLatitude(mGpsLocation.getLatitude());
            phoneLocation.setLongitude(mGpsLocation.getLongitude());
            cellMonitorLog.setPhoneGpsLocation(phoneLocation);
            
            /*
             * Pass a cell monitor log record to the Kaa client. The Kaa client will upload 
             * the log record according to the defined log upload strategy. 
             */
            mClient.addLogRecord(cellMonitorLog);
            
            mEventBus.post(new LogSent());
        }
    }

    public EventBus getEventBus() {
        return mEventBus;
    }

    public TelephonyManager getTelephonyManager() {
        return mTelephonyManager;
    }

    public CellLocation getCellLocation() {
        return mCellLocation;
    }

    public SignalStrength getSignalStrength() {
        return mSignalStrength;
    }
    
    public Location getGpsLocation() {
        return mGpsLocation;
    }
    
    public int getSentLogCount() {
        return mSentLogCount;
    }
    
    public long getLastLogTime() {
        return mLastLogTime;
    } 

    private class CellMonitorPhoneStateListener extends PhoneStateListener {
        
        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            mCellLocation = location;
            sendLog();
            mEventBus.post(new CellLocationChanged());
            LOG.info("Cell location changed!");
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength;
            sendLog();
            mEventBus.post(new SignalStrengthChanged());
            LOG.info("Signal strength changed!");
        }
    };
    
    private class GpsLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            mGpsLocation = location;
            sendLog();
            mEventBus.post(new GpsLocationChanged());
            LOG.info("GPS location changed!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
        
    }
}
