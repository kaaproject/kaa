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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public abstract class AbstractStructGrid<R extends AbstractStructureDto, T extends StructureRecordDto<R>, K> extends AbstractKaaGrid<T, K> {

    public AbstractStructGrid() {
        super(Unit.PX, true, true);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<T> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.description(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return item.getDescription();
                    }
                }, 160);
        
        prefWidth += constructBooleanColumn(table,
                Utils.constants.active(),
                new BooleanValueProvider<T>() {
                    @Override
                    public Boolean getValue(T item) {
                        return item.hasActive() && !item.hasDeprecated();
                    }
                }, 40);

        prefWidth += constructBooleanColumn(table,
                Utils.constants.draft(),
                new BooleanValueProvider<T>() {
                    @Override
                    public Boolean getValue(T item) {
                        return item.hasDraft();
                    }
                }, 40);

        return prefWidth;
    }

    @Override
    protected boolean canDelete(T value) {
        return value.hasDraft() || !value.hasDeprecated();
    }

}
