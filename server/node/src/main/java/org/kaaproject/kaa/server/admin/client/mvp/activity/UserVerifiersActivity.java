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
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.UserVerifiersDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifierPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifiersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserVerifiersActivity extends AbstractListActivity<UserVerifierDto, UserVerifiersPlace> {

    private String applicationId;

    public UserVerifiersActivity(UserVerifiersPlace place, ClientFactory clientFactory) {
        super(place, UserVerifierDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<UserVerifierDto> getView() {
        return clientFactory.getUserVerifiersView();
    }

    @Override
    protected AbstractDataProvider<UserVerifierDto, String> getDataProvider(AbstractGrid<UserVerifierDto, String> dataGrid) {
        return new UserVerifiersDataProvider(dataGrid, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new UserVerifierPlace(applicationId, "");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new UserVerifierPlace(applicationId, id);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        KaaAdmin.getDataSource().removeUserVerifier(id, callback);
    }

    @Override
    protected void onCustomRowAction(RowActionEvent<String> event) {
        Integer verifierId = Integer.valueOf(event.getClickedId());
        final int action = event.getAction();
        AsyncCallback<UserVerifierDto> callback = new AsyncCallback<UserVerifierDto>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }
            @Override
            public void onSuccess(UserVerifierDto key) {

                String jsonConfig = key.getJsonConfiguration();

                JSONObject json;
                //Some verifiers (ex:Trustful) has no jsonConfiguration field
                if(jsonConfig!=null && !jsonConfig.isEmpty()) {
                    json = (JSONObject) JSONParser.parseLenient(jsonConfig);
                }else{
                    json = new JSONObject();
                }

                json.put("pluginTypeName", new JSONString(key.getPluginTypeName()));
                json.put("pluginClassName", new JSONString(key.getPluginClassName()));

                ServletHelper.downloadJsonFile(json.toString(), key.getPluginTypeName() + ".json");
            }
        };

        switch (action) {
            case KaaRowAction.DOWNLOAD_SCHEMA:
                KaaAdmin.getDataSource().getUserVerifier(String.valueOf(verifierId), callback);
                break;
            default:
                break;
        }
    }
}
