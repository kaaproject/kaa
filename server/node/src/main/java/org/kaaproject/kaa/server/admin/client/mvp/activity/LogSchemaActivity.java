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
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.LogSchemaViewDto;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace.SchemaType;

public class LogSchemaActivity
        extends
        AbstractBaseCtlSchemaActivity<LogSchemaDto, LogSchemaViewDto, BaseCtlSchemaView, LogSchemaPlace> {

    public LogSchemaActivity(LogSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected LogSchemaViewDto newSchema() {
        return new LogSchemaViewDto();
    }

    @Override
    protected BaseCtlSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateLogSchemaView();
        } else {
            return clientFactory.getLogSchemaView();
        }
    }

    @Override
    protected void getEntity(String id, AsyncCallback<LogSchemaViewDto> callback) {
        KaaAdmin.getDataSource().getLogSchemaView(id, callback);
    }

    @Override
    protected void editEntity(LogSchemaViewDto entity, AsyncCallback<LogSchemaViewDto> callback) {
        KaaAdmin.getDataSource().saveLogSchemaView(entity, callback);
    }

    @Override
    protected void createEmptyCtlSchemaForm(AsyncCallback<CtlSchemaFormDto> callback) {
        KaaAdmin.getDataSource().createNewCTLSchemaFormInstance(null,
                null,
                applicationId,
                ConverterType.FORM_AVRO_CONVERTER,
                callback);
    }

    @Override
    public void loadFormData(String fileItemName,
                             AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateSimpleSchemaForm(fileItemName, callback);
    }

    @Override
    protected LogSchemaPlace existingSchemaPlace(String applicationId, String schemaId) {
        return new LogSchemaPlace(applicationId, schemaId);
    }

    @Override
    protected SchemaType getPlaceSchemaType() {
        return SchemaType.LOG_SCHEMA ;
    }


}
