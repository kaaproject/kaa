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

package org.kaaproject.kaa.server.admin.client.mvp.view.event;

import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class EcfsGrid extends AbstractKaaGrid<EventClassFamilyDto, String> {

    public EcfsGrid(Unit unit) {
        super(unit, false);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<EventClassFamilyDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.name(),
                new StringValueProvider<EventClassFamilyDto>() {
                    @Override
                    public String getValue(EventClassFamilyDto item) {
                        return item.getName();
                    }
                }, 120);

        prefWidth += constructStringColumn(table,
                Utils.constants.fqn(),
                new StringValueProvider<EventClassFamilyDto>() {
                    @Override
                    public String getValue(EventClassFamilyDto item) {
                        return item.getNamespace() + "." + item.getClassName();
                    }
                }, 160);

        prefWidth += constructStringColumn(table,
                Utils.constants.author(),
                new StringValueProvider<EventClassFamilyDto>() {
                    @Override
                    public String getValue(EventClassFamilyDto item) {
                        return item.getCreatedUsername();
                    }
                }, 80);

        prefWidth += constructStringColumn(table,
                Utils.constants.dateCreated(),
                new StringValueProvider<EventClassFamilyDto>() {
                    @Override
                    public String getValue(EventClassFamilyDto item) {
                        return Utils.millisecondsToDateString(item.getCreatedTime());
                    }
                }, 80);
        
        return prefWidth;
    }

}
