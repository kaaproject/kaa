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

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.EndpointGroupsDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EndpointGroupsActivity extends AbstractListActivity<EndpointGroupDto, EndpointGroupsPlace> {

    private String applicationId;

    public EndpointGroupsActivity(EndpointGroupsPlace place, ClientFactory clientFactory) {
        super(place, EndpointGroupDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<EndpointGroupDto> getView() {
        return clientFactory.getEndpointGroupsView();
    }

    @Override
    protected AbstractDataProvider<EndpointGroupDto, String> getDataProvider(
            AbstractGrid<EndpointGroupDto, String> dataGrid) {
        return new EndpointGroupsDataProvider(dataGrid, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new EndpointGroupPlace(applicationId, "", false, false);
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new EndpointGroupPlace(applicationId, id, false, false);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        KaaAdmin.getDataSource().deleteEndpointGroup(id, callback);
    }


}
