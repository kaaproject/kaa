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

import java.util.Collections;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.SendNotificationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.SendNotificationView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SendNotificationActivity extends AbstractDetailsActivity<NotificationDto, SendNotificationView, SendNotificationPlace> {

    private String applicationId;
    private String topicId;

    public SendNotificationActivity(SendNotificationPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
        this.topicId = place.getTopicId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    @Override
    protected String getEntityId(SendNotificationPlace place) {
        return null;
    }

    @Override
    protected SendNotificationView getView(boolean create) {
        return clientFactory.getSendNotificationView();
    }

    @Override
    protected NotificationDto newEntity() {
        NotificationDto dto = new NotificationDto();
        dto.setApplicationId(applicationId);
        dto.setTopicId(topicId);
        dto.setType(NotificationTypeDto.USER);
        return dto;
    }

    @Override
    protected void onEntityRetrieved() {
        KaaAdmin.getDataSource().getUserNotificationSchemaInfosByApplicationId(applicationId, 
                new BusyAsyncCallback<List<SchemaInfoDto>>() {
            @Override
            public void onSuccessImpl(List<SchemaInfoDto> result) {
                Collections.sort(result);
                SchemaInfoDto schemaInfo = result.get(result.size()-1);
                detailsView.getNotificationSchemaInfo().setValue(schemaInfo, true);
                detailsView.getNotificationSchemaInfo().setAcceptableValues(result);
            }
            @Override
            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
        });
    }
 
    @Override
    protected void onSave() {
        entity.setSchemaId(detailsView.getNotificationSchemaInfo().getValue().getId());
        entity.setExpiredAt(detailsView.getExpiredAt().getValue());
    }

    @Override
    protected void getEntity(String id, AsyncCallback<NotificationDto> callback) {
        callback.onSuccess(null);
    }

    @Override
    protected void editEntity(NotificationDto entity, final AsyncCallback<NotificationDto> callback) {
        String endpointKeyHash = detailsView.getEndpointKeyHash().getValue();
        if(endpointKeyHash == null || endpointKeyHash.equals("")) {
            KaaAdmin.getDataSource().sendNotification(entity, detailsView.getNotificationData().getValue(),
                    new AsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            callback.onSuccess(null);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure(caught);
                        }
                    });
        } else {
            KaaAdmin.getDataSource().sendUnicastNotification(entity, endpointKeyHash, detailsView.getNotificationData().getValue(),
                    new AsyncCallback<EndpointNotificationDto>() {
                        @Override
                        public void onSuccess(EndpointNotificationDto result) {
                            callback.onSuccess(null);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure(caught);
                        }
                    });
        }
    }

}
