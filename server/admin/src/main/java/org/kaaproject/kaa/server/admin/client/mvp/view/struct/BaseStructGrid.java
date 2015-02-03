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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.admin.StructureRecordKey;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class BaseStructGrid<T extends AbstractStructureDto> extends AbstractKaaGrid<StructureRecordDto<T>, StructureRecordKey> {

    public BaseStructGrid() {
        super(Unit.PX, true, true);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<StructureRecordDto<T>> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.schemaVersion(),
                new StringValueProvider<StructureRecordDto<T>>() {
                    @Override
                    public String getValue(StructureRecordDto<T> item) {
                        return item.getMajorVersion()+"."+item.getMinorVersion();
                    }
                }, 80);

        prefWidth += constructStringColumn(table,
                Utils.constants.description(),
                new StringValueProvider<StructureRecordDto<T>>() {
                    @Override
                    public String getValue(StructureRecordDto<T> item) {
                        return item.getDescription();
                    }
                }, 160);

        prefWidth += constructStringColumn(table,
                Utils.constants.numberOfEps(),
                new StringValueProvider<StructureRecordDto<T>>() {
                    @Override
                    public String getValue(StructureRecordDto<T> item) {
                        return item.getEndpointCount()+"";
                    }
                }, 80);

        prefWidth += constructBooleanColumn(table,
                Utils.constants.active(),
                new BooleanValueProvider<StructureRecordDto<T>>() {
                    @Override
                    public Boolean getValue(StructureRecordDto<T> item) {
                        return item.hasActive() && !item.hasDeprecated();
                    }
                }, 40);

        prefWidth += constructBooleanColumn(table,
                Utils.constants.draft(),
                new BooleanValueProvider<StructureRecordDto<T>>() {
                    @Override
                    public Boolean getValue(StructureRecordDto<T> item) {
                        return item.hasDraft();
                    }
                }, 40);

        return prefWidth;
    }

    @Override
    protected StructureRecordKey getObjectId(StructureRecordDto<T> value) {
        if (value != null) {
            return new StructureRecordKey(value.getSchemaId(), value.getEndpointGroupId());
        }
        else {
            return null;
        }
    }

    @Override
    protected boolean canDelete(StructureRecordDto<T> value) {
        return value.hasDraft() || !value.hasDeprecated();
    }

}
