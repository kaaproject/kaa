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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractRecordPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseDetailsView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public abstract class AbstractRecordActivity<T extends AbstractStructureDto, F, V extends BaseRecordView<T,F>, P extends AbstractRecordPlace> extends AbstractActivity implements BaseDetailsView.Presenter, ErrorMessageCustomizer {

    protected final ClientFactory clientFactory;
    protected final String applicationId;
    protected String schemaId;
    protected final String endpointGroupId;

    protected StructureRecordDto<T> record;
    protected EndpointGroupDto endpointGroup;

    protected boolean create;
    protected boolean showActive;
    protected V recordView;
    protected P place;

    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    public AbstractRecordActivity(P place, ClientFactory clientFactory) {
        this.place = place;
        this.applicationId = place.getApplicationId();
        this.schemaId = place.getSchemaId();
        this.endpointGroupId = place.getEndpointGroupId();
        this.clientFactory = clientFactory;
        this.create = place.isCreate();
        this.showActive = place.isShowActive();
    }

    protected abstract V getRecordView(boolean create);

    protected abstract T newStruct();

    protected abstract void getRecord(String schemaId, String endpointGroupId, AsyncCallback<StructureRecordDto<T>> callback);

    protected abstract void getVacantSchemas(String endpointGroupId, AsyncCallback<List<VersionDto>> callback);

    protected abstract void editStruct(T entity, AsyncCallback<T> callback);

    protected abstract void activateStruct(String id, AsyncCallback<T> callback);

    protected abstract void deactivateStruct(String id, AsyncCallback<T> callback);

    protected abstract P getRecordPlaceImpl(String applicationId, String schemaId, String endpointGroupId, boolean create, boolean showActive, double random);
    
    protected void schemaSelected(VersionDto schema) {}

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        recordView = getRecordView(create);
        recordView.setPresenter(this);
        bind(eventBus);
        containerWidget.setWidget(recordView.asWidget());
    }

    @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
          registration.removeHandler();
        }
        registrations.clear();
    }

    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    protected void bind(final EventBus eventBus) {
        registrations.add(recordView.getRecordPanel().getSaveButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doSave(eventBus);
            }
        }));

        registrations.add(recordView.getRecordPanel().getActivateButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doActivate(eventBus);
            }
        }));

        registrations.add(recordView.getRecordPanel().getDeactivateButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doDeactivate(eventBus);
            }
        }));

        registrations.add(recordView.getBackButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                goTo(place.getPreviousPlace());
            }
        }));

        recordView.reset();
        recordView.getRecordPanel().openActive();

        if (create) {
            record = new StructureRecordDto<>();
            T inactiveStruct = createInactiveStruct();
            record.setInactiveStructureDto(inactiveStruct);
            onRecordRetrieved();
        }
        else {
            KaaAdmin.getDataSource().getEndpointGroup(endpointGroupId, new BusyAsyncCallback<EndpointGroupDto>() {

                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, recordView);
                }

                @Override
                public void onSuccessImpl(EndpointGroupDto result) {
                    endpointGroup = result;
                    getRecord(schemaId, endpointGroupId, new AsyncCallback<StructureRecordDto<T>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Utils.handleException(caught, recordView);
                        }

                        @Override
                        public void onSuccess(StructureRecordDto<T> result) {
                            record = result;
                            onRecordRetrieved();
                        }
                    });
                }
            });
        }
    }

    private T createInactiveStruct() {
        T inactiveStruct = newStruct();
        inactiveStruct.setStatus(UpdateStatus.INACTIVE);
        inactiveStruct.setApplicationId(applicationId);
        inactiveStruct.setEndpointGroupId(endpointGroupId);
        return inactiveStruct;
    }

    protected void onRecordRetrieved() {
        if (create) {
            getVacantSchemas(endpointGroupId, new BusyAsyncCallback<List<VersionDto>>() {
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, recordView);
                }

                @Override
                public void onSuccessImpl(List<VersionDto> result) {
                    VersionDto schema = Utils.getMaxSchemaVersions(result);
                    recordView.getSchema().setValue(schema);
                    recordView.getSchema().setAcceptableValues(result);
                    recordView.getRecordPanel().setData(record);
                    schemaSelected(schema);
                    recordView.getRecordPanel().openDraft();
                }
            });
        }
        else {
            String version = record.getSchemaVersion() + "";
            recordView.getSchemaVersion().setValue(version);
            if (record.hasActive() && !record.hasDraft()) {
                T inactiveStruct = createInactiveStruct();
                inactiveStruct.setSchemaId(record.getSchemaId());
                inactiveStruct.setSchemaVersion(record.getSchemaVersion());
                inactiveStruct.setDescription(record.getDescription());
                copyBody(record.getActiveStructureDto(), inactiveStruct);
                record.setInactiveStructureDto(inactiveStruct);
            }
            recordView.getRecordPanel().setData(record);
            if (endpointGroup.getWeight()==0) {
                recordView.getRecordPanel().setReadOnly();
            }
            if (showActive && record.hasActive()) {
                recordView.getRecordPanel().openActive();
            }
            else {
                recordView.getRecordPanel().openDraft();
            }
        }
    }

    protected void doSave(final EventBus eventBus) {
        T inactiveStruct = record.getInactiveStructureDto();
        if (create) {
            schemaId = recordView.getSchema().getValue().getId();
            inactiveStruct.setSchemaId(schemaId);
            inactiveStruct.setSchemaVersion(recordView.getSchema().getValue().getVersion());
        }
        inactiveStruct.setDescription(recordView.getRecordPanel().getDescription().getValue());
        updateBody(inactiveStruct, recordView.getRecordPanel().getBody().getValue());
        editStruct(inactiveStruct,
                new BusyAsyncCallback<T>() {
                    public void onSuccessImpl(T result) {
                        goTo(getRecordPlace(applicationId, schemaId, endpointGroupId, false, false, Math.random()));
                    }

                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, recordView, AbstractRecordActivity.this);
                    }
        });
    }
    
    protected abstract void updateBody(T struct, F value);
    
    protected abstract void copyBody(T activeStruct, T inactiveStruct);

    protected void doActivate(final EventBus eventBus) {
        T inactiveStruct = record.getInactiveStructureDto();
        activateStruct(inactiveStruct.getId(),
                new BusyAsyncCallback<T>() {
            public void onSuccessImpl(T result) {
                goTo(getRecordPlace(applicationId, schemaId, endpointGroupId, false, true, Math.random()));
            }

            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, recordView);
            }
        });
    }

    protected void doDeactivate(final EventBus eventBus) {
        T activeStruct = record.getActiveStructureDto();
        deactivateStruct(activeStruct.getId(),
                new BusyAsyncCallback<T>() {
            public void onSuccessImpl(T result) {
                goTo(getRecordPlace(applicationId, schemaId, endpointGroupId, false, true, Math.random()));
            }

            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, recordView);
            }
        });
    }

    private P getRecordPlace(String applicationId, String schemaId, String endpointGroupId, boolean create, boolean showActive, double random) {
        P recordPlace = getRecordPlaceImpl(applicationId, schemaId, endpointGroupId, create, showActive, random);
        recordPlace.setPreviousPlace(place.getPreviousPlace());
        return recordPlace;
    }


}
