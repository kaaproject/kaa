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

import static org.kaaproject.kaa.server.admin.client.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
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

            LogAppenderTypeDto type = entity.getType();
            detailsView.getType().setValue(type);
            switch(type) {
                case FILE:break;
                case MONGO:break;
                case FLUME:
                	FlumeAppenderParametersDto flume = (FlumeAppenderParametersDto) entity.getProperties().getParameters();
                	detailsView.showFlumeCongurationFields(flume);
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
        LogAppenderTypeDto appenderType = detailsView.getType().getValue(); 
        entity.setType(appenderType);
        switch(appenderType) {
        case FILE:
        case MONGO:
        	entity.setProperties(new LogAppenderParametersDto());
        	break;
        case FLUME:
        	FlumeAppenderParametersDto flume = new FlumeAppenderParametersDto();
        	FlumeBalancingTypeDto type = detailsView.getFlumeBalancingType().getValue(); 
        	flume.setBalancingType(type);
        	FlexTable table = detailsView.getHostTable();
        	List<HostInfoDto> hosts = new ArrayList<>();
			for (int i = 1; i < table.getRowCount(); i++) {
				String host = ((SizedTextBox)table.getWidget(i, 0)).getValue();
				String port = ((SizedTextBox)table.getWidget(i, 1)).getValue();
				if (FlumeBalancingTypeDto.PRIORITIZED.equals(type)) {
					String priority = ((SizedTextBox)table.getWidget(i, 2)).getValue();
					if (isNotBlank(host) && isNotBlank(port) && isNotBlank(priority)) {
						hosts.add(new HostInfoDto(host, Integer.valueOf(port), Integer.valueOf(priority)));
					}
				} else if (isNotBlank(host) && isNotBlank(port)) {
					hosts.add(new HostInfoDto(host, Integer.valueOf(port), 0));
				}
			}
			flume.setHosts(hosts);
			entity.setProperties(new LogAppenderParametersDto(flume));
			break;
		}
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
