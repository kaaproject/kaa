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

import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractBaseCtlSchemasActivity<T extends BaseSchemaDto, P extends TreePlace> extends AbstractListActivity<T, P> {

    public AbstractBaseCtlSchemasActivity(P place, Class<T> dataClass,
            ClientFactory clientFactory) {
        super(place, dataClass, clientFactory);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        callback.onSuccess((Void)null);
    }

    @Override
    protected void onCustomRowAction(RowActionEvent<String> event) {
        String schemaId = event.getClickedId();
        switch (event.getAction()) {
        case KaaRowAction.CTL_EXPORT_SHALLOW:
            exportSchema(schemaId, CTLSchemaExportMethod.SHALLOW);
            break;
        case KaaRowAction.CTL_EXPORT_DEEP:
            exportSchema(schemaId, CTLSchemaExportMethod.DEEP);
            break;
        case KaaRowAction.CTL_EXPORT_FLAT:
            exportSchema(schemaId, CTLSchemaExportMethod.FLAT);
            break;
        case KaaRowAction.CTL_EXPORT_LIBRARY:
            exportSchema(schemaId, CTLSchemaExportMethod.LIBRARY);
            break;            
        }
    }
    
    private void exportSchema(String ctlSchemaId, CTLSchemaExportMethod method) {
        AsyncCallback<String> schemaExportCallback = new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, listView);
            }
            @Override
            public void onSuccess(String key) {
                ServletHelper.exportCtlSchema(key);
            }
        };
        KaaAdmin.getDataSource().prepareCTLSchemaExport(ctlSchemaId, method, schemaExportCallback);
    }
}
