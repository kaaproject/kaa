/*
 * Copyright 2014 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.AbstractSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

public abstract class AbstractSchemaActivity<T extends AbstractSchemaDto, V extends BaseSchemaView, P extends AbstractSchemaPlace> extends
    AbstractDetailsActivity<T, V, P> implements ErrorMessageCustomizer {

    private static final String  LEFT_SQUARE_BRACKET = "[";
    private static final String  RIGHT_SQUARE_BRACKET = "]";
    private static final String  SEMICOLON = ";";
    
    protected String applicationId;
    protected String fileItemName;

    public AbstractSchemaActivity(P place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    protected abstract T newSchema();

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
            fileItemName = detailsView.getSchemaFileUpload().getFileItemName();
            registrations.add(detailsView.getSchemaFileUpload().addSubmitCompleteHandler(new SubmitCompleteHandler() {
                @Override
                public void onSubmitComplete(SubmitCompleteEvent event) {
                    editEntity(entity,
                        new AsyncCallback<T>() {
                            @Override
                            public void onSuccess(T result) {
                                goTo(place.getPreviousPlace());
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                Utils.handleException(caught, detailsView, AbstractSchemaActivity.this);
                            }
                    });
                }
            }));
        }
        super.bind(eventBus);
    }

    @Override
    protected void onEntityRetrieved() {
        String version = entity.getMajorVersion() + "." + entity.getMinorVersion();
        detailsView.getVersion().setValue(version);
        detailsView.getName().setValue(entity.getName());
        detailsView.getDescription().setValue(entity.getDescription());
        detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
        detailsView.getEndpointCount().setValue(entity.getEndpointCount()+"");
        if (!create) {
            detailsView.getSchema().setValue(entity.getSchema());
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setDescription(detailsView.getDescription().getValue());

    }

    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();
        if (create) {
            detailsView.getSchemaFileUpload().submit();
        }
        else {
            editEntity(entity,
                    new AsyncCallback<T>() {
                        @Override
                        public void onSuccess(T result) {
                            goTo(place.getPreviousPlace());
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Utils.handleException(caught, detailsView);
                        }
            });
        }
    }
    
    @Override
    public String customizeErrorMessage(Throwable caught) {
        String errorMessage = caught.getMessage();        
        int leftSquareBracketIndex = errorMessage.indexOf(LEFT_SQUARE_BRACKET);
        int rightSquareBracketIndex = -1;
        if (leftSquareBracketIndex != -1) {
            rightSquareBracketIndex = errorMessage.indexOf(RIGHT_SQUARE_BRACKET, leftSquareBracketIndex);
        }        
        if (rightSquareBracketIndex != -1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Incorrect json schema: Please check your schema at");
            String[] array = errorMessage.substring(leftSquareBracketIndex, rightSquareBracketIndex).split(SEMICOLON);
            if (array != null && array.length == 2) {
                builder.append(array[1]);
                errorMessage = builder.toString();
            }
        } else {
            return "Incorrect schema: Please validate your schema.";
        }
        return errorMessage;
    }

}
