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

package org.kaaproject.kaa.server.admin.client.mvp.view.navigation;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SystemCtlSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantCtlSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace.PlaceCell;
import org.kaaproject.kaa.server.admin.client.mvp.place.UsersPlace;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class NavigationTreeViewModel implements TreeViewModel {

    private List<TreePlace> nodes = new ArrayList<TreePlace>();

    private SingleSelectionModel<TreePlace> selectionModel = new SingleSelectionModel<TreePlace>();

    private EventBus eventBus;

    public NavigationTreeViewModel() {
        KaaAuthorityDto autority = KaaAdmin.getAuthInfo().getAuthority();
        switch (autority) {
        case KAA_ADMIN:
            nodes.add(new TenantsPlace());
            nodes.add(new SystemCtlSchemasPlace());
            break;
        case TENANT_ADMIN:
            nodes.add(new ApplicationsPlace());
            nodes.add(new UsersPlace());
            nodes.add(new EcfsPlace());
            nodes.add(new TenantCtlSchemasPlace());
            break;
        case TENANT_DEVELOPER:
            nodes.add(new ApplicationsPlace());
            nodes.add(new TenantCtlSchemasPlace());
            break;
        case TENANT_USER:
            nodes.add(new ApplicationsPlace());
            nodes.add(new TenantCtlSchemasPlace());
            break;
        }
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public SingleSelectionModel<TreePlace> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            ListDataProvider<TreePlace> dataProvider = new ListDataProvider<TreePlace>(
                    nodes);
            PlaceCell cell = new PlaceCell();
            return new DefaultNodeInfo<TreePlace>(dataProvider, cell, selectionModel, null);
        } else if (value instanceof TreePlace) {
            return ((TreePlace)value).getNodeInfo(selectionModel, eventBus);
        }
        return null;
    }

    @Override
    public boolean isLeaf(Object value) {
        if (value instanceof TreePlace) {
            return ((TreePlace)value).isLeaf();
        }
        return value != null;
    }

}
