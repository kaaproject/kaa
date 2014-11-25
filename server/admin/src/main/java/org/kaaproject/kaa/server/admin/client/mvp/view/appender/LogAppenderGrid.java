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

package org.kaaproject.kaa.server.admin.client.mvp.view.appender;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowAction;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowActionEvent;
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

public class LogAppenderGrid extends AbstractGrid<LogAppenderDto, String> {

    private Column<LogAppenderDto,LogAppenderDto> downloadLibraryColumn;
    private Column<LogAppenderDto,LogAppenderDto> downloadSchemaColumn;

    public LogAppenderGrid(boolean embedded) {
        super(Unit.PX, true, embedded);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<LogAppenderDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table, Utils.constants.name(),
                new StringValueProvider<LogAppenderDto>() {
            @Override
            public String getValue(LogAppenderDto item) {
                return item.getName();
            }
        }, 80);
        prefWidth += constructStringColumn(table, Utils.constants.version(),
                new StringValueProvider<LogAppenderDto>() {
            @Override
            public String getValue(LogAppenderDto item) {
                return item.getSchemaVersion();

            }
        }, 80);
        prefWidth += constructStringColumn(table, Utils.constants.logAppenderType(),
                new StringValueProvider<LogAppenderDto>() {
            @Override
            public String getValue(LogAppenderDto item) {
                return item.getTypeName();
            }
        }, 80);

        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<LogAppenderDto> table, float prefWidth) {
        if (enableActions) {
            float result = 0;
            if (!embedded && (downloadLibraryColumn == null || table.getColumnIndex(downloadLibraryColumn) == -1)) {
                Header<SafeHtml> sendNotificationHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadRecordLibrary()));

                downloadLibraryColumn = constructDownloadLibraryColumn("");
                table.addColumn(downloadLibraryColumn, sendNotificationHeader);
                table.setColumnWidth(downloadLibraryColumn, 40, Unit.PX);
                result+= 40;
            }
            if (!embedded && (downloadSchemaColumn == null || table.getColumnIndex(downloadSchemaColumn) == -1)) {
                Header<SafeHtml> sendNotificationHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadRecordSchema()));

                downloadSchemaColumn = constructDownloadSchemaColumn("");
                table.addColumn(downloadSchemaColumn, sendNotificationHeader);
                table.setColumnWidth(downloadSchemaColumn, 40, Unit.PX);
                result+= 40;
            }
            if (deleteColumn == null || table.getColumnIndex(deleteColumn) == -1) {
                Header<SafeHtml> deleteHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(embedded ? Utils.constants.remove() : Utils.constants.delete()));

                deleteColumn = constructDeleteColumn("");
                table.addColumn(deleteColumn, deleteHeader);
                table.setColumnWidth(deleteColumn, 40, Unit.PX);
                result+= 40;
            }

            return result;
        }
        else {
            return 0;
        }
    }

    @Override
    protected String deleteQuestion() {
        if (embedded) {
            return Utils.messages.removeLogAppenderQuestion();
        }
        else {
            return super.deleteQuestion();
        }
    }

    @Override
    protected String deleteTitle() {
        if (embedded) {
            return Utils.messages.removeLogAppenderTitle();
        }
        else {
            return super.deleteTitle();
        }
    }

    private Column<LogAppenderDto, LogAppenderDto> constructDownloadLibraryColumn(String text) {
        ActionButtonCell<LogAppenderDto> cell = new ActionButtonCell<LogAppenderDto>(Utils.resources.download(), text, embedded,
                new ActionListener<LogAppenderDto>() {
                    @Override
                    public void onItemAction(LogAppenderDto value) {
                        Integer logSchemaVersion = value.getSchema().getMajorVersion();
                        RowActionEvent<String> rowDownloadLibraryEvent = new RowActionEvent<>(String.valueOf(logSchemaVersion), RowAction.DOWNLOAD_LIBRARY);
                        fireEvent(rowDownloadLibraryEvent);
                    }
                }, new ActionValidator<LogAppenderDto>() {
                    @Override
                    public boolean canPerformAction(LogAppenderDto value) {
                        return !embedded;
                    }
                });
        Column<LogAppenderDto, LogAppenderDto> column = new Column<LogAppenderDto, LogAppenderDto>(cell) {
            @Override
            public LogAppenderDto getValue(LogAppenderDto item) {
                return item;
            }
        };
        return column;
    }

    private Column<LogAppenderDto, LogAppenderDto> constructDownloadSchemaColumn(String text) {
        ActionButtonCell<LogAppenderDto> cell = new ActionButtonCell<LogAppenderDto>(Utils.resources.download(), text, embedded,
                new ActionListener<LogAppenderDto>() {
                    @Override
                    public void onItemAction(LogAppenderDto value) {
                        Integer logSchemaVersion = value.getSchema().getMajorVersion();
                        RowActionEvent<String> rowDownloadSchemaEvent = new RowActionEvent<>(String.valueOf(logSchemaVersion), RowAction.DOWNLOAD_SCHEMA);
                        fireEvent(rowDownloadSchemaEvent);
                    }
                }, new ActionValidator<LogAppenderDto>() {
                    @Override
                    public boolean canPerformAction(LogAppenderDto value) {
                        return !embedded;
                    }
                });
        Column<LogAppenderDto, LogAppenderDto> column = new Column<LogAppenderDto, LogAppenderDto>(cell) {
            @Override
            public LogAppenderDto getValue(LogAppenderDto item) {
                return item;
            }
        };
        return column;
    }
}
