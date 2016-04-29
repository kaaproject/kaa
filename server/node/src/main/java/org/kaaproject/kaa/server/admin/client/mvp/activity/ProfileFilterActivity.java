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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileFilterPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.ProfileFilterView;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.TestProfileFilterDialog;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.VersionListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProfileFilterActivity extends AbstractRecordActivity<ProfileFilterDto, 
                                           ProfileFilterRecordDto, String, 
                                           ProfileFilterView, ProfileFilterPlace> {
    
    private String endpointProfileSchemaId;
    private String serverProfileSchemaId;
    
    private List<ProfileVersionPairDto> profileVersionPairs;

    public ProfileFilterActivity(ProfileFilterPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.endpointProfileSchemaId = place.getEndpointProfileSchemaId();
        this.serverProfileSchemaId = place.getServerProfileSchemaId();
    }

    @Override
    protected ProfileFilterView getRecordView(boolean create) {
        if (create) {
            return clientFactory.getCreateProfileFilterView();
        } else {
            return clientFactory.getProfileFilterView();
        }
    }

    @Override
    protected ProfileFilterDto newStruct() {
        return new ProfileFilterDto();
    }
    
    @Override
    protected ProfileFilterRecordDto newRecord() {
        return new ProfileFilterRecordDto();
    }

    @Override
    protected void getRecord(String endpointGroupId,
            AsyncCallback<ProfileFilterRecordDto> callback) {
        KaaAdmin.getDataSource().getProfileFilterRecord(endpointProfileSchemaId, 
                serverProfileSchemaId, endpointGroupId, callback);
    }
    
    @Override
    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
        if (create) {
            registrations.add(recordView.getEndpointProfileSchema().addValueChangeHandler(new ValueChangeHandler<VersionDto>() {
                @Override
                public void onValueChange(ValueChangeEvent<VersionDto> event) {
                    List<VersionDto> newValues = extractServerProfileVersions(event.getValue());
                    updateValues(recordView.getServerProfileSchema(), newValues);
                }
            }));
            registrations.add(recordView.getServerProfileSchema().addValueChangeHandler(new ValueChangeHandler<VersionDto>() {
                @Override
                public void onValueChange(ValueChangeEvent<VersionDto> event) {
                    List<VersionDto> newValues = extractEndpointProfileVersions(event.getValue());
                    updateValues(recordView.getEndpointProfileSchema(), newValues);
                }
            }));
        }
        
        registrations.add(recordView.getTestFilterButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String profileSchemaId;
                String serverProfileSchemaId;
                if (create) {
                    profileSchemaId =  recordView.getEndpointProfileSchema().getValue().getId();
                    serverProfileSchemaId = recordView.getServerProfileSchema().getValue().getId();
                } else {
                    profileSchemaId = record.getEndpointProfileSchemaId();
                    serverProfileSchemaId = record.getServerProfileSchemaId();
                }
                String filterBody = recordView.getRecordPanel().getBody().getValue();
                
                TestProfileFilterDialog.showTestProfileFilterDialog(
                        new TestProfileFilterDialog.TestProfileFilterDialogListener() {
                            @Override
                            public void onClose(String filterBody) {
                                recordView.getRecordPanel().getBody().setValue(filterBody, true);
                            }
                        },                        
                        profileSchemaId, serverProfileSchemaId, filterBody);
            }
        }));
    }
    
    private void updateValues(VersionListBox box, List<VersionDto> newValues) {
        VersionDto valueToSet = box.getValue();
        if (!newValues.contains(valueToSet)) {
            valueToSet = Utils.getMaxSchemaVersions(newValues);
        }
        box.setValue(valueToSet);
        box.setAcceptableValues(newValues);
    }
    
    private List<VersionDto> extractEndpointProfileVersions(VersionDto serverProfileVersion) {
        Set<VersionDto> endpointProfileVersions = new HashSet<>();
        if (profileVersionPairs != null) {
            for (ProfileVersionPairDto profileVersionPair : profileVersionPairs) {
                if (serverProfileVersion == null || 
                        profileVersionPair.getServerProfileSchemaVersion() == null ||
                        profileVersionPair.getServerProfileSchemaVersion().intValue() == serverProfileVersion.getVersion()) {
                    String schemaId = profileVersionPair.getEndpointProfileSchemaid();
                    int version = profileVersionPair.getEndpointProfileSchemaVersion() != null ? 
                            profileVersionPair.getEndpointProfileSchemaVersion().intValue() : -1;
                    VersionDto endpointProfileVersion = new VersionDto(schemaId, version);
                    endpointProfileVersions.add(endpointProfileVersion);
                }
            }
        }
        List<VersionDto> endpointProfileVersionsList = new ArrayList<>(endpointProfileVersions);
        Collections.sort(endpointProfileVersionsList);
        return endpointProfileVersionsList;
    }
    
    private List<VersionDto> extractServerProfileVersions(VersionDto endpointProfileVersion) {
        Set<VersionDto> serverProfileVersions = new HashSet<>();
        if (profileVersionPairs != null) {
            for (ProfileVersionPairDto profileVersionPair : profileVersionPairs) {
                if (endpointProfileVersion == null || 
                        profileVersionPair.getEndpointProfileSchemaVersion() == null ||
                        profileVersionPair.getEndpointProfileSchemaVersion().intValue() == endpointProfileVersion.getVersion()) {
                    String schemaId = profileVersionPair.getServerProfileSchemaid();
                    int version = profileVersionPair.getServerProfileSchemaVersion() != null ? 
                            profileVersionPair.getServerProfileSchemaVersion().intValue() : -1;
                    VersionDto serverProfileVersion = new VersionDto(schemaId, version);
                    serverProfileVersions.add(serverProfileVersion);
                }
            }
        }
        List<VersionDto> serverProfileVersionsList = new ArrayList<>(serverProfileVersions);
        Collections.sort(serverProfileVersionsList);
        return serverProfileVersionsList;
    }
    
    @Override
    protected void onRecordRetrieved() {
        if (create) {
            getVacantSchemas(endpointGroupId, new BusyAsyncCallback<List<ProfileVersionPairDto>>() {
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, recordView);
                }

                @Override
                public void onSuccessImpl(List<ProfileVersionPairDto> result) {
                    profileVersionPairs = result;
                    List<VersionDto> endpointProfileVersions = extractEndpointProfileVersions(null);
                    VersionDto endpointProfileVersion = Utils.getMaxSchemaVersions(endpointProfileVersions);
                    List<VersionDto> serverProfileVersions = extractServerProfileVersions(endpointProfileVersion);
                    VersionDto serverProfileVersion = Utils.getMaxSchemaVersions(serverProfileVersions);
                    recordView.getEndpointProfileSchema().setValue(endpointProfileVersion);
                    recordView.getServerProfileSchema().setValue(serverProfileVersion);
                    recordView.getEndpointProfileSchema().setAcceptableValues(endpointProfileVersions);
                    recordView.getServerProfileSchema().setAcceptableValues(serverProfileVersions);
                    recordView.getRecordPanel().setData(record);
                    recordView.getRecordPanel().openDraft();
                }
            });
        } else {
            String endpointProfileVersion = record.getEndpointProfileSchemaVersion() != null ?
                    record.getEndpointProfileSchemaVersion().intValue() + "" : "";
            String serverProfileVersion = record.getServerProfileSchemaVersion() != null ?
                    record.getServerProfileSchemaVersion().intValue() + "" : "";
            
            recordView.getEndpointProfileSchemaVersion().setValue(endpointProfileVersion);
            recordView.getServerProfileSchemaVersion().setValue(serverProfileVersion);
            
            if (record.hasActive() && !record.hasDraft()) {
                ProfileFilterDto inactiveStruct = createInactiveStruct();
                inactiveStruct.setEndpointProfileSchemaId(record.getEndpointProfileSchemaId());
                inactiveStruct.setEndpointProfileSchemaVersion(record.getEndpointProfileSchemaVersion());
                inactiveStruct.setServerProfileSchemaId(record.getServerProfileSchemaId());
                inactiveStruct.setServerProfileSchemaVersion(record.getServerProfileSchemaVersion());
                inactiveStruct.setDescription(record.getDescription());
                inactiveStruct.setBody(record.getActiveStructureDto().getBody());
                record.setInactiveStructureDto(inactiveStruct);
            }
            recordView.getRecordPanel().setData(record);
            if (endpointGroup.getWeight()==0) {
                recordView.getRecordPanel().setReadOnly();
            }
            if (showActive && record.hasActive()) {
                recordView.getRecordPanel().openActive();
            } else {
                recordView.getRecordPanel().openDraft();
            }
        }
    }
    
    @Override
    protected void doSave(final EventBus eventBus) {
        ProfileFilterDto inactiveStruct = record.getInactiveStructureDto();
        if (create) {
            endpointProfileSchemaId = recordView.getEndpointProfileSchema().getValue().getId();
            serverProfileSchemaId = recordView.getServerProfileSchema().getValue().getId();
            inactiveStruct.setEndpointProfileSchemaId(endpointProfileSchemaId);
            inactiveStruct.setServerProfileSchemaId(serverProfileSchemaId);
            Integer endpointProfileSchemaVersion = recordView.getEndpointProfileSchema().getValue().getVersion() > -1 ?
                    new Integer(recordView.getEndpointProfileSchema().getValue().getVersion()) : null;
            Integer serverProfileSchemaVersion = recordView.getServerProfileSchema().getValue().getVersion() > -1 ?
                    new Integer(recordView.getServerProfileSchema().getValue().getVersion()) : null;
            inactiveStruct.setEndpointProfileSchemaVersion(endpointProfileSchemaVersion);        
            inactiveStruct.setServerProfileSchemaVersion(serverProfileSchemaVersion);
        }
        inactiveStruct.setDescription(recordView.getRecordPanel().getDescription().getValue());
        inactiveStruct.setBody(recordView.getRecordPanel().getBody().getValue());
        editProfileFilter(inactiveStruct,
                new BusyAsyncCallback<ProfileFilterDto>() {
                    public void onSuccessImpl(ProfileFilterDto result) {
                        goTo(getRecordPlace(applicationId, endpointGroupId, false, false, Math.random()));
                    }

                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, recordView, ProfileFilterActivity.this);
                    }
        });
    }

    private void getVacantSchemas(String endpointGroupId,
            final AsyncCallback<List<ProfileVersionPairDto>> callback) {
        KaaAdmin.getDataSource().getVacantProfileSchemas(endpointGroupId, callback);
    }
    
    private void editProfileFilter(ProfileFilterDto entity,
            AsyncCallback<ProfileFilterDto> callback) {
        KaaAdmin.getDataSource().editProfileFilter(entity, callback);
    }

    @Override
    protected void activateStruct(String id,
            AsyncCallback<ProfileFilterDto> callback) {
        KaaAdmin.getDataSource().activateProfileFilter(id, callback);
    }

    @Override
    protected void deactivateStruct(String id,
            AsyncCallback<ProfileFilterDto> callback) {
        KaaAdmin.getDataSource().deactivateProfileFilter(id, callback);
    }

    @Override
    protected ProfileFilterPlace getRecordPlaceImpl(String applicationId, String endpointGroupId, boolean create,
            boolean showActive, double random) {
        return new ProfileFilterPlace(applicationId, endpointProfileSchemaId, serverProfileSchemaId, 
                endpointGroupId, create, showActive, random);
    }

    @Override
    public String customizeErrorMessage(Throwable caught) {
        return Utils.parseErrorMessage(caught);
    }

}
