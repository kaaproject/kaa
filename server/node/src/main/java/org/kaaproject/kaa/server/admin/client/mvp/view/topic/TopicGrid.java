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

package org.kaaproject.kaa.server.admin.client.mvp.view.topic;

import java.util.Comparator;

import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
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

public class TopicGrid extends AbstractKaaGrid<TopicDto, String> {

    private Column<TopicDto,TopicDto> sendNotificationColumn;

    public TopicGrid(boolean embedded) {
        super(Unit.PX, true, embedded);
    }

    public TopicGrid(boolean enableActions, boolean embedded) {
        super(Unit.PX, enableActions, embedded);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<TopicDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.name(),
                new StringValueProvider<TopicDto>() {
                    @Override
                    public String getValue(TopicDto item) {
                        return item.getName();
                    }
                }, 
                new Comparator<TopicDto>() {
                    @Override
                    public int compare(TopicDto o1, TopicDto o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                },
                Boolean.TRUE,
                !embedded, 80);
        prefWidth += constructBooleanColumn(table,
                Utils.constants.mandatory(),
                new BooleanValueProvider<TopicDto>() {
                    @Override
                    public Boolean getValue(TopicDto item) {
                        return item.getType()==TopicTypeDto.MANDATORY;
                    }
                }, 40);
        if (!embedded) {
            prefWidth += constructStringColumn(table,
                    Utils.constants.author(),
                    new StringValueProvider<TopicDto>() {
                        @Override
                        public String getValue(TopicDto item) {
                            return item.getCreatedUsername();
                        }
                    }, 80);

            prefWidth += constructStringColumn(table,
                    Utils.constants.dateCreated(),
                    new StringValueProvider<TopicDto>() {
                        @Override
                        public String getValue(TopicDto item) {
                            return Utils.millisecondsToDateString(item.getCreatedTime());
                        }
                    }, 80);
        }
        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<TopicDto> table, float prefWidth) {
        if (enableActions) {
            float result = 0;
            if (!embedded && (sendNotificationColumn == null || table.getColumnIndex(sendNotificationColumn) == -1)) {
                Header<SafeHtml> sendNotificationHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(Utils.constants.sendNotification()));

                sendNotificationColumn = constructSendNotificationColumn("");
                table.addColumn(sendNotificationColumn, sendNotificationHeader);
                table.setColumnWidth(sendNotificationColumn, 40, Unit.PX);
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
        } else {
            return 0;
        }
    }

    @Override
    protected String deleteQuestion() {
        if (embedded) {
            return Utils.messages.removeTopicFromEndpointGroupQuestion();
        } else {
            return super.deleteQuestion();
        }
    }

    @Override
    protected String deleteTitle() {
        if (embedded) {
            return Utils.messages.removeTopicFromEndpointGroupTitle();
        } else {
            return super.deleteTitle();
        }
    }

    private Column<TopicDto, TopicDto> constructSendNotificationColumn(String text) {
        ActionButtonCell<TopicDto> cell = new ActionButtonCell<TopicDto>(Utils.resources.send(),
                text, embedded,
                new ActionListener<TopicDto> () {
                    @Override
                    public void onItemAction(TopicDto value) {
                        sendNotification(value);
                    }
                },
                new ActionValidator<TopicDto> () {
                    @Override
                    public boolean canPerformAction(TopicDto value) {
                        return !embedded;
                    }
                }
        );
        Column<TopicDto, TopicDto> column = new Column<TopicDto, TopicDto>(cell) {
            @Override
            public TopicDto getValue(TopicDto item) {
                return item;
            }
        };
        return column;
    }

    private void sendNotification(TopicDto value) {
        RowActionEvent<String> rowSendNotificationEvent = new RowActionEvent<>(value.getId(), KaaRowAction.SEND_NOTIFICATION);
        fireEvent(rowSendNotificationEvent);
    }

}

