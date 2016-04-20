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

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class EndpointGroupPlace extends EndpointGroupsPlace {

    private String endpointGroupId;
    private boolean includeDeprecatedProfileFilters;
    private boolean includeDeprecatedConfigurations;

    public EndpointGroupPlace(String applicationId,
            String endpointGroupId,
            boolean includeDeprecatedProfileFilters,
            boolean includeDeprecatedConfigurations) {
        super(applicationId);
        this.endpointGroupId = endpointGroupId;
        this.includeDeprecatedProfileFilters = includeDeprecatedProfileFilters;
        this.includeDeprecatedConfigurations = includeDeprecatedConfigurations;
    }

    public String getEndpointGroupId() {
        return endpointGroupId;
    }

    public boolean isIncludeDeprecatedProfileFilters() {
        return includeDeprecatedProfileFilters;
    }

    public boolean isIncludeDeprecatedConfigurations() {
        return includeDeprecatedConfigurations;
    }

    public void setIncludeDeprecatedProfileFilters(
            boolean includeDeprecatedProfileFilters) {
        this.includeDeprecatedProfileFilters = includeDeprecatedProfileFilters;
    }

    public void setIncludeDeprecatedConfigurations(
            boolean includeDeprecatedConfigurations) {
        this.includeDeprecatedConfigurations = includeDeprecatedConfigurations;
    }

    @Override
    public String getName() {
        return Utils.constants.endpointGroup();
    }

    @Prefix(value = "endGroup")
    public static class Tokenizer implements PlaceTokenizer<EndpointGroupPlace>, PlaceConstants {

        @Override
        public EndpointGroupPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new EndpointGroupPlace(PlaceParams.getParam(APPLICATION_ID),
                    PlaceParams.getParam(ENDPOINT_GROUP_ID),
                    PlaceParams.getBooleanParam(INCL_DEPR_PF),
                    PlaceParams.getBooleanParam(INCL_DEPR_CS));
        }

        @Override
        public String getToken(EndpointGroupPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(ENDPOINT_GROUP_ID, place.getEndpointGroupId());
            PlaceParams.putBooleanParam(INCL_DEPR_PF, place.isIncludeDeprecatedProfileFilters());
            PlaceParams.putBooleanParam(INCL_DEPR_CS, place.isIncludeDeprecatedConfigurations());
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
        EndpointGroupPlace other = (EndpointGroupPlace) obj;
        if (endpointGroupId == null) {
            if (other.endpointGroupId != null) {
                return false;
            }
        } else if (!endpointGroupId.equals(other.endpointGroupId)) {
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
        return new EndpointGroupsPlace(applicationId);
    }
}
