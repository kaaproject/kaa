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

package org.kaaproject.kaa.server.admin.client.mvp.view.appender;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.plugin.BasePluginGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.user.cellview.client.DataGrid;

public class LogAppenderGrid extends BasePluginGrid<LogAppenderDto> {

    public LogAppenderGrid(boolean embedded) {
        super(embedded);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<LogAppenderDto> table) {
        float prefWidth = super.constructColumnsImpl(table);

        prefWidth += constructStringColumn(table, Utils.constants.minSchemaVersion(),
                new StringValueProvider<LogAppenderDto>() {
            @Override
            public String getValue(LogAppenderDto item) {
                return String.valueOf(item.getMinLogSchemaVersion());
            }
        }, 80);
        prefWidth += constructStringColumn(table, Utils.constants.maxVersion(),
                new StringValueProvider<LogAppenderDto>() {
            @Override
            public String getValue(LogAppenderDto item) {
                if (item.getMaxLogSchemaVersion() == Integer.MAX_VALUE) {
                    return Utils.constants.infinite();
                } else {
                    return String.valueOf(item.getMaxLogSchemaVersion());
                }
            }
        }, 80);
        prefWidth += constructBooleanColumn(table,
                Utils.constants.confirmDelivery(),
                new BooleanValueProvider<LogAppenderDto>() {
                    @Override
                    public Boolean getValue(LogAppenderDto item) {
                        return item.isConfirmDelivery();
                    }
                }, 40);
        return prefWidth;
    }

    @Override
    protected String deleteQuestion() {
        return Utils.messages.removeLogAppenderQuestion();
    }

    @Override
    protected String deleteTitle() {
        return Utils.messages.removeLogAppenderTitle();
    }

}
