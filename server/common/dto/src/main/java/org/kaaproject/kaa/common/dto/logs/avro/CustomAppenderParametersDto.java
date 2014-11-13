package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;

public class CustomAppenderParametersDto implements Parameters, Serializable {

    private static final long serialVersionUID = 691679941017874525L;

    private String name;
    private String appenderClassName;   
    private String configuration;
    
    public CustomAppenderParametersDto() {        
    }
    
    public CustomAppenderParametersDto(String name, String appenderClassName,  String configuration) {
        this.name = name;
        this.appenderClassName = appenderClassName;
        this.configuration = configuration;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppenderClassName() {
        return appenderClassName;
    }

    public void setAppenderClassName(String appenderClassName) {
        this.appenderClassName = appenderClassName;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((appenderClassName == null) ? 0 : appenderClassName
                        .hashCode());
        result = prime * result
                + ((configuration == null) ? 0 : configuration.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        CustomAppenderParametersDto other = (CustomAppenderParametersDto) obj;
        if (appenderClassName == null) {
            if (other.appenderClassName != null) {
                return false;
            }
        } else if (!appenderClassName.equals(other.appenderClassName)) {
            return false;
        }
        if (configuration == null) {
            if (other.configuration != null) {
                return false;
            }
        } else if (!configuration.equals(other.configuration)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CustomAppenderParametersDto [name=" + name
                + ", appenderClassName=" + appenderClassName
                + ", configuration=" + configuration + "]";
    }

}
