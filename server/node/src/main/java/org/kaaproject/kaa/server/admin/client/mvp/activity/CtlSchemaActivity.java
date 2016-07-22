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

import java.util.Collections;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.ActionsButton.ActionMenuItemListener;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace.SchemaType;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.NotificationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ServerProfileSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel.FormDataLoader;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.SchemaErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.ConfigurationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.LogSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.NotificationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;

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
        return place.isCreate() ? null : place.getMetaInfoId();
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
                    goTo(new CtlSchemaPlace(place.getMetaInfoId(), event.getValue(), place.getScope(), place.getApplicationId(), place.isEditable(), false));
                }
            }));
            registrations.add(detailsView.getCreateNewSchemaVersionButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Integer version = detailsView.getVersion().getValue();
                    CtlSchemaPlace newPlace = new CtlSchemaPlace(place.getMetaInfoId(), version, place.getScope(), place.getApplicationId(), true, true); 
                    newPlace.setPreviousPlace(place);
                    goTo(newPlace);
                }
            }));
            
            registrations.add(detailsView.getUpdateSchemaScopeButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    CTLSchemaMetaInfoDto metaInfo = entity.getMetaInfo();

                    KaaAdmin.getDataSource().promoteScopeToTenant(metaInfo.getApplicationId(), metaInfo.getFqn(), new BusyAsyncCallback<CTLSchemaMetaInfoDto>() {
                        @Override
                        public void onFailureImpl(Throwable caught) {
                            Utils.handleException(caught, detailsView);
                        }
                        @Override
                        public void onSuccessImpl(CTLSchemaMetaInfoDto result) {
                            CtlSchemaPlace place = new CtlSchemaPlace(result.getId(), version, result.getScope(), 
                                    CtlSchemaActivity.this.place.getApplicationId(), result.getScope() == CTLSchemaScopeDto.APPLICATION, false);
                            goTo(place);
                        }
                    });
                }
            }));
            
            registrations.add(detailsView.getDeleteSchemaVersionButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final Integer version = detailsView.getVersion().getValue();
                    final String fqn = entity.getMetaInfo().getFqn();
                    ConfirmDialog.ConfirmListener listener = new ConfirmDialog.ConfirmListener() {
                        @Override
                        public void onNo() {}

                        @Override
                        public void onYes() {
                            KaaAdmin.getDataSource().deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(fqn, version, 
                                    entity.getMetaInfo().getTenantId(), place.getApplicationId(), new BusyAsyncCallback<Void>() {
                                @Override
                                public void onSuccessImpl(Void result) {
                                    List<Integer> versions = entity.getMetaInfo().getVersions();
                                    versions.remove(version);
                                    if (versions.isEmpty()) {
                                        goTo(place.getPreviousPlace());
                                    } else {
                                        goTo(new CtlSchemaPlace(place.getMetaInfoId(), versions.get(versions.size()-1), place.getScope(), place.getApplicationId(), true, false));
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
            ConverterType converterType;

            if (place.getSchemaType() == SchemaType.CONFIGURATION) {
                converterType = ConverterType.CONFIGURATION_FORM_AVRO_CONVERTER;
            } else {
                converterType = ConverterType.FORM_AVRO_CONVERTER;
            }

            KaaAdmin.getDataSource().createNewCTLSchemaFormInstance(place.getMetaInfoId(),
                    place.getVersion(), place.getApplicationId(), converterType,
                    new BusyAsyncCallback<CtlSchemaFormDto>() {
                        @Override
                        public void onSuccessImpl(CtlSchemaFormDto result) {
                            entity = result;
                            if (place.getSchemaType() != null) {
                                entity.getSchema().setDisplayNameFieldOptional(false);
                            }
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
            List<Integer> schemaVersions = entity.getMetaInfo().getVersions();
            Collections.sort(schemaVersions);
            
            if (version == null && !schemaVersions.isEmpty()) {
                version = schemaVersions.get(schemaVersions.size()-1);
            }
            if (version != null) {
                detailsView.getVersion().setValue(version);
            }
            detailsView.getVersion().setAcceptableValues(schemaVersions);
            detailsView.setTitle(entity.getMetaInfo().getFqn());
            
            registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.shallow(), new ActionMenuItemListener() {
                @Override
                public void onMenuItemSelected() {
                    exportSchema(CTLSchemaExportMethod.SHALLOW);
                }
            }));
            if (entity.hasDependencies()) {
                registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.deep(), new ActionMenuItemListener() {
                    @Override
                    public void onMenuItemSelected() {
                        exportSchema(CTLSchemaExportMethod.DEEP);
                    }
                }));
                registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.flat(), new ActionMenuItemListener() {
                    @Override
                    public void onMenuItemSelected() {
                        exportSchema(CTLSchemaExportMethod.FLAT);
                    }
                }));
            }
            registrations.add(detailsView.getExportActionsButton().addMenuItem(Utils.constants.javaLibrary(), new ActionMenuItemListener() {
                @Override
                public void onMenuItemSelected() {
                    exportSchema(CTLSchemaExportMethod.LIBRARY);
                }
            }));            
        }
        detailsView.getScope().setText(Utils.getCtlScopeTitleString(entity.getMetaInfo().getScope()));
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
        KaaAdmin.getDataSource().prepareCTLSchemaExport(entity.getId(), method, schemaExportCallback);
    }
    
    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        editEntity(entity,
                new BusyAsyncCallback<CtlSchemaFormDto>() {
                    public void onSuccessImpl(CtlSchemaFormDto result) {
                        if (!create) {
                            goTo(new CtlSchemaPlace(place.getMetaInfoId(), place.getVersion(), place.getScope(), place.getApplicationId(), true, false));
                        } else if (place.getSchemaType() != null) {
                            if (place.getSchemaType() == SchemaType.ENDPOINT_PROFILE) {
                                goTo(new ProfileSchemasPlace(place.getApplicationId()));
                            } else if(place.getSchemaType() == SchemaType.CONFIGURATION) {
                                goTo(new ConfigurationSchemasPlace(place.getApplicationId()));
                            } else if (place.getSchemaType() == SchemaType.SERVER_PROFILE) {
                                goTo(new ServerProfileSchemasPlace(place.getApplicationId()));
                            } else if (place.getSchemaType() == SchemaType.NOTIFICATION) {
                                goTo(new NotificationSchemasPlace(place.getApplicationId()));
                            } else if (place.getSchemaType() == SchemaType.LOG_SCHEMA) {
                                goTo(new LogSchemasPlace(place.getApplicationId()));
                            }
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
            final AsyncCallback<RecordField> callback) {
        KaaAdmin.getDataSource().generateCtlSchemaForm(fileItemName, place.getApplicationId(), new AsyncCallback<RecordField>() {
            @Override
            public void onSuccess(RecordField result) {
                if (place.getSchemaType() != null) {
                    result.setDisplayNameFieldOptional(false);
                }
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    @Override
    protected CtlSchemaView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateCtlSchemaView();
        } else {
            if (place.isEditable()) {
                if (place.getScope() == CTLSchemaScopeDto.APPLICATION) {
                    return clientFactory.getEditApplicationCtlSchemaView();
                } else {
                    return clientFactory.getEditCtlSchemaView();
                }
            } else {
                return clientFactory.getViewCtlSchemaView();
            }
        }
    }

    @Override
    protected void getEntity(String id,
            AsyncCallback<CtlSchemaFormDto> callback) {
        if (version == null) {
            KaaAdmin.getDataSource().getLatestCTLSchemaForm(id, callback);
        } else {
            KaaAdmin.getDataSource().getCTLSchemaFormByMetaInfoIdAndVer(id, version, callback);
        }
    }

    @Override
    protected void editEntity(final CtlSchemaFormDto entity,
            final AsyncCallback<CtlSchemaFormDto> callback) {
        if (place.getScope().getLevel() > CTLSchemaScopeDto.SYSTEM.getLevel()) {
            KaaAdmin.getDataSource().checkFqnExists(entity, new BusyAsyncCallback<Boolean>() {
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }
                @Override
                public void onSuccessImpl(Boolean result) {
                    if (!result) {
                        editSchema(entity, callback);
                    } else {
                        ConfirmDialog.ConfirmListener listener = new ConfirmDialog.ConfirmListener() {
                            @Override
                            public void onNo() {
                            }

                            @Override
                            public void onYes() {
                                editSchema(entity, callback);
                            }
                        };
                        ConfirmDialog dialog = new ConfirmDialog(listener, Utils.messages.commonTypeFqnAlreadyExistTitle(), 
                                Utils.messages.commonTypeFqnAlreadyExistsQuestion());
                        dialog.center();
                        dialog.show();
                    }
                }
            });
        } else {
            editSchema(entity, callback);
        }
    }
    
    private void editSchema(CtlSchemaFormDto entity, final AsyncCallback<CtlSchemaFormDto> callback) {
        if (create && place.getSchemaType() != null) {
            if (place.getSchemaType() == SchemaType.ENDPOINT_PROFILE) {
                KaaAdmin.getDataSource().createProfileSchemaFormCtlSchema(entity, 
                        new BusyAsyncCallback<ProfileSchemaViewDto>() {
                            @Override
                            public void onFailureImpl(Throwable caught) {
                                callback.onFailure(caught);
                            }
                            @Override
                            public void onSuccessImpl(ProfileSchemaViewDto result) {
                                callback.onSuccess(null);
                            }
                    });
            } else if(place.getSchemaType() == SchemaType.CONFIGURATION) {
                KaaAdmin.getDataSource().createConfigurationSchemaFormCtlSchema(entity,
                        new BusyAsyncCallback<ConfigurationSchemaViewDto>() {
                            @Override
                            public void onFailureImpl(Throwable caught) {
                                callback.onFailure(caught);
                            }
                            @Override
                            public void onSuccessImpl(ConfigurationSchemaViewDto result) {
                                callback.onSuccess(null);
                            }
                        });
            } else if (place.getSchemaType() == SchemaType.SERVER_PROFILE) {
                KaaAdmin.getDataSource().createServerProfileSchemaFormCtlSchema(entity, 
                        new BusyAsyncCallback<ServerProfileSchemaViewDto>() {
                            @Override
                            public void onFailureImpl(Throwable caught) {
                                callback.onFailure(caught);
                            }
                            @Override
                            public void onSuccessImpl(ServerProfileSchemaViewDto result) {
                                callback.onSuccess(null);
                            }
                    });
            } else if (place.getSchemaType() == SchemaType.NOTIFICATION) {
                KaaAdmin.getDataSource().createNotificationSchemaFormCtlSchema(entity, new BusyAsyncCallback<NotificationSchemaViewDto>() {
                    @Override
                    public void onFailureImpl(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccessImpl(NotificationSchemaViewDto notificationSchemaViewDto) {
                        callback.onSuccess(null);
                    }
                });
            } else if (place.getSchemaType() == SchemaType.LOG_SCHEMA) {
                KaaAdmin.getDataSource().createLogSchemaFormCtlSchema(entity, new BusyAsyncCallback<LogSchemaViewDto>() {
                    @Override
                    public void onFailureImpl(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccessImpl(LogSchemaViewDto logSchemaViewDto) {
                        callback.onSuccess(null);
                    }
                });
            }
        } else {
            KaaAdmin.getDataSource().editCTLSchemaForm(entity, ConverterType.FORM_AVRO_CONVERTER, callback);
        }
    }

}
