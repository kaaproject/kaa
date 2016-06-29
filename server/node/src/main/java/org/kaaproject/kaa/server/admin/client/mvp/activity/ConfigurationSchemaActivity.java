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

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.kaa.server.admin.services.schema.ConfigurationSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.shared.schema.ConfigurationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;

public class ConfigurationSchemaActivity extends
        AbstractBaseCtlSchemaActivity<ConfigurationSchemaDto, ConfigurationSchemaViewDto, BaseCtlSchemaView, ConfigurationSchemaPlace> {

    public ConfigurationSchemaActivity(ConfigurationSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }


    @Override
    protected ConfigurationSchemaViewDto newSchema() {
        return new ConfigurationSchemaViewDto();
    }


    @Override
    protected BaseCtlSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateConfigurationSchemaView();
        } else {
            return clientFactory.getConfigurationSchemaView();
        }
    }

    @Override
    protected void getEntity(String id, AsyncCallback<ConfigurationSchemaViewDto> callback) {
        KaaAdmin.getDataSource().getConfigurationSchemaView(id, callback);
    }


    @Override
    protected void editEntity(ConfigurationSchemaViewDto entity, AsyncCallback<ConfigurationSchemaViewDto> callback) {
        KaaAdmin.getDataSource().saveConfigurationSchemaView(entity, callback);
    }

    @Override
    protected ConfigurationSchemaPlace existingSchemaPlace(String applicationId, String schemaId) {
        return new ConfigurationSchemaPlace(applicationId, schemaId);
    }

    @Override
    protected void createEmptyCtlSchemaForm(AsyncCallback<CtlSchemaFormDto> callback) {
        KaaAdmin.getDataSource().createNewCTLSchemaFormInstance(null,
                null,
                applicationId,
                ConverterType.CONFIGURATION_FORM_AVRO_CONVERTER,
                callback);
    }

    @Override
    protected CtlSchemaPlace.SchemaType getPlaceSchemaType() {
        return CtlSchemaPlace.SchemaType.CONFIGURATION;
    }


    @Override
    public void loadFormData(String fileItemName,
            AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateConfigurationSchemaForm(fileItemName, callback);
    }
    
}
