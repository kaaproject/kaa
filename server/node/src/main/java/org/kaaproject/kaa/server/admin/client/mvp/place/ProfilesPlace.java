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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.view.client.HasData;
import com.google.web.bindery.event.shared.EventBus;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProfilesPlace extends TreePlace{

    protected String applicationId;
    private ProfilesPlaceDataProvider dataProvider;

    public ProfilesPlace(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;

    }

    @Override
    public String getName() {
        return Utils.constants.profile() + "s";
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new SchemasPlace(applicationId);
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        if (dataProvider == null) {
            dataProvider = new ProfilesPlaceDataProvider();
        }
        return dataProvider;
    }

    class ProfilesPlaceDataProvider extends TreePlaceDataProvider {

        @Override
        protected void loadData(LoadCallback callback, HasData<TreePlace> display) {
            List<TreePlace> result = new ArrayList<TreePlace>();
            result.add(new ProfileSchemasPlace(applicationId));
            result.add(new ServerProfileSchemasPlace(applicationId));
            callback.onSuccess(result, display);
        }
    }

    public static abstract class Tokenizer<P extends ProfilesPlace> implements PlaceTokenizer<P>, PlaceConstants {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfilesPlace that = (ProfilesPlace) o;

        return !(applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null);

    }

    @Override
    public int hashCode() {
        return applicationId != null ? applicationId.hashCode() : 0;
    }
}
