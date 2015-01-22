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

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.AppendersDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppendersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.MultiSelectionModel;

public class LogAppendersActivity extends AbstractListActivity<LogAppenderDto, LogAppendersPlace> {

    private String applicationId;

    public LogAppendersActivity(LogAppendersPlace place, ClientFactory clientFactory) {
        super(place, LogAppenderDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<LogAppenderDto> getView() {
        return clientFactory.getAppendersView();
    }

    @Override
    protected AbstractDataProvider<LogAppenderDto> getDataProvider(MultiSelectionModel<LogAppenderDto> selectionModel) {
        return new AppendersDataProvider(selectionModel, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new LogAppenderPlace(applicationId, "");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new LogAppenderPlace(applicationId, id);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        KaaAdmin.getDataSource().removeLogAppender(id, callback);
    }

}
