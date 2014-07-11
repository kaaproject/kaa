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

import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.EventClassesDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EcfSchemaActivity
        extends
        AbstractDetailsActivity<EventClassFamilyDto, EcfSchemaView, EcfSchemaPlace> {

    private EventClassesDataProvider eventClassesDataProvider;

    public EcfSchemaActivity(EcfSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
            AbstractGrid<EventClassDto, String> eventClassesGrid = detailsView.getEventClassesGrid();
            eventClassesDataProvider = new EventClassesDataProvider(eventClassesGrid.getSelectionModel(),
                            new DataLoadCallback<EventClassDto>(detailsView), entityId, place.getVersion());

            eventClassesDataProvider.addDataDisplay(eventClassesGrid.getDisplay());
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
    }

    @Override
    protected String getEntityId(EcfSchemaPlace place) {
        return place.getEcfId();
    }

    @Override
    protected EcfSchemaView getView(boolean create) {
        return clientFactory.getEcfSchemaView();
    }

    @Override
    protected EventClassFamilyDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {
        EventSchemaVersionDto schema = null;
        for (EventSchemaVersionDto schemaVersion : entity.getSchemas()) {
            if (schemaVersion.getVersion()==place.getVersion()) {
                schema = schemaVersion;
                break;
            }
        }
        detailsView.getVersion().setValue(""+schema.getVersion());
        detailsView.getCreatedUsername().setValue(schema.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(schema.getCreatedTime()));
        detailsView.getSchema().setValue(schema.getSchema());
    }

    @Override
    protected void onSave() {
    }

    @Override
    protected void getEntity(String id, AsyncCallback<EventClassFamilyDto> callback) {
        KaaAdmin.getDataSource().getEcf(id, callback);
    }

    @Override
    protected void editEntity(EventClassFamilyDto entity,
            AsyncCallback<EventClassFamilyDto> callback) {
        callback.onSuccess(null);
    }

    class DataLoadCallback<T> implements AsyncCallback<List<T>> {

        private EcfSchemaView view;

        DataLoadCallback(EcfSchemaView view) {
            this.view = view;
        }

        @Override
        public void onFailure(Throwable caught) {
            view.setErrorMessage(Utils.getErrorMessage(caught));
        }

        @Override
        public void onSuccess(List<T> result) {
            view.clearError();
        }
    }
 
}
