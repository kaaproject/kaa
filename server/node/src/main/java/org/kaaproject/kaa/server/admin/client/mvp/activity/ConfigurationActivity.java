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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.ConfigurationView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConfigurationActivity extends AbstractRecordActivity<ConfigurationRecordFormDto, 
                                                        ConfigurationRecordViewDto, RecordField, 
                                                        ConfigurationView, ConfigurationPlace> {

    private String schemaId;
    
    public ConfigurationActivity(ConfigurationPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.schemaId = place.getSchemaId();
    }

    @Override
    protected ConfigurationView getRecordView(boolean create) {
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
    protected ConfigurationRecordViewDto newRecord() {
        return new ConfigurationRecordViewDto();
    }

    @Override
    protected void getRecord(String endpointGroupId,
            AsyncCallback<ConfigurationRecordViewDto> callback) {
        KaaAdmin.getDataSource().getConfigurationRecordView(schemaId, endpointGroupId, callback);
    }
    
    @Override
    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
        if (create) {
            registrations.add(recordView.getSchema().addValueChangeHandler(new ValueChangeHandler<VersionDto>() {
                @Override
                public void onValueChange(ValueChangeEvent<VersionDto> event) {
                    schemaSelected(event.getValue());
                }
            }));
        }
    }
    
    private void schemaSelected(VersionDto schema) {
        RecordField configurationRecord = ((SchemaInfoDto)schema).getSchemaForm();
        ConfigurationRecordFormDto inactiveStruct = record.getInactiveStructureDto();
        inactiveStruct.setConfigurationRecord(configurationRecord);
        recordView.getRecordPanel().setInactiveBodyValue(inactiveStruct);
    }
    
    @Override
    protected void onRecordRetrieved() {
        if (create) {
            getVacantSchemas(endpointGroupId, new BusyAsyncCallback<List<SchemaInfoDto>>() {
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, recordView);
                }

                @Override
                public void onSuccessImpl(List<SchemaInfoDto> result) {
                    VersionDto schema = Utils.getMaxSchemaVersions(result);
                    recordView.getSchema().setValue(schema);
                    List<VersionDto> values = new ArrayList<>();
                    values.addAll(result);
                    Collections.sort(values);
                    recordView.getSchema().setAcceptableValues(values);
                    recordView.getRecordPanel().setData(record);
                    schemaSelected(schema);
                    recordView.getRecordPanel().openDraft();
                }
            });
        } else {
            String version = record.getSchemaVersion() + "";
            recordView.getSchemaVersion().setValue(version);
            if (record.hasActive() && !record.hasDraft()) {
                ConfigurationRecordFormDto inactiveStruct = createInactiveStruct();
                inactiveStruct.setSchemaId(record.getSchemaId());
                inactiveStruct.setSchemaVersion(record.getSchemaVersion());
                inactiveStruct.setDescription(record.getDescription());
                inactiveStruct.setConfigurationRecord(record.getActiveStructureDto().getConfigurationRecord());
                record.setInactiveStructureDto(inactiveStruct);
            }
            recordView.getRecordPanel().setData(record);
            if (endpointGroup.getWeight()==0) {
                recordView.getRecordPanel().setReadOnly();
            }
            if (showActive && record.hasActive()) {
                recordView.getRecordPanel().openActive();
            } else {
                recordView.getRecordPanel().openDraft();
            }
        }
    }
    
    @Override
    protected void doSave(final EventBus eventBus) {
        ConfigurationRecordFormDto inactiveStruct = record.getInactiveStructureDto();
        if (create) {
            schemaId = recordView.getSchema().getValue().getId();
            inactiveStruct.setSchemaId(schemaId);
            inactiveStruct.setSchemaVersion(recordView.getSchema().getValue().getVersion());
        }
        inactiveStruct.setDescription(recordView.getRecordPanel().getDescription().getValue());
        inactiveStruct.setConfigurationRecord(recordView.getRecordPanel().getBody().getValue());
        editConfiguration(inactiveStruct,
                new BusyAsyncCallback<ConfigurationRecordFormDto>() {
                    public void onSuccessImpl(ConfigurationRecordFormDto result) {
                        goTo(getRecordPlace(applicationId, endpointGroupId, false, false, Math.random()));
                    }

                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, recordView, ConfigurationActivity.this);
                    }
        });
    }

    private void getVacantSchemas(String endpointGroupId,
            final AsyncCallback<List<SchemaInfoDto>> callback) {
        KaaAdmin.getDataSource().getVacantConfigurationSchemaInfos(endpointGroupId, callback);
    }

    private void editConfiguration(ConfigurationRecordFormDto entity,
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
            String endpointGroupId, boolean create, boolean showActive,
            double random) {
        return new ConfigurationPlace(applicationId, schemaId, endpointGroupId, create, showActive, random);
    }

    @Override
    public String customizeErrorMessage(Throwable caught) {
        String message = caught.getLocalizedMessage();
        if (message != null && message.contains("uuid")) {
            return Utils.messages.incorrectConfiguration();
        }
        return message;
    }

}
