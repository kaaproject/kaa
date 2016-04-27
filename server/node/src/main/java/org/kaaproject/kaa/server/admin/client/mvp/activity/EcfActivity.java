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

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.EcfSchemasDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EcfActivity
        extends
        AbstractDetailsActivity<EventClassFamilyDto, EcfView, EcfPlace> {

    private EcfSchemasDataProvider ecfSchemasDataProvider;

    public EcfActivity(EcfPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
        if (!create) {
            AbstractGrid<EventSchemaVersionDto, Integer> ecfSchemasGrid = detailsView.getEcfSchemasGrid();
            ecfSchemasDataProvider = new EcfSchemasDataProvider(ecfSchemasGrid, detailsView);
        }
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);

        registrations.add(detailsView.getAddEcfSchemaButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                addEcfSchema();
            }
          }));

        registrations.add(detailsView.getEcfSchemasGrid().addRowActionHandler(new RowActionEventHandler<Integer>() {
              @Override
              public void onRowAction(RowActionEvent<Integer> event) {
                  Integer id = event.getClickedId();
                  if (event.getAction()==RowActionEvent.CLICK) {
                      EcfSchemaPlace ecfSchemaPlace = new EcfSchemaPlace(entityId, id);
                      ecfSchemaPlace.setPreviousPlace(place);
                      goTo(ecfSchemaPlace);
                  }
              }
          }));
    }

    @Override
    protected String getEntityId(EcfPlace place) {
        return place.getEcfId();
    }

    @Override
    protected EcfView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateEcfView();
        } else {
            return clientFactory.getEcfView();
        }
    }

    @Override
    protected EventClassFamilyDto newEntity() {
        EventClassFamilyDto ecf = new EventClassFamilyDto();
        return ecf;
    }

    @Override
    protected void onEntityRetrieved() {
        detailsView.getName().setValue(entity.getName());
        detailsView.getNamespace().setValue(entity.getNamespace());
        detailsView.getClassName().setValue(entity.getClassName());
        detailsView.getDescription().setValue(entity.getDescription());
        detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
        if (!create) {
            ecfSchemasDataProvider.setSchemas(entity.getSchemas());
            ecfSchemasDataProvider.reload();
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setNamespace(detailsView.getNamespace().getValue());
        entity.setClassName(detailsView.getClassName().getValue());
        entity.setDescription(detailsView.getDescription().getValue());
    }

    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        editEntity(entity,
            new BusyAsyncCallback<EventClassFamilyDto>() {
                public void onSuccessImpl(EventClassFamilyDto result) {
                    if (create) {
                        goTo(new EcfPlace(result.getId()));
                    } else {
                        goTo(place.getPreviousPlace());
                    }
                }

                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }
            });
    }

    @Override
    protected void getEntity(String id, AsyncCallback<EventClassFamilyDto> callback) {
        KaaAdmin.getDataSource().getEcf(id, callback);
    }

    @Override
    protected void editEntity(EventClassFamilyDto entity,
            AsyncCallback<EventClassFamilyDto> callback) {
        KaaAdmin.getDataSource().editEcf(entity, callback);
    }

    private void addEcfSchema() {
        EcfSchemaPlace ecfSchemaPlace = new EcfSchemaPlace(entityId, -1);
        ecfSchemaPlace.setPreviousPlace(place);
        goTo(ecfSchemaPlace);
    }

}
