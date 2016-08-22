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

package org.kaaproject.kaa.server.admin.client.mvp.data;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.RecordKey.RecordFiles;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEvent;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordViewDto;
import org.kaaproject.kaa.server.admin.shared.endpoint.EndpointProfileViewDto;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConfigurationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.LogSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.NotificationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.services.AdminUIService;
import org.kaaproject.kaa.server.admin.shared.services.AdminUIServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.ApplicationService;
import org.kaaproject.kaa.server.admin.shared.services.ApplicationServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.ConfigurationService;
import org.kaaproject.kaa.server.admin.shared.services.ConfigurationServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.CtlService;
import org.kaaproject.kaa.server.admin.shared.services.CtlServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.DeviceManagementService;
import org.kaaproject.kaa.server.admin.shared.services.DeviceManagementServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.EventService;
import org.kaaproject.kaa.server.admin.shared.services.EventServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.GroupService;
import org.kaaproject.kaa.server.admin.shared.services.GroupServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.LoggingService;
import org.kaaproject.kaa.server.admin.shared.services.LoggingServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.NotificationService;
import org.kaaproject.kaa.server.admin.shared.services.NotificationServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.ProfileService;
import org.kaaproject.kaa.server.admin.shared.services.ProfileServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.SdkService;
import org.kaaproject.kaa.server.admin.shared.services.SdkServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.TenantService;
import org.kaaproject.kaa.server.admin.shared.services.TenantServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.UserService;
import org.kaaproject.kaa.server.admin.shared.services.UserServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.VerifierService;
import org.kaaproject.kaa.server.admin.shared.services.VerifierServiceAsync;

import java.util.ArrayList;
import java.util.List;

public class DataSource {

    private final ApplicationServiceAsync applicationRpcService = GWT.create(ApplicationService.class);

    private final ConfigurationServiceAsync configurationRpcService = GWT.create(ConfigurationService.class);

    private final CtlServiceAsync ctlRpcService = GWT.create(CtlService.class);

    private final DeviceManagementServiceAsync deviceManagementRpcService = GWT.create(DeviceManagementService.class);

    private final EventServiceAsync eventRpcService = GWT.create(EventService.class);

    private final GroupServiceAsync groupRpcService = GWT.create(GroupService.class);

    private final LoggingServiceAsync loggingRpcService = GWT.create(LoggingService.class);

    private final NotificationServiceAsync notificationRpcService = GWT.create(NotificationService.class);

    private final ProfileServiceAsync profileRpcService = GWT.create(ProfileService.class);

    private final SdkServiceAsync sdkRpcService = GWT.create(SdkService.class);

    private final TenantServiceAsync tenantRpcService = GWT.create(TenantService.class);

    private final UserServiceAsync userRpcService = GWT.create(UserService.class);

    private final VerifierServiceAsync verifierRpcService = GWT.create(VerifierService.class);

    private final AdminUIServiceAsync adminUIRpcService = GWT.create(AdminUIService.class);

    private final EventBus eventBus;

    private List<TenantDto> tenants;

    private List<ApplicationDto> applications;

    private List<UserDto> users;

    private List<EventClassFamilyDto> ecfs;

    private List<PluginInfoDto> logAppenderPluginInfos;

    private List<PluginInfoDto> userVerifierPluginInfos;

    public DataSource(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void getUserProfile(
            final AsyncCallback<UserDto> callback) {
        userRpcService.getUserProfile(
                new DataCallback<UserDto>(callback) {
                    @Override
                    protected void onResult(UserDto result) {
                    }
                });
    }

    public void loadAllTenantAdminsByTenantId(String tenantId,  final AsyncCallback<List<UserDto>> callback) {

            userRpcService.findAllTenantAdminsByTenantId(tenantId, new DataCallback<List<UserDto>>(
                    callback) {
                @Override
                protected void onResult(List<UserDto> result) {
                    eventBus.fireEvent(new DataEvent(UserDto.class, true));
                }
            });
    }

    public void editUserProfile(UserProfileUpdateDto userProfileUpdateDto,
            final AsyncCallback<Void> callback) {
        userRpcService.editUserProfile(userProfileUpdateDto,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                    }
                });
    }

    public void getMailProperties(
            final AsyncCallback<PropertiesDto> callback) {
        adminUIRpcService.getMailProperties(
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void editMailProperties(PropertiesDto mailProperties,
                                   final AsyncCallback<PropertiesDto> callback) {
        adminUIRpcService.editMailProperties(mailProperties,
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void getGeneralProperties(
            final AsyncCallback<PropertiesDto> callback) {
        adminUIRpcService.getGeneralProperties(
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void editGeneralProperties(PropertiesDto mailProperties,
                                      final AsyncCallback<PropertiesDto> callback) {
        adminUIRpcService.editGeneralProperties(mailProperties,
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void loadTenants(final AsyncCallback<List<TenantDto>> callback) {
        loadTenants(callback, false);
    }


    public void loadTenants(final AsyncCallback<List<TenantDto>> callback,
            boolean refresh) {
        if (tenants == null || refresh) {
            tenants = new ArrayList<>();
            tenantRpcService.getTenants(new DataCallback<List<TenantDto>>(callback) {
                @Override
                protected void onResult(List<TenantDto> result) {
                    tenants.addAll(result);
                    eventBus.fireEvent(new DataEvent(TenantDto.class, true));
                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess(tenants);
            }
        }
    }

    private void refreshTenants() {
        loadTenants(null, true);
    }

    public void deleteTenant(String tenantId, final AsyncCallback<Void> callback) {
        tenantRpcService.deleteTenant(tenantId, new DataCallback<Void>(callback) {
            @Override
            protected void onResult(Void result) {
                refreshTenants();
            }
        });
    }

    public void editTenant(TenantDto tenant,
            final AsyncCallback<TenantDto> callback) {
        tenantRpcService.editTenant(tenant,
                new DataCallback<TenantDto>(callback) {
                    @Override
                    protected void onResult(TenantDto result) {
                        refreshTenants();
                    }
                });
    }

    public void getTenant(String tenantId,
            final AsyncCallback<TenantDto> callback) {
        tenantRpcService.getTenant(tenantId,
                new DataCallback<TenantDto>(callback) {
                    @Override
                    protected void onResult(TenantDto result) {
                    }
                });
    }

    public void loadApplications(
            final AsyncCallback<List<ApplicationDto>> callback) {
        loadApplications(callback, false);
    }

    public void loadApplications(
            final AsyncCallback<List<ApplicationDto>> callback, boolean refresh) {
        if (applications == null || refresh) {
            applications = new ArrayList<ApplicationDto>();
            applicationRpcService.getApplications(new DataCallback<List<ApplicationDto>>(
                    callback) {
                @Override
                protected void onResult(List<ApplicationDto> result) {
                    applications.addAll(result);
                    eventBus.fireEvent(new DataEvent(ApplicationDto.class, true));
                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess(applications);
            }
        }
    }

    private void refreshApplications() {
        loadApplications(null, true);
    }

    public void editApplication(ApplicationDto application,
                                final AsyncCallback<ApplicationDto> callback) {
        applicationRpcService.editApplication(application,
                new DataCallback<ApplicationDto>(callback) {
                    @Override
                    protected void onResult(ApplicationDto result) {
                        refreshApplications();
                    }
                });
    }

    public void getApplication(String applicationId,
                               final AsyncCallback<ApplicationDto> callback) {
        applicationRpcService.getApplication(applicationId,
                new DataCallback<ApplicationDto>(callback) {
                    @Override
                    protected void onResult(ApplicationDto result) {
                    }
                });
    }

    public void getSchemaVersionsByApplicationId(String applicationId,
                                                 final AsyncCallback<SchemaVersions> callback) {
        sdkRpcService.getSchemaVersionsByApplicationId(applicationId,
                new DataCallback<SchemaVersions>(callback) {
                    @Override
                    protected void onResult(SchemaVersions result) {
                    }
                });
    }

    public void generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform,
                            final AsyncCallback<String> callback) {
        sdkRpcService.generateSdk(sdkProfile, targetPlatform,
                new DataCallback<String>(callback) {
                    @Override
                    protected void onResult(String result) {
                    }
                });
    }

    public void getRecordData(String applicationId,
                              Integer logSchemaVersion, RecordFiles fileType,
                              final AsyncCallback<String> callback) {
        adminUIRpcService.getRecordDataByApplicationIdAndSchemaVersion(applicationId, logSchemaVersion, fileType,
                new DataCallback<String>(callback) {
                    @Override
                    protected void onResult(String result) {
                    }
                });
    }

    public void getRecordLibrary(String applicationId,
                                 Integer logSchemaVersion, RecordFiles fileType,
                                 final AsyncCallback<String> callback) {
        adminUIRpcService.getRecordLibraryByApplicationIdAndSchemaVersion(applicationId, logSchemaVersion, fileType,
                new DataCallback<String>(callback) {
                    @Override
                    protected void onResult(String result) {
                    }
                });
    }

    public void loadUsers(final AsyncCallback<List<UserDto>> callback) {
        loadUsers(callback, false);
    }

    public void loadUsers(final AsyncCallback<List<UserDto>> callback,
                          boolean refresh) {
        if (users == null || refresh) {
            users = new ArrayList<UserDto>();
            userRpcService.getUsers(new DataCallback<List<UserDto>>(callback) {
                @Override
                protected void onResult(List<UserDto> result) {
                    users.addAll(result);
                    eventBus.fireEvent(new DataEvent(UserDto.class, true));
                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess(users);
            }
        }
    }

    private void refreshUsers() {
        loadUsers(null, true);
    }

    public void deleteUser(String userId, final AsyncCallback<Void> callback) {
        userRpcService.deleteUser(userId, new DataCallback<Void>(callback) {
            @Override
            protected void onResult(Void result) {
                refreshUsers();
            }
        });
    }

    public void editUser(UserDto user, final AsyncCallback<UserDto> callback) {
        userRpcService.editUser(user, new DataCallback<UserDto>(callback) {
            @Override
            protected void onResult(UserDto result) {
                refreshUsers();
            }
        });
    }

    public void getUser(String userId, final AsyncCallback<UserDto> callback) {
        userRpcService.getUser(userId, new DataCallback<UserDto>(callback) {
            @Override
            protected void onResult(UserDto result) {
            }
        });
    }

    public void createSimpleEmptySchemaForm(final AsyncCallback<RecordField> callback) {
        adminUIRpcService.createSimpleEmptySchemaForm(new DataCallback<RecordField>(callback) {
            @Override
            protected void onResult(RecordField result) {
            }
        });
    }

    public void createCommonEmptySchemaForm(final AsyncCallback<RecordField> callback) {
        adminUIRpcService.createCommonEmptySchemaForm(new DataCallback<RecordField>(callback) {
            @Override
            protected void onResult(RecordField result) {
            }
        });
    }

    public void createConfigurationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm, final AsyncCallback<ConfigurationSchemaViewDto> callback) {
        configurationRpcService.createConfigurationSchemaFormCtlSchema(ctlSchemaForm, new DataCallback<ConfigurationSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(ConfigurationSchemaViewDto.class));
                    }
                });
    }

    public void createEcfEmptySchemaForm(final AsyncCallback<RecordField> callback) {
        eventRpcService.createEcfEmptySchemaForm(new DataCallback<RecordField>(callback) {
            @Override
            protected void onResult(RecordField result) {
            }
        });
    }

    public void generateSimpleSchemaForm(String fileItemName,
                                         final AsyncCallback<RecordField> callback) {
        adminUIRpcService.generateSimpleSchemaForm(fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void generateCommonSchemaForm(String fileItemName,
                                         final AsyncCallback<RecordField> callback) {
        adminUIRpcService.generateCommonSchemaForm(fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void generateConfigurationSchemaForm(String fileItemName,
                                                final AsyncCallback<RecordField> callback) {
        configurationRpcService.generateConfigurationSchemaForm(fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void generateEcfSchemaForm(String fileItemName,
                                      final AsyncCallback<RecordField> callback) {
        eventRpcService.generateEcfSchemaForm(fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void loadEcfs(
            final AsyncCallback<List<EventClassFamilyDto>> callback) {
        loadEcfs(callback, false);
    }

    public void loadEcfs(
            final AsyncCallback<List<EventClassFamilyDto>> callback, boolean refresh) {
        if (ecfs == null || refresh) {
            ecfs = new ArrayList<EventClassFamilyDto>();
            eventRpcService.getEventClassFamilies(new DataCallback<List<EventClassFamilyDto>>(
                    callback) {
                @Override
                protected void onResult(List<EventClassFamilyDto> result) {
                    ecfs.addAll(result);
                    eventBus.fireEvent(new DataEvent(EventClassFamilyDto.class, true));
                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess(ecfs);
            }
        }
    }

    private void refreshEcfs() {
        loadEcfs(null, true);
    }

    public void editEcf(EventClassFamilyDto ecf,
                        final AsyncCallback<EventClassFamilyDto> callback) {
        eventRpcService.editEventClassFamily(ecf,
                new DataCallback<EventClassFamilyDto>(callback) {
                    @Override
                    protected void onResult(EventClassFamilyDto result) {
                        refreshEcfs();
                    }
                });
    }

    public void getEcf(String ecfId,
                       final AsyncCallback<EventClassFamilyDto> callback) {
        eventRpcService.getEventClassFamily(ecfId,
                new DataCallback<EventClassFamilyDto>(callback) {
                    @Override
                    protected void onResult(EventClassFamilyDto result) {
                    }
                });
    }

    public void addEcfSchema(String ecfId, RecordField schemaForm,
                             final AsyncCallback<Void> callback) {
        eventRpcService.addEventClassFamilySchemaForm(ecfId, schemaForm,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(EventSchemaVersionDto.class));
                    }
                });
    }

    public void getEventClassesByFamilyIdVersionAndType(String ecfId, int version, EventClassType type,
                                                        final AsyncCallback<List<EventClassDto>> callback) {
        eventRpcService.getEventClassesByFamilyIdVersionAndType(ecfId, version, type,
                new DataCallback<List<EventClassDto>>(callback) {
                    @Override
                    protected void onResult(List<EventClassDto> result) {
                    }
                });
    }

    public void loadProfileSchemas(String applicationId,
                                   final AsyncCallback<List<EndpointProfileSchemaDto>> callback) {
        profileRpcService.getProfileSchemasByApplicationId(applicationId,
                new DataCallback<List<EndpointProfileSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<EndpointProfileSchemaDto> result) {
                    }
                });

    }

    public void saveProfileSchemaView(ProfileSchemaViewDto profileSchemaView,
                                      final AsyncCallback<ProfileSchemaViewDto> callback) {
        profileRpcService.saveProfileSchemaView(profileSchemaView,
                new DataCallback<ProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(EndpointProfileSchemaDto.class));
                    }
                });
    }

    public void createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm,
                                                 final AsyncCallback<ProfileSchemaViewDto> callback) {
        profileRpcService.createProfileSchemaFormCtlSchema(ctlSchemaForm,
                new DataCallback<ProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(EndpointProfileSchemaDto.class));
                    }
                });
    }

    public void getProfileSchemaView(String profileSchemaId,
                                     final AsyncCallback<ProfileSchemaViewDto> callback) {
        profileRpcService.getProfileSchemaView(profileSchemaId,
                new DataCallback<ProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ProfileSchemaViewDto result) {
                    }
                });
    }

    public void loadServerProfileSchemas(String applicationId,
                                         final AsyncCallback<List<ServerProfileSchemaDto>> callback) {
        profileRpcService.getServerProfileSchemasByApplicationId(applicationId,
                new DataCallback<List<ServerProfileSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<ServerProfileSchemaDto> result) {
                    }
                });
    }

    public void getServerProfileSchemaInfosByApplicationId(String applicationId,
                                                           final AsyncCallback<List<SchemaInfoDto>> callback) {
        profileRpcService.getServerProfileSchemaInfosByApplicationId(applicationId,
                new DataCallback<List<SchemaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<SchemaInfoDto> result) {
                    }
                });
    }

    public void getServerProfileSchemaInfosByEndpointKey(
            String endpointKeyHash,
            final AsyncCallback<List<SchemaInfoDto>> callback) {
        profileRpcService.getServerProfileSchemaInfosByEndpointKey(endpointKeyHash,
                new DataCallback<List<SchemaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<SchemaInfoDto> result) {
                    }
                });
    }

    public void getServerProfileSchemaView(String serverProfileSchemaId,
                                           final AsyncCallback<ServerProfileSchemaViewDto> callback) {
        profileRpcService.getServerProfileSchemaView(serverProfileSchemaId,
                new DataCallback<ServerProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ServerProfileSchemaViewDto result) {
                    }
                });
    }

    public void saveServerProfileSchemaView(ServerProfileSchemaViewDto servderProfileSchema,
                                            final AsyncCallback<ServerProfileSchemaViewDto> callback) {
        profileRpcService.saveServerProfileSchemaView(servderProfileSchema,
                new DataCallback<ServerProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ServerProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(ServerProfileSchemaDto.class));
                    }
                });
    }

    public void createServerProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm,
                                                       final AsyncCallback<ServerProfileSchemaViewDto> callback) {
        profileRpcService.createServerProfileSchemaFormCtlSchema(ctlSchemaForm,
                new DataCallback<ServerProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ServerProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(ServerProfileSchemaViewDto.class));
                    }
                });
    }

    public void getEndpointProfileSchemaInfo(String endpointProfileSchemaId,
                                             final AsyncCallback<SchemaInfoDto> callback) {
        profileRpcService.getEndpointProfileSchemaInfo(endpointProfileSchemaId,
                new DataCallback<SchemaInfoDto>(callback) {
                    @Override
                    protected void onResult(SchemaInfoDto result) {
                    }
                });
    }

    public void getServerProfileSchemaInfo(String serverProfileSchemaId,
                                           final AsyncCallback<SchemaInfoDto> callback) {
        profileRpcService.getServerProfileSchemaInfo(serverProfileSchemaId,
                new DataCallback<SchemaInfoDto>(callback) {
                    @Override
                    protected void onResult(SchemaInfoDto result) {
                    }
                });
    }

    public void testProfileFilter(RecordField endpointProfile, RecordField serverProfile,
                                  String filterBody,
                                  final AsyncCallback<Boolean> callback) {
        profileRpcService.testProfileFilter(endpointProfile, serverProfile, filterBody,
                new DataCallback<Boolean>(callback) {
                    @Override
                    protected void onResult(Boolean result) {
                    }
                });
    }

    public void getAvailableApplicationCTLSchemaReferences(String applicationId,
                                                           final AsyncCallback<List<CtlSchemaReferenceDto>> callback) {
        ctlRpcService.getAvailableApplicationCTLSchemaReferences(applicationId,
                new DataCallback<List<CtlSchemaReferenceDto>>(callback) {
                    @Override
                    protected void onResult(List<CtlSchemaReferenceDto> result) {
                    }
                });
    }

    public void loadConfigurationSchemas(String applicationId,
                                         final AsyncCallback<List<ConfigurationSchemaDto>> callback) {
        configurationRpcService.getConfigurationSchemasByApplicationId(applicationId,
                new DataCallback<List<ConfigurationSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<ConfigurationSchemaDto> result) {
                    }
                });

    }

    public void saveConfigurationSchemaView(
            ConfigurationSchemaViewDto configurationSchema,
            final AsyncCallback<ConfigurationSchemaViewDto> callback) {
        configurationRpcService.saveConfigurationSchemaView(configurationSchema,
                new DataCallback<ConfigurationSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(
                                ConfigurationSchemaDto.class));
                    }
                });
    }

    public void getConfigurationSchemaView(String configurationSchemaId,
                                           final AsyncCallback<ConfigurationSchemaViewDto> callback) {
        configurationRpcService.getConfigurationSchemaView(configurationSchemaId,
                new DataCallback<ConfigurationSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationSchemaViewDto result) {
                    }
                });
    }

    public void loadNotificationSchemas(String applicationId,
                                        final AsyncCallback<List<NotificationSchemaDto>> callback) {
        notificationRpcService.getNotificationSchemasByApplicationId(applicationId,
                new DataCallback<List<NotificationSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<NotificationSchemaDto> result) {
                    }
                });

    }

    public void saveNotificationSchemaView(
            NotificationSchemaViewDto notificationSchema,
            final AsyncCallback<NotificationSchemaViewDto> callback) {
        notificationRpcService.saveNotificationSchemaView(notificationSchema,
                new DataCallback<NotificationSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(NotificationSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(
                                NotificationSchemaViewDto.class));
                    }
                });
    }

    public void createNotificationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm,
                                                      final AsyncCallback<NotificationSchemaViewDto> callback) {
        notificationRpcService.createNotificationSchemaFormCtlSchema(ctlSchemaForm,
                new DataCallback<NotificationSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(NotificationSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(NotificationSchemaViewDto.class));
                    }
                });
    }

    public void getNotificationSchemaView(String profileSchemaId,
                                          final AsyncCallback<NotificationSchemaViewDto> callback) {
        notificationRpcService.getNotificationSchemaView(profileSchemaId,
                new DataCallback<NotificationSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(NotificationSchemaViewDto result) {
                    }
                });
    }

    public void getNotificationSchema(String profileSchemaId,
                                      final AsyncCallback<NotificationSchemaDto> callback) {
        notificationRpcService.getNotificationSchema(profileSchemaId,
                new DataCallback<NotificationSchemaDto>(callback) {
                    @Override
                    protected void onResult(NotificationSchemaDto result) {
                    }
                });
    }

    public void loadLogSchemas(String applicationId,
                               final AsyncCallback<List<LogSchemaDto>> callback) {
        loggingRpcService.getLogSchemasByApplicationId(applicationId,
                new DataCallback<List<LogSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<LogSchemaDto> result) {
                    }
                });

    }

    public void loadLogSchemasVersion(String applicationId,
                                      final AsyncCallback<List<VersionDto>> callback) {
        loggingRpcService.getLogSchemasVersions(applicationId,
                new DataCallback<List<VersionDto>>(callback) {
                    @Override
                    protected void onResult(List<VersionDto> result) {
                    }
                });
    }

    public void saveLogSchemaView(LogSchemaViewDto logSchema,
            final AsyncCallback<LogSchemaViewDto> callback) {
        loggingRpcService.saveLogSchemaView(logSchema,
                new DataCallback<LogSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(LogSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(LogSchemaViewDto.class));
                    }
                });
    }

    public void getLogSchemaView(String logSchemaId,
            final AsyncCallback<LogSchemaViewDto> callback) {
        loggingRpcService.getLogSchemaView(logSchemaId,
                new DataCallback<LogSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(LogSchemaViewDto result) {
                    }
                });
    }

    public void createLogSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm,
                                 final AsyncCallback<LogSchemaViewDto> callback) {
        loggingRpcService.createLogSchemaFormCtlSchema(ctlSchemaForm,
                new DataCallback<LogSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(LogSchemaViewDto result) {
                    }
                });
    }
    
    public void getSystemLevelCTLSchemas(
            final AsyncCallback<List<CTLSchemaMetaInfoDto>> callback) {
        ctlRpcService.getSystemLevelCTLSchemas(
                new DataCallback<List<CTLSchemaMetaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<CTLSchemaMetaInfoDto> result) {
                    }
                });
    }

    public void getTenantLevelCTLSchemas(
            final AsyncCallback<List<CTLSchemaMetaInfoDto>> callback) {
        ctlRpcService.getTenantLevelCTLSchemas(
                new DataCallback<List<CTLSchemaMetaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<CTLSchemaMetaInfoDto> result) {
                    }
                });
    }

    public void getApplicationLevelCTLSchemas(String applicationId,
                                              final AsyncCallback<List<CTLSchemaMetaInfoDto>> callback) {
        ctlRpcService.getApplicationLevelCTLSchemas(applicationId,
                new DataCallback<List<CTLSchemaMetaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<CTLSchemaMetaInfoDto> result) {
                    }
                });
    }

    public void getLatestCTLSchemaForm(String metaInfoId,
                                       final AsyncCallback<CtlSchemaFormDto> callback) {
        ctlRpcService.getLatestCTLSchemaForm(metaInfoId,
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                    }
                });
    }

    public void getCTLSchemaFormByMetaInfoIdAndVer(String metaInfoId, Integer version,
                                                   final AsyncCallback<CtlSchemaFormDto> callback) {
        ctlRpcService.getCTLSchemaFormByMetaInfoIdAndVer(metaInfoId, version,
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                    }
                });
    }
    
    public void createNewCTLSchemaFormInstance(String metaInfoId, Integer sourceVersion, 
            String applicationId, ConverterType converterType,
            final AsyncCallback<CtlSchemaFormDto> callback) {
        ctlRpcService.createNewCTLSchemaFormInstance(metaInfoId, sourceVersion, applicationId, converterType,
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                    }
                });
    }

    public void generateCtlSchemaForm(String fileItemName, String applicationId,
                                      final AsyncCallback<RecordField> callback) {
        ctlRpcService.generateCtlSchemaForm(fileItemName, applicationId,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void editCTLSchemaForm(CtlSchemaFormDto ctlSchemaForm,
                                  ConverterType converterType, final AsyncCallback<CtlSchemaFormDto> callback) {
        ctlRpcService.saveCTLSchemaForm(ctlSchemaForm, converterType,
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                        eventBus.fireEvent(new DataEvent(CTLSchemaMetaInfoDto.class));
                    }
                });
    }

    public void checkFqnExists(CtlSchemaFormDto ctlSchemaForm,
                               final AsyncCallback<Boolean> callback) {
        ctlRpcService.checkFqnExists(ctlSchemaForm,
                new DataCallback<Boolean>(callback) {
                    @Override
                    protected void onResult(Boolean result) {
                    }
                });
    }

    public void promoteScopeToTenant(String applicationId, String fqn,
                                     final AsyncCallback<CTLSchemaMetaInfoDto> callback) {
        ctlRpcService.promoteScopeToTenant(applicationId, fqn,
                new DataCallback<CTLSchemaMetaInfoDto>(callback) {
                    @Override
                    protected void onResult(CTLSchemaMetaInfoDto result) {
                        eventBus.fireEvent(new DataEvent(CTLSchemaMetaInfoDto.class));
                    }
                });
    }

    public void deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version,
                                                                    String tenantId, String applicationId,
                                                                    final AsyncCallback<Void> callback) {
        ctlRpcService.deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(fqn, version,
                tenantId, applicationId,
                new DataCallback<Void>(callback) {
            @Override
            protected void onResult(Void result) {
                eventBus.fireEvent(new DataEvent(CTLSchemaMetaInfoDto.class));
            }
        });
    }
    
    public void prepareCTLSchemaExport(String ctlSchemaId, CTLSchemaExportMethod method, 
            final AsyncCallback<String> callback) {
        ctlRpcService.prepareCTLSchemaExport(ctlSchemaId, method, new DataCallback<String>(callback) {
                    @Override
                    protected void onResult(String result) {
                    }
                });
    }

    public void loadApplicationEventFamilyMaps(String applicationId,
                                               final AsyncCallback<List<ApplicationEventFamilyMapDto>> callback) {
        eventRpcService.getApplicationEventFamilyMapsByApplicationId(applicationId,
                new DataCallback<List<ApplicationEventFamilyMapDto>>(callback) {
                    @Override
                    protected void onResult(List<ApplicationEventFamilyMapDto> result) {
                    }
                });

    }

    public void editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap,
                                              final AsyncCallback<ApplicationEventFamilyMapDto> callback) {
        eventRpcService.editApplicationEventFamilyMap(applicationEventFamilyMap,
                new DataCallback<ApplicationEventFamilyMapDto>(callback) {
                    @Override
                    protected void onResult(ApplicationEventFamilyMapDto result) {
                        eventBus.fireEvent(new DataEvent(ApplicationEventFamilyMapDto.class));
                    }
                });
    }

    public void getApplicationEventFamilyMap(String applicationEventFamilyMapId,
                                             final AsyncCallback<ApplicationEventFamilyMapDto> callback) {
        eventRpcService.getApplicationEventFamilyMap(applicationEventFamilyMapId,
                new DataCallback<ApplicationEventFamilyMapDto>(callback) {
                    @Override
                    protected void onResult(ApplicationEventFamilyMapDto result) {
                    }
                });
    }

    public void getVacantEventClassFamilies(String applicationId,
                                            final AsyncCallback<List<EcfInfoDto>> callback) {
        eventRpcService.getVacantEventClassFamiliesByApplicationId(applicationId,
                new DataCallback<List<EcfInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<EcfInfoDto> result) {
                    }
                });
    }

    public void getAefMaps(String applicationId,
                           final AsyncCallback<List<AefMapInfoDto>> callback) {
        eventRpcService.getEventClassFamiliesByApplicationId(applicationId,
                new DataCallback<List<AefMapInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<AefMapInfoDto> result) {
                    }
                });
    }

    public void loadEndpointGroups(String applicationId,
                                   final AsyncCallback<List<EndpointGroupDto>> callback) {
        groupRpcService.getEndpointGroupsByApplicationId(applicationId,
                new DataCallback<List<EndpointGroupDto>>(callback) {
                    @Override
                    protected void onResult(List<EndpointGroupDto> result) {
                    }
                });

    }

    public void deleteEndpointGroup(String endpointGroupId,
                                    final AsyncCallback<Void> callback) {
        groupRpcService.deleteEndpointGroup(endpointGroupId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                EndpointGroupDto.class));
                    }
                });
    }

    public void editEndpointGroup(
            EndpointGroupDto endpointGroup,
            final AsyncCallback<EndpointGroupDto> callback) {
        groupRpcService.editEndpointGroup(endpointGroup,
                new DataCallback<EndpointGroupDto>(callback) {
                    @Override
                    protected void onResult(EndpointGroupDto result) {
                        eventBus.fireEvent(new DataEvent(
                                EndpointGroupDto.class));
                    }
                });
    }

    public void getEndpointGroup(String endpointGroupId,
                                 final AsyncCallback<EndpointGroupDto> callback) {
        groupRpcService.getEndpointGroup(endpointGroupId,
                new DataCallback<EndpointGroupDto>(callback) {
                    @Override
                    protected void onResult(EndpointGroupDto result) {
                    }
                });
    }

    public void getEndpointProfileByGroupID(String groupID, String limit, String offset,
                                            AsyncCallback<EndpointProfilesPageDto> callback) {
        groupRpcService.getEndpointProfileByEndpointGroupId(groupID, limit, offset, callback);
    }

    public void getEndpointProfileByKeyHash(String endpointKeyHash,
                                            AsyncCallback<EndpointProfileDto> callback) {
        profileRpcService.getEndpointProfileByKeyHash(endpointKeyHash, callback);
    }

    public void getEndpointProfileViewByKeyHash(String endpointKeyHash,
                                                AsyncCallback<EndpointProfileViewDto> callback) {
        profileRpcService.getEndpointProfileViewByKeyHash(endpointKeyHash, callback);
    }

    public void updateServerProfile(String endpointKeyHash, int serverProfileVersion, RecordField serverProfileRecord,
                                    AsyncCallback<EndpointProfileDto> callback) {
        profileRpcService.updateServerProfile(endpointKeyHash, serverProfileVersion, serverProfileRecord, callback);
    }

    public void loadProfileFilterRecords(String endpointGroupId, boolean includeDeprecated,
                                         final AsyncCallback<List<ProfileFilterRecordDto>> callback) {
        groupRpcService.getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated,
                new DataCallback<List<ProfileFilterRecordDto>>(callback) {
                    @Override
                    protected void onResult(List<ProfileFilterRecordDto> result) {
                    }
                });
    }

    public void getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId,
                                       final AsyncCallback<ProfileFilterRecordDto> callback) {
        groupRpcService.getProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId,
                new DataCallback<ProfileFilterRecordDto>(callback) {
                    @Override
                    protected void onResult(ProfileFilterRecordDto result) {
                    }
                });
    }

    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId,
                                          String endpointGroupId,
                                          final AsyncCallback<Void> callback) {
        groupRpcService.deleteProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                ProfileFilterDto.class));
                    }
                });
    }

    public void editProfileFilter(ProfileFilterDto profileFilter,
                                  final AsyncCallback<ProfileFilterDto> callback) {
        groupRpcService.editProfileFilter(profileFilter,
                new DataCallback<ProfileFilterDto>(callback) {
                    @Override
                    protected void onResult(ProfileFilterDto result) {
                        eventBus.fireEvent(new DataEvent(ProfileFilterDto.class));
                    }
                });
    }

    public void activateProfileFilter(String profileFilterId,
                                      final AsyncCallback<ProfileFilterDto> callback) {
        groupRpcService.activateProfileFilter(profileFilterId,
                new DataCallback<ProfileFilterDto>(callback) {
                    @Override
                    protected void onResult(ProfileFilterDto result) {
                        eventBus.fireEvent(new DataEvent(ProfileFilterDto.class));
                    }
                });
    }

    public void deactivateProfileFilter(String profileFilterId,
                                        final AsyncCallback<ProfileFilterDto> callback) {
        groupRpcService.deactivateProfileFilter(profileFilterId,
                new DataCallback<ProfileFilterDto>(callback) {
                    @Override
                    protected void onResult(ProfileFilterDto result) {
                        eventBus.fireEvent(new DataEvent(ProfileFilterDto.class));
                    }
                });
    }

    public void loadConfigurationRecords(String endpointGroupId, boolean includeDeprecated,
                                         final AsyncCallback<List<ConfigurationRecordDto>> callback) {
        configurationRpcService.getConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated,
                new DataCallback<List<ConfigurationRecordDto>>(callback) {
                    @Override
                    protected void onResult(List<ConfigurationRecordDto> result) {
                    }
                });
    }

    public void getConfigurationRecordView(String schemaId, String endpointGroupId,
                                           final AsyncCallback<ConfigurationRecordViewDto> callback) {
        configurationRpcService.getConfigurationRecordView(schemaId, endpointGroupId,
                new DataCallback<ConfigurationRecordViewDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationRecordViewDto result) {
                    }
                });
    }

    public void deleteConfigurationRecord(String schemaId, String endpointGroupId,
                                          final AsyncCallback<Void> callback) {
        configurationRpcService.deleteConfigurationRecord(schemaId, endpointGroupId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                ConfigurationDto.class));
                    }
                });
    }

    public void editConfigurationRecordForm(ConfigurationRecordFormDto configuration,
                                            final AsyncCallback<ConfigurationRecordFormDto> callback) {
        configurationRpcService.editConfigurationRecordForm(configuration,
                new DataCallback<ConfigurationRecordFormDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationRecordFormDto result) {
                        eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
                    }
                });
    }

    public void activateConfigurationRecordForm(String configurationId,
                                                final AsyncCallback<ConfigurationRecordFormDto> callback) {
        configurationRpcService.activateConfigurationRecordForm(configurationId,
                new DataCallback<ConfigurationRecordFormDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationRecordFormDto result) {
                        eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
                    }
                });
    }

    public void deactivateConfigurationRecordForm(String configurationId,
                                                  final AsyncCallback<ConfigurationRecordFormDto> callback) {
        configurationRpcService.deactivateConfigurationRecordForm(configurationId,
                new DataCallback<ConfigurationRecordFormDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationRecordFormDto result) {
                        eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
                    }
                });
    }

    public void getVacantProfileSchemas(String endpointGroupId,
                                        final AsyncCallback<List<ProfileVersionPairDto>> callback) {
        groupRpcService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId,
                new DataCallback<List<ProfileVersionPairDto>>(callback) {
                    @Override
                    protected void onResult(List<ProfileVersionPairDto> result) {
                    }
                });
    }

    public void getVacantConfigurationSchemaInfos(String endpointGroupId,
                                                  final AsyncCallback<List<SchemaInfoDto>> callback) {
        configurationRpcService.getVacantConfigurationSchemaInfosByEndpointGroupId(endpointGroupId,
                new DataCallback<List<SchemaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<SchemaInfoDto> result) {
                    }
                });
    }

    public void getUserNotificationSchemas(String applicationId,
                                           final AsyncCallback<List<VersionDto>> callback) {
        notificationRpcService.getUserNotificationSchemasByApplicationId(applicationId,
                new DataCallback<List<VersionDto>>(callback) {
                    @Override
                    protected void onResult(List<VersionDto> result) {
                    }
                });
    }

    public void getUserNotificationSchemaInfosByApplicationId(String applicationId,
                                                              final AsyncCallback<List<SchemaInfoDto>> callback) {
        notificationRpcService.getUserNotificationSchemaInfosByApplicationId(applicationId,
                new DataCallback<List<SchemaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<SchemaInfoDto> result) {
                    }
                });
    }

    public void loadTopics(String applicationId,
                           final AsyncCallback<List<TopicDto>> callback) {
        notificationRpcService.getTopicsByApplicationId(applicationId,
                new DataCallback<List<TopicDto>>(callback) {
                    @Override
                    protected void onResult(List<TopicDto> result) {
                    }
                });
    }

    public void loadTopicsByEndpointGroupId(String endpointGroupId,
                                            final AsyncCallback<List<TopicDto>> callback) {
        notificationRpcService.getTopicsByEndpointGroupId(endpointGroupId,
                new DataCallback<List<TopicDto>>(callback) {
                    @Override
                    protected void onResult(List<TopicDto> result) {
                    }
                });
    }

    public void loadVacantTopicsByEndpointGroupId(String endpointGroupId,
                                                  final AsyncCallback<List<TopicDto>> callback) {
        notificationRpcService.getVacantTopicsByEndpointGroupId(endpointGroupId,
                new DataCallback<List<TopicDto>>(callback) {
                    @Override
                    protected void onResult(List<TopicDto> result) {
                    }
                });
    }

    public void deleteTopic(String topicId,
                            final AsyncCallback<Void> callback) {
        notificationRpcService.deleteTopic(topicId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                TopicDto.class));
                    }
                });
    }

    public void removeEndpointProfileByKeyHash(String endpointKeyHash, final AsyncCallback<Void> callback) {
        profileRpcService.removeEndpointProfileByKeyHash(endpointKeyHash,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                EndpointProfileDto.class));
                    }
                });
    }

    public void editTopic(
            TopicDto topic,
            final AsyncCallback<TopicDto> callback) {
        notificationRpcService.editTopic(topic,
                new DataCallback<TopicDto>(callback) {
                    @Override
                    protected void onResult(TopicDto result) {
                        eventBus.fireEvent(new DataEvent(
                                TopicDto.class));
                    }
                });
    }

    public void getTopic(String topicId,
                         final AsyncCallback<TopicDto> callback) {
        notificationRpcService.getTopic(topicId,
                new DataCallback<TopicDto>(callback) {
                    @Override
                    protected void onResult(TopicDto result) {
                    }
                });
    }

    public void addTopicToEndpointGroup(String endpointGroupId, String topicId,
                                        final AsyncCallback<Void> callback) {
        notificationRpcService.addTopicToEndpointGroup(endpointGroupId, topicId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                TopicDto.class));
                    }
                });
    }

    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId,
                                             final AsyncCallback<Void> callback) {
        notificationRpcService.removeTopicFromEndpointGroup(endpointGroupId, topicId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                TopicDto.class));
                    }
                });
    }

    public void getRecordDataFromFile(String schema, String fileItemName,
                                      final AsyncCallback<RecordField> callback) {
        adminUIRpcService.getRecordDataFromFile(schema, fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void getConfigurationRecordDataFromFile(String schema, String fileItemName,
                                                   final AsyncCallback<RecordField> callback) {
        configurationRpcService.getConfigurationRecordDataFromFile(schema, fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void sendNotification(
            NotificationDto notification, RecordField notificationData,
            final AsyncCallback<Void> callback) {
        notificationRpcService.sendNotification(notification, notificationData,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                    }
                });
    }

    public void sendUnicastNotification(
            NotificationDto notification, String endpointKeyHash, RecordField notificationData,
            final AsyncCallback<EndpointNotificationDto> callback) {
        notificationRpcService.sendUnicastNotification(notification, endpointKeyHash, notificationData,
                new DataCallback<EndpointNotificationDto>(callback) {
                    @Override
                    protected void onResult(EndpointNotificationDto result) {
                    }
                });
    }

    public void loadLogAppenders(String applicationId,
                                 final AsyncCallback<List<LogAppenderDto>> callback) {
        loggingRpcService.getLogAppendersByApplicationId(applicationId,
                new DataCallback<List<LogAppenderDto>>(callback) {
                    @Override
                    protected void onResult(List<LogAppenderDto> result) {
                    }
                });
    }

    public void getLogAppender(String appenderId, final AsyncCallback<LogAppenderDto> callback) {
        loggingRpcService.getRestLogAppender(appenderId,
                new DataCallback<LogAppenderDto>(callback) {
                    @Override
                    protected void onResult(LogAppenderDto result) {
                    }
                });
    }

    public void getLogAppenderForm(String appenderId,
                                   final AsyncCallback<LogAppenderDto> callback) {
        loggingRpcService.getLogAppenderForm(appenderId,
                new DataCallback<LogAppenderDto>(callback) {
                    @Override
                    protected void onResult(LogAppenderDto result) {
                    }
                });
    }

    public void editLogAppenderForm(LogAppenderDto dto,
                                    final AsyncCallback<LogAppenderDto> callback) {
        loggingRpcService.editLogAppenderForm(dto,
                new DataCallback<LogAppenderDto>(callback) {
                    @Override
                    protected void onResult(LogAppenderDto result) {
                    }
                });
    }

    public void removeLogAppender(String appenderId,
                                  final AsyncCallback<Void> callback) {
        loggingRpcService.deleteLogAppender(appenderId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                LogAppenderDto.class));
                    }
                });
    }

    public void loadLogAppenderPluginInfos(
            final AsyncCallback<List<PluginInfoDto>> callback) {
        if (logAppenderPluginInfos == null) {
            logAppenderPluginInfos = new ArrayList<PluginInfoDto>();
            loggingRpcService.getLogAppenderPluginInfos(new DataCallback<List<PluginInfoDto>>(callback) {
                @Override
                protected void onResult(List<PluginInfoDto> result) {
                    logAppenderPluginInfos.addAll(result);
                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess(logAppenderPluginInfos);
            }
        }
    }

    public void loadUserVerifiers(String applicationId,
                                  final AsyncCallback<List<UserVerifierDto>> callback) {
        verifierRpcService.getUserVerifiersByApplicationId(applicationId,
                new DataCallback<List<UserVerifierDto>>(callback) {
                    @Override
                    protected void onResult(List<UserVerifierDto> result) {
                    }
                });
    }

    public void getUserVerifierForm(String userVerifierId,
                                    final AsyncCallback<UserVerifierDto> callback) {
        verifierRpcService.getUserVerifierForm(userVerifierId,
                new DataCallback<UserVerifierDto>(callback) {
                    @Override
                    protected void onResult(UserVerifierDto result) {
                    }
                });
    }

    public void getUserVerifier(String userVerifierId, final AsyncCallback<UserVerifierDto> callback) {
        verifierRpcService.getRestUserVerifier(userVerifierId,
                new DataCallback<UserVerifierDto>(callback) {
                    @Override
                    protected void onResult(UserVerifierDto result) {
                    }
                });
    }

    public void editUserVerifierForm(UserVerifierDto dto,
                                     final AsyncCallback<UserVerifierDto> callback) {
        verifierRpcService.editUserVerifierForm(dto,
                new DataCallback<UserVerifierDto>(callback) {
                    @Override
                    protected void onResult(UserVerifierDto result) {
                    }
                });
    }

    public void removeUserVerifier(String userVerifierId,
                                   final AsyncCallback<Void> callback) {
        verifierRpcService.deleteUserVerifier(userVerifierId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                UserVerifierDto.class));
                    }
                });
    }

    public void loadUserVerifierPluginInfos(
            final AsyncCallback<List<PluginInfoDto>> callback) {
        if (userVerifierPluginInfos == null) {
            userVerifierPluginInfos = new ArrayList<PluginInfoDto>();
            verifierRpcService.getUserVerifierPluginInfos(new DataCallback<List<PluginInfoDto>>(callback) {
                @Override
                protected void onResult(List<PluginInfoDto> result) {
                    userVerifierPluginInfos.addAll(result);
                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess(userVerifierPluginInfos);
            }
        }
    }

    public void getUserConfigurationSchemaInfosByApplicationId(String applicationId,
                                                               final AsyncCallback<List<SchemaInfoDto>> callback) {
        configurationRpcService.getUserConfigurationSchemaInfosByApplicationId(applicationId,
                new DataCallback<List<SchemaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<SchemaInfoDto> result) {
                    }
                });
    }

    public void editUserConfiguration(
            EndpointUserConfigurationDto endpointUserConfiguration,
            String applicationId, RecordField configurationData,
            final AsyncCallback<Void> callback) {
        configurationRpcService.editUserConfiguration(endpointUserConfiguration, applicationId, configurationData,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                    }
                });
    }

    abstract class DataCallback<T> implements AsyncCallback<T> {

        AsyncCallback<T> callback;

        DataCallback(AsyncCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void onFailure(Throwable caught) {
            if (callback != null) {
                callback.onFailure(caught);
            }
        }

        @Override
        public void onSuccess(T result) {
            onResult(result);
            if (callback != null) {
                callback.onSuccess(result);
            }
        }

        protected abstract void onResult(T result);

    }

    public void addSdkProfile(SdkProfileDto sdkProfile, final AsyncCallback<SdkProfileDto> callback) {
        sdkRpcService.createSdkProfile(sdkProfile, new DataCallback<SdkProfileDto>(callback) {
            @Override
            protected void onResult(SdkProfileDto callback) {
            }
        });
    }

    public void deleteSdkProfile(String sdkProfileId, final AsyncCallback<Void> callback) {
        sdkRpcService.deleteSdkProfile(sdkProfileId, new DataCallback<Void>(callback) {
            @Override
            protected void onResult(Void result) {
                eventBus.fireEvent(new DataEvent(SdkProfileDto.class));
            }
        });
    }

    public void getSdkProfile(String sdkProfileId, final AsyncCallback<SdkProfileDto> callback) {
        sdkRpcService.getSdkProfile(sdkProfileId, new DataCallback<SdkProfileDto>(callback) {
            @Override
            protected void onResult(SdkProfileDto result) {
            }
        });
    }

    public void getSdkProfileView(String sdkProfileId, final AsyncCallback<SdkProfileViewDto> callback) {
        sdkRpcService.getSdkProfileView(sdkProfileId, new DataCallback<SdkProfileViewDto>(callback) {
            @Override
            protected void onResult(SdkProfileViewDto result) {
            }
        });
    }

    public void loadSdkProfiles(String applicationId, final AsyncCallback<List<SdkProfileDto>> callback) {
        sdkRpcService.getSdkProfilesByApplicationId(applicationId, new DataCallback<List<SdkProfileDto>>(callback) {
            @Override
            protected void onResult(List<SdkProfileDto> result) {
            }
        });
    }

    public void getCredentialsServiceNames(final AsyncCallback<List<String>> callback) {
        deviceManagementRpcService.getCredentialsServiceNames(new DataCallback<List<String>>(callback) {
            @Override
            protected void onResult(List<String> result) {
            }
        });
    }
}
