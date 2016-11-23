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
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.EcfVersionDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEventHandler;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfVersionPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EventClassPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfVersionView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EcfVersionActivity
    extends AbstractBaseCtlSchemasActivity<EventClassDto, EcfVersionPlace> {

  AbstractGrid<EventClassDto, String> dataGrid;
  private String ecfId;
  private String ecfVersionId;
  private int ecfVersion;
  private EcfVersionView listView;

  /**
   * Instantiates a new EcfVersionActivity.
   */
  public EcfVersionActivity(EcfVersionPlace place, ClientFactory clientFactory) {
    super(place, EventClassDto.class, clientFactory);
    this.ecfId = place.getEcfId();
    this.ecfVersion = place.getEcfVersion();
    this.ecfVersionId = place.getEcfVersionId();
    initListView();
    this.listView.setPresenter(this);
    setEnabledForAddButton();
  }

  private void setEnabledForAddButton() {
    listView.addButton().setEnabled(false);
    if (place.getEventClassDtoList() != null && !place.getEventClassDtoList().isEmpty()) {
      listView.addButton().setEnabled(true);
    }
  }

  private void initListView() {
    if (ecfVersionId == null || ecfVersionId.isEmpty()) {
      this.listView = clientFactory.getCreateEcfVersionView();
    } else {
      this.listView = clientFactory.getEcfVersionView();
    }
  }

  @Override
  protected BaseListView<EventClassDto> getView() {
    if (ecfVersionId == null || ecfVersionId.isEmpty()) {
      return clientFactory.getCreateEcfVersionView();
    }
    return clientFactory.getEcfVersionView();
  }

  @Override
  protected AbstractDataProvider<EventClassDto, String> getDataProvider(
      AbstractGrid<EventClassDto, String> dataGrid) {
    this.dataGrid = dataGrid;
    return new EcfVersionDataProvider(dataGrid, listView, ecfId,
        ecfVersion, place.getEventClassDtoList());
  }

  @Override
  protected Place newEntityPlace() {
    return new EventClassPlace(ecfId, ecfVersionId, ecfVersion, "", place.getEventClassDtoList());
  }

  @Override
  protected Place existingEntityPlace(String id) {
    return new EventClassPlace(ecfId, ecfVersionId, ecfVersion, id, place.getEventClassDtoList());
  }

  @Override
  public void bind(final EventBus eventBus) {

    listView.clearError();

    registrations.add(listView.addButtonEventClass().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        goTo(newEntityPlace());
      }
    }));

    registrations.add(listView.getAddButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {

        KaaAdmin.getDataSource().addEventClassFamilyVersionFromView(place.getEcfId(),
            place.getEventClassDtoList(),
            new AsyncCallback<Void>() {
              @Override
              public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
              }

              @Override
              public void onSuccess(Void callback) {
                place.setEventClassDtoList(null);
                goTo(new EcfPlace(place.getEcfId()));
              }
            });
      }
    }));

    registrations.add(listView.getRowActionsSource().addRowActionHandler(
        new RowActionEventHandler<String>() {
          @Override
          public void onRowAction(RowActionEvent<String> event) {
            String id = String.valueOf(event.getClickedId());
            if (event.getAction() == RowActionEvent.CLICK) {
              goTo(existingEntityPlace(id));
            }
            onCustomRowAction(event);
          }
        }));

    registrations.add(eventBus.addHandler(DataEvent.getType(), new DataEventHandler() {
      @Override
      public void onDataChanged(DataEvent event) {
        if (event.checkClass(dataClass)) {
          dataProvider.reload();
          onCustomDataChangedEvent(event);
        }
      }
    }));

    final Place previousPlace = place.getPreviousPlace();
    if (previousPlace != null) {
      listView.setBackEnabled(true);
      registrations.add(listView.getBackButton().addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          goTo(previousPlace);
        }
      }));
    }
  }

  @Override
  protected void deleteEntity(final String id, final AsyncCallback<Void> callback) {
    EcfVersionActivity.this.getView().clearError();
  }

}