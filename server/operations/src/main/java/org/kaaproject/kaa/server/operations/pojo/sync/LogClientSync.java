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
package org.kaaproject.kaa.server.operations.pojo.sync;

import java.util.List;

public class LogClientSync {
    private String requestId;
    private List<LogEntry> logEntries;

    public LogClientSync() {
    }

    /**
     * All-args constructor.
     */
    public LogClientSync(String requestId, List<LogEntry> logEntries) {
        this.requestId = requestId;
        this.logEntries = logEntries;
    }

    /**
     * Gets the value of the 'requestId' field.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the 'requestId' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the 'logEntries' field.
     */
    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    /**
     * Sets the value of the 'logEntries' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setLogEntries(List<LogEntry> value) {
        this.logEntries = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logEntries == null) ? 0 : logEntries.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
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
        LogClientSync other = (LogClientSync) obj;
        if (logEntries == null) {
            if (other.logEntries != null) {
                return false;
            }
        } else if (!logEntries.equals(other.logEntries)) {
            return false;
        }
        if (requestId == null) {
            if (other.requestId != null) {
                return false;
            }
        } else if (!requestId.equals(other.requestId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogClientSync [requestId=");
        builder.append(requestId);
        builder.append("]");
        return builder.toString();
    }
}
