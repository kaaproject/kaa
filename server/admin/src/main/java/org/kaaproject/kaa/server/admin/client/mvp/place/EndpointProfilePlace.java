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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class EndpointProfilePlace extends EndpointProfilesPlace {

    private String endpointID;

    public EndpointProfilePlace(String applicationId, String endpointID) {
        super(applicationId);
        this.endpointID = endpointID;
    }

    @Override
    public String getName() {
        return "Endpoint profile";
    }

    @Prefix(value = "endProfId")
    public static class Tokenizer implements PlaceTokenizer<EndpointProfilePlace>, PlaceConstants {

        @Override
        public EndpointProfilePlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new EndpointProfilePlace(PlaceParams.getParam(APPLICATION_ID),
                    PlaceParams.getParam(ENDPOINT_PROFILE_ID));
        }

        @Override
        public String getToken(EndpointProfilePlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(ENDPOINT_PROFILE_ID, place.getEndpointID());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EndpointProfilePlace other = (EndpointProfilePlace) obj;
        if (endpointID == null) {
            if (other.endpointID != null)
                return false;
        } else if (!endpointID.equals(other.endpointID))
            return false;
        return true;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new EndpointProfilesPlace(applicationId);
    }

    public String getEndpointID() {
        return endpointID;
    }
}
