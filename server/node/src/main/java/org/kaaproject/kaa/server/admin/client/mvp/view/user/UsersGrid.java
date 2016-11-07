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

package org.kaaproject.kaa.server.admin.client.mvp.view.user;

import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class UsersGrid extends AbstractKaaGrid<UserDto, String> {

    private boolean showRole;

    public UsersGrid() {
        this(false, false);
    }

    public UsersGrid(boolean showRole) {
        this(showRole, false);
    }

    public UsersGrid(boolean showRole, boolean embeded) {
        super(Unit.PX, true, embeded);
        this.showRole=showRole;
    }

    @Override
    protected float constructColumnsImpl(DataGrid<UserDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.userName(),
                new StringValueProvider<UserDto>() {
                    @Override
                    public String getValue(UserDto item) {
                        return item.getUsername();
                    }
                }, 160);

        prefWidth += constructStringColumn(table,
                Utils.constants.email(),
                new StringValueProvider<UserDto>() {
                    @Override
                    public String getValue(UserDto item) {
                        return item.getMail();
                    }
                }, 160);

        if(showRole) {
            prefWidth += constructStringColumn(table,
                    Utils.constants.role(),
                    new StringValueProvider<UserDto>() {
                        @Override
                        public String getValue(UserDto item) {
                            return Utils.constants.getString(item.getAuthority().getResourceKey());
                        }
                    }, 160);
        }

        return prefWidth;
    }


}
