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

import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AefMapPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.NotificationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.SdkProfileView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SdkProfileActivity extends
        AbstractDetailsActivity<SdkProfileViewDto, SdkProfileView, SdkProfilePlace>{

    public SdkProfileActivity(SdkProfilePlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected String getEntityId(SdkProfilePlace place) {
        return place.getSdkProfileId();
    }

    @Override
    protected SdkProfileView getView(boolean create) {
        return clientFactory.getSdkProfileView();
    }

    @Override
    protected SdkProfileViewDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {
        detailsView.getSdkProfileToken().setValue(entity.getSdkProfile().getToken());
        detailsView.getSdkName().setValue(entity.getSdkProfile().getName());
        detailsView.getSdkAuthor().setValue(entity.getSdkProfile().getCreatedUsername());
        detailsView.getSdkDateCreated().setValue(Utils.millisecondsToDateString(
                entity.getSdkProfile().getCreatedTime()));

        List<ApplicationEventFamilyMapDto> aefMapDtoList = entity.getAefMapDtoList();
        if (aefMapDtoList != null) {
            detailsView.getSdkAefMapsGrid().getDataGrid().setRowData(aefMapDtoList);
        } else {
            detailsView.getSdkAefMapsGrid().getDataGrid().setRowData(new ArrayList<ApplicationEventFamilyMapDto>());
        }

        detailsView.getSdkConfigurationVersion().setText(entity.getConfigurationSchemaName());
        registrations.add(detailsView.getSdkConfigurationVersion().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ConfigurationSchemaPlace configurationSchemaPlace =
                        new ConfigurationSchemaPlace(place.getApplicationId(), entity.getConfigurationSchemaId());
                configurationSchemaPlace.setPreviousPlace(place);
                goTo(configurationSchemaPlace);
            }
        }));

        detailsView.getSdkProfileVersion().setText(entity.getProfileSchemaName());
        registrations.add(detailsView.getSdkProfileVersion().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ProfileSchemaPlace profileSchemaPlace =
                        new ProfileSchemaPlace(place.getApplicationId(), entity.getProfileSchemaId());
                profileSchemaPlace.setPreviousPlace(place);
                goTo(profileSchemaPlace);
            }
        }));

        detailsView.getSdkNotificationVersion().setText(entity.getNotificationSchemaName());
        registrations.add(detailsView.getSdkNotificationVersion().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                NotificationSchemaPlace notificationSchemaPlace =
                        new NotificationSchemaPlace(place.getApplicationId(), entity.getNotificationSchemaId());
                notificationSchemaPlace.setPreviousPlace(place);
                goTo(notificationSchemaPlace);
            }
        }));

        detailsView.getSdkLoggingVersion().setText(entity.getLogSchemaName());
        registrations.add(detailsView.getSdkLoggingVersion().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                LogSchemaPlace logSchemaPlace = new LogSchemaPlace(place.getApplicationId(), entity.getLogSchemaId());
                logSchemaPlace.setPreviousPlace(place);
                goTo(logSchemaPlace);
            }
        }));
    }

    @Override
    protected void bind(EventBus eventBus) {
        super.bind(eventBus);

        registrations.add(detailsView.getSdkAefMapsGrid().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> rowActionEvent) {
                String id = rowActionEvent.getClickedId();
                AefMapPlace aefMapPlace = new AefMapPlace(place.getApplicationId(), id);
                aefMapPlace.setPreviousPlace(place);
                goTo(aefMapPlace);
            }
        }));
    }

    @Override
    protected void onSave() {}

    @Override
    protected void getEntity(String id, AsyncCallback<SdkProfileViewDto> callback) {
        KaaAdmin.getDataSource().getSdkProfileView(id, callback);
    }

    @Override
    protected void editEntity(SdkProfileViewDto entity, AsyncCallback<SdkProfileViewDto> callback) {}
}
