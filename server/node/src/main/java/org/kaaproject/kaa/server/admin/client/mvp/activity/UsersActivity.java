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
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.UsersDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UsersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class UsersActivity extends AbstractListActivity<UserDto, UsersPlace> {

    public UsersActivity(UsersPlace place, ClientFactory clientFactory) {
        super(place, UserDto.class, clientFactory);
    }

    @Override
    protected BaseListView<UserDto> getView() {
        return clientFactory.getUsersView();
    }

    @Override
    protected AbstractDataProvider<UserDto, String> getDataProvider(
            AbstractGrid<UserDto, String> dataGrid) {
        return new UsersDataProvider(dataGrid, listView);
    }

    @Override
    protected Place newEntityPlace() {
        return new UserPlace("");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new UserPlace(id);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        KaaAdmin.getDataSource().deleteUser(id, callback);
    }

}
