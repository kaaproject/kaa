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

import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserActivity extends
        AbstractUserActivity<UserDto, UserView, UserPlace> {

    public UserActivity(UserPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected UserView getView(boolean create) {
      if (create) {
          return clientFactory.getCreateUserView();
      } else {
          return clientFactory.getUserView();
      }
    }

    @Override
    protected UserDto newEntity() {
        return new UserDto();
    }

      @Override
      protected void onEntityRetrieved() {
          super.onEntityRetrieved();
          if (!create) {
              detailsView.setTitle(entity.getUsername());
          }
          detailsView.getAuthority().setValue(entity.getAuthority());
            entity.setTenantId(place.getTenId());
      }

      @Override
      protected void onSave() {
          super.onSave();
          entity.setAuthority(detailsView.getAuthority().getValue());
      }

      @Override
      protected void getEntity(String id, AsyncCallback<UserDto> callback) {
          KaaAdmin.getDataSource().getUser(id, callback);
      }

      @Override
      protected void editEntity(UserDto entity,
              AsyncCallback<UserDto> callback) {
          KaaAdmin.getDataSource().editUser(entity, callback);
      }

}
