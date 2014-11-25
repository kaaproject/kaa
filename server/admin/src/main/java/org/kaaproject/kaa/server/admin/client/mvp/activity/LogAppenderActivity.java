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

import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderFormWrapper;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderInfoDto;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ValueListBox;

public class LogAppenderActivity extends AbstractDetailsActivity<LogAppenderFormWrapper, LogAppenderView, LogAppenderPlace> {

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
    protected LogAppenderFormWrapper newEntity() {
        LogAppenderFormWrapper dto = new LogAppenderFormWrapper();
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
                detailsView.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
        
        if (create) {
            KaaAdmin.getDataSource().loadLogSchemasVersion(applicationId, new AsyncCallback<List<SchemaDto>>() {

                @Override
                public void onFailure(Throwable caught) {
                    detailsView.setErrorMessage(Utils.getErrorMessage(caught));
                }

                @Override
                public void onSuccess(List<SchemaDto> result) {
                    ValueListBox<SchemaDto> versions = detailsView.getSchemaVersions();
                    versions.setValue(Utils.getMaxSchemaVersions(result));
                    versions.setAcceptableValues(result);
                }
            });
        } else {
            detailsView.getName().setValue(entity.getName());
            detailsView.getStatus().setValue(entity.getStatus() == LogAppenderStatusDto.REGISTERED);

            detailsView.getDescription().setValue(entity.getDescription());
            detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
            detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
            detailsView.getSchemaVersions().setValue(entity.getSchema());
            detailsView.setMetadataListBox(entity.getHeaderStructure());
            detailsView.getConfiguration().setValue(entity.getConfiguration());
            LogAppenderInfoDto appenderInfo = 
                    new LogAppenderInfoDto(entity.getTypeName(), entity.getConfiguration(), entity.getAppenderClassName());
            detailsView.getAppenderInfo().setValue(appenderInfo);
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setSchema(detailsView.getSchemaVersions().getValue());
        entity.setStatus(LogAppenderStatusDto.REGISTERED);
        entity.setDescription(detailsView.getDescription().getValue());
        entity.setHeaderStructure(detailsView.getHeader());
        LogAppenderInfoDto appenderInfo = detailsView.getAppenderInfo().getValue();
        entity.setTypeName(appenderInfo.getName());
        entity.setAppenderClassName(appenderInfo.getAppenderClassName());
        entity.setConfiguration(detailsView.getConfiguration().getValue());
    }

    @Override
    protected void getEntity(String id, AsyncCallback<LogAppenderFormWrapper> callback) {
        KaaAdmin.getDataSource().getLogAppenderForm(id, callback);
    }

    @Override
    protected void editEntity(LogAppenderFormWrapper entity, AsyncCallback<LogAppenderFormWrapper> callback) {
        KaaAdmin.getDataSource().editLogAppenderForm(entity, callback);
    }

}
