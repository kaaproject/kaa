/*
 * Copyright 2014-2016 CyberVision, Inc.
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

public final class UserAttachResponse {
    private SyncStatus result;
    private UserVerifierErrorCode errorCode;
    private String errorReason;

    public UserAttachResponse() {
    }

    /**
     * All-args constructor.
     */
    public UserAttachResponse(SyncStatus result, UserVerifierErrorCode errorCode, String errorReason) {
        this.result = result;
        this.errorCode = errorCode;
        this.errorReason = errorReason;
    }

    /**
     * Gets the value of the 'result' field.
     */
    public SyncStatus getResult() {
        return result;
    }

    /**
     * Sets the value of the 'result' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setResult(SyncStatus value) {
        this.result = value;
    }

    public UserVerifierErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorReason() {
        return errorReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserAttachResponse that = (UserAttachResponse) o;

        if (errorCode != that.errorCode) {
            return false;
        }
        if (errorReason != null ? !errorReason.equals(that.errorReason) : that.errorReason != null) {
            return false;
        }
        if (result != that.result) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = result != null ? result.hashCode() : 0;
        result1 = 31 * result1 + (errorCode != null ? errorCode.hashCode() : 0);
        result1 = 31 * result1 + (errorReason != null ? errorReason.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserAttachResponse [result=");
        builder.append(result);
        builder.append(", errorCode=");
        builder.append(errorCode);
        builder.append(", errorReason=");
        builder.append(errorReason);
        builder.append("]");
        return builder.toString();
    }
}
