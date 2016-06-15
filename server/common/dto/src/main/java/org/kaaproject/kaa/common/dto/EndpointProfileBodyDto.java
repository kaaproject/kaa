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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Arrays;

public class EndpointProfileBodyDto implements Serializable {

    private static final long serialVersionUID = 1459044085470010138L;

    private byte[] endpointKeyHash;
    private String clientSideProfile;
    private String serverSideProfile;
    private int clientSideProfileVersion;
    private int serverSideProfileVersion;
    @JsonIgnore
    private String appId;

    public EndpointProfileBodyDto() {}

    public EndpointProfileBodyDto(byte[] endpointKeyHash, String clientSideProfile, String serverSideProfile, int clientSideProfileVersion,
                                  int serverSideProfileVersion, String appId) {
        this.endpointKeyHash = endpointKeyHash;
        this.clientSideProfile = clientSideProfile;
        this.serverSideProfile = serverSideProfile;
        this.clientSideProfileVersion = clientSideProfileVersion;
        this.serverSideProfileVersion = serverSideProfileVersion;
        this.appId = appId;
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getClientSideProfile() {
        return clientSideProfile;
    }

    public void setClientSideProfile(String clientSideProfile) {
        this.clientSideProfile = clientSideProfile;
    }

    public String getServerSideProfile() {
        return serverSideProfile;
    }

    public void setServerSideProfile(String serverSideProfile) {
        this.serverSideProfile = serverSideProfile;
    }

    public String getAppId() {
        return appId;
    }

    public int getClientSideProfileVersion() {
        return clientSideProfileVersion;
    }

    public void setClientSideProfileVersion(int clientSideProfileVersion) {
        this.clientSideProfileVersion = clientSideProfileVersion;
    }

    public int getServerSideProfileVersion() {
        return serverSideProfileVersion;
    }

    public void setServerSideProfileVersion(int serverSideProfileVersion) {
        this.serverSideProfileVersion = serverSideProfileVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndpointProfileBodyDto that = (EndpointProfileBodyDto) o;

        if (clientSideProfileVersion != that.clientSideProfileVersion) {
            return false;
        }
        if (serverSideProfileVersion != that.serverSideProfileVersion) {
            return false;
        }
        if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) {
            return false;
        }
        if (clientSideProfile != null ? !clientSideProfile.equals(that.clientSideProfile) : that.clientSideProfile != null) {
            return false;
        }
        if (serverSideProfile != null ? !serverSideProfile.equals(that.serverSideProfile) : that.serverSideProfile != null) {
            return false;
        }
        return !(appId != null ? !appId.equals(that.appId) : that.appId != null);

    }

    @Override
    public int hashCode() {
        int result = endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0;
        result = 31 * result + (clientSideProfile != null ? clientSideProfile.hashCode() : 0);
        result = 31 * result + (serverSideProfile != null ? serverSideProfile.hashCode() : 0);
        result = 31 * result + clientSideProfileVersion;
        result = 31 * result + serverSideProfileVersion;
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        return result;
    }
}
