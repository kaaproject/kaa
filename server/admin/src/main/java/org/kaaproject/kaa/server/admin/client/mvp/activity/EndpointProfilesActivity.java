/*
 * Copyright 2015 CyberVision, Inc.
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

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfilesView;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;

import java.util.ArrayList;
import java.util.List;

public class EndpointProfilesActivity extends AbstractActivity implements BaseListView.Presenter {

    protected final ClientFactory clientFactory;
    private EndpointProfilesView listView;
    private EndpointProfilesPlace place;
    private String applicationId;
    private boolean gridLoaded;

    private AbstractEndpointProfileDataProvider dataProvider;
    private List<HandlerRegistration> registrations = new ArrayList<>();

    public EndpointProfilesActivity(EndpointProfilesPlace place, ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.applicationId = place.getApplicationId();
        this.gridLoaded = place.isGridLoaded();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        listView = clientFactory.getEndpointProfilesView();
        getGroupsList();
        listView.setPresenter(this);
        bind();
        containerWidget.setWidget(listView.asWidget());
    }

    private void getGroupsList() {
        KaaAdmin.getDataSource().loadEndpointGroups(applicationId, new AsyncCallback<List<EndpointGroupDto>>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }
            @Override
            public void onSuccess(List<EndpointGroupDto> result) {
                if (!gridLoaded) {
                    populateListBox(result);
                } else {
                    dataProvider = new EndpointProfileDataProvider(listView.getListWidget(), listView, null);
                }
            }
        });
    }

    private void populateListBox(List<EndpointGroupDto> result) {
        for (EndpointGroupDto endGroup: result) {
            if (endGroup.getWeight() == 0) {
                dataProvider = getDataProvider(endGroup.getId());
                listView.getEndpointGroupsInfo().setValue(endGroup);
            }
        }
        listView.getEndpointGroupsInfo().setAcceptableValues(result);
        gridLoaded = true;
    }

    private void bind() {

        listView.clearError();

        registrations.add(listView.getRowActionsSource().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> event) {
                String id = event.getClickedId();
                if (event.getAction()==RowActionEvent.CLICK) {
                    goTo(new EndpointProfilePlace(applicationId, id, gridLoaded));
                }
            }
        }));

        registrations.add(listView.getEndpointGroupsInfo().addValueChangeHandler(new ValueChangeHandler<EndpointGroupDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<EndpointGroupDto> valueChangeEvent) {
                dataProvider.setNewGroup(valueChangeEvent.getValue().getId());
                listView.getListWidget().getDataGrid().setVisibleRangeAndClearData(
                        new Range(0, Integer.valueOf(EndpointProfileDataProvider.DEFAULT_LIMIT) -1),true);
            }
        }));

        registrations.add(listView.getFindEndpointButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                findEndpointFromThisApplication(listView.getEndpointKeyHashTextBox().getValue());
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

    private void findEndpointFromThisApplication(String endpointKeyHash) {
        KaaAdmin.getDataSource().getEndpointProfileByKeyHash(endpointKeyHash, new AsyncCallback<EndpointProfileDto>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }

            @Override
            public void onSuccess(EndpointProfileDto endpointProfileDto) {
                List<EndpointProfileDto> result = new ArrayList<>();
                if (endpointProfileDto.getApplicationId().equals(applicationId)) {
                    result.add(endpointProfileDto);
                }
                listView.getListWidget().getDataGrid().setRowData(result);
            }
        });
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
        dataProvider.removeDataDisplay(listView.getListWidget().getDataGrid());
        dataProvider = null;
    }

    public AbstractEndpointProfileDataProvider getDataProvider(String groupID) {
        return new EndpointProfileDataProvider(listView.getListWidget(), listView, groupID);
    }

    private abstract class AbstractEndpointProfileDataProvider extends AbstractDataProvider<EndpointProfileDto> {

        public AbstractEndpointProfileDataProvider(AbstractGrid<EndpointProfileDto, ?> dataGrid,
                                                   HasErrorMessage hasErrorMessage,
                                                   boolean addDisplay) {
            super(dataGrid, hasErrorMessage, addDisplay);
        }

        protected abstract void setNewGroup(String groupID);
    }

    private class EndpointProfileDataProvider extends AbstractEndpointProfileDataProvider {

        public static final String DEFAULT_LIMIT = "11";
        public static final String DEFAULT_OFFSET = "0";
        private String limit = DEFAULT_LIMIT;
        private String offset = DEFAULT_OFFSET;
        private String groupID;
        private List<EndpointProfileDto> endpointProfilesList;
        private int previousStart = -1;

        public EndpointProfileDataProvider(AbstractGrid<EndpointProfileDto, ?> dataGrid,
                                           HasErrorMessage hasErrorMessage,
                                           String groupID) {
            super(dataGrid, hasErrorMessage, false);
            this.groupID = groupID;
            endpointProfilesList = new ArrayList<>();
            addDataDisplay();
        }

        @Override
        protected void onRangeChanged(HasData<EndpointProfileDto> display) {
            if (groupID != null) {
                int start = display.getVisibleRange().getStart();
                if (previousStart < start) {
                    previousStart = start;
                    setLoaded(false);
                }
                super.onRangeChanged(display);
            }
        }

        @Override
        protected void loadData(final LoadCallback callback) {
            KaaAdmin.getDataSource().getEndpointProfileByGroupID(groupID, limit, offset,
                    new AsyncCallback<EndpointProfilesPageDto>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            if (caught instanceof KaaAdminServiceException) {
                                if (((KaaAdminServiceException) caught).getErrorCode() == ServiceErrorCode.ITEM_NOT_FOUND) {
                                    endpointProfilesList.clear();
                                    callback.onSuccess(endpointProfilesList);
                                }
                            } else Utils.handleException(caught, listView);
                        }

                        @Override
                        public void onSuccess(EndpointProfilesPageDto result) {
                            endpointProfilesList.addAll(result.getEndpointProfiles());
                            offset = result.getPageLinkDto().getOffset();
                            callback.onSuccess(endpointProfilesList);
                        }
                    });
        }

        @Override
        public void setNewGroup(String groupID) {
            this.groupID = groupID;
            reset();
        }

        private void reset() {
            endpointProfilesList.clear();
            previousStart = -1;
            limit = DEFAULT_LIMIT;
            offset = DEFAULT_OFFSET;
        }
    }
}
