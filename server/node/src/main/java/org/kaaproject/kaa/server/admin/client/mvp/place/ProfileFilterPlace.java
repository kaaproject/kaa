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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.Prefix;

public class ProfileFilterPlace extends AbstractRecordPlace {
    
    private String endpointProfileSchemaId;
    private String serverProfileSchemaId;

    public ProfileFilterPlace(String applicationId, String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId, boolean create, boolean showActive, double random) {
        super(applicationId, endpointGroupId, create, showActive, random);
        this.endpointProfileSchemaId = endpointProfileSchemaId;
        this.serverProfileSchemaId = serverProfileSchemaId;
    }
    
    public String getEndpointProfileSchemaId() {
        return endpointProfileSchemaId;
    }

    public String getServerProfileSchemaId() {
        return serverProfileSchemaId;
    }

    @Prefix(value = "profFilter")
    public static class Tokenizer extends AbstractRecordPlace.Tokenizer<ProfileFilterPlace> {

        @Override
        protected ProfileFilterPlace getPlaceImpl(String applicationId, String endpointGroupId, boolean create, boolean showActive, double random) {
            return new ProfileFilterPlace(applicationId, 
                    PlaceParams.getParam(ENDPOINT_PROFILE_SCHEMA_ID), 
                    PlaceParams.getParam(SERVER_PROFILE_SCHEMA_ID),
                    endpointGroupId, create, showActive, random);
        }

        @Override
        protected void updateTokenImpl(ProfileFilterPlace place) {
            PlaceParams.putParam(ENDPOINT_PROFILE_SCHEMA_ID, place.getEndpointProfileSchemaId());
            PlaceParams.putParam(SERVER_PROFILE_SCHEMA_ID, place.getServerProfileSchemaId());
        }
    }

    @Override
    public String getName() {
        return Utils.constants.profileFilter();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProfileFilterPlace other = (ProfileFilterPlace) obj;
        if (endpointProfileSchemaId == null) {
            if (other.endpointProfileSchemaId != null) {
                return false;
            }
        } else if (!endpointProfileSchemaId
                .equals(other.endpointProfileSchemaId)) {
            return false;
        }
        if (serverProfileSchemaId == null) {
            if (other.serverProfileSchemaId != null) {
                return false;
            }
        } else if (!serverProfileSchemaId.equals(other.serverProfileSchemaId)) {
            return false;
        }
        return true;
    }

}
