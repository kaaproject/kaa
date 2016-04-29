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
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.EventMapDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.AefMapPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.AefMapView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class AefMapActivity
        extends
        AbstractDetailsActivity<ApplicationEventFamilyMapDto, AefMapView, AefMapPlace> {

    private EventMapDataProvider eventMapDataProvider;

    public AefMapActivity(AefMapPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
        AbstractGrid<ApplicationEventMapDto, String> eventMapGrid = detailsView.getEventMapGrid();
        eventMapDataProvider = new EventMapDataProvider(eventMapGrid, detailsView);
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
        if (create) {
            registrations.add(detailsView.getEcf().addValueChangeHandler(new ValueChangeHandler<EcfInfoDto>() {
                @Override
                public void onValueChange(ValueChangeEvent<EcfInfoDto> event) {
                    EcfInfoDto ecf = detailsView.getEcf().getValue();
                    eventMapDataProvider.setEcf(ecf);
                    eventMapDataProvider.reload();
                }
            }));
        }
    }

    @Override
    protected String getEntityId(AefMapPlace place) {
        return place.getAefMapId();
    }

    @Override
    protected AefMapView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateAefMapView();
        } else {
            return clientFactory.getAefMapView();
        }
    }

    @Override
    protected ApplicationEventFamilyMapDto newEntity() {
        ApplicationEventFamilyMapDto aefMap = new ApplicationEventFamilyMapDto();
        aefMap.setApplicationId(place.getApplicationId());
        return aefMap;
    }

    @Override
    protected void onEntityRetrieved() {
        if (create) {
            KaaAdmin.getDataSource().getVacantEventClassFamilies(place.getApplicationId(), new BusyAsyncCallback<List<EcfInfoDto>> () {
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }

                @Override
                public void onSuccessImpl(List<EcfInfoDto> result) {
                    detailsView.updateEcfs(result);
                }
            });
        } else {
            detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
            detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
            detailsView.getEcfName().setValue(entity.getEcfName());
            detailsView.getEcfVersion().setValue(entity.getVersion()+"");
            eventMapDataProvider.setEventMaps(entity.getEventMaps());
            eventMapDataProvider.reload();
        }
    }

    @Override
    protected void onSave() {
        EcfInfoDto ecf = detailsView.getEcf().getValue();
        entity.setEcfId(ecf.getEcfId());
        entity.setEcfName(ecf.getEcfName());
        entity.setVersion(ecf.getVersion());
        entity.setEventMaps(eventMapDataProvider.getData());
    }

    @Override
    protected void getEntity(String id, AsyncCallback<ApplicationEventFamilyMapDto> callback) {
        KaaAdmin.getDataSource().getApplicationEventFamilyMap(id, callback);
    }

    @Override
    protected void editEntity(ApplicationEventFamilyMapDto entity,
            AsyncCallback<ApplicationEventFamilyMapDto> callback) {
        KaaAdmin.getDataSource().editApplicationEventFamilyMap(entity, callback);
    }
 
}
