package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;

public class FileAppenderParametersDto implements Parameters, Serializable {

    private static final long serialVersionUID = -1395835197692635927L;

    private String filePath;

    public FileAppenderParametersDto(String filePath) {
        this.filePath = filePath;
    }

    public FileAppenderParametersDto() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
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
        FileAppenderParametersDto other = (FileAppenderParametersDto) obj;
        if (filePath == null) {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FileAppenderParametersDto [filePath=" + filePath + "]";
    }

}
