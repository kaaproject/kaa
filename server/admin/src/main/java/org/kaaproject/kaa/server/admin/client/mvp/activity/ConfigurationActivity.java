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

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConfigurationActivity extends AbstractRecordActivity<ConfigurationRecordFormDto, RecordField, BaseRecordView<ConfigurationRecordFormDto, RecordField>, ConfigurationPlace> {

    public ConfigurationActivity(ConfigurationPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected BaseRecordView<ConfigurationRecordFormDto, RecordField> getRecordView(boolean create) {
        if (create) {
            return clientFactory.getCreateConfigurationView();
        } else {
            return clientFactory.getConfigurationView();
        }
    }

    @Override
    protected ConfigurationRecordFormDto newStruct() {
        return new ConfigurationRecordFormDto();
    }

    @Override
    protected void getRecord(String schemaId, String endpointGroupId,
            AsyncCallback<StructureRecordDto<ConfigurationRecordFormDto>> callback) {
        KaaAdmin.getDataSource().getConfigurationRecordForm(schemaId, endpointGroupId, callback);
    }

    @Override
    protected void getVacantSchemas(String endpointGroupId,
            AsyncCallback<List<SchemaDto>> callback) {
        KaaAdmin.getDataSource().getVacantConfigurationSchemas(endpointGroupId, callback);
    }

    @Override
    protected void editStruct(ConfigurationRecordFormDto entity,
            AsyncCallback<ConfigurationRecordFormDto> callback) {
        KaaAdmin.getDataSource().editConfigurationRecordForm(entity, callback);
    }

    @Override
    protected void activateStruct(String id,
            AsyncCallback<ConfigurationRecordFormDto> callback) {
        KaaAdmin.getDataSource().activateConfigurationRecordForm(id, callback);
    }

    @Override
    protected void deactivateStruct(String id,
            AsyncCallback<ConfigurationRecordFormDto> callback) {
        KaaAdmin.getDataSource().deactivateConfigurationRecordForm(id, callback);
    }

    @Override
    protected ConfigurationPlace getRecordPlaceImpl(String applicationId,
            String schemaId, String endpointGroupId, boolean create,
            boolean showActive, double random) {
        return new ConfigurationPlace(applicationId, schemaId, endpointGroupId, create, showActive, random);
    }

    @Override
    public String customizeErrorMessage(Throwable caught) {
        String message = caught.getMessage();
        if (message != null && message.contains("uuid")) {
            return Utils.messages.incorrectConfiguration();
        }
        return message;
    }

    @Override
    protected void updateBody(ConfigurationRecordFormDto struct,
            RecordField value) {
        struct.setConfigurationRecord(value);
    }

    @Override
    protected void copyBody(ConfigurationRecordFormDto activeStruct,
            ConfigurationRecordFormDto inactiveStruct) {
        inactiveStruct.setConfigurationRecord(activeStruct.getConfigurationRecord());
    }

}
