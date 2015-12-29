/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.view.ctl;

import java.util.Comparator;

import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaFqnDto;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class CtlGrid extends AbstractKaaGrid<SchemaFqnDto, String> {

    private static final int DEFAULT_PAGE_SIZE = 12;
    
    public CtlGrid(Unit unit) {
        super(unit, false, DEFAULT_PAGE_SIZE);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<SchemaFqnDto> table) {
        float prefWidth = 0;
        
        prefWidth += constructStringColumn(table,
                Utils.constants.name(),
                new StringValueProvider<SchemaFqnDto>() {
                    @Override
                    public String getValue(SchemaFqnDto item) {
                        return item.getSchemaName();
                    }
                }, 
                new Comparator<SchemaFqnDto>() {
                    @Override
                    public int compare(SchemaFqnDto o1, SchemaFqnDto o2) {
                        return o1.getSchemaName().compareTo(o2.getSchemaName());
                    }
                },
                Boolean.TRUE,
                true,
                80);

        prefWidth += constructStringColumn(table,
                Utils.constants.fqn(),
                new StringValueProvider<SchemaFqnDto>() {
                    @Override
                    public String getValue(SchemaFqnDto item) {
                        return item.getFqnString();
                    }
                }, 
                new Comparator<SchemaFqnDto>() {
                    @Override
                    public int compare(SchemaFqnDto o1, SchemaFqnDto o2) {
                        return o1.compareTo(o2);
                    }
                },
                Boolean.TRUE,
                true,
                160);

        return prefWidth;
    }

}
