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
package org.kaaproject.kaa.server.admin.client.mvp.view.plugin;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class BasePluginGrid<T extends PluginDto> extends AbstractKaaGrid<T, String> {

    public BasePluginGrid(boolean embedded) {
        super(Unit.PX, true, embedded);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<T> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table, Utils.constants.name(),
                new StringValueProvider<T>() {
            @Override
            public String getValue(T item) {
                return item.getName();
            }
        }, 80);
        
        prefWidth += constructStringColumn(table, Utils.constants.type(),
                new StringValueProvider<T>() {
            @Override
            public String getValue(T item) {
                return item.getPluginTypeName();
            }
        }, 80);
        return prefWidth;
    }

}
