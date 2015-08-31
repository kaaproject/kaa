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

import static org.kaaproject.kaa.server.admin.client.util.Utils.getMaxSchemaVersions;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.GenerateSdkPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.GenerateSdkView;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class GenerateSdkActivity extends AbstractDetailsActivity<SdkPropertiesDto, GenerateSdkView, GenerateSdkPlace> {

    private String applicationId;

    public GenerateSdkActivity(GenerateSdkPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    @Override
    protected String getEntityId(GenerateSdkPlace place) {
        return null;
    }

    @Override
    protected GenerateSdkView getView(boolean create) {
        return clientFactory.getGenerateSdkView();
    }

    @Override
    protected SdkPropertiesDto newEntity() {
        SdkPropertiesDto sdkPropertiesDto = new SdkPropertiesDto();
        sdkPropertiesDto.setApplicationId(applicationId);
        return sdkPropertiesDto;
    }

    @Override
    protected void onEntityRetrieved() {
        BusyPopup.showPopup();
        KaaAdmin.getDataSource().getSchemaVersionsByApplicationId(applicationId, new AsyncCallback<SchemaVersions>() {
            @Override
            public void onFailure(Throwable caught) {
                BusyPopup.hidePopup();
                Utils.handleException(caught, detailsView);
            }

            @Override
            public void onSuccess(final SchemaVersions schemaVersions) {
                KaaAdmin.getDataSource().getAefMaps(applicationId, new AsyncCallback<List<AefMapInfoDto>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        BusyPopup.hidePopup();
                        Utils.handleException(caught, detailsView);
                    }

                    @Override
                    public void onSuccess(final List<AefMapInfoDto> ecfs) {
                        KaaAdmin.getDataSource().loadUserVerifiers(applicationId, 
                                new AsyncCallback<List<UserVerifierDto>>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        BusyPopup.hidePopup();
                                        Utils.handleException(caught, detailsView);
                                    }

                                    @Override
                                    public void onSuccess(
                                            List<UserVerifierDto> userVerifiers) {
                                        BusyPopup.hidePopup();
                                        onInfoRetrieved(schemaVersions, ecfs, userVerifiers);
                                    }
                        });
                    }
                });
            }
        });
    }
    
    private void onInfoRetrieved(SchemaVersions schemaVersions, 
            List<AefMapInfoDto> aefMaps, 
            List<UserVerifierDto> userVerifiers) {
        
        List<SchemaDto> confSchemaVersions = schemaVersions.getConfigurationSchemaVersions();
        detailsView.getConfigurationSchemaVersion().setValue(getMaxSchemaVersions(confSchemaVersions), true);
        detailsView.getConfigurationSchemaVersion().setAcceptableValues(confSchemaVersions);
        
        List<SchemaDto> pfSchemaVersions = schemaVersions.getProfileSchemaVersions();
        detailsView.getProfileSchemaVersion().setValue(getMaxSchemaVersions(pfSchemaVersions), true);
        detailsView.getProfileSchemaVersion().setAcceptableValues(pfSchemaVersions);

        List<SchemaDto> notSchemaVersions = schemaVersions.getNotificationSchemaVersions();
        detailsView.getNotificationSchemaVersion().setValue(getMaxSchemaVersions(notSchemaVersions), true);
        detailsView.getNotificationSchemaVersion().setAcceptableValues(notSchemaVersions);
        
        List<SchemaDto> logSchemaVersions = schemaVersions.getLogSchemaVersions();
        detailsView.getLogSchemaVersion().setValue(getMaxSchemaVersions(logSchemaVersions), true);
        detailsView.getLogSchemaVersion().setAcceptableValues(logSchemaVersions);
        
        detailsView.setAefMaps(aefMaps);
        
        detailsView.getDefaultUserVerifier().setAcceptableValues(userVerifiers);
    }
 
    @Override
    protected void onSave() {
        entity.setConfigurationSchemaVersion(detailsView.getConfigurationSchemaVersion().
                getValue().getMajorVersion());
        entity.setProfileSchemaVersion(detailsView.getProfileSchemaVersion().
                getValue().getMajorVersion());
        entity.setNotificationSchemaVersion(detailsView.getNotificationSchemaVersion().
                getValue().getMajorVersion());
        entity.setLogSchemaVersion(detailsView.getLogSchemaVersion().
                getValue().getMajorVersion());
        entity.setTargetPlatform(detailsView.getTargetPlatform().getValue());
        
        List<String> aefMapIds = new ArrayList<>();
        List<AefMapInfoDto> aefMaps = detailsView.getSelectedAefMaps().getValues();
        if (aefMaps != null) {
            for (AefMapInfoDto aefMap : aefMaps) {
                aefMapIds.add(aefMap.getAefMapId());
            }
        }
        entity.setAefMapIds(aefMapIds);
        if (detailsView.getDefaultUserVerifier().getValue() != null) {
            entity.setDefaultVerifierToken(detailsView.getDefaultUserVerifier()
                    .getValue().getVerifierToken());
        }
    }
    
    @Override
    protected void loadEntity() {
        onEntityRetrieved();
    }

    @Override
    protected void getEntity(String id, AsyncCallback<SdkPropertiesDto> callback) {}

    
    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        if (!entity.getAefMapIds().isEmpty() && entity.getDefaultVerifierToken() == null) {
            detailsView.setErrorMessage(Utils.constants.specifyVerifier());
        } else {

            KaaAdmin.getDataSource().generateSdk(entity,new BusyAsyncCallback<String>() {

                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }

                @Override
                public void onSuccessImpl(String key) {
                    detailsView.clearError();
                    ServletHelper.downloadSdk(key);
                }
            });
        }
    }
    
    @Override
    protected void editEntity(SdkPropertiesDto entity, final AsyncCallback<SdkPropertiesDto> callback) {}
}
