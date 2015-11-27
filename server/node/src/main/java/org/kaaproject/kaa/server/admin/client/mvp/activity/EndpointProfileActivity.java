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

import com.google.common.io.BaseEncoding;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileRecordFieldDto;
import org.kaaproject.kaa.common.dto.EndpointProfileViewDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ServerProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;

import java.util.ArrayList;
import java.util.List;

public class EndpointProfileActivity extends
        AbstractDetailsActivity<EndpointProfileViewDto, EndpointProfileView, EndpointProfilePlace> {

    public EndpointProfileActivity(EndpointProfilePlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
        BusyPopup.hidePopup();
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
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

        detailsView.reset();

        final EndpointProfileDto profileDto = entity.getEndpointProfileDto();
        EndpointUserDto userDto = entity.getEndpointUserDto();
        final ProfileSchemaDto profileSchemaDto = entity.getProfileSchemaDto();
        final ServerProfileSchemaDto serverProfileSchemaDto = entity.getServerProfileSchemaDto();

        detailsView.getKeyHash().setValue(BaseEncoding.base64().encode(profileDto.getEndpointKeyHash()));

        if (userDto != null) {
            detailsView.getUserID().setValue(userDto.getId());
            detailsView.getUserExternalID().setValue(userDto.getExternalId());

            for (Widget widget : detailsView.getUserInfoList()) {
                widget.setVisible(true);
            }
        } else {
            for (Widget widget : detailsView.getUserInfoList()) {
                widget.setVisible(false);
            }
        }

        List<EndpointGroupDto> groupDtoList = entity.getGroupDtoList();
        if (groupDtoList != null) {
            detailsView.getGroupsGrid().getDataGrid().setRowData(groupDtoList);
        }

        List<TopicDto> endpointNotificationTopics = entity.getEndpointNotificationTopics();
        if (endpointNotificationTopics != null) {
            detailsView.getTopicsGrid().getDataGrid().setRowData(endpointNotificationTopics);
        } else detailsView.getTopicsGrid().getDataGrid().setRowData(new ArrayList<TopicDto>());

        detailsView.getEndpointProfSchemaName().setText(profileSchemaDto.getName());
        registrations.add(detailsView.getEndpointProfSchemaName().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ProfileSchemaPlace endpointProfSchemaPlace = new ProfileSchemaPlace(place.getApplicationId(),
                        profileSchemaDto.getId());
                endpointProfSchemaPlace.setPreviousPlace(place);
                goTo(endpointProfSchemaPlace);
            }
        }));

        final RecordField endpointProfileRecord = entity.getEndpointProfileRecord();
        if (endpointProfileRecord != null) {
            detailsView.getEndpointProfForm().reset();
            detailsView.getEndpointProfForm().setValue(endpointProfileRecord);
        }


        registrations.add(detailsView.getDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                deleteItem(profileDto);
            }
        }));

        RecordField serverProfileRecord = entity.getServerProfileRecord();
        if (serverProfileRecord != null) {
            detailsView.getAddButton().setEnabled(false);
            detailsView.getEditButton().setEnabled(true);
            detailsView.getDeleteButton().setEnabled(true);

            detailsView.getServerProfForm().reset();
            detailsView.getServerProfForm().setValue(serverProfileRecord);
            detailsView.getServerProfForm().setReadOnly(true);
            detailsView.getServerProfRecord().clear();
            detailsView.getServerProfRecord().setValue(serverProfileRecord);
        } else {
            detailsView.getAddButton().setEnabled(true);
            detailsView.getDeleteButton().setEnabled(false);
            detailsView.getEditButton().setEnabled(false);
        }

        registrations.add(detailsView.getServerProfForm().addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> valueChangeEvent) {
                updateButtonsState();
            }
        }));

        registrations.add(detailsView.getServerProfRecord().addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> valueChangeEvent) {
                updateSaveProfileButtonState();
            }
        }));

        if (serverProfileSchemaDto != null) {
            String serverProfName = serverProfileSchemaDto.getSchemaDto().getName();
            detailsView.getServerProfSchemaName().setText(
                    serverProfName != null ? serverProfName : "");
            registrations.add(detailsView.getServerProfSchemaName().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    ServerProfileSchemaPlace serverProfSchemaPlace = new ServerProfileSchemaPlace(place.getApplicationId(),
                            serverProfileSchemaDto.getId());
                    serverProfSchemaPlace.setPreviousPlace(place);
                    goTo(serverProfSchemaPlace);
                }
            }));
        }

        KaaAdmin.getDataSource().loadServerProfileSchemas(place.getApplicationId(),
                new AsyncCallback<List<ServerProfileSchemaDto>>() {
            @Override
            public void onFailure(Throwable throwable) {
                org.kaaproject.kaa.server.admin.client.util.Utils.handleException(throwable, detailsView);
            }

            @Override
            public void onSuccess(List<ServerProfileSchemaDto> result) {
                detailsView.getServerSchemasListBox().setAcceptableValues(result);
                if (serverProfileSchemaDto != null && serverProfileSchemaDto.getId() != null) {
                    for (ServerProfileSchemaDto dto : result) {
                        if (serverProfileSchemaDto.getId().equals(dto.getId())) {
                            detailsView.getServerSchemasListBox().setValue(dto);
                        }
                    }
                } else {
                    detailsView.getServerSchemasListBox().setValue(result.isEmpty()? null : result.get(0));
                }
            }
        });

        registrations.add(detailsView.getServerSchemasListBox().addValueChangeHandler(new ValueChangeHandler<ServerProfileSchemaDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<ServerProfileSchemaDto> valueChangeEvent) {
                String schema = valueChangeEvent.getValue().getSchemaDto().getBody();
                KaaAdmin.getDataSource().generateRecordFromSchemaJson(schema, new AsyncCallback<RecordField>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        org.kaaproject.kaa.server.admin.client.util.Utils.handleException(throwable, detailsView);
                    }

                    @Override
                    public void onSuccess(RecordField recordField) {
                        detailsView.getServerProfRecord().setValue(recordField);
                    }
                });
            }
        }));

        registrations.add(detailsView.getSaveProfileButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                EndpointProfileRecordFieldDto endpointProfileRecordFieldDto = new EndpointProfileRecordFieldDto();
                final RecordField value = detailsView.getServerProfRecord().getValue();
                String serverSchemaId = detailsView.getServerSchemasListBox().getValue().getId();
                final String serverSchemaName = detailsView.getServerSchemasListBox().getValue().getSchemaDto().getName();
                profileDto.setServerProfileSchemaId(serverSchemaId);
                endpointProfileRecordFieldDto.setProfileRecord(value);
                endpointProfileRecordFieldDto.setProfileDto(profileDto);

                KaaAdmin.getDataSource().updateEndpointProfile(endpointProfileRecordFieldDto,
                        new AsyncCallback<EndpointProfileRecordFieldDto>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        org.kaaproject.kaa.server.admin.client.util.Utils.handleException(throwable, detailsView);
                    }

                    @Override
                    public void onSuccess(EndpointProfileRecordFieldDto endpointProfileDto) {
                        detailsView.getServerProfForm().reset();
                        detailsView.getServerProfForm().setValue(value);
                        detailsView.getServerProfSchemaName().setText(serverSchemaName);
                        detailsView.getAddButton().setEnabled(false);
                        detailsView.getEditButton().setEnabled(true);
                        detailsView.getDeleteButton().setEnabled(true);
                    }
                });

            }
        }));
    }

    private void updateButtonsState(){
        detailsView.getAddButton().setEnabled(detailsView.getServerProfForm().getValue() == null);
        detailsView.getDeleteButton().setEnabled(detailsView.getServerProfForm().getValue() != null);
        detailsView.getEditButton().setEnabled(detailsView.getServerProfForm().getValue() != null);
    }

    private void updateSaveProfileButtonState(){
        detailsView.getSaveProfileButton().setEnabled(detailsView.getServerProfForm().getValue().isValid());
    }

    private void deleteItem(final EndpointProfileDto profileDto) {
        ConfirmDialog.ConfirmListener listener = new ConfirmDialog.ConfirmListener() {
            public void onNo() {}

            public void onYes() {
                EndpointProfileRecordFieldDto endpointProfileRecordFieldDto = new EndpointProfileRecordFieldDto();
                profileDto.setServerProfileSchemaId(null);
                endpointProfileRecordFieldDto.setProfileRecord(null);
                endpointProfileRecordFieldDto.setProfileDto(profileDto);

                KaaAdmin.getDataSource().updateEndpointProfile(endpointProfileRecordFieldDto,
                        new AsyncCallback<EndpointProfileRecordFieldDto>() {
                            @Override
                            public void onFailure(Throwable throwable) {
                                org.kaaproject.kaa.server.admin.client.util.Utils.handleException(throwable, detailsView);
                            }

                            @Override
                            public void onSuccess(EndpointProfileRecordFieldDto endpointProfileDto) {
                                detailsView.getServerProfForm().reset();
                                detailsView.getServerProfForm().setValue(null);
                                detailsView.getServerProfSchemaName().setText("");
                                detailsView.getAddButton().setEnabled(true);
                                detailsView.getDeleteButton().setEnabled(false);
                                detailsView.getEditButton().setEnabled(false);
                            }
                        });
            }
        };
        String question = org.kaaproject.avro.ui.gwt.client.util.Utils.messages.deleteSelectedEntryQuestion();
        String title = org.kaaproject.avro.ui.gwt.client.util.Utils.messages.deleteSelectedEntryTitle();
        ConfirmDialog dialog = new ConfirmDialog(listener, title, question);
        dialog.center();
        dialog.show();
    }

    @Override
    protected void onSave() {}

    @Override
    protected void getEntity(String id, final AsyncCallback<EndpointProfileViewDto> callback) {
        KaaAdmin.getDataSource().getEndpointProfileViewDtoByEndpointProfileKeyHash(id, callback);
    }

    @Override
    protected void editEntity(EndpointProfileViewDto entity, AsyncCallback<EndpointProfileViewDto> callback) {}
}
