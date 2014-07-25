/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import java.util.List;

import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileFilterPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProfileFilterActivity extends AbstractRecordActivity<ProfileFilterDto, BaseRecordView<ProfileFilterDto>, ProfileFilterPlace> {

    public ProfileFilterActivity(ProfileFilterPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected BaseRecordView<ProfileFilterDto> getRecordView(boolean create) {
        if (create) {
            return clientFactory.getCreateProfileFilterView();
        } else {
            return clientFactory.getProfileFilterView();
        }
    }

    @Override
    protected ProfileFilterDto newStruct() {
        return new ProfileFilterDto();
    }

    @Override
    protected void getRecord(String schemaId, String endpointGroupId,
            AsyncCallback<StructureRecordDto<ProfileFilterDto>> callback) {
        KaaAdmin.getDataSource().getProfileFilterRecord(schemaId, endpointGroupId, callback);
    }

    @Override
    protected void getVacantSchemas(String endpointGroupId,
            AsyncCallback<List<SchemaDto>> callback) {
        KaaAdmin.getDataSource().getVacantProfileSchemas(endpointGroupId, callback);
    }

    @Override
    protected void editStruct(ProfileFilterDto entity,
            AsyncCallback<ProfileFilterDto> callback) {
        KaaAdmin.getDataSource().editProfileFilter(entity, callback);
    }

    @Override
    protected void activateStruct(String id,
            AsyncCallback<ProfileFilterDto> callback) {
        KaaAdmin.getDataSource().activateProfileFilter(id, callback);
    }

    @Override
    protected void deactivateStruct(String id,
            AsyncCallback<ProfileFilterDto> callback) {
        KaaAdmin.getDataSource().deactivateProfileFilter(id, callback);
    }

    @Override
    protected ProfileFilterPlace getRecordPlaceImpl(String applicationId,
            String schemaId, String endpointGroupId, boolean create,
            boolean showActive, double random) {
        return new ProfileFilterPlace(applicationId, schemaId, endpointGroupId, create, showActive, random);
    }

    @Override
    protected String customizeErrorMessage(Throwable caught) {
        return Utils.getErrorMessage(caught);
    }

}
