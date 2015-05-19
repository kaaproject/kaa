package org.kaaproject.kaa.demo.iotworld.citylights.state;

public class StreetLightState {

    private volatile Integer zoneId;

    public Integer getZoneId() {
        return zoneId;
    }

    public void setZoneId(Integer zoneId) {
        this.zoneId = zoneId;
    }

}
