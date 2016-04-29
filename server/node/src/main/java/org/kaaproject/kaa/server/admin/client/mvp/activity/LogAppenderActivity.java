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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LogAppenderActivity extends AbstractPluginActivity<LogAppenderDto, LogAppenderView, LogAppenderPlace> {

    public LogAppenderActivity(LogAppenderPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected LogAppenderView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateAppenderView();
        } else {
            return clientFactory.getAppenderView();
        }
    }

    @Override
    protected LogAppenderDto newEntity() {
        LogAppenderDto dto = new LogAppenderDto();
        dto.setApplicationId(applicationId);
        return dto;
    }

    @Override
    protected void loadPluginInfos(AsyncCallback<List<PluginInfoDto>> callback) {
        KaaAdmin.getDataSource().loadLogAppenderPluginInfos(callback);
    }

    @Override
    protected void onEntityRetrieved() {
        super.onEntityRetrieved();
        KaaAdmin.getDataSource().loadLogSchemasVersion(applicationId, new BusyAsyncCallback<List<VersionDto>>() {
            @Override
            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
            @Override
            public void onSuccessImpl(List<VersionDto> result) {
                onSchemaVersionsRetrieved(result);
            }
        });
    }
    
    private void onSchemaVersionsRetrieved(List<VersionDto> result) {
        Collections.sort(result);
        List<Integer> versions = new ArrayList<>(result.size());
        for (VersionDto schema : result) {
            versions.add(schema.getVersion());
        }
        if (!create) {
            detailsView.getMinSchemaVersion().setValue(entity.getMinLogSchemaVersion());
            detailsView.getMaxSchemaVersion().setValue(entity.getMaxLogSchemaVersion());
            detailsView.getConfirmDelivery().setValue(entity.isConfirmDelivery());
            detailsView.setMetadataListBox(entity.getHeaderStructure());
        }
        detailsView.setSchemaVersions(versions);
    }

    @Override
    protected void onSave() {
        super.onSave();
        entity.setMinLogSchemaVersion(detailsView.getMinSchemaVersion().getValue());
        entity.setMaxLogSchemaVersion(detailsView.getMaxSchemaVersion().getValue());
        entity.setConfirmDelivery(detailsView.getConfirmDelivery().getValue());        
        entity.setHeaderStructure(detailsView.getHeader());
    }

    @Override
    protected void getEntity(String id, AsyncCallback<LogAppenderDto> callback) {
        KaaAdmin.getDataSource().getLogAppenderForm(id, callback);
    }

    @Override
    protected void editEntity(LogAppenderDto entity, AsyncCallback<LogAppenderDto> callback) {
        KaaAdmin.getDataSource().editLogAppenderForm(entity, callback);
    }


}
