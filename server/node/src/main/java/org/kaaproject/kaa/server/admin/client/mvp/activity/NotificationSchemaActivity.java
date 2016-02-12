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
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.NotificationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class NotificationSchemaActivity
        extends
        AbstractSchemaActivity<NotificationSchemaDto, BaseSchemaView, NotificationSchemaPlace> {

    public NotificationSchemaActivity(NotificationSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected NotificationSchemaDto newSchema() {
        return new NotificationSchemaDto();
    }

    @Override
    protected BaseSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateNotificationSchemaView();
        } else {
            return clientFactory.getNotificationSchemaView();
        }
    }

    @Override
    protected void getEntity(String id,
            AsyncCallback<NotificationSchemaDto> callback) {
        KaaAdmin.getDataSource().getNotificationSchemaForm(id, callback);
    }

    @Override
    protected void editEntity(NotificationSchemaDto entity,
            AsyncCallback<NotificationSchemaDto> callback) {
        KaaAdmin.getDataSource().editNotificationSchemaForm(entity, callback);
    }

    @Override
    protected void createEmptySchemaForm(AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().createCommonEmptySchemaForm(callback);
    }

    @Override
    public void loadFormData(String fileItemName,
            AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateCommonSchemaForm(fileItemName, callback);
    }

}
