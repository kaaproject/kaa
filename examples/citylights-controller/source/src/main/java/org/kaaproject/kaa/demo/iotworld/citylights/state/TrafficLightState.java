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
