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
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEventHandler;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public abstract class AbstractListActivity<T extends HasId, P extends TreePlace> extends AbstractActivity implements BaseListView.Presenter {

    protected final ClientFactory clientFactory;
    private final Class<T> dataClass;

    protected AbstractDataProvider<T, String> dataProvider;

    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    protected BaseListView<T> listView;
    protected P place;

    public AbstractListActivity(P place, Class<T> dataClass, ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.dataClass = dataClass;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        listView = getView();
        this.dataProvider = getDataProvider(listView.getListWidget());
        listView.setPresenter(this);
        bind(eventBus);
        containerWidget.setWidget(listView.asWidget());
    }

    protected abstract BaseListView<T> getView();

    protected abstract AbstractDataProvider<T, String> getDataProvider(AbstractGrid<T,String> dataGrid);

    protected abstract Place newEntityPlace();

    protected abstract Place existingEntityPlace(String id);

    protected abstract void deleteEntity(String id, AsyncCallback<Void> callback);

    @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
          registration.removeHandler();
        }
        registrations.clear();
    }

    /**
     * Navigate to a new Place in the browser
     */
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

      public void bind(final EventBus eventBus) {

          listView.clearError();

          registrations.add(listView.getAddButton().addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                  goTo(newEntityPlace());
              }
            }));

          registrations.add(listView.getRowActionsSource().addRowActionHandler(new RowActionEventHandler<String>() {
                @Override
                public void onRowAction(RowActionEvent<String> event) {
                    String id = event.getClickedId();
                    if (event.getAction()==RowActionEvent.CLICK) {
                        goTo(existingEntityPlace(id));
                    } else if (event.getAction()==RowActionEvent.DELETE) {
                        deleteEntity(id, new BusyAsyncCallback<Void>() {
                            @Override
                            public void onFailureImpl(Throwable caught) {
                                Utils.handleException(caught, listView);
                            }

                            @Override
                            public void onSuccessImpl(Void result) {}
                        });
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

      protected void onCustomRowAction(RowActionEvent<String> event) {}
      protected void onCustomDataChangedEvent(DataEvent event) {}

}
