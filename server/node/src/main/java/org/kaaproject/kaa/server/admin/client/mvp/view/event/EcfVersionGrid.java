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


import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseCtlSchemasGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EcfVersionGrid extends BaseCtlSchemasGrid<EventClassDto> {

    public EcfVersionGrid() {
    }

    @Override
    protected float constructColumnsImpl(DataGrid<EventClassDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.name(),
                new StringValueProvider<EventClassDto>() {
                    @Override
                    public String getValue(EventClassDto item) {
                        return item.getName();
                    }
                }, 120);

        prefWidth += constructStringColumn(table,
                Utils.constants.namespace(),
                new StringValueProvider<EventClassDto>() {
                    @Override
                    public String getValue(EventClassDto item) {
                        return item.getFqn();
                    }
                }, 160);

        prefWidth += constructStringColumn(table,
                Utils.constants.classType(),
                new StringValueProvider<EventClassDto>() {
                    @Override
                    public String getValue(EventClassDto item) {
                        return item.getType().toString();
                    }
                }, 80);

        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<EventClassDto> table, float prefWidth) {
        super.constructActions(table, prefWidth);
        float result = 0;
            Header<SafeHtml> deleteHeader;
            if (embedded) {
                deleteHeader = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(Utils.constants.remove()));
            } else {
                deleteHeader = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(Utils.constants.delete()));
            }

            deleteColumn = constructDeleteColumn("");
            table.addColumn(deleteColumn, deleteHeader);
            table.setColumnWidth(deleteColumn, 40, Style.Unit.PX);
            result += 40;

        return result;
    }
}
