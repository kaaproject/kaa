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

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.EndpointProfilesDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

public class EndpointProfilesActivity extends AbstractListActivity<EndpointProfileDto, EndpointProfilesPlace> {

    private String applicationId;

    public EndpointProfilesActivity(EndpointProfilesPlace place, ClientFactory clientFactory) {
        super(place, EndpointProfileDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<EndpointProfileDto> getView() {
        return clientFactory.getEndpointProfilesView();
    }

    @Override
    protected AbstractDataProvider<EndpointProfileDto> getDataProvider(
            AbstractGrid<EndpointProfileDto, ?> dataGrid) {
        return new EndpointProfilesDataProvider(dataGrid, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() { return null; }

    @Override
    protected Place existingEntityPlace(String id) { return null; }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {}
}
