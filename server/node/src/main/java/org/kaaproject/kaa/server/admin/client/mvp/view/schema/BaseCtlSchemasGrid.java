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

package org.kaaproject.kaa.server.admin.client.mvp.view.schema;

import java.util.Comparator;

import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionsButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;

public class BaseCtlSchemasGrid<T extends BaseSchemaDto> extends AbstractKaaGrid<T, String> {

    private static final int DEFAULT_PAGE_SIZE = 12;
    
    private Column<T, T> downloadSchemaColumn;

    public BaseCtlSchemasGrid() {
        super(Unit.PX, false, DEFAULT_PAGE_SIZE);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<T> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.version(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return item.getVersion()+"";
                    }
                }, 
                new Comparator<T>() {
                    @Override
                    public int compare(T o1, T o2) {
                        return o1.compareTo(o2);
                    }
                },
                Boolean.FALSE, 
                80);

        prefWidth += constructStringColumn(table,
                Utils.constants.name(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return item.getName();
                    }
                }, 
                new Comparator<T>() {
                    @Override
                    public int compare(T o1, T o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                },
                null, 
                true,
                80);

        prefWidth += constructStringColumn(table,
                Utils.constants.author(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return item.getCreatedUsername();
                    }
                }, 60);

        prefWidth += constructStringColumn(table,
                Utils.constants.dateCreated(),
                new StringValueProvider<T>() {
                    @Override
                    public String getValue(T item) {
                        return Utils.millisecondsToDateString(item.getCreatedTime());
                    }
                }, 40);
        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<T> table, float prefWidth) {
        float result = 0;
        if (downloadSchemaColumn == null || table.getColumnIndex(downloadSchemaColumn) == -1) {
            Header<SafeHtml> downloadRecordSchemaHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadRecordSchema()));
            downloadSchemaColumn = constructDownloadSchemaColumn();
            table.addColumn(downloadSchemaColumn, downloadRecordSchemaHeader);
            table.setColumnWidth(downloadSchemaColumn, 60, Unit.PX);
            result += 60;
        }
        return result;
    }

    protected Column<T, T> constructDownloadSchemaColumn() {
        ActionsButtonCell<T> cell = new ActionsButtonCell<>(Utils.resources.export(), Utils.constants.export());
        cell.addMenuItem(Utils.constants.shallow(), new ActionsButtonCell.ActionMenuItemListener<T>() {
            @Override
            public void onMenuItemSelected(T value) {
                RowActionEvent<String> schemaEvent =
                        new RowActionEvent<>(value.getCtlSchemaId(), KaaRowAction.CTL_EXPORT_SHALLOW);
                fireEvent(schemaEvent);
            }
        });
        cell.addMenuItem(Utils.constants.deep(), new ActionsButtonCell.ActionMenuItemListener<T>() {
            @Override
            public void onMenuItemSelected(T value) {
                RowActionEvent<String> schemaEvent =
                        new RowActionEvent<>(value.getCtlSchemaId(), KaaRowAction.CTL_EXPORT_DEEP);
                fireEvent(schemaEvent);
            }
        });
        cell.addMenuItem(Utils.constants.flat(), new ActionsButtonCell.ActionMenuItemListener<T>() {
            @Override
            public void onMenuItemSelected(T value) {
                RowActionEvent<String> schemaEvent =
                        new RowActionEvent<>(value.getCtlSchemaId(), KaaRowAction.CTL_EXPORT_FLAT);
                fireEvent(schemaEvent);
            }
        });
        cell.addMenuItem(Utils.constants.javaLibrary(), new ActionsButtonCell.ActionMenuItemListener<T>() {
            @Override
            public void onMenuItemSelected(T value) {
                RowActionEvent<String> schemaEvent =
                        new RowActionEvent<>(value.getCtlSchemaId(), KaaRowAction.CTL_EXPORT_LIBRARY);
                fireEvent(schemaEvent);
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

