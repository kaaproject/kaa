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

import java.util.List;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifierPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserVerifierView;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserVerifierActivity extends AbstractPluginActivity<UserVerifierDto, UserVerifierView, UserVerifierPlace> {

    public UserVerifierActivity(UserVerifierPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected UserVerifierView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateUserVerifierView();
        } else {
            return clientFactory.getUserVerifierView();
        }
    }

    @Override
    protected UserVerifierDto newEntity() {
        UserVerifierDto dto = new UserVerifierDto();
        dto.setApplicationId(applicationId);
        return dto;
    }

    @Override
    protected void loadPluginInfos(AsyncCallback<List<PluginInfoDto>> callback) {
        KaaAdmin.getDataSource().loadUserVerifierPluginInfos(callback);
    }

    @Override
    protected void onEntityRetrieved() {
        super.onEntityRetrieved();
        if (!create) {
            detailsView.getVerifierToken().setValue(entity.getVerifierToken());
        }
    }

    @Override
    protected void getEntity(String id, AsyncCallback<UserVerifierDto> callback) {
        KaaAdmin.getDataSource().getUserVerifierForm(id, callback);
    }

    @Override
    protected void editEntity(UserVerifierDto entity, AsyncCallback<UserVerifierDto> callback) {
        KaaAdmin.getDataSource().editUserVerifierForm(entity, callback);
    }


}