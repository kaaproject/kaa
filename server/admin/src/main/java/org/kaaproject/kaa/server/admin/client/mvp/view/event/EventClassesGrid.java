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

package org.kaaproject.kaa.server.admin.client.mvp.view.event;

import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.ViewSchemaDialog;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;

public class EventClassesGrid extends AbstractGrid<EventClassDto, String> {

    private Column<EventClassDto,EventClassDto> viewSchemaColumn;
    
    public EventClassesGrid(Unit unit) {
        super(unit, true, true);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<EventClassDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.fqn(),
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
                        return Utils.constants.getString(item.getType().name().toLowerCase());
                    }
                }, 40);

        return prefWidth;
    }
    
    @Override
    protected float constructActions(DataGrid<EventClassDto> table, float prefWidth) {
        if (enableActions) {
            float result = 0;
            if (viewSchemaColumn == null || table.getColumnIndex(viewSchemaColumn) == -1) {
                Header<SafeHtml> viewSchemaHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(Utils.constants.schema()));

                viewSchemaColumn = constructViewSchemaColumn("");
                table.addColumn(viewSchemaColumn, viewSchemaHeader);
                table.setColumnWidth(viewSchemaColumn, 40, Unit.PX);
                result+= 40;
            }
            return result;
        }
        else {
            return 0;
        }
    }
    
    private Column<EventClassDto, EventClassDto> constructViewSchemaColumn(String text) {
        ActionButtonCell<EventClassDto> cell = new ActionButtonCell<EventClassDto>(Utils.resources.details(),
                text, embedded,
                new ActionListener<EventClassDto> () {
                    @Override
                    public void onItemAction(EventClassDto value) {
                        showSchema(value);
                    }
                },
                new ActionValidator<EventClassDto> () {
                    @Override
                    public boolean canPerformAction(EventClassDto value) {
                        return true;
                    }
                }
        );
        Column<EventClassDto, EventClassDto> column = new Column<EventClassDto, EventClassDto>(cell) {
            @Override
            public EventClassDto getValue(EventClassDto item) {
                return item;
            }
        };
        return column;
    }

    private void showSchema(EventClassDto value) {
        ViewSchemaDialog.showViewSchemaDialog(value.getSchema());
    }

}
