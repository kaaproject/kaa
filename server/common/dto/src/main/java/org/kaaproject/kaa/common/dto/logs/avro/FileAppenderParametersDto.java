package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;

public class FileAppenderParametersDto implements Parameters, Serializable {

    private static final long serialVersionUID = -1395835197692635927L;

    private String logDirectoryPath;
    private String username;
    private String sshKey;

    public FileAppenderParametersDto() {
    }

    public FileAppenderParametersDto(String logDirectoryPath, String username, String sshKey) {
        super();
        this.logDirectoryPath = logDirectoryPath;
        this.username = username;
        this.sshKey = sshKey;
    }

    public String getLogDirectoryPath() {
        return logDirectoryPath;
    }

    public void setLogDirectoryPath(String logDirectoryPath) {
        this.logDirectoryPath = logDirectoryPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logDirectoryPath == null) ? 0 : logDirectoryPath.hashCode());
        result = prime * result + ((sshKey == null) ? 0 : sshKey.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
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
        if (logDirectoryPath == null) {
            if (other.logDirectoryPath != null)
                return false;
        } else if (!logDirectoryPath.equals(other.logDirectoryPath))
            return false;
        if (sshKey == null) {
            if (other.sshKey != null)
                return false;
        } else if (!sshKey.equals(other.sshKey))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FileAppenderParametersDto [logDirectoryPath=" + logDirectoryPath + ", username=" + username + ", sshKey=" + sshKey + "]";
    }

}
