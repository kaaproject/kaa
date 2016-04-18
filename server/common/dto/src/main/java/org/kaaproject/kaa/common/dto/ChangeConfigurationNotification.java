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

public class ChangeConfigurationNotification implements Serializable {

    private static final long serialVersionUID = 1787325211443607655L;

    private ConfigurationDto configurationDto;

    private ChangeNotificationDto changeNotificationDto;

    public ConfigurationDto getConfigurationDto() {
        return configurationDto;
    }

    public void setConfigurationDto(ConfigurationDto configurationDto) {
        this.configurationDto = configurationDto;
    }

    public ChangeNotificationDto getChangeNotificationDto() {
        return changeNotificationDto;
    }

    public void setChangeNotificationDto(ChangeNotificationDto changeNotificationDto) {
        this.changeNotificationDto = changeNotificationDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChangeConfigurationNotification)) {
            return false;
        }
        ChangeConfigurationNotification that = (ChangeConfigurationNotification) o;

        if (configurationDto != null ? !configurationDto.equals(that.configurationDto) : that.configurationDto != null) {
            return false;
        }
        if (changeNotificationDto != null ? !changeNotificationDto.equals(that.changeNotificationDto) : that.changeNotificationDto != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = configurationDto != null ? configurationDto.hashCode() : 0;
        result = 31 * result + (changeNotificationDto != null ? changeNotificationDto.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChangeConfigurationNotification{" +
                "configurationDto=" + configurationDto +
                ", changeNotificationDto=" + changeNotificationDto +
                '}';
    }
}
