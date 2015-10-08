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

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.EndpointProfilesDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfilesView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EndpointProfilesActivity extends AbstractActivity implements BaseListView.Presenter {

    private String applicationId;

    protected final ClientFactory clientFactory;

    private EndpointProfilesDataProvider dataProvider;

    private List<HandlerRegistration> registrations = new ArrayList<>();

    private EndpointProfilesView listView;
    private EndpointProfilesPlace place;



    protected List<EndpointProfileDto> data;




    public EndpointProfilesActivity(EndpointProfilesPlace place, ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        listView = getView();
        getGroupsList();
        listView.setPresenter(this);
        bind();
        containerWidget.setWidget(listView.asWidget());


    }



    private void loadDataDirectly(String groupID, String limit, String offset) {
        KaaAdmin.getDataSource().getEndpointProfileByGroupID(groupID, limit, offset,
                new AsyncCallback<EndpointProfilesPageDto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                            /*
                                dirty hack because of exception throwing on empty list in
                                KaaAdminServiceImpl#getEndpointProfileByEndpointGroupId()
                             */
                        if (caught instanceof KaaAdminServiceException) {
                            if (((KaaAdminServiceException) caught).getErrorCode() == ServiceErrorCode.ITEM_NOT_FOUND) {
                                updateData(new ArrayList<EndpointProfileDto>());
                            }
                        } else Utils.handleException(caught, listView);
                    }

                    @Override
                    public void onSuccess(EndpointProfilesPageDto result) {
                        updateData(result.getEndpointProfiles());
                    }
                });
    }


    private void updateData (List<EndpointProfileDto> data) {

        GWT.log("hahahaha: " + data.size());
        ColumnSortList sortList = listView.getListWidget().getDataGrid().getColumnSortList();
        Column<?,?> column = (sortList == null || sortList.size() == 0) ? null
                : sortList.get(0).getColumn();
        boolean isSortAscending = (sortList == null || sortList.size() == 0) ? false
                : sortList.get(0).isAscending();
        if (column != null) {
            listView.getListWidget().sort(data, column, isSortAscending);
        }
        updateRowData(listView.getListWidget().getDataGrid(), 0, data);
    }

    protected void updateRowData(HasData<EndpointProfileDto> display, int start, List<EndpointProfileDto> values) {
        int end = start + values.size();
        Range range = display.getVisibleRange();
        int curStart = range.getStart();
        int curLength = range.getLength();
        int curEnd = curStart + curLength;
        if(start == curStart || curStart < end && curEnd > start) {
            int realStart = curStart < start?start:curStart;
            int realEnd = curEnd > end?end:curEnd;
            int realLength = realEnd - realStart;
            List realValues = values.subList(realStart - start, realStart - start + realLength);
            display.setRowData(realStart, realValues);
        }

    }

    private void findEndpointByKeyHash(String endpointKeyHash) {
        KaaAdmin.getDataSource().getEndpointProfileByKeyHash(endpointKeyHash, new AsyncCallback<EndpointProfileDto>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(EndpointProfileDto endpointProfileDto) {
                goTo(new EndpointProfilePlace(applicationId, endpointProfileDto.getId()));
            }
        });
    }

    private void getGroupsList() {
        KaaAdmin.getDataSource().loadEndpointGroups(applicationId, new AsyncCallback<List<EndpointGroupDto>>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }
            @Override
            public void onSuccess(List<EndpointGroupDto> result) {
                populateListBox(result);
            }
        });
    }

    private void populateListBox(List<EndpointGroupDto> result) {
        for (EndpointGroupDto endGroup: result) {
            if (endGroup.getWeight() == 0) {
//                dataProvider = getDataProvider(listView.getListWidget(), endGroup.getId());
                loadDataDirectly(endGroup.getId(), "10", "0");
                listView.getEndpointGroupsInfo().setValue(endGroup);
            }
        }
        listView.getEndpointGroupsInfo().setAcceptableValues(result);
    }

    private void bind() {

        listView.clearError();

        registrations.add(listView.getRowActionsSource().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> event) {
                String id = event.getClickedId();
                if (event.getAction()==RowActionEvent.CLICK) {
                    goTo(existingEntityPlace(id));
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

        listView.getEndpointGroupsInfo().addValueChangeHandler(new ValueChangeHandler<EndpointGroupDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<EndpointGroupDto> valueChangeEvent) {
                dataProvider.setGroupID(valueChangeEvent.getValue().getId());
                dataProvider.reload();
            }
        });

        listView.getFindEndpointButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                findEndpointByKeyHash(listView.getEndpointKeyHashTextBox().getValue());
                listView.getEndpointKeyHashTextBox().setValue("", false);
            }
        });
    }

    private Place existingEntityPlace(String id) {
        return new EndpointProfilePlace(applicationId, id);
    }

    private EndpointProfilesView getView() {
        return clientFactory.getEndpointProfilesView();
    }

    private EndpointProfilesDataProvider getDataProvider(AbstractGrid<EndpointProfileDto, ?> dataGrid, String groupID) {
        return new EndpointProfilesDataProvider(dataGrid, listView, groupID);
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
}
