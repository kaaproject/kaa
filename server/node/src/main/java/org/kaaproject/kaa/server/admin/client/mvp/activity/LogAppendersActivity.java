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
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.AppendersDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppendersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
    protected AbstractDataProvider<LogAppenderDto, String> getDataProvider(AbstractGrid<LogAppenderDto,String> dataGrid) {
        return new AppendersDataProvider(dataGrid, listView, applicationId);
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
                JSONObject json = (JSONObject)JSONParser.parseLenient(key.getJsonConfiguration());
                json.put("minLogSchemaVersion", new JSONNumber(key.getMinLogSchemaVersion()));
                json.put("maxLogSchemaVersion", new JSONNumber(key.getMaxLogSchemaVersion()));
                json.put("pluginTypeName", new JSONString(key.getPluginTypeName()));
                json.put("pluginClassName", new JSONString(key.getPluginClassName()));
                JSONArray headersStructure = new JSONArray();
                for(LogHeaderStructureDto header : key.getHeaderStructure()){
                    headersStructure.set(headersStructure.size(), new JSONString(header.getValue()));
                }
                json.put("headerStructure", headersStructure);

                ServletHelper.downloadJsonFile(json.toString(), key.getPluginTypeName()+".json");
            }
        };

        switch (action) {
            case KaaRowAction.DOWNLOAD_SCHEMA:
                KaaAdmin.getDataSource().getLogAppender(String.valueOf(appenderId), callback);
                break;
            default:
                break;
        }
    }
}
