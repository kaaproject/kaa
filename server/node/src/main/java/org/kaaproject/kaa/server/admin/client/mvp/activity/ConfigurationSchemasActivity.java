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
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey.RecordFiles;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.ConfigurationSchemasDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConfigurationSchemasActivity extends AbstractBaseCtlSchemasActivity<ConfigurationSchemaDto, ConfigurationSchemasPlace> {

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
    protected AbstractDataProvider<ConfigurationSchemaDto, String> getDataProvider(
            AbstractGrid<ConfigurationSchemaDto, String> dataGrid) {
        return new ConfigurationSchemasDataProvider(dataGrid, listView, applicationId);
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

    @Override
    protected void onCustomRowAction(RowActionEvent<String> event) {
        super.onCustomRowAction(event);
        Integer schemaVersion = Integer.valueOf(event.getClickedId());
        final int action = event.getAction();
        AsyncCallback<String> callback = new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }
            @Override
            public void onSuccess(String key) {
                ServletHelper.downloadRecordLibrary(key);
            }
        };

        switch (action) {
            case KaaRowAction.DOWNLOAD_SCHEMA:
                KaaAdmin.getDataSource().getRecordData(applicationId, schemaVersion, RecordFiles.CONFIGURATION_SCHEMA, callback);
                break;
            case KaaRowAction.DOWNLOAD_BASE_SCHEMA:
                KaaAdmin.getDataSource().getRecordData(applicationId, schemaVersion, RecordFiles.CONFIGURATION_BASE_SCHEMA, callback);
                break;
            case KaaRowAction.DOWNLOAD_OVERRIDE_SCHEMA:
                KaaAdmin.getDataSource().getRecordData(applicationId, schemaVersion, RecordFiles.CONFIGURATION_OVERRIDE_SCHEMA, callback);
                break;
            default:
                break;
        }
    }
}
