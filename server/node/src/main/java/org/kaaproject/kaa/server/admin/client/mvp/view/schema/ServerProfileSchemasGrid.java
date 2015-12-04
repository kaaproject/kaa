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

package org.kaaproject.kaa.server.admin.client.mvp.view.schema;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import java.util.Comparator;

public class ServerProfileSchemasGrid<T extends ServerProfileSchemaViewDto> extends AbstractKaaGrid<T, String> {

    private Column<T, T> downloadSchemaColumn;

    public ServerProfileSchemasGrid() {
        super(Style.Unit.PX, false);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<T> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.version(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return item.getCtlSchemaDto().getMetaInfo().getVersion() + ".0";
                    }
                },
                new Comparator<T>() {
                    @Override
                    public int compare(T o1, T o2) {
                        return o1.getCtlSchemaDto().getMetaInfo().getVersion()
                                .compareTo(o2.getCtlSchemaDto().getMetaInfo().getVersion());
                    }
                },
                Boolean.FALSE,
                80);

        prefWidth += constructStringColumn(table,
                Utils.constants.name(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return item.getCtlSchemaDto().getName();
                    }
                },
                new Comparator<T>() {
                    @Override
                    public int compare(T o1, T o2) {
                        return o1.getCtlSchemaDto().getName().compareToIgnoreCase(o2.getCtlSchemaDto().getName());
                    }
                },
                null,
                80);

        prefWidth += constructStringColumn(table,
                Utils.constants.author(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return item.getCtlSchemaDto().getCreatedUsername();
                    }
                }, 80);

        prefWidth += constructStringColumn(table,
                Utils.constants.dateCreated(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return Utils.millisecondsToDateString(item.getProfileSchemaDto().getCreatedTime());
                    }
                },
                new Comparator<T>() {
                    @Override
                    public int compare(T o1, T o2) {
                        return o1.getProfileSchemaDto().getCreatedTime().compareTo(o2.getProfileSchemaDto().getCreatedTime());
                    }
                },
                Boolean.FALSE,
                80);

        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<T> table, float prefWidth) {
        float result = 0;
        if (!embedded && (downloadSchemaColumn == null || table.getColumnIndex(downloadSchemaColumn) == -1)) {
            Header<SafeHtml> downloadRecordSchemaHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadRecordSchema()));
            downloadSchemaColumn = constructDownloadSchemaColumn("");
            table.addColumn(downloadSchemaColumn, downloadRecordSchemaHeader);
            table.setColumnWidth(downloadSchemaColumn, ACTION_COLUMN_WIDTH, Style.Unit.PX);
            result += ACTION_COLUMN_WIDTH;
        }
        return result;
    }

    protected Column<T, T> constructDownloadSchemaColumn(String text) {
        ActionButtonCell<T> cell = new ActionButtonCell<T>(Utils.resources.download(), text, embedded,
                new ActionButtonCell.ActionListener<T>() {
                    @Override
                    public void onItemAction(T value) {
                        String schemaVersion = value.getProfileSchemaDto().getId();
                        RowActionEvent<String> rowDownloadSchemaEvent =
                                new RowActionEvent<>(schemaVersion, KaaRowAction.DOWNLOAD_SCHEMA);
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
