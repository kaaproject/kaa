/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ServerProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;

public class ServerProfileSchemaActivity extends
        AbstractServerProfileSchemaActivity<ServerProfileSchemaDto, BaseSchemaView, ServerProfileSchemaPlace> {

    public ServerProfileSchemaActivity(ServerProfileSchemaPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected ServerProfileSchemaDto newSchema() {
        RecordField rec = new RecordField();
        CTLSchemaDto ctlDto = new CTLSchemaDto();
        ServerProfileSchemaDto serverProfileSchemaDto = new ServerProfileSchemaDto();
        serverProfileSchemaDto.setSchemaDto(ctlDto);
        serverProfileSchemaDto.setSchemaForm(rec);
        return serverProfileSchemaDto;
    }

    @Override
    protected BaseSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateServerProfileSchemaView();
        } else {
            return clientFactory.getServerProfileSchemaView();
        }
    }

    @Override
    protected void getEntity(String id,
                             AsyncCallback<ServerProfileSchemaDto> callback) {
        KaaAdmin.getDataSource().getServerProfileSchemaForm(id, callback);
    }

    @Override
    protected void editEntity(ServerProfileSchemaDto entity,
                              AsyncCallback<ServerProfileSchemaDto> callback) {
        KaaAdmin.getDataSource().editServerProfileSchemaForm(entity, callback);
    }

    @Override
    protected void createEmptySchemaForm(AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().createSimpleEmptySchemaForm(callback);
    }

    @Override
    public void loadFormData(String fileItemName,
                             AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateSimpleSchemaForm(fileItemName, callback);
    }
}
