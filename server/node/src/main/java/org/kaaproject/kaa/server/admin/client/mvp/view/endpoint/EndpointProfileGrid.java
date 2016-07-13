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

package org.kaaproject.kaa.server.admin.client.mvp.view.endpoint;

import com.google.common.io.BaseEncoding;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.view.client.Range;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EndpointProfileGrid extends AbstractGrid<EndpointProfileDto, String> {

    public EndpointProfileGrid(int pageSize) {
        super(Style.Unit.PX, true, pageSize);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<EndpointProfileDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.keyHash(),
                new StringValueProvider<EndpointProfileDto>() {
                    @Override
                    public String getValue(EndpointProfileDto item) {
                        return BaseEncoding.base64().encode(item.getEndpointKeyHash());
                    }
                }, 160);

        prefWidth += constructStringColumn(table,
                Utils.constants.profileSchemaVersion(),
                new StringValueProvider<EndpointProfileDto>() {
                    @Override
                    public String getValue(EndpointProfileDto item) {
                        return item.getClientProfileVersion() + "";
                    }
                }, 80);
        prefWidth += constructStringColumn(table,
                Utils.constants.serverProfileSchemaVersion(),
                new StringValueProvider<EndpointProfileDto>() {
                    @Override
                    public String getValue(EndpointProfileDto item) {
                        return item.getServerProfileVersion() + "";
                    }
                }, 80);
        prefWidth += constructStringColumn(table,
                Utils.constants.configurationSchemaVersion(),
                new StringValueProvider<EndpointProfileDto>() {
                    @Override
                    public String getValue(EndpointProfileDto item) {
                        return item.getConfigurationVersion() + "";
                    }
                }, 80);

        prefWidth += constructStringColumn(table,
                Utils.constants.notificationSchemaVersion(),
                new StringValueProvider<EndpointProfileDto>() {
                    @Override
                    public String getValue(EndpointProfileDto item) {
                        return item.getUserNfVersion() + "";
                    }
                }, 80);

        prefWidth += constructStringColumn(table,
                Utils.constants.logSchemaVersion(),
                new StringValueProvider<EndpointProfileDto>() {
                    @Override
                    public String getValue(EndpointProfileDto item) {
                        return item.getLogSchemaVersion() + "";
                    }
                }, 80);

        return prefWidth;
    }

    @Override
    protected float constructActions(DataGrid<EndpointProfileDto> table, float prefWidth) {
        if (enableActions) {
            float result = 0;

            if (deleteColumn == null || table.getColumnIndex(deleteColumn) == -1) {
                Header<SafeHtml> deleteHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(embedded ? Utils.constants.remove() : Utils.constants.delete()));

                deleteColumn = constructDeleteColumn("");
                table.addColumn(deleteColumn, deleteHeader);
                table.setColumnWidth(deleteColumn, 40, Style.Unit.PX);
                result+= 40;
            }

            return result;
        } else {
            return 0;
        }
    }

    @Override
    protected String getObjectId(EndpointProfileDto value) {
        return BaseEncoding.base64().encode(value.getEndpointKeyHash());
    }

    @Override
    protected SimplePager getPager() {
        return new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true){
            @Override
            protected String createText() {
                Range range = getDisplay().getVisibleRange();
                int currentPage = range.getStart() / (range.getLength() != 0 ? range.getLength() : 1) + 1;
                return Utils.messages.pagerText(currentPage + "");
            }

            @Override
            public void setPageStart(int index) {
                if (getDisplay() != null) {
                    Range range = getDisplay().getVisibleRange();
                    int pageSize = range.getLength();

                    index = Math.max(0, index);
                    if (index != range.getStart()) {
                        getDisplay().setVisibleRange(index, pageSize);
                    }
                }
            }
        };
    }
}
