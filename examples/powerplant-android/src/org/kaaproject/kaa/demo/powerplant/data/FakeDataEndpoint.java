package org.kaaproject.kaa.demo.powerplant.data;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.powerplant.fragment.DashboardFragment;
import org.kaaproject.kaa.demo.powerplant.pojo.DataPoint;
import org.kaaproject.kaa.demo.powerplant.pojo.DataReport;

import android.util.Log;

public class FakeDataEndpoint extends AbstractDataEndpoint {

    private static final String TAG = FakeDataEndpoint.class.getSimpleName();
    private static final float MIN_GEN_VOLTAGE = 2.5f;
    private static final float MAX_GEN_VOLTAGE = 5.0f;
    private static final int MAX_POINTS_COUNT = 150;

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
        long time = (System.currentTimeMillis() / 1000) * 1000;
        for (int i = 0; i < MAX_POINTS_COUNT; i++) {
            long pointTime = time - (i * 1000);
            reports.add(genDataReport(pointTime));
        }
        return reports;
    }
    
    private DataReport genDataReport(long time) {
        List<DataPoint> dataPoints = new ArrayList<DataPoint>();
        for (int i = 0; i < DashboardFragment.NUM_PANELS; i++) {
            dataPoints.add(new DataPoint(i, genRandomVoltage()));
        }
        return new DataReport(time, dataPoints, getConsumption());
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

}
