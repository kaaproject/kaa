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
package org.kaaproject.kaa.server.sync;

import java.nio.ByteBuffer;

public final class ConfigurationServerSync {

    private int appStateSeqNumber;
    private SyncResponseStatus responseStatus;
    private ByteBuffer confSchemaBody;
    private ByteBuffer confDeltaBody;

    public ConfigurationServerSync() {
    }

    /**
     * All-args constructor.
     */
    public ConfigurationServerSync(int appStateSeqNumber, SyncResponseStatus responseStatus, ByteBuffer confSchemaBody,
            ByteBuffer confDeltaBody) {
        this.appStateSeqNumber = appStateSeqNumber;
        this.responseStatus = responseStatus;
        this.confSchemaBody = confSchemaBody;
        this.confDeltaBody = confDeltaBody;
    }

    /**
     * Gets the value of the 'appStateSeqNumber' field.
     */
    public int getAppStateSeqNumber() {
        return appStateSeqNumber;
    }

    /**
     * Sets the value of the 'appStateSeqNumber' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setAppStateSeqNumber(int value) {
        this.appStateSeqNumber = value;
    }

    /**
     * Gets the value of the 'responseStatus' field.
     */
    public SyncResponseStatus getResponseStatus() {
        return responseStatus;
    }

    /**
     * Sets the value of the 'responseStatus' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setResponseStatus(SyncResponseStatus value) {
        this.responseStatus = value;
    }

    /**
     * Gets the value of the 'confSchemaBody' field.
     */
    public ByteBuffer getConfSchemaBody() {
        return confSchemaBody;
    }

    /**
     * Sets the value of the 'confSchemaBody' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setConfSchemaBody(ByteBuffer value) {
        this.confSchemaBody = value;
    }

    /**
     * Gets the value of the 'confDeltaBody' field.
     */
    public ByteBuffer getConfDeltaBody() {
        return confDeltaBody;
    }

    /**
     * Sets the value of the 'confDeltaBody' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setConfDeltaBody(ByteBuffer value) {
        this.confDeltaBody = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigurationServerSync that = (ConfigurationServerSync) o;

        if (appStateSeqNumber != that.appStateSeqNumber) {
            return false;
        }
        if (confDeltaBody != null ? !confDeltaBody.equals(that.confDeltaBody) : that.confDeltaBody != null) {
            return false;
        }
        if (confSchemaBody != null ? !confSchemaBody.equals(that.confSchemaBody) : that.confSchemaBody != null) {
            return false;
        }
        if (responseStatus != that.responseStatus) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = appStateSeqNumber;
        result = 31 * result + (responseStatus != null ? responseStatus.hashCode() : 0);
        result = 31 * result + (confSchemaBody != null ? confSchemaBody.hashCode() : 0);
        result = 31 * result + (confDeltaBody != null ? confDeltaBody.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConfigurationServerSync [appStateSeqNumber=");
        builder.append(appStateSeqNumber);
        builder.append(", responseStatus=");
        builder.append(responseStatus);
        builder.append(", confSchemaBody=");
        builder.append(confSchemaBody);
        builder.append(", confDeltaBody=");
        builder.append(confDeltaBody);
        builder.append("]");
        return builder.toString();
    }
}
