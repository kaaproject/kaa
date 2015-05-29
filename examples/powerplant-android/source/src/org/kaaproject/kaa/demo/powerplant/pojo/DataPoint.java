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

package org.kaaproject.kaa.demo.powerplant.pojo;

public class DataPoint {

    private final int zoneId;

    private final int panelCount;
    
    private final float voltage;

    public DataPoint(int zoneId, int panelCount, float voltage) {
        super();
        this.zoneId = zoneId;
        this.panelCount = panelCount;
        this.voltage = voltage;
    }

    public int getPanelId() {
        return zoneId;
    }

    public int getPanelCount() {
    	return panelCount;
    }
    
    public float getVoltage() {
        return voltage;
    }
    
    public float getAverageVoltage() {
    	return voltage / panelCount;
    }

    @Override
    public String toString() {
        return "DataPoint{" +
                "zoneId=" + zoneId +
                ", panelCount=" + panelCount + 
                ", voltage=" + voltage +
                ", avgVoltage=" + getAverageVoltage() +
                '}';
    }
}
