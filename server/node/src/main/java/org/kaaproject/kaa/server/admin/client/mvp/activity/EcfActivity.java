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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.EcfVersionsDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfVersionPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import java.util.List;

public class EcfActivity
    extends
    AbstractDetailsActivity<EventClassFamilyDto, EcfView, EcfPlace> {

  private EcfVersionsDataProvider ecfVersionsDataProvider;

  public EcfActivity(EcfPlace place,
                     ClientFactory clientFactory) {
    super(place, clientFactory);
  }

  @Override
  public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
    super.start(containerWidget, eventBus);
    if (!create) {
      AbstractGrid<EventClassFamilyVersionDto, Integer> ecfVersionsGrid =
          detailsView.getEcfVersionsGrid();
      ecfVersionsDataProvider = new EcfVersionsDataProvider(ecfVersionsGrid, detailsView);
    }
  }

  protected void bind(final EventBus eventBus) {
    super.bind(eventBus);

    registrations.add(detailsView.getAddEcfVersionButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addEcfVersion();
      }
    }));

    registrations.add(detailsView.getEcfVersionsGrid().addRowActionHandler(
        new RowActionEventHandler<Integer>() {
          @Override
          public void onRowAction(RowActionEvent<Integer> event) {
            Integer id = event.getClickedId();
            if (event.getAction() == RowActionEvent.CLICK) {
              EcfVersionPlace ecfVersionPlace = new EcfVersionPlace(entityId, place.getEcfId(), id);
              ecfVersionPlace.setPreviousPlace(place);
              goTo(ecfVersionPlace);
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
    detailsView.getCreatedDateTime()
        .setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
    if (!create) {
      KaaAdmin.getDataSource().getEventClassFamilyVersions(entity.getId(),
          new AsyncCallback<List<EventClassFamilyVersionDto>>() {
            @Override
            public void onFailure(Throwable caught) {
              Utils.handleException(caught, EcfActivity.this.detailsView);
            }

            @Override
            public void onSuccess(List<EventClassFamilyVersionDto> eventClassFamilyVersionDtos) {
              ecfVersionsDataProvider.setSchemas(eventClassFamilyVersionDtos);
              ecfVersionsDataProvider.reload();
            }
          });
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

  private void addEcfVersion() {
    EcfVersionPlace ecfVersionPlace = new EcfVersionPlace(entityId, "", -1, null);
    ecfVersionPlace.setPreviousPlace(place);
    goTo(ecfVersionPlace);
  }

}
