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


import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EventClassPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

public class EventClassActivity
        extends AbstractBaseCtlSchemaActivity<EventClassDto, EventClassViewDto, BaseCtlSchemaView, EventClassPlace>{


    public EventClassActivity(EventClassPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected EventClassViewDto newSchema() {
        return new EventClassViewDto();
    }

    @Override
    protected BaseCtlSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateEventClassView();
        } else {
            return clientFactory.getEventClassView();
        }
    }

    @Override
    protected void getEntity(String id,
                             AsyncCallback<EventClassViewDto> callback) {
        //KaaAdmin.getDataSource().getNotificationSchemaView(id, callback);
    }

    @Override
    protected void editEntity(EventClassViewDto entity,
                              AsyncCallback<EventClassViewDto> callback) {
        //KaaAdmin.getDataSource().saveNotificationSchemaView(entity, callback);
    }

    @Override
    protected void createEmptyCtlSchemaForm(AsyncCallback<CtlSchemaFormDto> callback) {
//        KaaAdmin.getDataSource().createNewCTLSchemaFormInstance(null,
//                null,
//                applicationId,
//                callback);
    }

    @Override
    public void loadFormData(String fileItemName,
                             AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateCommonSchemaForm(fileItemName, callback);
    }

    @Override
    protected EventClassPlace existingSchemaPlace(String ec, String schemaId) {
        return null;
    }

    @Override
    protected CtlSchemaPlace.SchemaType getPlaceSchemaType() {
        return CtlSchemaPlace.SchemaType.NOTIFICATION;
    }
}
