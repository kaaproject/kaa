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

import com.google.gwt.activity.shared.AbstractActivity;
import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.EndpointProfileDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfilesView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EndpointProfilesActivity extends AbstractActivity implements BaseListView.Presenter {

    protected final ClientFactory clientFactory;
    private EndpointProfilesView listView;
    private EndpointProfilesPlace place;
    private String applicationId;
    private EndpointProfileDataProvider dataProvider;
    private EndpointGroupDto groupAll;
    private List<HandlerRegistration> registrations = new ArrayList<>();

    public EndpointProfilesActivity(EndpointProfilesPlace place, ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        listView = clientFactory.getEndpointProfilesView();
        dataProvider = new EndpointProfileDataProvider(listView.getListWidget(), listView, this.applicationId);
        listView.setPresenter(this);
        bind();
        containerWidget.setWidget(listView.asWidget());
    }

    private void bind() {

        listView.clearError();

        registrations.add(listView.getResetButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                reset();
            }
        }));

        registrations.add(listView.getRowActionsSource().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> event) {
                String id = event.getClickedId();
                if (event.getAction()==RowActionEvent.CLICK) {
                    goTo(new EndpointProfilePlace(applicationId, id));
                } else if (event.getAction()==RowActionEvent.DELETE) {
                    deleteEntity(id, new BusyAsyncCallback<Void>() {
                        @Override
                        public void onFailureImpl(Throwable caught) {
                            Utils.handleException(caught, listView);
                        }

                        @Override
                        public void onSuccessImpl(Void result) {
                            dataProvider.update();
                        }
                    });
                }
            }
        }));

        registrations.add(listView.getEndpointGroupsInfo().addValueChangeHandler(new ValueChangeHandler<EndpointGroupDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<EndpointGroupDto> valueChangeEvent) {
                findByEndpointGroup();
            }
        }));

        registrations.add(listView.getFindEndpointButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                findByEndpointKeyHash();
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
        registrations.add(listView.getEndpointGroupButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                findByEndpointGroup();
            }
        }));
        registrations.add(listView.getEndpointKeyHashButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                findByEndpointKeyHash();
            }
        }));

        reset();
    }

    private void findByEndpointGroup() {
        listView.getEndpointGroupButton().setValue(true);
        listView.getEndpointKeyHashButton().setValue(false);
        dataProvider.setNewGroup(listView.getEndpointGroupsInfo().getValue().getId());
        dataProvider.update();
    }

    private void findByEndpointKeyHash() {
        listView.getEndpointGroupButton().setValue(false);
        listView.getEndpointKeyHashButton().setValue(true);
        listView.clearError();
        String value = listView.getEndpointKeyHashTextBox().getValue();
        dataProvider.setEndpointKeyHash(value);
        dataProvider.update();
    }

    private void reset() {
        listView.getEndpointKeyHashTextBox().setValue("");
        listView.getEndpointKeyHashButton().setValue(false);
        listView.getEndpointGroupButton().setValue(true);
        listView.getEndpointGroupsInfo().reset();
        getGroupsList();
    }

    private void getGroupsList() {
        KaaAdmin.getDataSource().loadEndpointGroups(applicationId, new AsyncCallback<List<EndpointGroupDto>>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }

            @Override
            public void onSuccess(List<EndpointGroupDto> result) {
                populateListBoxAndGrid(result);
            }
        });
    }

    private void populateListBoxAndGrid(List<EndpointGroupDto> result) {
        for (EndpointGroupDto endGroup: result) {
            if (endGroup.getWeight() == 0) {
                groupAll = endGroup;
                listView.getEndpointGroupsInfo().setValue(groupAll, true);
            }
        }
        listView.getEndpointGroupsInfo().setAcceptableValues(result);
    }

    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
    }

    private void deleteEntity(String endpointKeyHash, AsyncCallback<Void> callback){
        KaaAdmin.getDataSource().removeEndpointProfileByKeyHash(endpointKeyHash, callback);
    }

}