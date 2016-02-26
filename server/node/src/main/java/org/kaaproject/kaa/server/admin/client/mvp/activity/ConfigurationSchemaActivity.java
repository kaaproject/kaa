/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConfigurationSchemaActivity
        extends
        AbstractSchemaActivity<ConfigurationSchemaDto, BaseSchemaView, ConfigurationSchemaPlace> {

    public ConfigurationSchemaActivity(ConfigurationSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected ConfigurationSchemaDto newSchema() {
        return new ConfigurationSchemaDto();
    }

    @Override
    protected BaseSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateConfigurationSchemaView();
        } else {
            return clientFactory.getConfigurationSchemaView();
        }
    }

    @Override
    protected void getEntity(String id,
            AsyncCallback<ConfigurationSchemaDto> callback) {
        KaaAdmin.getDataSource().getConfigurationSchemaForm(id, callback);
    }

    @Override
    protected void editEntity(ConfigurationSchemaDto entity,
            AsyncCallback<ConfigurationSchemaDto> callback) {
        KaaAdmin.getDataSource().editConfigurationSchemaForm(entity, callback);
    }

    @Override
    protected void createEmptySchemaForm(AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().createConfigurationEmptySchemaForm(callback);
    }

    @Override
    public void loadFormData(String fileItemName,
            AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateConfigurationSchemaForm(fileItemName, callback);
    }
    
}
