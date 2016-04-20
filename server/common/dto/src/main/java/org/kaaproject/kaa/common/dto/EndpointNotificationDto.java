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

package org.kaaproject.kaa.common.dto;


import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.common.dto.Util.getArrayCopy;


public class EndpointNotificationDto implements HasId, Serializable {

    private static final long serialVersionUID = -5548269571722364843L;

    private String id;
    private byte[] endpointKeyHash;
    private NotificationDto notificationDto;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NotificationDto getNotificationDto() {
        return notificationDto;
    }

    public void setNotificationDto(NotificationDto notificationDto) {
        this.notificationDto = notificationDto;
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = getArrayCopy(endpointKeyHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndpointNotificationDto)) {
            return false;
        }

        EndpointNotificationDto that = (EndpointNotificationDto) o;

        if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) {
            return false;
        }
        if (notificationDto != null ? !notificationDto.equals(that.notificationDto) : that.notificationDto != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0;
        result = 31 * result + (notificationDto != null ? notificationDto.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointNotificationDto{" +
                "id='" + id + '\'' +
                ", endpointKeyHash=" + Arrays.toString(endpointKeyHash) +
                ", notificationDto=" + notificationDto +
                '}';
    }
}
