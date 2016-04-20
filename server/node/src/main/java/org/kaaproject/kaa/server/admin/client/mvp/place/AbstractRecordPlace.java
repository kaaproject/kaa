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

public abstract class AbstractRecordPlace extends TreePlace {

    private String applicationId;
    private String endpointGroupId;
    private boolean create;
    private boolean showActive;
    private double random;

    public AbstractRecordPlace(String applicationId,
            String endpointGroupId,
            boolean create,
            boolean showActive,
            double random) {
        this.applicationId = applicationId;
        this.endpointGroupId = endpointGroupId;
        this.create = create;
        this.showActive = showActive;
        this.random = random;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getEndpointGroupId() {
        return endpointGroupId;
    }

    public boolean isCreate() {
        return create;
    }

    public boolean isShowActive() {
        return showActive;
    }

    public double getRandom() {
        return random;
    }

    public static abstract class Tokenizer<P extends AbstractRecordPlace> implements PlaceTokenizer<P>, PlaceConstants {

        @Override
        public P getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return getPlaceImpl(PlaceParams.getParam(APPLICATION_ID),
                               PlaceParams.getParam(ENDPOINT_GROUP_ID),
                               PlaceParams.getBooleanParam(CREATE),
                               PlaceParams.getBooleanParam(SHOW_ACTIVE),
                               PlaceParams.getDoubleParam(RANDOM));
        }

        protected abstract P getPlaceImpl(String applicationId, String endpointGroupId, boolean create, boolean showActive, double random);

        @Override
        public String getToken(P place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(ENDPOINT_GROUP_ID, place.getEndpointGroupId());
            PlaceParams.putBooleanParam(CREATE, place.isCreate());
            PlaceParams.putBooleanParam(SHOW_ACTIVE, place.isShowActive());
            PlaceParams.putDoubleParam(RANDOM, place.getRandom());
            updateTokenImpl(place);
            return PlaceParams.generateToken();
        }
        
        protected abstract void updateTokenImpl(P place);
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
        AbstractRecordPlace other = (AbstractRecordPlace) obj;
        if (create != other.create) {
            return false;
        }
        if (endpointGroupId == null) {
            if (other.endpointGroupId != null) {
                return false;
            }
        } else if (!endpointGroupId.equals(other.endpointGroupId)) {
            return false;
        }
        if (Double.doubleToLongBits(random) != Double
                .doubleToLongBits(other.random)) {
            return false;
        }
        if (showActive != other.showActive) {
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
        return new EndpointGroupPlace(applicationId, endpointGroupId, false, false);
    }

}
