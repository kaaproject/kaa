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

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.view.client.HasData;

public class ApplicationPlace extends TreePlace {

    private String applicationId;
    private String applicationName;

    private ApplicationDetailsPlaceDataProvider dataProvider;

    public ApplicationPlace(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setApplicationName(String name) {
        this.applicationName = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Prefix(value = "app")
    public static class Tokenizer implements PlaceTokenizer<ApplicationPlace>, PlaceConstants {

        @Override
        public ApplicationPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new ApplicationPlace(PlaceParams.getParam(APPLICATION_ID));
        }

        @Override
        public String getToken(ApplicationPlace place) {
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
        ApplicationPlace other = (ApplicationPlace) obj;
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
    public String getName() {
        return applicationName;
    }

    @Override
    public boolean isLeaf() {
        return !KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        if (dataProvider == null) {
            dataProvider = new ApplicationDetailsPlaceDataProvider();
        }
        return dataProvider;
    }

    class ApplicationDetailsPlaceDataProvider extends TreePlaceDataProvider {

        @Override
        protected void loadData(LoadCallback callback,
                HasData<TreePlace> display) {
            List<TreePlace> result = new ArrayList<TreePlace>();
            result.add(new SdkProfilesPlace(applicationId));
            result.add(new SchemasPlace(applicationId));
            result.add(new TopicsPlace(applicationId));
            result.add(new EndpointGroupsPlace(applicationId));
            result.add(new AefMapsPlace(applicationId));
            result.add(new LogAppendersPlace(applicationId));
            result.add(new UserVerifiersPlace(applicationId));
            result.add(new EndpointUsersPlace(applicationId));
            result.add(new EndpointProfilesPlace(applicationId));
            result.add(new ApplicationCtlSchemasPlace(applicationId));
            callback.onSuccess(result, display);
        }

    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new ApplicationsPlace();
    }
}
