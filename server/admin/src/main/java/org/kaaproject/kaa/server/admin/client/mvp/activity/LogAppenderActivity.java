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

import static org.kaaproject.kaa.server.admin.client.util.Utils.isNotBlank;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderInfoDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.CustomAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FileAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlexTable;

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
                    SchemaListBox versions = detailsView.getSchemaVersions();
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

            LogAppenderTypeDto type = entity.getType();
            switch (type) {
                case FILE:
                    FileAppenderParametersDto file = (FileAppenderParametersDto) entity.getProperties().getParameters();
                    detailsView.setPublicKey(file.getSshKey());
                    detailsView.getAppenderInfo().setValue(new LogAppenderInfoDto(type));
                    break;
                case MONGO:
                    detailsView.hideFileCongurationFields();
                    detailsView.getAppenderInfo().setValue(new LogAppenderInfoDto(type));
                    break;
                case FLUME:
                    FlumeAppenderParametersDto flume = (FlumeAppenderParametersDto) entity.getProperties().getParameters();
                    detailsView.showFlumeCongurationFields(flume);
                    detailsView.getAppenderInfo().setValue(new LogAppenderInfoDto(type));
                    break;
                case CUSTOM:
                    CustomAppenderParametersDto custom = (CustomAppenderParametersDto) entity.getProperties().getParameters();
                    detailsView.hideFileCongurationFields();
                    detailsView.showCustomConfigurationFields();
                    detailsView.setConfiguration(custom.getConfiguration());
                    LogAppenderInfoDto appenderInfo = 
                            new LogAppenderInfoDto(type, custom.getName(), custom.getConfiguration(), custom.getAppenderClassName());
                    detailsView.getAppenderInfo().setValue(appenderInfo);
                    break;
            }
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
        entity.setType(appenderInfo.getType());
        LogAppenderParametersDto parameters = new LogAppenderParametersDto();
        switch (appenderInfo.getType()) {
            case FILE:
                FileAppenderParametersDto fileDto = new FileAppenderParametersDto();
                fileDto.setSshKey(detailsView.getPublicKey());
                parameters.setParameters(fileDto);
                break;
            case MONGO:
                break;
            case FLUME:
                FlumeAppenderParametersDto flume = new FlumeAppenderParametersDto();
                FlumeBalancingTypeDto type = detailsView.getFlumeBalancingType().getValue();
                flume.setBalancingType(type);
                FlexTable table = detailsView.getHostTable();
                List<HostInfoDto> hosts = new ArrayList<>();
                for (int i = 1; i < table.getRowCount(); i++) {
                    String host = ((SizedTextBox) table.getWidget(i, 0)).getValue();
                    String port = ((SizedTextBox) table.getWidget(i, 1)).getValue();
                    if (FlumeBalancingTypeDto.PRIORITIZED.equals(type)) {
                        String priority = ((SizedTextBox) table.getWidget(i, 2)).getValue();
                        if (isNotBlank(host) && isNotBlank(port) && isNotBlank(priority)) {
                            hosts.add(new HostInfoDto(host, Integer.valueOf(port), Integer.valueOf(priority)));
                        }
                    } else if (isNotBlank(host) && isNotBlank(port)) {
                        hosts.add(new HostInfoDto(host, Integer.valueOf(port), 0));
                    }
                }
                flume.setHosts(hosts);
                parameters.setParameters(flume);
                break;
            case CUSTOM:
                CustomAppenderParametersDto customParameters = new CustomAppenderParametersDto();
                customParameters.setName(appenderInfo.getName());
                customParameters.setAppenderClassName(appenderInfo.getAppenderClassName());
                customParameters.setConfiguration(detailsView.getConfiguration());
                parameters.setParameters(customParameters);
                break;
        }
        entity.setProperties(parameters);
    }

    @Override
    protected void getEntity(String id, AsyncCallback<LogAppenderDto> callback) {
        KaaAdmin.getDataSource().getLogAppender(id, callback);
    }

    @Override
    protected void editEntity(LogAppenderDto entity, AsyncCallback<LogAppenderDto> callback) {
        KaaAdmin.getDataSource().editLogAppender(entity, callback);
    }

}
