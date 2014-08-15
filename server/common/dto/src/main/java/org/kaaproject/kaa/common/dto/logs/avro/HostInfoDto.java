package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;

public class HostInfoDto implements Serializable, Comparable<HostInfoDto> {

    private static final long serialVersionUID = 2162204267075066877L;

    private static final int GT = 1;
    private static final int LT = -1;
    private static final int EQ = 0;

    private String hostname;
    private int port;
    private int priority;

    public HostInfoDto() {
    }

    public HostInfoDto(String hostname, int port, int priority) {
        this.hostname = hostname;
        this.port = port;
        this.priority = priority;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + port;
        result = prime * result + priority;
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
        HostInfoDto other = (HostInfoDto) obj;
        if (hostname == null) {
            if (other.hostname != null)
                return false;
        } else if (!hostname.equals(other.hostname))
            return false;
        if (port != other.port)
            return false;
        if (priority != other.priority)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "HostInfoDto [hostname=" + hostname + ", port=" + port + ", priority=" + priority + "]";
    }

    @Override
    public int compareTo(HostInfoDto o) {
        int result;
        if (o != null) {
            if (priority < o.priority) {
                result = LT;
            } else if (priority > o.priority) {
                result = GT;
            } else {
                result = EQ;
            }
        } else {
            result = LT;
        }
        return result;
    }

}
