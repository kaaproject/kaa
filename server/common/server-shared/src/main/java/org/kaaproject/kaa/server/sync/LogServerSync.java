/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.sync;

import java.util.ArrayList;
import java.util.List;

public final class LogServerSync {
    private List<LogDeliveryStatus> deliveryStatuses;

    public LogServerSync() {
        this(new ArrayList<LogDeliveryStatus>());
    }
    
    public LogServerSync(List<LogDeliveryStatus> deliveryStatuses) {
        super();
        this.deliveryStatuses = deliveryStatuses;
    }

    public List<LogDeliveryStatus> getDeliveryStatuses() {
        return deliveryStatuses;
    }

    public void setDeliveryStatuses(List<LogDeliveryStatus> deliveryStatuses) {
        this.deliveryStatuses = deliveryStatuses;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deliveryStatuses == null) ? 0 : deliveryStatuses.hashCode());
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
        LogServerSync other = (LogServerSync) obj;
        if (deliveryStatuses == null) {
            if (other.deliveryStatuses != null) {
                return false;
            }
        } else if (!deliveryStatuses.equals(other.deliveryStatuses)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogServerSync [deliveryStatuses=");
        builder.append(deliveryStatuses);
        builder.append("]");
        return builder.toString();
    }
}
