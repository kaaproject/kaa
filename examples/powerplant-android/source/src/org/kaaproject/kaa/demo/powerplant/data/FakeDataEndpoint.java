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

package org.kaaproject.kaa.demo.powerplant.data;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.powerplant.fragment.DashboardFragment;
import org.kaaproject.kaa.demo.powerplant.pojo.DataPoint;
import org.kaaproject.kaa.demo.powerplant.pojo.DataReport;

import android.util.Log;

public class FakeDataEndpoint extends AbstractDataEndpoint {

    private static final String TAG = FakeDataEndpoint.class.getSimpleName();
    private static final float MIN_GEN_VOLTAGE = 0f;
    private static final float MAX_GEN_VOLTAGE = 6000f;
    private static final int MAX_POINTS_COUNT = 150;
    private static final int PANNELS_PER_ZONE = 1;

    @Override
    public DataReport getLatestData() {
        sleepABit();
        long time = System.currentTimeMillis();
        return genDataReport(time);
    }

    @Override
    public List<DataReport> getHistoryData(long fromTime) {
        sleepABit();
        List<DataReport> reports = new ArrayList<DataReport>(MAX_POINTS_COUNT);
        long time = System.currentTimeMillis() / 1000;
        for (int i = 0; i < MAX_POINTS_COUNT; i++) {
            long pointTime = time - (i * 1000);
            reports.add(genDataReport(pointTime));
        }
        return reports;
    }
    
    private DataReport genDataReport(long time) {
        List<DataPoint> dataPoints = new ArrayList<DataPoint>();
        for (int i = 0; i < DashboardFragment.NUM_ZONES; i++) {
            dataPoints.add(new DataPoint(i, 1000, genRandomVoltage()));
        }
        return new DataReport(time, dataPoints, getConsumption(PANNELS_PER_ZONE * DashboardFragment.NUM_ZONES));
    }

    private float genRandomVoltage() {
        float voltage = MIN_GEN_VOLTAGE + (float) Math.random() * (MAX_GEN_VOLTAGE - MIN_GEN_VOLTAGE);
        return voltage;
    }

    private void sleepABit() {
        try {
            Thread.sleep(200l);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted during server delay emulation", e);
        }
    }

    @Override
    public void stop() {
    	
    }
    
}
