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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.view.client.HasData;

public class EndpointUsersPlace extends TreePlace {

    protected String applicationId;

    private EndpointUsersPlaceDataProvider dataProvider;

    public EndpointUsersPlace(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String getName() {
        return Utils.constants.users();
    }

    public static abstract class Tokenizer<P extends EndpointUsersPlace> implements PlaceTokenizer<P>, PlaceConstants {

        @Override
        public P getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return getPlaceImpl(PlaceParams.getParam(APPLICATION_ID));
        }

        protected abstract P getPlaceImpl(String applicationId);

        @Override
        public String getToken(P place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
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
        EndpointUsersPlace other = (EndpointUsersPlace) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        if (dataProvider == null) {
            dataProvider = new EndpointUsersPlaceDataProvider();
        }
        return dataProvider;
    }

    class EndpointUsersPlaceDataProvider extends TreePlaceDataProvider {

        @Override
        protected void loadData(LoadCallback callback,
                HasData<TreePlace> display) {
            List<TreePlace> result = new ArrayList<TreePlace>();
            result.add(new UpdateUserConfigPlace(applicationId));
            callback.onSuccess(result, display);
        }

    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new ApplicationPlace(applicationId);
    }
}
