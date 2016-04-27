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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.ProfileSchemasDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;

public class ProfileSchemasActivity extends AbstractBaseCtlSchemasActivity<EndpointProfileSchemaDto, ProfileSchemasPlace> {

    private String applicationId;

    public ProfileSchemasActivity(ProfileSchemasPlace place, ClientFactory clientFactory) {
        super(place, EndpointProfileSchemaDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<EndpointProfileSchemaDto> getView() {
        return clientFactory.getProfileSchemasView();
    }

    @Override
    protected AbstractDataProvider<EndpointProfileSchemaDto, String> getDataProvider(
            AbstractGrid<EndpointProfileSchemaDto, String> dataGrid) {
        return new ProfileSchemasDataProvider(dataGrid, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new ProfileSchemaPlace(applicationId, "");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new ProfileSchemaPlace(applicationId, id);
    }

}
