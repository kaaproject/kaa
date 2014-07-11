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

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.LogSchemasDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.MultiSelectionModel;

public class LogSchemasActivity extends AbstractListActivity<LogSchemaDto, LogSchemasPlace> {

    private String applicationId;

    public LogSchemasActivity(LogSchemasPlace place, ClientFactory clientFactory) {
        super(place, LogSchemaDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<LogSchemaDto> getView() {
        return clientFactory.getLogSchemasView();
    }

    @Override
    protected AbstractDataProvider<LogSchemaDto> getDataProvider(
            MultiSelectionModel<LogSchemaDto> selectionModel,
            AsyncCallback<List<LogSchemaDto>> asyncCallback) {
        return new LogSchemasDataProvider(selectionModel, asyncCallback, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new LogSchemaPlace(applicationId, "");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new LogSchemaPlace(applicationId, id);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        callback.onSuccess((Void)null);
    }

}
