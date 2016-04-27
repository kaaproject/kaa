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
import org.kaaproject.kaa.common.dto.AbstractSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel.FormDataLoader;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.SchemaErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractSchemaActivity<T extends AbstractSchemaDto, V extends BaseSchemaView, P extends AbstractSchemaPlace> extends
    AbstractDetailsActivity<T, V, P> implements ErrorMessageCustomizer, FormDataLoader {

    private static final ErrorMessageCustomizer schemaErrorMessageCustomizer = new SchemaErrorMessageCustomizer();
    
    protected String applicationId;

    public AbstractSchemaActivity(P place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    protected abstract T newSchema();
    
    protected abstract void createEmptySchemaForm(AsyncCallback<RecordField> callback);

    @Override
    protected String getEntityId(P place) {
        return place.getSchemaId();
    }

    @Override
    protected T newEntity() {
        T schema = newSchema();
        schema.setApplicationId(applicationId);
        return schema;
    }

    @Override
    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
    }

    @Override
    protected void onEntityRetrieved() {
        String version = entity.getVersion() + "";
        detailsView.getVersion().setValue(version);
        detailsView.getName().setValue(entity.getName());
        detailsView.getDescription().setValue(entity.getDescription());
        detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
        detailsView.getEndpointCount().setValue(entity.getEndpointCount()+"");
        if (create) {
            createEmptySchemaForm(new BusyAsyncCallback<RecordField>() {
                @Override
                public void onSuccessImpl(RecordField result) {
                    detailsView.getSchemaForm().setValue(result);
                }
                
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }
            });
            detailsView.getSchemaForm().setFormDataLoader(this);
        } else {
            detailsView.getSchemaForm().setValue(entity.getSchemaForm());
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setDescription(detailsView.getDescription().getValue());
        entity.setSchemaForm(detailsView.getSchemaForm().getValue());
    }

    @Override
    public String customizeErrorMessage(Throwable caught) {
        String errorMessage = schemaErrorMessageCustomizer.customizeErrorMessage(caught);
        if (errorMessage == null) {
            errorMessage = "Incorrect schema: Please validate your schema.";
        }
        return errorMessage;
    }

}
