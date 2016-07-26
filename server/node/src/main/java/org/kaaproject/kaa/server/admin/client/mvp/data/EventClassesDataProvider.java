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


import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.shared.util.Utils;

import java.util.Collections;
import java.util.List;

public class EventClassesDataProvider extends AbstractDataProvider<EventClassDto, String> {

    private String ecfId;
    private int version;

    public EventClassesDataProvider(AbstractGrid<EventClassDto, String> dataGrid, HasErrorMessage hasErrorMessage, String ecfId, int version) {
        super(dataGrid, hasErrorMessage);
        this.ecfId = ecfId;
        this.version = version;
    }

    @Override
    protected void loadData(final LoadCallback callback) {
        if (!Utils.isEmpty(ecfId)) {
            KaaAdmin.getDataSource().getEventClassesByFamilyIdVersionAndType(ecfId, version, EventClassType.EVENT, new AsyncCallback<List<EventClassDto>>() {
                @Override
                public void onFailure(Throwable cause) {
                    callback.onFailure(cause);
                }

                @Override
                public void onSuccess(List<EventClassDto> result) {
                    callback.onSuccess(result);
                }
            });
        } else {
            List<EventClassDto> data = Collections.emptyList();
            callback.onSuccess(data);
        }
    }

}
