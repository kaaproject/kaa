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

package org.kaaproject.kaa.server.admin.client.mvp.view.application;

import java.util.Comparator;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class ApplicationsGrid extends AbstractKaaGrid<ApplicationDto, String> {

    public ApplicationsGrid(Unit unit) {
        super(unit, false);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<ApplicationDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.appName(),
                new StringValueProvider<ApplicationDto>() {
                    @Override
                    public String getValue(ApplicationDto item) {
                        return item.getName();
                    }
                }, 
                new Comparator<ApplicationDto>() {
                    @Override
                    public int compare(ApplicationDto o1, ApplicationDto o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                },
                Boolean.TRUE,
                true,
                160);

        return prefWidth;
    }

}
