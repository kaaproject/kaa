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

package org.kaaproject.kaa.server.admin.client.mvp.view.profile;

import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.AbstractStructGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.profile.ProfileFilterRecordKey;

import com.google.gwt.user.cellview.client.DataGrid;

public class ProfileFilterStructGrid extends AbstractStructGrid<ProfileFilterDto, ProfileFilterRecordDto, ProfileFilterRecordKey> {

    @Override
    protected float constructColumnsImpl(DataGrid<ProfileFilterRecordDto> table) {
        float prefWidth = 0;
        prefWidth += constructStringColumn(table,
                Utils.constants.endpointProfileSchema(),
                new StringValueProvider<ProfileFilterRecordDto>() {
                    @Override
                    public String getValue(ProfileFilterRecordDto item) {
                        return item.getEndpointProfileSchemaVersion() != null ? 
                                item.getEndpointProfileSchemaVersion() + "" : "";
                    }
                }, 60);
        prefWidth += constructStringColumn(table,
                Utils.constants.serverProfileSchema(),
                new StringValueProvider<ProfileFilterRecordDto>() {
                    @Override
                    public String getValue(ProfileFilterRecordDto item) {
                        return item.getServerProfileSchemaVersion() != null ? 
                                item.getServerProfileSchemaVersion() + "" : "";
                    }
                }, 60);
        
        prefWidth += super.constructColumnsImpl(table);
        
        return prefWidth;
    }
    
    @Override
    protected ProfileFilterRecordKey getObjectId(ProfileFilterRecordDto value) {
        if (value != null) {
            return new ProfileFilterRecordKey(value.getEndpointProfileSchemaId(), 
                    value.getServerProfileSchemaId(), value.getEndpointGroupId());
        } else {
            return null;
        }
    }
    
}
