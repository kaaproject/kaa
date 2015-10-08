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

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;

public class EndpointProfileActivity extends
        AbstractDetailsActivity<EndpointProfileDto, EndpointProfileView, EndpointProfilePlace> {

    private String applicationId;

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
        return place.getEndpointID();
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

    }

    @Override
    protected void onSave() {

    }

    @Override
    protected void getEntity(String id, AsyncCallback<EndpointProfileDto> callback) {

    }

    @Override
    protected void editEntity(EndpointProfileDto entity, AsyncCallback<EndpointProfileDto> callback) {

    }
}
