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

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractPluginPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePluginView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public abstract class AbstractPluginActivity<T extends PluginDto, V extends BasePluginView, P extends AbstractPluginPlace> extends AbstractDetailsActivity<T, V, P> {

    protected String applicationId;

    public AbstractPluginActivity(P place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    @Override
    protected String getEntityId(P place) {
        return place.getPluginId();
    }
    
    protected abstract void loadPluginInfos(AsyncCallback<List<PluginInfoDto>> callback);

    @Override
    protected void onEntityRetrieved() {
        loadPluginInfos(new BusyAsyncCallback<List<PluginInfoDto>>() {
            @Override
            public void onSuccessImpl(List<PluginInfoDto> result) {
                detailsView.getPluginInfo().setAcceptableValues(result);
            }
            @Override
            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
        });
        
        if (!create) {
            detailsView.getName().setValue(entity.getName());
            detailsView.getDescription().setValue(entity.getDescription());
            detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
            detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
            detailsView.getConfiguration().setValue(entity.getFieldConfiguration());
            PluginInfoDto appenderInfo = 
                    new PluginInfoDto(entity.getPluginTypeName(), entity.getFieldConfiguration(), entity.getPluginClassName());
            detailsView.getPluginInfo().setValue(appenderInfo);
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setDescription(detailsView.getDescription().getValue());
        PluginInfoDto appenderInfo = detailsView.getPluginInfo().getValue();
        entity.setPluginTypeName(appenderInfo.getPluginTypeName());
        entity.setPluginClassName(appenderInfo.getPluginClassName());
        entity.setFieldConfiguration(detailsView.getConfiguration().getValue());
    }

}
