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

package org.kaaproject.kaa.server.admin.client.mvp.view.sdk;

import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
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
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
public class SdkProfilesGrid extends AbstractKaaGrid<SdkProfileDto, String> {

    private Column<SdkProfileDto, SdkProfileDto> generateSdkColumn;

    public SdkProfilesGrid() {
        super(Unit.PX, true);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<SdkProfileDto> table) {
        float prefWidth = 0;

        prefWidth += this.constructStringColumn(table, Utils.constants.name(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return Utils.abbreviateText(item.getName(), 50);
                    }
                }, 60);

        prefWidth += this.constructStringColumn(table, Utils.constants.author(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return item.getCreatedUsername();
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.dateCreated(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return Utils.millisecondsToDateString(item.getCreatedTime());
                    }
                }, 40);

        prefWidth += this.constructStringColumn(table, Utils.constants.configuration(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return "v" + item.getConfigurationSchemaVersion().toString();
                    }
                }, 30);

        prefWidth += this.constructStringColumn(table, Utils.constants.profile(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return "v" + item.getProfileSchemaVersion().toString();
                    }
                }, 30);

        prefWidth += this.constructStringColumn(table, Utils.constants.notification(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return "v" + item.getNotificationSchemaVersion().toString();
                    }
                }, 30);

        prefWidth += this.constructStringColumn(table, Utils.constants.log(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return "v" + item.getLogSchemaVersion().toString();
                    }
                }, 30);

        prefWidth += this.constructStringColumn(table, Utils.constants.sdkToken(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return Utils.abbreviateText(item.getToken(), 20);
                    }
                }, 60);

        prefWidth += this.constructStringColumn(table, Utils.constants.ecfs(),
                new StringValueProvider<SdkProfileDto>() {
                    @Override
                    public String getValue(SdkProfileDto item) {
                        return Integer.toString(item.getAefMapIds().size());
                    }
                }, 40);

        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<SdkProfileDto> table, float prefWidth) {
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

    private Column<SdkProfileDto, SdkProfileDto> constructGenerateSdkColumn(String label) {
        ActionListener<SdkProfileDto> actionListener = new ActionListener<SdkProfileDto>() {
            @Override
            public void onItemAction(SdkProfileDto value) {
                SdkProfilesGrid.this.generateSdk(value);
            }
        };

        ActionValidator<SdkProfileDto> actionValidator = new ActionValidator<SdkProfileDto>() {
            @Override
            public boolean canPerformAction(SdkProfileDto value) {
                return !embedded;
            }
        };

        ActionButtonCell<SdkProfileDto> cell;
        cell = new ActionButtonCell<SdkProfileDto>(Utils.resources.download_grey(), label, embedded, actionListener, actionValidator);

        return new Column<SdkProfileDto, SdkProfileDto>(cell) {
            @Override
            public SdkProfileDto getValue(SdkProfileDto item) {
                return item;
            }
        };
    }

    private void generateSdk(SdkProfileDto value) {
        RowActionEvent<String> rowGenerateSdkEvent = new RowActionEvent<>(value.getId(), KaaRowAction.GENERATE_SDK);
        this.fireEvent(rowGenerateSdkEvent);
    }
}
