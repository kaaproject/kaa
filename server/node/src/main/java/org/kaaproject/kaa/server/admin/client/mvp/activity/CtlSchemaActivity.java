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

import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.ActionsButton.ActionMenuItemListener;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel.FormDataLoader;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.SchemaErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CtlSchemaActivity extends AbstractDetailsActivity<CtlSchemaFormDto, CtlSchemaView, CtlSchemaPlace> 
                                                implements ErrorMessageCustomizer, FormDataLoader {

    private static final ErrorMessageCustomizer schemaErrorMessageCustomizer = new SchemaErrorMessageCustomizer();
    
    private Integer version = null;

    public CtlSchemaActivity(CtlSchemaPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        version = place.getVersion();
    }

    @Override
    protected String getEntityId(CtlSchemaPlace place) {
        return place.getFqn();
    }

    @Override
    protected CtlSchemaFormDto newEntity() {
        return null;
    }

    @Override
    protected void bind(final EventBus eventBus) {
        if (create) {
            detailsView.getSchemaForm().setFormDataLoader(this);
        } else {
            registrations.add(detailsView.getVersion().addValueChangeHandler(new ValueChangeHandler<Integer>() {
                @Override
                public void onValueChange(ValueChangeEvent<Integer> event) {
                    detailsView.getVersion().setValue(version);
                    goTo(new CtlSchemaPlace(place.getFqn(), event.getValue(), false));
                }
            }));
            registrations.add(detailsView.getCreateNewSchemaVersionButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Integer version = detailsView.getVersion().getValue();
                    CtlSchemaPlace newPlace = new CtlSchemaPlace(place.getFqn(), version, true); 
                    newPlace.setPreviousPlace(place);
                    goTo(newPlace);
                }
            }));
            registrations.add(detailsView.getDeleteSchemaVersionButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final Integer version = detailsView.getVersion().getValue();
                    final String fqn = place.getFqn();
                    ConfirmDialog.ConfirmListener listener = new ConfirmDialog.ConfirmListener() {
                        @Override
                        public void onNo() {}

                        @Override
                        public void onYes() {
                            KaaAdmin.getDataSource().deleteCTLSchema(fqn, version, new BusyAsyncCallback<Void>() {
                                @Override
                                public void onSuccessImpl(Void result) {
                                    List<Integer> versions = entity.getAvailableVersions();
                                    versions.remove(version);
                                    if (versions.isEmpty()) {
                                        goTo(place.getPreviousPlace());
                                    } else {
                                        goTo(new CtlSchemaPlace(place.getFqn(), versions.get(versions.size()-1), false));
                                    }
                                }
                                @Override
                                public void onFailureImpl(Throwable caught) {
                                    Utils.handleException(caught, detailsView);
                                }
                            });
                        }
                    };
                    
                    ConfirmDialog dialog = new ConfirmDialog(listener, Utils.messages.deleteCommonTypeVersionTitle(), 
                            Utils.messages.deleteCommonTypeVersionQuestion(fqn, version.toString()));
                    dialog.center();
                    dialog.show();
                }
            }));
        }
        super.bind(eventBus);
    }

    @Override
    protected void onEntityRetrieved() {
        if (create) {
            KaaAdmin.getDataSource().createNewCTLSchemaFormInstance(place.getSourceFqn(), 
                    place.getSourceVersion(), CTLSchemaScopeDto.TENANT, null, 
                    new BusyAsyncCallback<CtlSchemaFormDto>() {
                        @Override
                        public void onSuccessImpl(CtlSchemaFormDto result) {
                            entity = result;
                            bindDetailsView(true);
                        }
                        @Override
                        public void onFailureImpl(Throwable caught) {
                            Utils.handleException(caught, detailsView);
                        }
            });
        } else {
            if (entity == null) {
                goTo(place.getPreviousPlace());
            } else {
                bindDetailsView(false);
            }
        }
    }
    
    private void bindDetailsView(boolean fireChanged) {
        if (!create) {
            List<Integer> schemaVersions = entity.getAvailableVersions();
            
            if (version == null && !schemaVersions.isEmpty()) {
                version = schemaVersions.get(schemaVersions.size()-1);
            }
            if (version != null) {
                detailsView.getVersion().setValue(version);
            }
            detailsView.getVersion().setAcceptableValues(schemaVersions);
            detailsView.setTitle(entity.getFqnString());
            
            if (entity.hasDependencies()) {
                registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.shallow(), new ActionMenuItemListener() {
                    @Override
                    public void onMenuItemSelected() {
                        exportSchema(CTLSchemaExportMethod.SHALLOW);
                    }
                }));
                registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.deep(), new ActionMenuItemListener() {
                    @Override
                    public void onMenuItemSelected() {
                        exportSchema(CTLSchemaExportMethod.DEEP);
                    }
                }));
            }
            registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.flat(), new ActionMenuItemListener() {
                @Override
                public void onMenuItemSelected() {
                    exportSchema(CTLSchemaExportMethod.FLAT);
                }
            }));
            registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.javaLibrary(), new ActionMenuItemListener() {
                @Override
                public void onMenuItemSelected() {
                    exportSchema(CTLSchemaExportMethod.LIBRARY);
                }
            }));            
        }
        detailsView.getName().setValue(entity.getSchemaName());
        detailsView.getDescription().setValue(entity.getDescription());
        detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
        detailsView.getSchemaForm().setValue(entity.getSchema(), fireChanged);
    }
    
    private void exportSchema(CTLSchemaExportMethod method) {
        AsyncCallback<String> schemaExportCallback = new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
            @Override
            public void onSuccess(String key) {
                ServletHelper.exportCtlSchema(key);
            }
        };
        KaaAdmin.getDataSource().prepareCTLSchemaExport(entity.getCtlSchemaId(), method, schemaExportCallback);
    }
    
    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        editEntity(entity,
                new BusyAsyncCallback<CtlSchemaFormDto>() {
                    public void onSuccessImpl(CtlSchemaFormDto result) {
                        if (!create) {
                            goTo(new CtlSchemaPlace(place.getFqn(), place.getVersion(), false));
                        } else if (place.getPreviousPlace() != null) {
                            goTo(place.getPreviousPlace());
                        }
                    }

                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, detailsView);
                    }
        });
    }

    @Override
    protected void onSave() {
        entity.setSchemaName(detailsView.getName().getValue());
        entity.setDescription(detailsView.getDescription().getValue());
        entity.setSchema(detailsView.getSchemaForm().getValue());
    }

    @Override
    public String customizeErrorMessage(Throwable caught) {
        String errorMessage = schemaErrorMessageCustomizer.customizeErrorMessage(caught);
        if (errorMessage == null) {
            errorMessage = caught.getLocalizedMessage();        
        }
        return errorMessage;
    }

    @Override
    public void loadFormData(String fileItemName,
            AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateCtlSchemaForm(fileItemName, callback);
    }

    @Override
    protected CtlSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateCtlSchemaView();
        } else {
            return clientFactory.getCtlSchemaView();
        }
    }

    @Override
    protected void getEntity(String id,
            AsyncCallback<CtlSchemaFormDto> callback) {
        KaaAdmin.getDataSource().getCTLSchemaForm(id, place.getVersion(), callback);
    }

    @Override
    protected void editEntity(CtlSchemaFormDto entity,
            AsyncCallback<CtlSchemaFormDto> callback) {
        KaaAdmin.getDataSource().editCTLSchemaForm(entity, callback);
    }

}
