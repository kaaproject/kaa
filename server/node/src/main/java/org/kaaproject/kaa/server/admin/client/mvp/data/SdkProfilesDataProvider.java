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

package org.kaaproject.kaa.server.admin.client.mvp.data;

import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.shared.util.Utils;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
public class SdkProfilesDataProvider extends AbstractDataProvider<SdkProfileDto, String> {

    private final String applicationId;

    public SdkProfilesDataProvider(AbstractGrid<SdkProfileDto, String> dataGrid, HasErrorMessage hasErrorMessage, String applicationId) {
        super(dataGrid, hasErrorMessage, false);
        this.applicationId = applicationId;
        this.addDataDisplay();
    }

    @Override
    protected void loadData(final LoadCallback callback) {
        if (!Utils.isEmpty(applicationId)) {
            KaaAdmin.getDataSource().loadSdkProfiles(applicationId, new AsyncCallback<List<SdkProfileDto>>() {

                @Override
                public void onSuccess(List<SdkProfileDto> result) {
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(Throwable cause) {
                    callback.onFailure(cause);
                }
            });
        }
    }
}
