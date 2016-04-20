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

import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace.SchemaType;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel.FormDataLoader;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.SchemaErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.BaseSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractBaseCtlSchemaActivity<S extends BaseSchemaDto, 
                                                   T extends BaseSchemaViewDto<S>, 
                                                   V extends BaseCtlSchemaView, 
                                                   P extends AbstractSchemaPlace> extends
                                                   AbstractDetailsActivity<T, V, P> implements ErrorMessageCustomizer, FormDataLoader {

    private static final ErrorMessageCustomizer schemaErrorMessageCustomizer = new SchemaErrorMessageCustomizer();
    
    protected String applicationId;

    public AbstractBaseCtlSchemaActivity(P place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    protected abstract T newSchema();
    
    protected abstract P existingSchemaPlace(String applicationId, String schemaId);
    
    protected abstract void createEmptyCtlSchemaForm(AsyncCallback<CtlSchemaFormDto> callback);

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
        if (create) {
            detailsView.getSchemaForm().setFormDataLoader(this);
        }
        super.bind(eventBus);
    }

    @Override
    protected void onEntityRetrieved() {
        if (create) {
            registrations.add(detailsView.getNewCtlButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    CtlSchemaPlace newCtlPlace = new CtlSchemaPlace("", null, CTLSchemaScopeDto.APPLICATION, place.getApplicationId(), true, true);
                    newCtlPlace.setSchemaType(getPlaceSchemaType());
                    newCtlPlace.setPreviousPlace(place);
                    canceled = true;
                    goTo(newCtlPlace);
                }
            }));
            KaaAdmin.getDataSource().getAvailableApplicationCTLSchemaReferences(applicationId, 
                  new BusyAsyncCallback<List<CtlSchemaReferenceDto>>() {
                      @Override
                      public void onFailureImpl(Throwable caught) {
                          Utils.handleException(caught, detailsView);
                      }
                      @Override
                      public void onSuccessImpl(List<CtlSchemaReferenceDto> result) {
                          detailsView.getCtlSchemaReference().setAcceptableValues(result);
                          bindDetailsView(true);
                      }
              });
              detailsView.getSchemaForm().setFormDataLoader(this);
        } else {
            bindDetailsView(false);
        }
    }
    
    protected abstract SchemaType getPlaceSchemaType();
    
    private void bindDetailsView(boolean fireChanged) {
        S schema = entity.getSchema();
        String version = schema.getVersion() + "";
        detailsView.getVersion().setValue(version);
        detailsView.getName().setValue(schema.getName());
        detailsView.getDescription().setValue(schema.getDescription());
        detailsView.getCreatedUsername().setValue(schema.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(schema.getCreatedTime()));
        if (entity.getCtlSchemaForm() != null) {
            detailsView.getSchemaForm().setValue(entity.getCtlSchemaForm().getSchema(), fireChanged);
        }
    }
    
    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        editEntity(entity,
                new BusyAsyncCallback<T>() {
                    public void onSuccessImpl(T result) {
                        if (!create) {                            
                            goTo(existingSchemaPlace(applicationId, result.getId()));
                        } else if (place.getPreviousPlace() != null) {
                            goTo(place.getPreviousPlace());
                        }
                    }

                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, detailsView);
                    }
        });
    }

    @Override
    protected void onSave() {
        S schema = entity.getSchema();
        schema.setName(detailsView.getName().getValue());
        schema.setDescription(detailsView.getDescription().getValue());
        if (create) {
            entity.setUseExistingCtlSchema(detailsView.useExistingCtlSchema());
            if (detailsView.useExistingCtlSchema()) {
                entity.setExistingMetaInfo(detailsView.getCtlSchemaReference().getValue());
            } 
        }
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
