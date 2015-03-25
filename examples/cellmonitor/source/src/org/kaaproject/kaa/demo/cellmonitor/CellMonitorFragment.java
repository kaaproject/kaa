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

import static org.kaaproject.kaa.demo.cellmonitor.CellMonitorApplication.UNDEFINED;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.kaaproject.kaa.demo.cellmonitor.event.CellLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.GpsLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.LogSent;
import org.kaaproject.kaa.demo.cellmonitor.event.SignalStrengthChanged;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.telephony.CellLocation;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * The implementation of the {@link Fragment} class.
 * Implements common fragment lifecycle functions. Stores references to common application resources.
 * Provides a view with the information about current GSM cell location, signal strength and phone GPS location.
 * Displays current statistics about logs sent to the Kaa cluster.   
 */
public class CellMonitorFragment extends Fragment {

    private CellMonitorActivity mActivity;
    private CellMonitorApplication mApplication;
    private ActionBar mActionBar;
    
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private Calendar mCalendar = Calendar.getInstance();
    
    private TextView mNetworkOperatorValue;
    private TextView mNetworkOperatorNameValue;
    private TextView mGsmCellIdValue;
    private TextView mGsmLacValue;
    private TextView mGsmSignalStrengthValue;
    private TextView mGpsLocationValue;
    private TextView mLastLogTimeValue;
    private TextView mSentLogCountValue;
    
    public CellMonitorFragment() {
        super();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cell_monitor, container,
                false);
        
        mNetworkOperatorValue = (TextView) rootView.findViewById(R.id.networkOperatorValue);
        mNetworkOperatorNameValue = (TextView) rootView.findViewById(R.id.networkOperatorNameValue);
        mGsmCellIdValue = (TextView) rootView.findViewById(R.id.gsmCellIdValue);
        mGsmLacValue = (TextView) rootView.findViewById(R.id.gsmLacValue);
        mGsmSignalStrengthValue = (TextView) rootView.findViewById(R.id.gsmSignalStrengthValue);
        mGpsLocationValue = (TextView) rootView.findViewById(R.id.gpsLocationValue);
        mLastLogTimeValue = (TextView) rootView.findViewById(R.id.lastLogTimeValue);
        mSentLogCountValue = (TextView) rootView.findViewById(R.id.logsSentValue);
        
        updateAll();
        
        return rootView;
    }
    
    private void updateAll() {
        
        String networkOperator = mApplication.getTelephonyManager().getNetworkOperator();
        if (networkOperator != null) {
            mNetworkOperatorValue.setText(networkOperator);
        } else {
            mNetworkOperatorValue.setText(R.string.unavailable);
        }
        String networkOperatorName = mApplication.getTelephonyManager().getNetworkOperatorName();
        if (networkOperatorName != null) {
            mNetworkOperatorNameValue.setText(networkOperatorName);
        } else {
            mNetworkOperatorNameValue.setText(R.string.unavailable);
        }
        
        updateGsmCellLocation();
        updateGsmSignalStrength();
        updateGpsLocation();
        updateSentLogs();
    }
    
    private void updateGsmCellLocation() {
        int cid = UNDEFINED;
        int lac = UNDEFINED;
        CellLocation cellLocation = mApplication.getCellLocation();
        if (cellLocation != null && cellLocation instanceof GsmCellLocation) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation)cellLocation;
            cid = gsmCellLocation.getCid();
            lac = gsmCellLocation.getLac();
        }
        if (cid != UNDEFINED) {
            mGsmCellIdValue.setText(String.valueOf(cid));
        } else {
            mGsmCellIdValue.setText(R.string.unavailable);
        }
        if (lac != UNDEFINED) {
            mGsmLacValue.setText(String.valueOf(lac));
        } else {
            mGsmLacValue.setText(R.string.unavailable);
        }
    }
    
    private void updateGsmSignalStrength() {
        int gsmSignalStrength = UNDEFINED;
        SignalStrength signalStrength = mApplication.getSignalStrength();
        if (signalStrength != null) {
            gsmSignalStrength = signalStrength.getGsmSignalStrength();
        }
        if (gsmSignalStrength != UNDEFINED) {
            mGsmSignalStrengthValue.setText(String.valueOf(gsmSignalStrength));
        } else {
            mGsmSignalStrengthValue.setText(R.string.unavailable);
        }
    }
    
    private void updateGpsLocation() {
        Location gpsLocation = mApplication.getGpsLocation();
        if (gpsLocation != null) {
            double latitude = gpsLocation.getLatitude();
            double longitude = gpsLocation.getLongitude();
            StringBuilder sb = new StringBuilder();
            sb.append(latitude).append(", ").append(longitude);
            mGpsLocationValue.setText(sb.toString());
        } else {
            mGpsLocationValue.setText(R.string.unavailable);
        }
    }
    
    private void updateSentLogs() {
        long lastLogTime = mApplication.getLastLogTime();
        if (lastLogTime > 0) {
            mCalendar.setTimeInMillis(lastLogTime);
            mLastLogTimeValue.setText(mDateFormat.format(mCalendar.getTime()));
        } else {
            mLastLogTimeValue.setText(R.string.unavailable);
        }
        mSentLogCountValue.setText(String.valueOf(mApplication.getSentLogCount()));
    }
    
    public void onEventMainThread(CellLocationChanged cellLocationChanged) {
        updateGsmCellLocation();
    }
    
    public void onEventMainThread(SignalStrengthChanged signalStrengthChanged) {
        updateGsmSignalStrength();
    }
    
    public void onEventMainThread(GpsLocationChanged gpsLocationChanged) {
        updateGpsLocation();
    }
    
    public void onEventMainThread(LogSent logSent) {
        updateSentLogs();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (CellMonitorActivity) activity;
            mActionBar = mActivity.getSupportActionBar();
            mApplication = mActivity.getCellMonitorApplication();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mActionBar != null) {
            int options = ActionBar.DISPLAY_SHOW_TITLE;
            mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.setTitle(getText(R.string.cell_monitor_title));
            mActionBar.setDisplayShowTitleEnabled(true);
        }
        if (!mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().register(this);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().unregister(this);
        }
    }

    
}
