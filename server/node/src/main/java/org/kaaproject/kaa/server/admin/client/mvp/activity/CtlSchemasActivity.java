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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.DataFilter;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemasView;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class CtlSchemasActivity<P extends TreePlace> extends AbstractListActivity<CTLSchemaMetaInfoDto, P> {

    public CtlSchemasActivity(P place, Class<CTLSchemaMetaInfoDto> dataClass, ClientFactory clientFactory) {
        super(place, dataClass, clientFactory);
    }
    
    protected abstract CtlSchemasView getView();
    
    private CtlSchemasView ctlSchemaView() {
        return (CtlSchemasView) listView;
    }
    
    @Override
    public void bind(final EventBus eventBus) {
        super.bind(eventBus);
        if (ctlSchemaView().displayShowHigherLevelScopeCheckBox()) {
            DataFilter<CTLSchemaMetaInfoDto> dataFilter = new DataFilter<CTLSchemaMetaInfoDto>() {
                @Override
                public boolean accept(CTLSchemaMetaInfoDto value) {
                    return ctlSchemaView().getShowHigherScopeCheckBox().getValue() 
                            || value.getScope() == getCurrentScope();
                }
            };
            dataProvider.setDataFilter(dataFilter);
            registrations.add(ctlSchemaView().getShowHigherScopeCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    dataProvider.updateData();
                }
            }));
        }
    }
    
    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        callback.onSuccess((Void) null);
    }
    
    protected abstract CTLSchemaScopeDto getCurrentScope();

}
