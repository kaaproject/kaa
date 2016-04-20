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

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.ApplicationView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ValueListBox;

public class ApplicationActivity
        extends
        AbstractDetailsActivity<ApplicationDto, ApplicationView, ApplicationPlace> {

    public ApplicationActivity(ApplicationPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
        if (KaaAdmin.isDevMode()) {
            registrations.add(detailsView.getGenerateSdkButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    goTo(new SdkProfilesPlace(entityId));
                }
            }));
        }
    }

    @Override
    protected String getEntityId(ApplicationPlace place) {
        return place.getApplicationId();
    }

    @Override
    protected ApplicationView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateApplicationView();
        } else {
            return clientFactory.getApplicationView();
        }
    }

    @Override
    protected ApplicationDto newEntity() {
        return new ApplicationDto();
    }

    @Override
    protected void onEntityRetrieved() {
        if (!create) {
            detailsView.setTitle(entity.getName());
            detailsView.getApplicationToken().setValue(entity.getApplicationToken());
        }
        detailsView.getApplicationName().setValue(entity.getName());

        ValueListBox<String> serviceNames = this.detailsView.getCredentialsServiceName();
        if (serviceNames != null) {
            KaaAdmin.getDataSource().getCredentialsServiceNames(new AsyncCallback<List<String>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Utils.handleException(caught, ApplicationActivity.this.detailsView);
                }

                @Override
                public void onSuccess(List<String> result) {
                    ApplicationActivity.this.detailsView.getCredentialsServiceName().setAcceptableValues(result);
                }
            });
            serviceNames.setValue(this.entity.getCredentialsServiceName());
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getApplicationName().getValue());
        entity.setCredentialsServiceName(detailsView.getCredentialsServiceName().getValue());
    }

    @Override
    protected void getEntity(String id, AsyncCallback<ApplicationDto> callback) {
        KaaAdmin.getDataSource().getApplication(id, callback);
    }

    @Override
    protected void editEntity(ApplicationDto entity,
            AsyncCallback<ApplicationDto> callback) {
        KaaAdmin.getDataSource().editApplication(entity, callback);
    }
}
