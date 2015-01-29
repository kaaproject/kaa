package org.kaaproject.kaa.server.sync;

public class LogDeliveryStatus {
    
    private int requestId;
    private SyncStatus result;
    private LogDeliveryErrorCode errorCode;
    
    public LogDeliveryStatus(int requestId, SyncStatus result, LogDeliveryErrorCode errorCode) {
        super();
        this.requestId = requestId;
        this.result = result;
        this.errorCode = errorCode;
    }

    public int getRequestId() {
        return requestId;
    }

    public SyncStatus getResult() {
        return result;
    }

    public LogDeliveryErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((errorCode == null) ? 0 : errorCode.hashCode());
        result = prime * result + requestId;
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
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
        LogDeliveryStatus other = (LogDeliveryStatus) obj;
        if (errorCode != other.errorCode) {
            return false;
        }
        if (requestId != other.requestId) {
            return false;
        }
        if (result != other.result) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogDeliveryStatus [requestId=");
        builder.append(requestId);
        builder.append(", result=");
        builder.append(result);
        builder.append(", errorCode=");
        builder.append(errorCode);
        builder.append("]");
        return builder.toString();
    }
}
