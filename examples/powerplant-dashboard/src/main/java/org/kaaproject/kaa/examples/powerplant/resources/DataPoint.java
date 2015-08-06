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

package org.kaaproject.kaa.examples.powerplant.resources;

import java.util.Comparator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DataPoint {

    @XmlElement
    public int zoneId;
    
    @XmlElement
    public double voltage;
    
    @XmlElement
    public long time;

    public DataPoint(int zoneId, double voltage, long time) {
        super();
        this.zoneId = zoneId;
        this.voltage = voltage;
        this.time = time;
    }
    
    static final Comparator<DataPoint> TS_COMPARATOR = new Comparator<DataPoint>() {
        @Override
        public int compare(DataPoint o1, DataPoint o2) {
            if (o1.time > o2.time) {
                return 1;
            } else {
                return o1.time == o2.time ? 0 : -1;
            }
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + zoneId;
        result = prime * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataPoint other = (DataPoint) obj;
        if (zoneId != other.zoneId) {
            return false;
        }
        if (time != other.time) {
            return false;
        }
        return true;
    }
}
