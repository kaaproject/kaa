/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TenantActivity extends
        AbstractUserActivity<TenantUserDto, TenantView, TenantPlace> {

    public TenantActivity(TenantPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
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
    protected TenantUserDto newEntity() {
        return new TenantUserDto();
    }

      @Override
      protected void onEntityRetrieved() {
          super.onEntityRetrieved();
          if (!create) {
              detailsView.setTitle(entity.getTenantName());
          }
          detailsView.getTenantName().setValue(entity.getTenantName());
      }

      @Override
      protected void onSave() {
          super.onSave();
          entity.setTenantName(detailsView.getTenantName().getValue());
          if (create) {
              entity.setAuthority(KaaAuthorityDto.TENANT_ADMIN);
          }
      }

      @Override
      protected void getEntity(String id, AsyncCallback<TenantUserDto> callback) {
          KaaAdmin.getDataSource().getTenant(id, callback);
      }

      @Override
      protected void editEntity(TenantUserDto entity,
              AsyncCallback<TenantUserDto> callback) {
          KaaAdmin.getDataSource().editTenant(entity, callback);
      }

}
