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

import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.MailPropertiesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePropertiesView;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MailPropertiesActivity
        extends
        AbstractPropertiesActivity<MailPropertiesPlace> {

    public MailPropertiesActivity(MailPropertiesPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.create = false;
    }

    @Override
    protected BasePropertiesView getView(boolean create) {
        return clientFactory.getMailPropertiesView();
    }

    @Override
    protected void getEntity(String id, AsyncCallback<PropertiesDto> callback) {
        KaaAdmin.getDataSource().getMailProperties(callback);
    }

    @Override
    protected void editEntity(PropertiesDto entity,
            AsyncCallback<PropertiesDto> callback) {
        KaaAdmin.getDataSource().editMailProperties(entity, callback);
    }

}
