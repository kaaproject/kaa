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
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.admin.RecordKey.RecordFiles;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.LogSchemasDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LogSchemasActivity extends AbstractBaseCtlSchemasActivity<LogSchemaDto, LogSchemasPlace> {

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
    protected AbstractDataProvider<LogSchemaDto, String> getDataProvider(
            AbstractGrid<LogSchemaDto, String> dataGrid) {
        return new LogSchemasDataProvider(dataGrid, listView, applicationId);
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

    @Override
    protected void onCustomRowAction(RowActionEvent<String> event) {
        super.onCustomRowAction(event);
        Integer schemaVersion = Integer.valueOf(event.getClickedId());

        if (event.getAction() == KaaRowAction.DOWNLOAD_LOG_SCHEMA_LIBRARY) {
            KaaAdmin.getDataSource().getRecordData(applicationId, schemaVersion, RecordFiles.LOG_LIBRARY,
                    new AsyncCallback<String>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Utils.handleException(caught, listView);
                        }

                        @Override
                        public void onSuccess(String key) {
                            ServletHelper.downloadRecordLibrary(key);
                        }
                    });
        }
    }
}
