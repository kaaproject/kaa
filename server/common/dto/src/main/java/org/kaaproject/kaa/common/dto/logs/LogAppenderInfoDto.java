/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.common.dto.logs;

import java.io.Serializable;

public class LogAppenderInfoDto implements Serializable {

    private static final long serialVersionUID = 5417936799807172505L;
    
    private LogAppenderTypeDto type;
    private String name;
    private String defaultConfig;
    private String appenderClassName;
    
    public LogAppenderInfoDto() {
        super();
    }
    
    public LogAppenderInfoDto(LogAppenderTypeDto type) {
        super();
        this.type = type;
        this.name = type.getLabel();
    }

    public LogAppenderInfoDto(LogAppenderTypeDto type, String name,
            String defaultConfig, String appenderClassName) {
        super();
        this.type = type;
        this.name = name;
        this.defaultConfig = defaultConfig;
        this.appenderClassName = appenderClassName;
    }

    public LogAppenderTypeDto getType() {
        return type;
    }

    public void setType(LogAppenderTypeDto type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(String defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public String getAppenderClassName() {
        return appenderClassName;
    }

    public void setAppenderClassName(String appenderClassName) {
        this.appenderClassName = appenderClassName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((appenderClassName == null) ? 0 : appenderClassName
                        .hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        LogAppenderInfoDto other = (LogAppenderInfoDto) obj;
        if (appenderClassName == null) {
            if (other.appenderClassName != null) {
                return false;
            }
        } else if (!appenderClassName.equals(other.appenderClassName)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LogAppenderInfoDto [type=" + type + ", name=" + name
                + ", defaultConfig=" + defaultConfig + ", appenderClassName="
                + appenderClassName + "]";
    }

}
