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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderInfoDto;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LogAppenderActivity extends AbstractDetailsActivity<LogAppenderDto, LogAppenderView, LogAppenderPlace> {

    private String applicationId;

    public LogAppenderActivity(LogAppenderPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    @Override
    protected String getEntityId(LogAppenderPlace place) {
        return place.getAppenderId();
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
    protected void onEntityRetrieved() {
        KaaAdmin.getDataSource().loadAppenderInfos(new AsyncCallback<List<LogAppenderInfoDto>>() {
            @Override
            public void onSuccess(List<LogAppenderInfoDto> result) {
                detailsView.getAppenderInfo().setAcceptableValues(result);
            }
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
        });
    
        KaaAdmin.getDataSource().loadLogSchemasVersion(applicationId, new AsyncCallback<List<SchemaDto>>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
            @Override
            public void onSuccess(List<SchemaDto> result) {
                onSchemaVersionsRetrieved(result);
            }
        });
    }
    
    private void onSchemaVersionsRetrieved(List<SchemaDto> result) {
        Collections.sort(result);
        List<Integer> versions = new ArrayList<>(result.size());
        for (SchemaDto schema : result) {
            versions.add(schema.getMajorVersion());
        }
        if (!create) {
            detailsView.getName().setValue(entity.getName());
            detailsView.getDescription().setValue(entity.getDescription());
            detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
            detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
            detailsView.getMinSchemaVersion().setValue(entity.getMinLogSchemaVersion());
            detailsView.getMaxSchemaVersion().setValue(entity.getMaxLogSchemaVersion());
            detailsView.getConfirmDelivery().setValue(entity.isConfirmDelivery());
            detailsView.setMetadataListBox(entity.getHeaderStructure());
            detailsView.getConfiguration().setValue(entity.getFieldConfiguration());
            LogAppenderInfoDto appenderInfo = 
                    new LogAppenderInfoDto(entity.getPluginTypeName(), entity.getFieldConfiguration(), entity.getPluginClassName());
            detailsView.getAppenderInfo().setValue(appenderInfo);
        }
        detailsView.setSchemaVersions(versions);
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setMinLogSchemaVersion(detailsView.getMinSchemaVersion().getValue());
        entity.setMaxLogSchemaVersion(detailsView.getMaxSchemaVersion().getValue());
        entity.setConfirmDelivery(detailsView.getConfirmDelivery().getValue());        
        entity.setDescription(detailsView.getDescription().getValue());
        entity.setHeaderStructure(detailsView.getHeader());
        LogAppenderInfoDto appenderInfo = detailsView.getAppenderInfo().getValue();
        entity.setPluginTypeName(appenderInfo.getName());
        entity.setPluginClassName(appenderInfo.getAppenderClassName());
        entity.setFieldConfiguration(detailsView.getConfiguration().getValue());
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
