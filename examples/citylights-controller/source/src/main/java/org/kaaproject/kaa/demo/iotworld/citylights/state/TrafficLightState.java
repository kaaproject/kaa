package org.kaaproject.kaa.demo.iotworld.citylights.state;

public class TrafficLightState {

    private volatile boolean mainRoad = true;
    private volatile boolean carPending;
    private volatile boolean pedestrianPending;

    public boolean isMainRoad() {
        return mainRoad;
    }

    public void setMainRoad(boolean state) {
        this.mainRoad = state;
    }

    public boolean isCarPending() {
        return carPending;
    }

    public void setCarPending(boolean carPending) {
        this.carPending = carPending;
    }

    public boolean isPedestrianPending() {
        return pedestrianPending;
    }

    public void setPedestrianPending(boolean pedestrianPending) {
        this.pedestrianPending = pedestrianPending;
    }
}
