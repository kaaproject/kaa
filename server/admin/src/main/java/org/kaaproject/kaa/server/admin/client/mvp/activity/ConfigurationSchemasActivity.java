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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.ConfigurationSchemasDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.MultiSelectionModel;

public class ConfigurationSchemasActivity extends AbstractListActivity<ConfigurationSchemaDto, ConfigurationSchemasPlace> {

    private String applicationId;

    public ConfigurationSchemasActivity(ConfigurationSchemasPlace place, ClientFactory clientFactory) {
        super(place, ConfigurationSchemaDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<ConfigurationSchemaDto> getView() {
        return clientFactory.getConfigurationSchemasView();
    }

    @Override
    protected AbstractDataProvider<ConfigurationSchemaDto> getDataProvider(
            MultiSelectionModel<ConfigurationSchemaDto> selectionModel) {
        return new ConfigurationSchemasDataProvider(selectionModel, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new ConfigurationSchemaPlace(applicationId, "");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new ConfigurationSchemaPlace(applicationId, id);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        callback.onSuccess((Void)null);
    }

}
