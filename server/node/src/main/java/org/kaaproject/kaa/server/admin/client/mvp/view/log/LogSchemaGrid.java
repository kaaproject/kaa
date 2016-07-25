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

package org.kaaproject.kaa.server.admin.client.mvp.view.log;

import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseCtlSchemasGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseSchemasGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;

public class LogSchemaGrid extends BaseCtlSchemasGrid<LogSchemaDto>{

    private Column<LogSchemaDto,LogSchemaDto> downloadLibraryColumn;

    @Override
    protected float constructActions(DataGrid<LogSchemaDto> table, float prefWidth) {
        float result = super.constructActions(table, prefWidth);
        if (!embedded && (downloadLibraryColumn == null || table.getColumnIndex(downloadLibraryColumn) == -1)) {
            Header<SafeHtml> downloadLibraryHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadRecordLibrary()));

            downloadLibraryColumn = constructDownloadLibraryColumn("");
            table.addColumn(downloadLibraryColumn, downloadLibraryHeader);
            table.setColumnWidth(downloadLibraryColumn, ACTION_COLUMN_WIDTH, Unit.PX);
            result+= ACTION_COLUMN_WIDTH;
        }
        if (enableActions) {
            if (deleteColumn == null || table.getColumnIndex(deleteColumn) == -1) {
                Header<SafeHtml> deleteHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(embedded ? Utils.constants.remove() : Utils.constants.delete()));

                deleteColumn = constructDeleteColumn("");
                table.addColumn(deleteColumn, deleteHeader);
                table.setColumnWidth(deleteColumn, ACTION_COLUMN_WIDTH, Unit.PX);
                result+= ACTION_COLUMN_WIDTH;
            }

        }
        return result;
    }

    private Column<LogSchemaDto, LogSchemaDto> constructDownloadLibraryColumn(String text) {
        ActionButtonCell<LogSchemaDto> cell = new ActionButtonCell<>(Utils.resources.download_grey(), text, embedded,
                new ActionListener<LogSchemaDto>() {
                    @Override
                    public void onItemAction(LogSchemaDto value) {
                        Integer logSchemaVersion = value.getVersion();
                        RowActionEvent<String> rowDownloadLibraryEvent = new RowActionEvent<>(String.valueOf(logSchemaVersion), KaaRowAction.DOWNLOAD_LOG_SCHEMA_LIBRARY);
                        fireEvent(rowDownloadLibraryEvent);
                    }
                }, new ActionValidator<LogSchemaDto>() {
                    @Override
                    public boolean canPerformAction(LogSchemaDto value) {
                        return !embedded;
                    }
                });
        Column<LogSchemaDto, LogSchemaDto> column = new Column<LogSchemaDto, LogSchemaDto>(cell) {
            @Override
            public LogSchemaDto getValue(LogSchemaDto item) {
                return item;
            }
        };
        return column;
    }


}
