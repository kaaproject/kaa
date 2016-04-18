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

package org.kaaproject.kaa.server.admin.client.mvp.view.plugin;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class BasePluginGrid<T extends PluginDto> extends AbstractKaaGrid<T, String> {
    private Column<T, T> downloadPropsColumn;

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



    @Override
    protected float constructActions(DataGrid<T> table, float prefWidth) {

        float result = 0;
        if (!embedded && (downloadPropsColumn == null || table.getColumnIndex(downloadPropsColumn) == -1)) {
            Header<SafeHtml> downloadRecordSchemaHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.configuration()));
            downloadPropsColumn = constructDownloadSchemaColumn("");
            table.addColumn(downloadPropsColumn, downloadRecordSchemaHeader);
            table.setColumnWidth(downloadPropsColumn, ACTION_COLUMN_WIDTH, Unit.PX);
            result += ACTION_COLUMN_WIDTH;
        }
        result+=super.constructActions(table, prefWidth);
        return result;
    }

    protected Column<T, T> constructDownloadSchemaColumn(String text) {
        ActionButtonCell<T> cell = new ActionButtonCell<T>(Utils.resources.download_grey(), text, embedded,
                new ActionButtonCell.ActionListener<T>() {
                    @Override
                    public void onItemAction(T value) {
                        RowActionEvent<String> rowDownloadSchemaEvent = new RowActionEvent<>(value.getId(), KaaRowAction.DOWNLOAD_SCHEMA);
                        fireEvent(rowDownloadSchemaEvent);
                    }
                }, new ActionButtonCell.ActionValidator<T>() {
            @Override
            public boolean canPerformAction(T value) {
                return !embedded;
            }
        });
        Column<T, T> column = new Column<T, T>(cell) {
            @Override
            public T getValue(T item) {
                return item;
            }
        };
        return column;
    }


}
