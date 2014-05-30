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

import java.util.List;

import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConfigurationActivity extends AbstractRecordActivity<ConfigurationDto, BaseRecordView<ConfigurationDto>, ConfigurationPlace> {

    public ConfigurationActivity(ConfigurationPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected BaseRecordView<ConfigurationDto> getRecordView(boolean create) {
        if (create) {
            return clientFactory.getCreateConfigurationView();
        } else {
            return clientFactory.getConfigurationView();
        }
    }

    @Override
    protected ConfigurationDto newStruct() {
        return new ConfigurationDto();
    }

    @Override
    protected void getRecord(String schemaId, String endpointGroupId,
            AsyncCallback<StructureRecordDto<ConfigurationDto>> callback) {
        KaaAdmin.getDataSource().getConfigurationRecord(schemaId, endpointGroupId, callback);
    }

    @Override
    protected void getVacantSchemas(String endpointGroupId,
            AsyncCallback<List<SchemaDto>> callback) {
        KaaAdmin.getDataSource().getVacantConfigurationSchemas(endpointGroupId, callback);
    }

    @Override
    protected void editStruct(ConfigurationDto entity,
            AsyncCallback<ConfigurationDto> callback) {
        KaaAdmin.getDataSource().editConfiguration(entity, callback);
    }

    @Override
    protected void activateStruct(String id,
            AsyncCallback<ConfigurationDto> callback) {
        KaaAdmin.getDataSource().activateConfiguration(id, callback);
    }

    @Override
    protected void deactivateStruct(String id,
            AsyncCallback<ConfigurationDto> callback) {
        KaaAdmin.getDataSource().deactivateConfiguration(id, callback);
    }

    @Override
    protected ConfigurationPlace getRecordPlaceImpl(String applicationId,
            String schemaId, String endpointGroupId, boolean create,
            boolean showActive, double random) {
        return new ConfigurationPlace(applicationId, schemaId, endpointGroupId, create, showActive, random);
    }

}
