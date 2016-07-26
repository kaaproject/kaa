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

import com.google.gwt.place.shared.Place;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.EventClassesDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EventClassPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EcfSchemaActivity extends AbstractListActivity<EventClassDto, EcfSchemaPlace>{

    private String ecfId;
    private int version;

    public EcfSchemaActivity(EcfSchemaPlace place, ClientFactory clientFactory) {
        super(place, EventClassDto.class, clientFactory);
        this.ecfId = place.getEcfId();
        this.version = place.getVersion();
    }

    @Override
    protected BaseListView<EventClassDto> getView() {
        return clientFactory.getCreateEcfSchemaView();
    }

    @Override
    protected AbstractDataProvider<EventClassDto, String> getDataProvider(AbstractGrid<EventClassDto, String> dataGrid) {
        return new EventClassesDataProvider(dataGrid, listView, ecfId, version);
    }

    @Override
    protected Place newEntityPlace() {
        return new EventClassPlace("", 0);
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new EventClassPlace(ecfId, version);
    }

    @Override
    protected void deleteEntity(final String id, final AsyncCallback<Void> callback) {
        EcfSchemaActivity.this.getView().clearError();

        BusyPopup.showPopup();
        KaaAdmin.getDataSource().deleteEventClassById(id, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                BusyPopup.hidePopup();
                Utils.handleException(caught, EcfSchemaActivity.this.getView());
            }

            @Override
            public void onSuccess(Void result) {
                BusyPopup.hidePopup();
            }
        });
    }

}

//        extends
//        AbstractDetailsActivity<EventClassFamilyDto, EcfSchemaView, EcfSchemaPlace> implements FormDataLoader {
//
//    public EcfSchemaActivity(EcfSchemaPlace place,
//                             ClientFactory clientFactory) {
//        super(place, clientFactory);
//    }
//
//    @Override
//    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
//        super.start(containerWidget, eventBus);
//    }
//
//    protected void bind(final EventBus eventBus) {
//        super.bind(eventBus);
//    }
//
//    @Override
//    protected String getEntityId(EcfSchemaPlace place) {
//        if (place.getVersion() > -1) {
//            return place.getEcfId();
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    protected EcfSchemaView getView(boolean create) {
//        if (create) {
//            return clientFactory.getCreateEcfSchemaView();
//        } else {
//            return clientFactory.getEcfSchemaView();
//        }
//    }
//
//    @Override
//    protected EventClassFamilyDto newEntity() {
//        return null;
//    }
//
//    @Override
//    protected void onEntityRetrieved() {
//        if (create) {
//            KaaAdmin.getDataSource().createEcfEmptySchemaForm(new BusyAsyncCallback<RecordField>() {
//                @Override
//                public void onSuccessImpl(RecordField result) {
//                    detailsView.getEcfSchemaForm().setValue(result);
//                }
//
//                @Override
//                public void onFailureImpl(Throwable caught) {
//                    Utils.handleException(caught, detailsView);
//                }
//            });
//            detailsView.getEcfSchemaForm().setFormDataLoader(this);
//        } else {
////            EventClassesDataProvider eventClassesDataProvider = new EventClassesDataProvider(place.getEcfId() ,place.getVersion());
//            EventClassFamilyVersionDto schema = null;
////            for (EventClassFamilyVersionDto schemaVersion : entity.getSchemas()) {
////                if (schemaVersion.getVersion()==place.getVersion()) {
////                    schema = schemaVersion;
////                    break;
////                }
////            }
//            detailsView.getVersion().setValue("" + schema.getVersion());
//            detailsView.getCreatedUsername().setValue(schema.getCreatedUsername());
//            detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(schema.getCreatedTime()));
////            detailsView.getEcfSchemaForm().setValue(schema.getSchemaForm());
//        }
//    }
//
//    @Override
//    protected void onSave() {
//    }
//
//    @Override
//    protected void getEntity(String id, AsyncCallback<EventClassFamilyDto> callback) {
//        KaaAdmin.getDataSource().getEcf(id, callback);
//    }
//
//    @Override
//    protected void editEntity(EventClassFamilyDto entity,
//                              final AsyncCallback<EventClassFamilyDto> callback) {
//        KaaAdmin.getDataSource().addEcfSchema(place.getEcfId(), detailsView.getEcfSchemaForm().getValue(),
//                new AsyncCallback<Void>() {
//                    @Override
//                    public void onFailure(Throwable caught) {
//                        callback.onFailure(caught);
//                    }
//
//                    @Override
//                    public void onSuccess(Void result) {
//                        callback.onSuccess(null);
//                    }
//                });
//    }
//
//    @Override
//    public void loadFormData(String fileItemName,
//                             AsyncCallback<RecordField> callback) {
//        KaaAdmin.getDataSource().generateEcfSchemaForm(fileItemName, callback);
//    }
//
//}
