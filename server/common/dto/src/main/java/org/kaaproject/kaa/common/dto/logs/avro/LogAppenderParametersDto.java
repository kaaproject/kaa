package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;

public class LogAppenderParametersDto implements Serializable {

    private static final long serialVersionUID = -4693534332083126772L;

    private Parameters parameters;

    public LogAppenderParametersDto() {
    }

    public LogAppenderParametersDto(Parameters parameters) {
        this.parameters = parameters;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogAppenderParametersDto other = (LogAppenderParametersDto) obj;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogAppenderParametersDto [parameters=" + parameters + "]";
    }

}
