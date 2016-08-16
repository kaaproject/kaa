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
package org.kaaproject.kaa.server.admin.client.mvp.data;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;

import java.util.List;


public class TenantAdminDataProvider extends AbstractDataProvider<UserDto, String> {

   private String tenantId;


    public TenantAdminDataProvider(AbstractGrid<UserDto, String> dataGrid, HasErrorMessage hasErrorMessage,String tenantId) {
        super(dataGrid, hasErrorMessage);
        this.tenantId=tenantId;
    }

    @Override
    protected void loadData(final LoadCallback callback) {
        KaaAdmin.getDataSource().loadAllTenantAdminsByTenantId(tenantId, new AsyncCallback<List<UserDto>>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(List<UserDto> userDtos) {
                callback.onSuccess(userDtos);
            }
        });
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
