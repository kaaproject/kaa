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

package org.kaaproject.kaa.server.admin.client.mvp.view.config;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseCtlSchemasGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseSchemasGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class ConfigSchemaGrid extends BaseCtlSchemasGrid<ConfigurationSchemaDto> {

    private Column<ConfigurationSchemaDto,ConfigurationSchemaDto> downloadBaseSchemaColumn;
    private Column<ConfigurationSchemaDto,ConfigurationSchemaDto> downloadOverrideSchemaColumn;

    @Override
    protected float constructActions(DataGrid<ConfigurationSchemaDto> table, float prefWidth) {
        float result = super.constructActions(table, prefWidth);
        if (!embedded && (downloadBaseSchemaColumn == null || table.getColumnIndex(downloadBaseSchemaColumn) == -1)) {
            Header<SafeHtml> downloadBaseSchemaHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadBaseSchema()));

            downloadBaseSchemaColumn = constructDownloadBaseSchemaColumnColumn("");
            table.addColumn(downloadBaseSchemaColumn, downloadBaseSchemaHeader);
            table.setColumnWidth(downloadBaseSchemaColumn, ACTION_COLUMN_WIDTH, Style.Unit.PX);
            result+= ACTION_COLUMN_WIDTH;
        }

        if (!embedded && (downloadOverrideSchemaColumn == null || table.getColumnIndex(downloadOverrideSchemaColumn) == -1)) {
            Header<SafeHtml> downloadOverrideSchemaHeader = new SafeHtmlHeader(
                    SafeHtmlUtils.fromSafeConstant(Utils.constants.downloadOverrideSchema()));

            downloadOverrideSchemaColumn = constructDownloadOverrideSchemaColumnColumn("");
            table.addColumn(downloadOverrideSchemaColumn, downloadOverrideSchemaHeader);
            table.setColumnWidth(downloadOverrideSchemaColumn, ACTION_COLUMN_WIDTH, Style.Unit.PX);
            result+= ACTION_COLUMN_WIDTH;
        }
        return result;
    }

    private Column<ConfigurationSchemaDto, ConfigurationSchemaDto> constructDownloadBaseSchemaColumnColumn(String text) {
        ActionButtonCell<ConfigurationSchemaDto> cell = new ActionButtonCell<>(Utils.resources.download_grey(), text, embedded,
                new ActionButtonCell.ActionListener<ConfigurationSchemaDto>() {
                    @Override
                    public void onItemAction(ConfigurationSchemaDto value) {
                        Integer schemaVersion = value.getVersion();
                        RowActionEvent<String> rowDownloadBaseSchemaEvent = new RowActionEvent<>(String.valueOf(schemaVersion), KaaRowAction.DOWNLOAD_BASE_SCHEMA);
                        fireEvent(rowDownloadBaseSchemaEvent);
                    }
                }, new ActionButtonCell.ActionValidator<ConfigurationSchemaDto>() {
            @Override
            public boolean canPerformAction(ConfigurationSchemaDto value) {
                return !embedded;
            }
        });
        Column<ConfigurationSchemaDto, ConfigurationSchemaDto> column = new Column<ConfigurationSchemaDto, ConfigurationSchemaDto>(cell) {
            @Override
            public ConfigurationSchemaDto getValue(ConfigurationSchemaDto item) {
                return item;
            }
        };
        return column;
    }


    private Column<ConfigurationSchemaDto, ConfigurationSchemaDto> constructDownloadOverrideSchemaColumnColumn(String text) {
        ActionButtonCell<ConfigurationSchemaDto> cell = new ActionButtonCell<>(Utils.resources.download_grey(), text, embedded,
                new ActionButtonCell.ActionListener<ConfigurationSchemaDto>() {
                    @Override
                    public void onItemAction(ConfigurationSchemaDto value) {
                        Integer schemaVersion = value.getVersion();
                        RowActionEvent<String> rowDownloadOverrideSchemaEvent = new RowActionEvent<>(String.valueOf(schemaVersion), KaaRowAction.DOWNLOAD_OVERRIDE_SCHEMA);
                        fireEvent(rowDownloadOverrideSchemaEvent);
                    }
                }, new ActionButtonCell.ActionValidator<ConfigurationSchemaDto>() {
            @Override
            public boolean canPerformAction(ConfigurationSchemaDto value) {
                return !embedded;
            }
        });
        Column<ConfigurationSchemaDto, ConfigurationSchemaDto> column = new Column<ConfigurationSchemaDto, ConfigurationSchemaDto>(cell) {
            @Override
            public ConfigurationSchemaDto getValue(ConfigurationSchemaDto item) {
                return item;
            }
        };
        return column;
    }

}
