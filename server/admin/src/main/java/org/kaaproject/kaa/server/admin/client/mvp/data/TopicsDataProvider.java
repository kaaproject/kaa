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

package org.kaaproject.kaa.server.admin.client.mvp.data;

import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.util.List;

import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;

public class TopicsDataProvider extends AbstractDataProvider<TopicDto>{

    private String applicationId;
    private String endpointGroupId;

    public TopicsDataProvider(MultiSelectionModel<TopicDto> selectionModel,
                                    AsyncCallback<List<TopicDto>> asyncCallback,
                                    String applicationId, String endpointGroupId) {
        super(selectionModel, asyncCallback);
        this.applicationId = applicationId;
        this.endpointGroupId = endpointGroupId;
    }

    @Override
    protected void loadData(final LoadCallback callback, final HasData<TopicDto> display) {
        if (!isEmpty(applicationId)) {
            KaaAdmin.getDataSource().loadTopics(applicationId, new AsyncCallback<List<TopicDto>>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);

                }
                @Override
                public void onSuccess(List<TopicDto> result) {
                    callback.onSuccess(result, display);
                }
            });
        }
        else if (!isEmpty(endpointGroupId)) {
            KaaAdmin.getDataSource().loadTopicsByEndpointGroupId(endpointGroupId, new AsyncCallback<List<TopicDto>>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);

                }
                @Override
                public void onSuccess(List<TopicDto> result) {
                    callback.onSuccess(result, display);
                }
            });
        }
    }

}
