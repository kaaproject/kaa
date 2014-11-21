package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;

public class CustomAppenderParametersDto implements Parameters, Serializable {

    private static final long serialVersionUID = 691679941017874525L;

    private String name;
    private String appenderClassName;   
    private byte[] rawConfiguration;
    
    public CustomAppenderParametersDto() {        
    }
    
    public CustomAppenderParametersDto(String name, String appenderClassName, byte[] rawConfiguration) {
        this.name = name;
        this.appenderClassName = appenderClassName;
        this.rawConfiguration = rawConfiguration;
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

    public byte[] getRawConfiguration() {
        return rawConfiguration;
    }

    public void setRawConfiguration(byte[] rawConfiguration) {
        this.rawConfiguration = rawConfiguration;
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
                + ((rawConfiguration == null) ? 0 : rawConfiguration.hashCode());
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
        if (rawConfiguration == null) {
            if (other.rawConfiguration != null) {
                return false;
            }
        } else if (!rawConfiguration.equals(other.rawConfiguration)) {
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
                + ", rawConfiguration=" + rawConfiguration + "]";
    }

}
