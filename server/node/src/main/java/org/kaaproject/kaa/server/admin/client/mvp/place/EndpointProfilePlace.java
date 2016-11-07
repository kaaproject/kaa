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

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EndpointProfilePlace extends EndpointProfilesPlace {

    private String endpointKeyHash;

    public EndpointProfilePlace(String applicationId, String endpointKeyHash) {
        super(applicationId);
        this.endpointKeyHash = endpointKeyHash;
    }

    @Override
    public String getName() {
        return Utils.constants.profile();
    }

    public String getEndpointKeyHash() {
        return endpointKeyHash;
    }

    @Prefix(value = "endProfKeyHash")
    public static class Tokenizer implements PlaceTokenizer<EndpointProfilePlace>, PlaceConstants {

        @Override
        public EndpointProfilePlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new EndpointProfilePlace(PlaceParams.getParam(APPLICATION_ID),
                    PlaceParams.getParam(ENDPOINT_PROFILE_KEY_HASH));
        }
        @Override
        public String getToken(EndpointProfilePlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(ENDPOINT_PROFILE_KEY_HASH, place.getEndpointKeyHash());
            return PlaceParams.generateToken();
        }

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
        EndpointProfilePlace other = (EndpointProfilePlace) obj;
        if (endpointKeyHash == null) {
            if (other.endpointKeyHash != null) {
                return false;
            }
        } else if (!endpointKeyHash.equals(other.endpointKeyHash)) {
            return false;
        }
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


}
