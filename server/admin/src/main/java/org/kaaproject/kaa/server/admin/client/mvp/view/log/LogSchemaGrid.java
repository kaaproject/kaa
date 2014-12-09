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

package org.kaaproject.kaa.server.admin.client.mvp.view.log;

import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowAction;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowActionEvent;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseSchemasGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;

public class LogSchemaGrid extends BaseSchemasGrid<LogSchemaDto>{

    private Column<LogSchemaDto,LogSchemaDto> downloadLibraryColumn;
    private Column<LogSchemaDto,LogSchemaDto> downloadSchemaColumn;
    
    @Override
    protected float constructActions(DataGrid<LogSchemaDto> table, float prefWidth) {
        float result = 0;
        if (!embedded && (downloadLibraryColumn == null || table.getColumnIndex(downloadLibraryColumn) == -1)) {
            Header<SafeHtml> downloadLibraryHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadRecordLibrary()));

            downloadLibraryColumn = constructDownloadLibraryColumn("");
            table.addColumn(downloadLibraryColumn, downloadLibraryHeader);
            table.setColumnWidth(downloadLibraryColumn, 40, Unit.PX);
            result+= 40;
        }
        if (!embedded && (downloadSchemaColumn == null || table.getColumnIndex(downloadSchemaColumn) == -1)) {
            Header<SafeHtml> downloadRecordSchemaHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadRecordSchema()));

            downloadSchemaColumn = constructDownloadSchemaColumn("");
            table.addColumn(downloadSchemaColumn, downloadRecordSchemaHeader);
            table.setColumnWidth(downloadSchemaColumn, 40, Unit.PX);
            result+= 40;
        }
        if (enableActions) {
            if (deleteColumn == null || table.getColumnIndex(deleteColumn) == -1) {
                Header<SafeHtml> deleteHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(embedded ? Utils.constants.remove() : Utils.constants.delete()));

                deleteColumn = constructDeleteColumn("");
                table.addColumn(deleteColumn, deleteHeader);
                table.setColumnWidth(deleteColumn, 40, Unit.PX);
                result+= 40;
            }

        }
        return result;
    }
    
    private Column<LogSchemaDto, LogSchemaDto> constructDownloadLibraryColumn(String text) {
        ActionButtonCell<LogSchemaDto> cell = new ActionButtonCell<LogSchemaDto>(Utils.resources.download(), text, embedded,
                new ActionListener<LogSchemaDto>() {
                    @Override
                    public void onItemAction(LogSchemaDto value) {
                        Integer logSchemaVersion = value.getMajorVersion();
                        RowActionEvent<String> rowDownloadLibraryEvent = new RowActionEvent<>(String.valueOf(logSchemaVersion), RowAction.DOWNLOAD_LIBRARY);
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

    private Column<LogSchemaDto, LogSchemaDto> constructDownloadSchemaColumn(String text) {
        ActionButtonCell<LogSchemaDto> cell = new ActionButtonCell<LogSchemaDto>(Utils.resources.download(), text, embedded,
                new ActionListener<LogSchemaDto>() {
                    @Override
                    public void onItemAction(LogSchemaDto value) {
                        Integer logSchemaVersion = value.getMajorVersion();
                        RowActionEvent<String> rowDownloadSchemaEvent = new RowActionEvent<>(String.valueOf(logSchemaVersion), RowAction.DOWNLOAD_SCHEMA);
                        fireEvent(rowDownloadSchemaEvent);
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
