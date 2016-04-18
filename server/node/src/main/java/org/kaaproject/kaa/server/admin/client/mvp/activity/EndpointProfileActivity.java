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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ServerProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TopicPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.EditSchemaRecordDialog;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.endpoint.EndpointProfileViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.servlet.ServletParams.ProfileType;

import com.google.common.io.BaseEncoding;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public class EndpointProfileActivity extends AbstractDetailsActivity<EndpointProfileViewDto, EndpointProfileView, EndpointProfilePlace> {

    public EndpointProfileActivity(EndpointProfilePlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);

        registrations.add(detailsView.getGroupsGrid().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> rowActionEvent) {
                String id = rowActionEvent.getClickedId();
                EndpointGroupPlace endpointGroupPlace = new EndpointGroupPlace(place.getApplicationId(), id, false, false);
                endpointGroupPlace.setPreviousPlace(place);
                goTo(endpointGroupPlace);
            }
        }));

        registrations.add(detailsView.getTopicsGrid().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> rowActionEvent) {
                String id = rowActionEvent.getClickedId();
                TopicPlace topicPlace = new TopicPlace(place.getApplicationId(), id);
                topicPlace.setPreviousPlace(place);
                goTo(topicPlace);
            }
        }));
    }

    @Override
    protected String getEntityId(EndpointProfilePlace place) {
        return place.getEndpointKeyHash();
    }

    @Override
    protected EndpointProfileView getView(boolean create) {
        return clientFactory.getEndpointProfileView();
    }

    @Override
    protected EndpointProfileViewDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {
        detailsView.getKeyHash().setValue(BaseEncoding.base64().encode(entity.getEndpointKeyHash()));

        if (entity.getUserId() != null) {
            detailsView.getUserID().setValue(entity.getUserId());
            detailsView.getUserExternalID().setValue(entity.getUserExternalId());
            for (Widget widget : detailsView.getUserInfoList()) {
                widget.setVisible(true);
            }
        } else {
            for (Widget widget : detailsView.getUserInfoList()) {
                widget.setVisible(false);
            }
        }

        final SdkProfileDto sdkDto = entity.getSdkProfileDto();
        if (sdkDto != null) {
            String sdkName = sdkDto.getName();
            detailsView.getSdkAnchor().setText((sdkName != null && !sdkName.isEmpty()) ? sdkName : sdkDto.getToken());
            registrations.add(detailsView.getSdkAnchor().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    SdkProfilePlace sdkProfilePlace = new SdkProfilePlace(place.getApplicationId(), sdkDto.getId());
                    sdkProfilePlace.setPreviousPlace(place);
                    goTo(sdkProfilePlace);
                }
            }));
        } else {
            detailsView.getSdkAnchor().setText("");
        }

        List<EndpointGroupDto> groupDtoList = entity.getEndpointGroups();
        if (groupDtoList != null) {
            detailsView.getGroupsGrid().getDataGrid().setRowData(groupDtoList);
        }

        List<TopicDto> endpointNotificationTopics = entity.getTopics();
        if (endpointNotificationTopics != null) {
            detailsView.getTopicsGrid().getDataGrid().setRowData(endpointNotificationTopics);
        } else {
            detailsView.getTopicsGrid().getDataGrid().setRowData(new ArrayList<TopicDto>());
        }

        detailsView.getEndpointProfSchemaName().setText(entity.getProfileSchemaName());
        registrations.add(detailsView.getEndpointProfSchemaName().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ProfileSchemaPlace endpointProfSchemaPlace = new ProfileSchemaPlace(place.getApplicationId(), entity
                        .getProfileSchemaVersion().getId());
                endpointProfSchemaPlace.setPreviousPlace(place);
                goTo(endpointProfSchemaPlace);
            }
        }));
        
        registrations.add(detailsView.getDownloadEndpointProfileJsonButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ServletHelper.downloadEndpointProfile(BaseEncoding.base64().encode(entity.getEndpointKeyHash()), ProfileType.CLIENT);
            }
        }));
        
        detailsView.getEndpointProfForm().setValue(entity.getProfileRecord());
        detailsView.getServerProfForm().setValue(entity.getServerProfileRecord());
        detailsView.getServerProfForm().setReadOnly(true);

        detailsView.getServerProfSchemaName().setText(entity.getServerProfileSchemaName());
        registrations.add(detailsView.getServerProfSchemaName().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ServerProfileSchemaPlace serverProfSchemaPlace = new ServerProfileSchemaPlace(place.getApplicationId(), entity
                        .getServerProfileSchemaVersion().getId());
                serverProfSchemaPlace.setPreviousPlace(place);
                goTo(serverProfSchemaPlace);
            }
        }));
        
        registrations.add(detailsView.getDownloadServerProfileJsonButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ServletHelper.downloadEndpointProfile(BaseEncoding.base64().encode(entity.getEndpointKeyHash()), ProfileType.SERVER);
            }
        }));

        registrations.add(detailsView.getEditServerProfileButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                KaaAdmin.getDataSource().getServerProfileSchemaInfosByEndpointKey(place.getEndpointKeyHash(),
                        new BusyAsyncCallback<List<SchemaInfoDto>>() {
                            @Override
                            public void onFailureImpl(Throwable caught) {
                                Utils.handleException(caught, detailsView);
                            }

                            @Override
                            public void onSuccessImpl(List<SchemaInfoDto> result) {
                                EditSchemaRecordDialog.Listener editSchemaListener = new EditSchemaRecordDialog.Listener() {

                                    @Override
                                    public void onSave(SchemaInfoDto newValue) {
                                        AsyncCallback<EndpointProfileDto> callback = new BusyAsyncCallback<EndpointProfileDto>() {

                                            @Override
                                            public void onFailureImpl(Throwable caught) {
                                                Utils.handleException(caught, detailsView);
                                            }

                                            @Override
                                            public void onSuccessImpl(EndpointProfileDto result) {
                                                reload();
                                            }
                                        };
                                        KaaAdmin.getDataSource().updateServerProfile(
                                                BaseEncoding.base64().encode(entity.getEndpointKeyHash()), newValue.getVersion(),
                                                newValue.getSchemaForm(), callback);
                                    }

                                    @Override
                                    public void onCancel() {
                                    }
                                };
                                EditSchemaRecordDialog.showEditSchemaRecordDialog(editSchemaListener, Utils.constants.editServerProfile(),
                                        result, entity.getServerProfileSchemaVersion().getVersion());
                            }
                        });
            }
        }));
    }

    @Override
    protected void onSave() {
    }

    @Override
    protected void getEntity(String id, final AsyncCallback<EndpointProfileViewDto> callback) {
        KaaAdmin.getDataSource().getEndpointProfileViewByKeyHash(id, callback);
    }

    @Override
    protected void editEntity(EndpointProfileViewDto entity, AsyncCallback<EndpointProfileViewDto> callback) {
    }
}
