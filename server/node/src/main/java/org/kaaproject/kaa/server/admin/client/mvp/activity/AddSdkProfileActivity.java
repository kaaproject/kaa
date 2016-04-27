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

import static org.kaaproject.kaa.server.admin.client.util.Utils.getMaxSchemaVersions;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AddSdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.AddSdkProfileView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class AddSdkProfileActivity extends AbstractDetailsActivity<SdkProfileDto, AddSdkProfileView, AddSdkProfilePlace> {

    private String applicationId;

    public AddSdkProfileActivity(AddSdkProfilePlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    @Override
    protected String getEntityId(AddSdkProfilePlace place) {
        return null;
    }

    @Override
    protected AddSdkProfileView getView(boolean create) {
        return clientFactory.getAddSdkProfileView();
    }

    @Override
    protected SdkProfileDto newEntity() {
        SdkProfileDto sdkPropertiesDto = new SdkProfileDto();
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

        List<VersionDto> confSchemaVersions = schemaVersions.getConfigurationSchemaVersions();
        detailsView.getConfigurationSchemaVersion().setValue(getMaxSchemaVersions(confSchemaVersions));
        detailsView.getConfigurationSchemaVersion().setAcceptableValues(confSchemaVersions);

        List<VersionDto> pfSchemaVersions = schemaVersions.getProfileSchemaVersions();
        detailsView.getProfileSchemaVersion().setValue(getMaxSchemaVersions(pfSchemaVersions));
        detailsView.getProfileSchemaVersion().setAcceptableValues(pfSchemaVersions);

        List<VersionDto> notSchemaVersions = schemaVersions.getNotificationSchemaVersions();
        detailsView.getNotificationSchemaVersion().setValue(getMaxSchemaVersions(notSchemaVersions));
        detailsView.getNotificationSchemaVersion().setAcceptableValues(notSchemaVersions);

        List<VersionDto> logSchemaVersions = schemaVersions.getLogSchemaVersions();
        detailsView.getLogSchemaVersion().setValue(getMaxSchemaVersions(logSchemaVersions));
        detailsView.getLogSchemaVersion().setAcceptableValues(logSchemaVersions);

        detailsView.setAefMaps(aefMaps);

        detailsView.getDefaultUserVerifier().setAcceptableValues(userVerifiers);
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());

        entity.setConfigurationSchemaVersion(detailsView.getConfigurationSchemaVersion().
                getValue().getVersion());
        entity.setProfileSchemaVersion(detailsView.getProfileSchemaVersion().
                getValue().getVersion());
        entity.setNotificationSchemaVersion(detailsView.getNotificationSchemaVersion().
                getValue().getVersion());
        entity.setLogSchemaVersion(detailsView.getLogSchemaVersion().
                getValue().getVersion());

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
    protected void getEntity(String id, AsyncCallback<SdkProfileDto> callback) {}


    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        if (!entity.getAefMapIds().isEmpty() && entity.getDefaultVerifierToken() == null) {
            detailsView.setErrorMessage(Utils.constants.specifyVerifier());
        } else {

            KaaAdmin.getDataSource().addSdkProfile(entity, new BusyAsyncCallback<SdkProfileDto>() {

                @Override
                public void onSuccessImpl(SdkProfileDto result) {
                    detailsView.reset();
                    AddSdkProfileActivity.this.goTo(new SdkProfilesPlace(applicationId));
                }

                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }
            });
        }
    }

    @Override
    protected void editEntity(SdkProfileDto entity, final AsyncCallback<SdkProfileDto> callback) {}
}
