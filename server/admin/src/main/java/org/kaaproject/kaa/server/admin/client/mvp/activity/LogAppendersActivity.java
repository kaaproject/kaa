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

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
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
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.controller.KaaAdminController;
import org.kaaproject.kaa.server.admin.services.KaaAdminServiceImpl;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminService;

import java.util.Arrays;

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

    @Override
    protected void onCustomRowAction(final RowActionEvent<String> event) {
        Integer appenderId = Integer.valueOf(event.getClickedId());
        final int action = event.getAction();
        AsyncCallback<LogAppenderDto> callback = new AsyncCallback<LogAppenderDto>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }
            @Override
            public void onSuccess(LogAppenderDto key) {

                //TODO move it on other layer and use AvroJsonConverter

                StringBuilder jsonBuilder = new StringBuilder("{");

                jsonBuilder.append("\"applicationToken\":\"").append(key.getApplicationToken()).append("\",")
                        .append("\"tenantId\":\"").append(key.getTenantId()).append("\",")
                        .append("\"minLogSchemaVersion\":\"").append(key.getMinLogSchemaVersion()).append("\",")
                        .append("\"maxLogSchemaVersion\":\"").append(key.getMaxLogSchemaVersion()).append("\",")
                        .append("\"applicationId\":\"").append(key.getApplicationId()).append("\",")
                        .append("\"applicationToken\":\"").append(key.getApplicationToken()).append("\",")
                        .append("\"pluginTypeName\":\"").append(key.getPluginTypeName()).append("\",")
                        .append("\"pluginClassName\":\"").append(key.getPluginClassName()).append("\",")
                        .append("\"jsonConfiguration\":\"").append(key.getJsonConfiguration().replace("\"", "\\\"")).append("\",")
                        .append("\"headerStructure\":[],")
                        .append("\"name\":\"").append(key.getPluginTypeName()).append("\",")
                        .append("\"description\":\"\"")
                        .append("}");
                downloadPropertiesJson(jsonBuilder.toString());



            }
        };

        switch (action) {
            case KaaRowAction.DOWNLOAD_SCHEMA:
                KaaAdmin.getDataSource().getLogAppender(String.valueOf(appenderId), callback);
            default:
                break;
        }
    }

    protected native void downloadPropertiesJson(String json)/*-{

        var win = $wnd.open("", "win",
            "width=940,height=400,status=1,resizeable=1,scrollbars=1");
        win.document.open("text/html", "replace");
        win.document.write(json);
        win.document.close();
        win.focus();
    }-*/;
}
