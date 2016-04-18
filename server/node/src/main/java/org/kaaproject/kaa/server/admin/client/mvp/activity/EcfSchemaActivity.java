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

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel.FormDataLoader;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EcfSchemaActivity
        extends
        AbstractDetailsActivity<EventClassFamilyDto, EcfSchemaView, EcfSchemaPlace> implements FormDataLoader {

    public EcfSchemaActivity(EcfSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
    }

    @Override
    protected String getEntityId(EcfSchemaPlace place) {
        if (place.getVersion() > -1) {
            return place.getEcfId();
        } else {
            return null;
        }
    }

    @Override
    protected EcfSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateEcfSchemaView();
        } else {
            return clientFactory.getEcfSchemaView();
        }
    }

    @Override
    protected EventClassFamilyDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {
        if (create) {
            KaaAdmin.getDataSource().createEcfEmptySchemaForm(new BusyAsyncCallback<RecordField>() {
                @Override
                public void onSuccessImpl(RecordField result) {
                    detailsView.getEcfSchemaForm().setValue(result);
                }
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }
            });
            detailsView.getEcfSchemaForm().setFormDataLoader(this);
        } else {
            EventSchemaVersionDto schema = null;
            for (EventSchemaVersionDto schemaVersion : entity.getSchemas()) {
                if (schemaVersion.getVersion()==place.getVersion()) {
                    schema = schemaVersion;
                    break;
                }
            }
            detailsView.getVersion().setValue(""+schema.getVersion());
            detailsView.getCreatedUsername().setValue(schema.getCreatedUsername());
            detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(schema.getCreatedTime()));
            detailsView.getEcfSchemaForm().setValue(schema.getSchemaForm());
        }
    }

    @Override
    protected void onSave() {
    }

    @Override
    protected void getEntity(String id, AsyncCallback<EventClassFamilyDto> callback) {
        KaaAdmin.getDataSource().getEcf(id, callback);
    }

    @Override
    protected void editEntity(EventClassFamilyDto entity,
            final AsyncCallback<EventClassFamilyDto> callback) {
        KaaAdmin.getDataSource().addEcfSchema(place.getEcfId(), detailsView.getEcfSchemaForm().getValue(), 
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        callback.onSuccess(null);
                    }
        });
    }

    @Override
    public void loadFormData(String fileItemName,
            AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateEcfSchemaForm(fileItemName, callback);
    }
 
}
