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

import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.AbstractStructGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigRecordKey;

import com.google.gwt.user.cellview.client.DataGrid;

public class ConfigurationStructGrid extends AbstractStructGrid<ConfigurationDto, ConfigurationRecordDto, ConfigRecordKey> {

    @Override
    protected float constructColumnsImpl(DataGrid<ConfigurationRecordDto> table) {
        float prefWidth = 0;
        prefWidth += constructStringColumn(table,
                Utils.constants.configurationSchema(),
                new StringValueProvider<ConfigurationRecordDto>() {
                    @Override
                    public String getValue(ConfigurationRecordDto item) {
                        return item.getSchemaVersion() + "";
                    }
                }, 80);
        
        prefWidth += super.constructColumnsImpl(table);
        
        return prefWidth;
    }
    
    @Override
    protected ConfigRecordKey getObjectId(ConfigurationRecordDto value) {
        if (value != null) {
            return new ConfigRecordKey(value.getSchemaId(), value.getEndpointGroupId());
        } else {
            return null;
        }
    }
    
}
