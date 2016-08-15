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
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.DataSource;
import org.kaaproject.kaa.server.admin.client.mvp.data.TenantAdminDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class TenantActivity extends
        AbstractDetailsActivity<TenantDto, TenantView, TenantPlace> {

    public TenantActivity(TenantPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    private TenantAdminDataProvider usersDataProvider;

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
        if (!create) {
            AbstractGrid<UserDto, String> tenantAdminsGrid = detailsView.getTenantAdminsGrid();
            usersDataProvider = new TenantAdminDataProvider(tenantAdminsGrid, detailsView,entityId );
        }
    }

    @Override
    protected String getEntityId(TenantPlace place) {
        return place.getTenantId();
    }

    @Override
    protected TenantView getView(boolean create) {
      if (create) {
          return clientFactory.getCreateTenantView();
      } else {
          return clientFactory.getTenantView();
      }
    }

    @Override
    protected TenantDto newEntity() {
        return new TenantDto();
    }

      @Override
      protected void onEntityRetrieved() {
          if (!create) {
              detailsView.setTitle(entity.getName());
          }
          detailsView.getTenantName().setValue(entity.getName());
          if (!create) {
              usersDataProvider.setTenantId(entity.getId());
              usersDataProvider.reload();
          }
      }

      @Override
      protected void onSave() {
          entity.setName(detailsView.getTenantName().getValue());

      }

      @Override
      protected void getEntity(String id, AsyncCallback<TenantDto> callback) {
          KaaAdmin.getDataSource().getTenant(id, callback);
      }

      @Override
      protected void editEntity(TenantDto entity,
              AsyncCallback<TenantDto> callback) {
          KaaAdmin.getDataSource().editTenant(entity, callback);
      }



    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);

        registrations.add(detailsView.getAddTenantAdminButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                addTenantAdmin();
            }
        }));


        registrations.add(detailsView.getTenantAdminsGrid().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> event) {
                String id = event.getClickedId();
                if (event.getAction()==RowActionEvent.CLICK) {
                    UserPlace adminPlace = new UserPlace(id,entity.getId());
                    adminPlace.setPreviousPlace(place);
                    goTo(adminPlace);
                }
                else if (event.getAction()==RowActionEvent.DELETE) {
                    KaaAdmin.getDataSource().deleteUser(id, new BusyAsyncCallback<Void>() {
                        @Override
                        public void onFailureImpl(Throwable throwable) {
                            Utils.handleException(throwable,detailsView);
                        }

                        @Override
                        public void onSuccessImpl(Void aVoid) {
                            usersDataProvider.reload();
                        }
                    });
                }
            }
        }));
    }


    private void addTenantAdmin() {
        UserPlace adminPlace = new UserPlace("",entity.getId());
        adminPlace.setPreviousPlace(place);
        goTo(adminPlace);
    }

}
