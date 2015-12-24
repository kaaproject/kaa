/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.TenantCtlSchemasDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantCtlSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaFqnDto;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TenantCtlSchemasActivity extends AbstractListActivity<SchemaFqnDto, TenantCtlSchemasPlace> {

    public TenantCtlSchemasActivity(TenantCtlSchemasPlace place, ClientFactory clientFactory) {
        super(place, SchemaFqnDto.class, clientFactory);
    }

    @Override
    protected BaseListView<SchemaFqnDto> getView() {
        return clientFactory.getCtlSchemasView();
    }

    @Override
    protected AbstractDataProvider<SchemaFqnDto> getDataProvider(
            AbstractGrid<SchemaFqnDto,?> dataGrid) {
        return new TenantCtlSchemasDataProvider(dataGrid, listView);
    }

    @Override
    protected Place newEntityPlace() {
        return new CtlSchemaPlace("", null, true);
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new CtlSchemaPlace(id, null, false);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        callback.onSuccess((Void) null);
    }

}
