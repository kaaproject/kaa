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

package org.kaaproject.kaa.server.admin.client.mvp.view.sdk;

import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
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

/**
 *
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 *
 */
public class SdkProfilesGrid extends AbstractKaaGrid<SdkPropertiesDto, String> {

    private Column<SdkPropertiesDto, SdkPropertiesDto> generateSdkColumn;

    public SdkProfilesGrid() {
        super(Unit.PX, true);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<SdkPropertiesDto> table) {
        float prefWidth = 0;

        prefWidth += this.constructStringColumn(table, Utils.constants.name(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return item.getName();
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.author(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return item.getCreatedUsername();
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.dateCreated(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return Utils.millisecondsToDateString(item.getCreatedTime());
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.configuration(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return "v" + item.getConfigurationSchemaVersion().toString();
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.profile(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return "v" + item.getProfileSchemaVersion().toString();
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.notification(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return "v" + item.getNotificationSchemaVersion().toString();
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.log(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return "v" + item.getLogSchemaVersion().toString();
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.sdkToken(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        // Limit to six characters and add ellipsis
                        return item.getToken().substring(0, 6) + "\u2026";
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.ecfs(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return Integer.toString(item.getAefMapIds().size());
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.numberOfEps(),
                new StringValueProvider<SdkPropertiesDto>() {
                    @Override
                    public String getValue(SdkPropertiesDto item) {
                        return item.getEndpointCount().toString();
                    }
                }, 40);

        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<SdkPropertiesDto> table, float prefWidth) {
        float result = 0;

        if (enableActions) {
            if (generateSdkColumn == null || table.getColumnIndex(generateSdkColumn) == -1) {
                Header<SafeHtml> generateSdkHeader;
                generateSdkHeader = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(Utils.constants.generateSdk()));

                generateSdkColumn = constructGenerateSdkColumn("");
                table.addColumn(generateSdkColumn, generateSdkHeader);
                table.setColumnWidth(generateSdkColumn, 40, Unit.PX);
                result += 40;
            }

            if (deleteColumn == null || table.getColumnIndex(deleteColumn) == -1) {
                Header<SafeHtml> deleteHeader;
                if (embedded) {
                    deleteHeader = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(Utils.constants.remove()));
                } else {
                    deleteHeader = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(Utils.constants.delete()));
                }

                deleteColumn = constructDeleteColumn("");
                table.addColumn(deleteColumn, deleteHeader);
                table.setColumnWidth(deleteColumn, 40, Unit.PX);
                result += 40;
            }
        }

        return result;
    }

    private Column<SdkPropertiesDto, SdkPropertiesDto> constructGenerateSdkColumn(String label) {
        ActionListener<SdkPropertiesDto> actionListener = new ActionListener<SdkPropertiesDto>() {
            @Override
            public void onItemAction(SdkPropertiesDto value) {
                SdkProfilesGrid.this.generateSdk(value);
            }
        };

        ActionValidator<SdkPropertiesDto> actionValidator = new ActionValidator<SdkPropertiesDto>() {
            @Override
            public boolean canPerformAction(SdkPropertiesDto value) {
                return !embedded;
            }
        };

        ActionButtonCell<SdkPropertiesDto> cell;
        cell = new ActionButtonCell<SdkPropertiesDto>(Utils.resources.download(), label, embedded, actionListener, actionValidator);

        return new Column<SdkPropertiesDto, SdkPropertiesDto>(cell) {
            @Override
            public SdkPropertiesDto getValue(SdkPropertiesDto item) {
                return item;
            }
        };
    }

    private void generateSdk(SdkPropertiesDto value) {
        RowActionEvent<String> rowGenerateSdkEvent = new RowActionEvent<>(value.getId(), KaaRowAction.GENERATE_SDK);
        this.fireEvent(rowGenerateSdkEvent);
    }

    @Override
    public void onRowClicked(String id) {
    }
}
