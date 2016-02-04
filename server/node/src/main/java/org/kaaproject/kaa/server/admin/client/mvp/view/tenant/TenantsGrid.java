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

package org.kaaproject.kaa.server.admin.client.mvp.view.tenant;

import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class TenantsGrid extends AbstractKaaGrid<TenantUserDto, String> {

    public TenantsGrid() {
        super(Unit.PX, true);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<TenantUserDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.tenantName(),
                new StringValueProvider<TenantUserDto>() {
                    @Override
                    public String getValue(TenantUserDto item) {
                        return item.getTenantName();
                    }
                }, 160);

        prefWidth += constructStringColumn(table,
                Utils.constants.tenantUser(),
                new StringValueProvider<TenantUserDto>() {
                    @Override
                    public String getValue(TenantUserDto item) {
                        return item.getUsername();
                    }
                }, 160);

        return prefWidth;
    }

}
