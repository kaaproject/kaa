package org.kaaproject.kaa.demo.powerplant.pojo;

import java.util.List;

public class DataReport {

    private final long time;

    private final List<DataPoint> dataPoints;
    
    private float powerConsumption;

    public DataReport(long time, List<DataPoint> dataPoints, float powerConsumption) {
        super();
        this.time = time;
        this.dataPoints = dataPoints;
        this.powerConsumption = powerConsumption;
    }

    public long getTime() {
        return time;
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public float getPowerConsumption() {
        return powerConsumption;
    }
}
