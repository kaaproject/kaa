/*
 * Copyright 2015 CyberVision, Inc.
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
import com.google.gwt.view.client.HasData;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;

import java.util.ArrayList;
import java.util.List;

public class EndpointProfileDataProvider extends AbstractDataProvider<EndpointProfileDto> {

    public static final String DEFAULT_OFFSET = "0";
    private String limit = "20";  // took from AbstractGrid<...>.class see AvroUI
    private String pageSize;
    private String offset = DEFAULT_OFFSET;
    private String groupID;
    private List<EndpointProfileDto> endpointProfilesList;
    private int previousStart = -1;

    private static volatile EndpointProfileDataProvider instance;

    private EndpointProfileDataProvider(AbstractGrid<EndpointProfileDto, ?> dataGrid,
                                       HasErrorMessage hasErrorMessage, String groupID) {
        super(dataGrid, hasErrorMessage, true);
        this.groupID = groupID;
        endpointProfilesList = new ArrayList<>();
        pageSize = limit = (dataGrid.getPageSize() + 1) + "";
    }

    @Override
    protected void onRangeChanged(HasData<EndpointProfileDto> display) {
        if (groupID != null) {
            int start = display.getVisibleRange().getStart();
            if (previousStart < start) {
                previousStart = start;
                setLoaded(false);
            }
            super.onRangeChanged(display);
        }
    }

    @Override
    protected void loadData(final LoadCallback callback) {
        KaaAdmin.getDataSource().getEndpointProfileByGroupID(groupID, limit, offset,
                new AsyncCallback<EndpointProfilesPageDto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof KaaAdminServiceException) {
                            if (((KaaAdminServiceException) caught).getErrorCode() == ServiceErrorCode.ITEM_NOT_FOUND) {
                                endpointProfilesList.clear();
                                callback.onSuccess(endpointProfilesList);
                            }
                        } else callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(EndpointProfilesPageDto result) {
                        endpointProfilesList.addAll(result.getEndpointProfiles());
                        offset = result.getPageLinkDto().getOffset();
                        callback.onSuccess(endpointProfilesList);
                    }
                });
    }

    public void setNewGroup(String groupID) {
        this.groupID = groupID;
        reset();
    }

    private void reset() {
        endpointProfilesList.clear();
        previousStart = -1;
        limit = pageSize;
        offset = DEFAULT_OFFSET;
    }

    public static synchronized EndpointProfileDataProvider getInstance(AbstractGrid<EndpointProfileDto, ?> dataGrid,
                                                                       HasErrorMessage hasErrorMessage, String groupID) {
        if (instance == null) {
            instance = new EndpointProfileDataProvider(dataGrid, hasErrorMessage, groupID);
        }
        return instance;
    }
}
