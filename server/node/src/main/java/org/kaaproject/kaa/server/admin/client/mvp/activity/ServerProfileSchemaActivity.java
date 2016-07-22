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

import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace.SchemaType;
import org.kaaproject.kaa.server.admin.client.mvp.place.ServerProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ServerProfileSchemaActivity
        extends
        AbstractBaseCtlSchemaActivity<ServerProfileSchemaDto, ServerProfileSchemaViewDto, BaseCtlSchemaView, ServerProfileSchemaPlace> {

    public ServerProfileSchemaActivity(ServerProfileSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected ServerProfileSchemaViewDto newSchema() {
        return new ServerProfileSchemaViewDto();
    }

    @Override
    protected BaseCtlSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateServerProfileSchemaView();
        } else {
            return clientFactory.getServerProfileSchemaView();
        }
    }

    @Override
    protected void getEntity(String id,
            AsyncCallback<ServerProfileSchemaViewDto> callback) {
        KaaAdmin.getDataSource().getServerProfileSchemaView(id, callback);
    }

    @Override
    protected void editEntity(ServerProfileSchemaViewDto entity,
            AsyncCallback<ServerProfileSchemaViewDto> callback) {
        KaaAdmin.getDataSource().saveServerProfileSchemaView(entity, callback);
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
        KaaAdmin.getDataSource().generateCommonSchemaForm(fileItemName, callback);
    }

    @Override
    protected ServerProfileSchemaPlace existingSchemaPlace(
            String applicationId, String schemaId) {
        return new ServerProfileSchemaPlace(applicationId, schemaId);
    }

    @Override
    protected SchemaType getPlaceSchemaType() {
        return SchemaType.SERVER_PROFILE;
    }

}
