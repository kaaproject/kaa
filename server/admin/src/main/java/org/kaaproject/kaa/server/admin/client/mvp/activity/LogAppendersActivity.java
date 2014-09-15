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

import org.kaaproject.kaa.common.dto.admin.RecordKey.RecordFiles;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.AppendersDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowAction;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowActionEvent;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppendersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ImageTextButton;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.MultiSelectionModel;

public class LogAppendersActivity extends AbstractListActivity<LogAppenderDto, LogAppendersPlace> {

    private String applicationId;
    private LoadDataHandler handler;

    public LogAppendersActivity(LogAppendersPlace place, ClientFactory clientFactory) {
        super(place, LogAppenderDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
        this.handler = new LoadDataHandler();
    }

    @Override
    protected BaseListView<LogAppenderDto> getView() {
        return clientFactory.getAppendersView();
    }

    @Override
    protected AbstractDataProvider<LogAppenderDto> getDataProvider(MultiSelectionModel<LogAppenderDto> selectionModel,
            AsyncCallback<List<LogAppenderDto>> asyncCallback) {
        return new AppendersDataProvider(selectionModel, asyncCallback, applicationId, handler);
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

    protected void onCustomRowAction(RowActionEvent<String> event) {
        Integer logSchemaVersion = Integer.valueOf(event.getClickedId());
        final RowAction action = event.getAction();

        AsyncCallback<String> callback = new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                listView.setErrorMessage(Utils.getErrorMessage(caught));
            }
            @Override
            public void onSuccess(String key) {
                ServletHelper.downloadRecordLibrary(key);
            }
        };

        switch (action) {
            case DOWNLOAD_LIBRARY:
                KaaAdmin.getDataSource().getRecordLibrary(applicationId, logSchemaVersion, RecordFiles.LIBRARY, callback);
                break;
            case DOWNLOAD_SCHEMA:
                KaaAdmin.getDataSource().getRecordLibrary(applicationId, logSchemaVersion, RecordFiles.SCHEMA, callback);
                break;
            default:
                break;
        }
    }

    public class LoadDataHandler {

        public void onLoad(List<LogAppenderDto> result) {
            if (result != null && !result.isEmpty()) {
                ((ImageTextButton) listView.getAddButton()).setEnabled(false);
                return;
            }
            ((ImageTextButton) listView.getAddButton()).setEnabled(true);
        }

        public void onError() {
            ((ImageTextButton) listView.getAddButton()).setEnabled(true);
        }
    }

}
