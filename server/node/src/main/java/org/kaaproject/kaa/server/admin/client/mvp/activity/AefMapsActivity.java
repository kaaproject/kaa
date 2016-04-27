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
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.AefMapsDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.AefMapPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.AefMapsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AefMapsActivity extends AbstractListActivity<ApplicationEventFamilyMapDto, AefMapsPlace> {

    private String applicationId;

    public AefMapsActivity(AefMapsPlace place, ClientFactory clientFactory) {
        super(place, ApplicationEventFamilyMapDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<ApplicationEventFamilyMapDto> getView() {
        return clientFactory.getAefMapsView();
    }

    @Override
    protected AbstractDataProvider<ApplicationEventFamilyMapDto, String> getDataProvider(
            AbstractGrid<ApplicationEventFamilyMapDto, String> dataGrid) {
        return new AefMapsDataProvider(dataGrid, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new AefMapPlace(applicationId, "");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new AefMapPlace(applicationId, id);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        callback.onSuccess((Void)null);
    }

}
