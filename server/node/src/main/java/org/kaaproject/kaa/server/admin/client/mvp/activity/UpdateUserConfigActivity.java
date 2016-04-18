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
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.UpdateUserConfigPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.UpdateUserConfigView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class UpdateUserConfigActivity extends AbstractDetailsActivity<EndpointUserConfigurationDto, UpdateUserConfigView, UpdateUserConfigPlace> {

    private String applicationId;

    public UpdateUserConfigActivity(UpdateUserConfigPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    @Override
    protected String getEntityId(UpdateUserConfigPlace place) {
        return null;
    }

    @Override
    protected UpdateUserConfigView getView(boolean create) {
        return clientFactory.getUpdateUserConfigView();
    }

    @Override
    protected EndpointUserConfigurationDto newEntity() {
        EndpointUserConfigurationDto dto = new EndpointUserConfigurationDto();
        return dto;
    }

    @Override
    protected void onEntityRetrieved() {
        KaaAdmin.getDataSource().getUserConfigurationSchemaInfosByApplicationId(applicationId,
                new BusyAsyncCallback<List<SchemaInfoDto>>() {
            @Override
            public void onSuccessImpl(List<SchemaInfoDto> result) {
                Collections.sort(result);
                SchemaInfoDto schemaInfo = result.get(result.size()-1);
                detailsView.getConfigurationSchemaInfo().setValue(schemaInfo);
                detailsView.getConfigurationSchemaInfo().setAcceptableValues(result);
                detailsView.getConfigurationData().setValue(schemaInfo != null ? schemaInfo.getSchemaForm() : null);
            }
            @Override
            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
        });
    }

    @Override
    protected void onSave() {
        entity.setUserId(detailsView.getUserId().getValue());
        entity.setSchemaVersion(detailsView.getConfigurationSchemaInfo().getValue().getVersion());
    }

    @Override
    protected void getEntity(String id, AsyncCallback<EndpointUserConfigurationDto> callback) {
        callback.onSuccess(null);
    }

    @Override
    protected void editEntity(EndpointUserConfigurationDto entity, final AsyncCallback<EndpointUserConfigurationDto> callback) {
        KaaAdmin.getDataSource().editUserConfiguration(entity, applicationId,
                detailsView.getConfigurationData().getValue(),
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
    }

}
