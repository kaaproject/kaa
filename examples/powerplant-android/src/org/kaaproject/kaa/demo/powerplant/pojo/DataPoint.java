package org.kaaproject.kaa.demo.powerplant.pojo;

public class DataPoint {

    private final int panelId;

    private final float voltage;

    public DataPoint(int panelId, float voltage) {
        super();
        this.panelId = panelId;
        this.voltage = voltage;
    }

    public int getPanelId() {
        return panelId;
    }

    public float getVoltage() {
        return voltage;
    }
}
