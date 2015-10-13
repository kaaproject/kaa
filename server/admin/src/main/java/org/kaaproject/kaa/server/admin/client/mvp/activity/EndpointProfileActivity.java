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

import com.google.common.io.BaseEncoding;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileViewDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.ConfigurationsDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import java.util.List;

public class EndpointProfileActivity extends
        AbstractDetailsActivity<EndpointProfileDto, EndpointProfileView, EndpointProfilePlace> {

    private String applicationId;
    private ConfigurationsDataProvider configurationsDataProvider;

    public EndpointProfileActivity(EndpointProfilePlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
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
    protected EndpointProfileDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {

        detailsView.getKeyHash().setValue(BaseEncoding.base64().encode(entity.getEndpointKeyHash()));
        detailsView.getId().setValue(entity.getId());
        detailsView.getAppId().setValue(entity.getApplicationId());
        detailsView.getProfileVersion().setValue(entity.getProfileVersion() + "");
        detailsView.getConfigurationHash().setValue(BaseEncoding.base64().encode(entity.getConfigurationHash()));
        detailsView.getConfigurationVersion().setValue(entity.getConfigurationVersion() + "");
        detailsView.getNotificationVersion().setValue(entity.getNotificationVersion() + "");
        detailsView.getSystemNfVersion().setValue(entity.getSystemNfVersion() + "");
        detailsView.getUserNfVersion().setValue(entity.getUserNfVersion() + "");
        detailsView.getLogSchemaVer().setValue(entity.getLogSchemaVersion() + "");
        detailsView.getServerHash().setValue(entity.getServerHash());

        KaaAdmin.getDataSource().loadTopics(applicationId, new AsyncCallback<List<TopicDto>>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }

            @Override
            public void onSuccess(List<TopicDto> result) {
                /*
                    TODO: filter by endpoint notification topics
                    if there is no topics - returns empty list
                 */
                detailsView.getTopicsGrid().getDataGrid().setRowData(result);
            }
        });

        KaaAdmin.getDataSource().getEndpointProfileViewDtoByEndpointProfileKeyHash(applicationId, new AsyncCallback<EndpointProfileViewDto>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("ups!");
            }

            @Override
            public void onSuccess(EndpointProfileViewDto result) {
                GWT.log("result: " + result.getEndpointKeyHash());
            }
        });
    }

    @Override
    protected void onSave() {

    }

    @Override
    protected void getEntity(String id, final AsyncCallback<EndpointProfileDto> callback) {
        KaaAdmin.getDataSource().getEndpointProfileByKeyHash(id, callback);
    }

    @Override
    protected void editEntity(EndpointProfileDto entity, AsyncCallback<EndpointProfileDto> callback) {

    }
}
