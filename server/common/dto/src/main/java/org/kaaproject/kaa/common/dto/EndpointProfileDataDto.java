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

package org.kaaproject.kaa.common.dto;

public class EndpointProfileDataDto {

    private final String id;
    private final String endpointKey;
    private final String clientProfileBody;
    private final String serverProfileBody;

    private final int clientProfileVersion;
    private final int serverProfileVersion;

    public EndpointProfileDataDto(String id, String endpointKey, int clientProfileVersion, String clientProfileBody,
            int serverProfileVersion, String serverProfileBody) {
        super();
        this.id = id;
        this.endpointKey = endpointKey;
        this.clientProfileVersion = clientProfileVersion;
        this.clientProfileBody = clientProfileBody;
        this.serverProfileVersion = serverProfileVersion;
        this.serverProfileBody = serverProfileBody;
    }

    public String getId() {
        return id;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public String getClientProfileBody() {
        return clientProfileBody;
    }

    public String getServerProfileBody() {
        return serverProfileBody;
    }

    public int getClientProfileVersion() {
        return clientProfileVersion;
    }

    public int getServerProfileVersion() {
        return serverProfileVersion;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointProfileDataDto [id=");
        builder.append(id);
        builder.append(", endpointKey=");
        builder.append(endpointKey);
        builder.append(", clientProfileBody=");
        builder.append(clientProfileBody);
        builder.append(", serverProfileBody=");
        builder.append(serverProfileBody);
        builder.append(", clientProfileVersion=");
        builder.append(clientProfileVersion);
        builder.append(", serverProfileVersion=");
        builder.append(serverProfileVersion);
        builder.append("]");
        return builder.toString();
    }

}
