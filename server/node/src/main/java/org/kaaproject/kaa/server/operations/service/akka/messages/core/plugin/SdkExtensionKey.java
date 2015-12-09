/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.server.operations.service.akka.messages.core.plugin;

public class SdkExtensionKey {

    private final String sdkToken;
    private final int extensionId;

    public SdkExtensionKey(String sdkToken, int extensionId) {
        super();
        this.sdkToken = sdkToken;
        this.extensionId = extensionId;
    }

    public String getSdkToken() {
        return sdkToken;
    }

    public int getExtensionId() {
        return extensionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + extensionId;
        result = prime * result + ((sdkToken == null) ? 0 : sdkToken.hashCode());
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
        SdkExtensionKey other = (SdkExtensionKey) obj;
        if (extensionId != other.extensionId)
            return false;
        if (sdkToken == null) {
            if (other.sdkToken != null)
                return false;
        } else if (!sdkToken.equals(other.sdkToken))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[sdkToken=" + sdkToken + ", extensionId=" + extensionId + "]";
    }

}
