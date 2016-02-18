/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.admin.client.mvp.data;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey.RecordFiles;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
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
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

public class DataSource {

    private final KaaAdminServiceAsync rpcService;
    private final EventBus eventBus;

    private List<TenantUserDto> tenants;

    private List<ApplicationDto> applications;

    private List<UserDto> users;

    private List<EventClassFamilyDto> ecfs;

    private List<PluginInfoDto> logAppenderPluginInfos;

    private List<PluginInfoDto> userVerifierPluginInfos;

    public DataSource(KaaAdminServiceAsync rpcService, EventBus eventBus) {
        this.rpcService = rpcService;
        this.eventBus = eventBus;
    }

    public void getUserProfile(
            final AsyncCallback<UserDto> callback) {
        rpcService.getUserProfile(
                new DataCallback<UserDto>(callback) {
                    @Override
                    protected void onResult(UserDto result) {
                    }
                });
    }

    public void editUserProfile(UserDto user,
            final AsyncCallback<UserDto> callback) {
        rpcService.editUserProfile(user,
                new DataCallback<UserDto>(callback) {
                    @Override
                    protected void onResult(UserDto result) {
                    }
                });
    }

    public void getMailProperties(
            final AsyncCallback<PropertiesDto> callback) {
        rpcService.getMailProperties(
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void editMailProperties(PropertiesDto mailProperties,
            final AsyncCallback<PropertiesDto> callback) {
        rpcService.editMailProperties(mailProperties,
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void getGeneralProperties(
            final AsyncCallback<PropertiesDto> callback) {
        rpcService.getGeneralProperties(
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void editGeneralProperties(PropertiesDto mailProperties,
            final AsyncCallback<PropertiesDto> callback) {
        rpcService.editGeneralProperties(mailProperties,
                new DataCallback<PropertiesDto>(callback) {
                    @Override
                    protected void onResult(PropertiesDto result) {
                    }
                });
    }

    public void loadTenants(final AsyncCallback<List<TenantUserDto>> callback) {
        loadTenants(callback, false);
    }

    public void loadTenants(final AsyncCallback<List<TenantUserDto>> callback,
            boolean refresh) {
        if (tenants == null || refresh) {
            tenants = new ArrayList<TenantUserDto>();
            rpcService.getTenants(new DataCallback<List<TenantUserDto>>(
                    callback) {
                @Override
                protected void onResult(List<TenantUserDto> result) {
                    tenants.addAll(result);
                    eventBus.fireEvent(new DataEvent(TenantUserDto.class, true));
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
        rpcService.deleteTenant(tenantId, new DataCallback<Void>(callback) {
            @Override
            protected void onResult(Void result) {
                refreshTenants();
            }
        });
    }

    public void editTenant(TenantUserDto tenant,
            final AsyncCallback<TenantUserDto> callback) {
        rpcService.editTenant(tenant,
                new DataCallback<TenantUserDto>(callback) {
                    @Override
                    protected void onResult(TenantUserDto result) {
                        refreshTenants();
                    }
                });
    }

    public void getTenant(String tenantId,
            final AsyncCallback<TenantUserDto> callback) {
        rpcService.getTenant(tenantId,
                new DataCallback<TenantUserDto>(callback) {
                    @Override
                    protected void onResult(TenantUserDto result) {
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
            rpcService.getApplications(new DataCallback<List<ApplicationDto>>(
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

    public void deleteApplication(String applicationId,
            final AsyncCallback<Void> callback) {
        rpcService.deleteApplication(applicationId, new DataCallback<Void>(
                callback) {
            @Override
            protected void onResult(Void result) {
                refreshApplications();
            }
        });
    }

    public void editApplication(ApplicationDto application,
            final AsyncCallback<ApplicationDto> callback) {
        rpcService.editApplication(application,
                new DataCallback<ApplicationDto>(callback) {
                    @Override
                    protected void onResult(ApplicationDto result) {
                        refreshApplications();
                    }
                });
    }

    public void getApplication(String applicationId,
            final AsyncCallback<ApplicationDto> callback) {
        rpcService.getApplication(applicationId,
                new DataCallback<ApplicationDto>(callback) {
                    @Override
                    protected void onResult(ApplicationDto result) {
                    }
                });
    }

    public void getSchemaVersionsByApplicationId(String applicationId,
            final AsyncCallback<SchemaVersions> callback) {
        rpcService.getSchemaVersionsByApplicationId(applicationId,
                new DataCallback<SchemaVersions>(callback) {
            @Override
            protected void onResult(SchemaVersions result) {
            }
        });
    }

    public void generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform,
            final AsyncCallback<String> callback) {
        rpcService.generateSdk(sdkProfile, targetPlatform,
                new DataCallback<String>(callback) {
                    @Override
                    protected void onResult(String result) {
                    }
        });
    }

    public void getRecordData(String applicationId,
                                 Integer logSchemaVersion, RecordFiles fileType,
                                 final AsyncCallback<String> callback) {
        rpcService.getRecordDataByApplicationIdAndSchemaVersion(applicationId, logSchemaVersion, fileType,
                new DataCallback<String>(callback) {
                    @Override
                    protected void onResult(String result) {
                    }
                });
    }

    public void getRecordLibrary(String applicationId,
            Integer logSchemaVersion, RecordFiles fileType,
            final AsyncCallback<String> callback) {
        rpcService.getRecordLibraryByApplicationIdAndSchemaVersion(applicationId, logSchemaVersion, fileType,
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
            rpcService.getUsers(new DataCallback<List<UserDto>>(callback) {
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
        rpcService.deleteUser(userId, new DataCallback<Void>(callback) {
            @Override
            protected void onResult(Void result) {
                refreshUsers();
            }
        });
    }

    public void editUser(UserDto user, final AsyncCallback<UserDto> callback) {
        rpcService.editUser(user, new DataCallback<UserDto>(callback) {
            @Override
            protected void onResult(UserDto result) {
                refreshUsers();
            }
        });
    }

    public void getUser(String userId, final AsyncCallback<UserDto> callback) {
        rpcService.getUser(userId, new DataCallback<UserDto>(callback) {
            @Override
            protected void onResult(UserDto result) {
            }
        });
    }

    public void createSimpleEmptySchemaForm(final AsyncCallback<RecordField> callback) {
        rpcService.createSimpleEmptySchemaForm(new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void createCommonEmptySchemaForm(final AsyncCallback<RecordField> callback) {
        rpcService.createCommonEmptySchemaForm(new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void createConfigurationEmptySchemaForm(final AsyncCallback<RecordField> callback) {
        rpcService.createConfigurationEmptySchemaForm(new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void createEcfEmptySchemaForm(final AsyncCallback<RecordField> callback) {
        rpcService.createEcfEmptySchemaForm(new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void generateSimpleSchemaForm(String fileItemName,
            final AsyncCallback<RecordField> callback) {
        rpcService.generateSimpleSchemaForm(fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void generateCommonSchemaForm(String fileItemName,
            final AsyncCallback<RecordField> callback) {
        rpcService.generateCommonSchemaForm(fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void generateConfigurationSchemaForm(String fileItemName,
            final AsyncCallback<RecordField> callback) {
        rpcService.generateConfigurationSchemaForm(fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void generateEcfSchemaForm(String fileItemName,
            final AsyncCallback<RecordField> callback) {
        rpcService.generateEcfSchemaForm(fileItemName,
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
            rpcService.getEventClassFamilies(new DataCallback<List<EventClassFamilyDto>>(
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
        rpcService.editEventClassFamily(ecf,
                new DataCallback<EventClassFamilyDto>(callback) {
                    @Override
                    protected void onResult(EventClassFamilyDto result) {
                        refreshEcfs();
                    }
                });
    }

    public void getEcf(String ecfId,
            final AsyncCallback<EventClassFamilyDto> callback) {
        rpcService.getEventClassFamily(ecfId,
                new DataCallback<EventClassFamilyDto>(callback) {
                    @Override
                    protected void onResult(EventClassFamilyDto result) {
                    }
                });
    }

    public void addEcfSchema(String ecfId, RecordField schemaForm,
            final AsyncCallback<Void> callback) {
        rpcService.addEventClassFamilySchemaForm(ecfId, schemaForm,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(EventSchemaVersionDto.class));
                    }
                });
    }

    public void getEventClassesByFamilyIdVersionAndType(String ecfId, int version, EventClassType type,
            final AsyncCallback<List<EventClassDto>> callback) {
        rpcService.getEventClassesByFamilyIdVersionAndType(ecfId, version, type,
                new DataCallback<List<EventClassDto>>(callback) {
            @Override
            protected void onResult(List<EventClassDto> result) {
            }
        });
    }

    public void loadProfileSchemas(String applicationId,
            final AsyncCallback<List<EndpointProfileSchemaDto>> callback) {
        rpcService.getProfileSchemasByApplicationId(applicationId,
                new DataCallback<List<EndpointProfileSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<EndpointProfileSchemaDto> result) {
                    }
                });

    }

    public void saveProfileSchemaView(ProfileSchemaViewDto profileSchemaView,
            final AsyncCallback<ProfileSchemaViewDto> callback) {
        rpcService.saveProfileSchemaView(profileSchemaView,
                new DataCallback<ProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(EndpointProfileSchemaDto.class));
                    }
                });
    }
    
    public void createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm,
            final AsyncCallback<ProfileSchemaViewDto> callback) {
        rpcService.createProfileSchemaFormCtlSchema(ctlSchemaForm,
                new DataCallback<ProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(EndpointProfileSchemaDto.class));
                    }
                });
    }

    public void getProfileSchemaView(String profileSchemaId,
            final AsyncCallback<ProfileSchemaViewDto> callback) {
        rpcService.getProfileSchemaView(profileSchemaId,
                new DataCallback<ProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ProfileSchemaViewDto result) {
                    }
                });
    }
    public void loadServerProfileSchemas(String applicationId,
                                   final AsyncCallback<List<ServerProfileSchemaDto>> callback) {
        rpcService.getServerProfileSchemasByApplicationId(applicationId,
                new DataCallback<List<ServerProfileSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<ServerProfileSchemaDto> result) {
                    }
                });
    }
    
    public void  getServerProfileSchemaInfosByApplicationId(String applicationId,
                                            final AsyncCallback<List<SchemaInfoDto>> callback) {
        rpcService.getServerProfileSchemaInfosByApplicationId(applicationId,  
                new DataCallback<List<SchemaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<SchemaInfoDto> result) {
                    }
                });
    }
    
    public void getServerProfileSchemaInfosByEndpointKey(
            String endpointKeyHash,
            final AsyncCallback<List<SchemaInfoDto>> callback) {
        rpcService.getServerProfileSchemaInfosByEndpointKey(endpointKeyHash,
                new DataCallback<List<SchemaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<SchemaInfoDto> result) {
                    }
                });
    }

    public void getServerProfileSchemaView(String serverProfileSchemaId,
            final AsyncCallback<ServerProfileSchemaViewDto> callback) {
        rpcService.getServerProfileSchemaView(serverProfileSchemaId,
                new DataCallback<ServerProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ServerProfileSchemaViewDto result) {
                    }
                });
    }
    
    public void saveServerProfileSchemaView(ServerProfileSchemaViewDto servderProfileSchema,
            final AsyncCallback<ServerProfileSchemaViewDto> callback) {
        rpcService.saveServerProfileSchemaView(servderProfileSchema,
                new DataCallback<ServerProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ServerProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(ServerProfileSchemaDto.class));
                    }
                });
    }
    
    public void createServerProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm,
            final AsyncCallback<ServerProfileSchemaViewDto> callback) {
        rpcService.createServerProfileSchemaFormCtlSchema(ctlSchemaForm,
                new DataCallback<ServerProfileSchemaViewDto>(callback) {
                    @Override
                    protected void onResult(ServerProfileSchemaViewDto result) {
                        eventBus.fireEvent(new DataEvent(ServerProfileSchemaViewDto.class));
                    }
                });
    }
    
    public void getEndpointProfileSchemaInfo(String endpointProfileSchemaId,
            final AsyncCallback<SchemaInfoDto> callback) {
        rpcService.getEndpointProfileSchemaInfo(endpointProfileSchemaId,
                new DataCallback<SchemaInfoDto>(callback) {
                    @Override
                    protected void onResult(SchemaInfoDto result) {
                    }
                });
    }
    
    public void getServerProfileSchemaInfo(String serverProfileSchemaId,
            final AsyncCallback<SchemaInfoDto> callback) {
        rpcService.getServerProfileSchemaInfo(serverProfileSchemaId,
                new DataCallback<SchemaInfoDto>(callback) {
                    @Override
                    protected void onResult(SchemaInfoDto result) {
                    }
                });
    }
    
    public void testProfileFilter(RecordField endpointProfile, RecordField serverProfile, 
            String filterBody,
            final AsyncCallback<Boolean> callback) {
        rpcService.testProfileFilter(endpointProfile, serverProfile, filterBody, 
                new DataCallback<Boolean>(callback) {
                    @Override
                    protected void onResult(Boolean result) {
                    }
                });
    }
    
    public void getAvailableApplicationCTLSchemaReferences(String applicationId, 
            final AsyncCallback<List<CtlSchemaReferenceDto>> callback) {
        rpcService.getAvailableApplicationCTLSchemaReferences(applicationId, 
                new DataCallback<List<CtlSchemaReferenceDto>>(callback) {
                    @Override
                    protected void onResult(List<CtlSchemaReferenceDto> result) {
                    }
                });
    }

    public void loadConfigurationSchemas(String applicationId,
            final AsyncCallback<List<ConfigurationSchemaDto>> callback) {
        rpcService.getConfigurationSchemasByApplicationId(applicationId,
                new DataCallback<List<ConfigurationSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<ConfigurationSchemaDto> result) {
                    }
                });

    }

    public void editConfigurationSchemaForm(
            ConfigurationSchemaDto configurationSchema,
            final AsyncCallback<ConfigurationSchemaDto> callback) {
        rpcService.editConfigurationSchemaForm(configurationSchema,
                new DataCallback<ConfigurationSchemaDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationSchemaDto result) {
                        eventBus.fireEvent(new DataEvent(
                                ConfigurationSchemaDto.class));
                    }
                });
    }

    public void getConfigurationSchemaForm(String configurationSchemaId,
            final AsyncCallback<ConfigurationSchemaDto> callback) {
        rpcService.getConfigurationSchemaForm(configurationSchemaId,
                new DataCallback<ConfigurationSchemaDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationSchemaDto result) {
                    }
                });
    }

    public void loadNotificationSchemas(String applicationId,
            final AsyncCallback<List<NotificationSchemaDto>> callback) {
        rpcService.getNotificationSchemasByApplicationId(applicationId,
                new DataCallback<List<NotificationSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<NotificationSchemaDto> result) {
                    }
                });

    }

    public void editNotificationSchemaForm(
            NotificationSchemaDto notificationSchema,
            final AsyncCallback<NotificationSchemaDto> callback) {
        rpcService.editNotificationSchemaForm(notificationSchema,
                new DataCallback<NotificationSchemaDto>(callback) {
                    @Override
                    protected void onResult(NotificationSchemaDto result) {
                        eventBus.fireEvent(new DataEvent(
                                NotificationSchemaDto.class));
                    }
                });
    }

    public void getNotificationSchemaForm(String notificationSchemaId,
            final AsyncCallback<NotificationSchemaDto> callback) {
        rpcService.getNotificationSchemaForm(notificationSchemaId,
                new DataCallback<NotificationSchemaDto>(callback) {
                    @Override
                    protected void onResult(NotificationSchemaDto result) {
                    }
                });
    }

    public void loadLogSchemas(String applicationId,
            final AsyncCallback<List<LogSchemaDto>> callback) {
        rpcService.getLogSchemasByApplicationId(applicationId,
                new DataCallback<List<LogSchemaDto>>(callback) {
                    @Override
                    protected void onResult(List<LogSchemaDto> result) {
                    }
                });

    }

    public void loadLogSchemasVersion(String applicationId,
            final AsyncCallback<List<VersionDto>> callback) {
        rpcService.getLogSchemasVersions(applicationId,
                new DataCallback<List<VersionDto>>(callback) {
                    @Override
                    protected void onResult(List<VersionDto> result) {
                    }
                });
    }

    public void editLogSchemaForm(LogSchemaDto logSchema,
            final AsyncCallback<LogSchemaDto> callback) {
        rpcService.editLogSchemaForm(logSchema,
                new DataCallback<LogSchemaDto>(callback) {
                    @Override
                    protected void onResult(LogSchemaDto result) {
                        eventBus.fireEvent(new DataEvent(LogSchemaDto.class));
                    }
                });
    }

    public void getLogSchemaForm(String logSchemaId,
            final AsyncCallback<LogSchemaDto> callback) {
        rpcService.getLogSchemaForm(logSchemaId,
                new DataCallback<LogSchemaDto>(callback) {
                    @Override
                    protected void onResult(LogSchemaDto result) {
                    }
                });
    }
    
    public void getSystemLevelCTLSchemas(
            final AsyncCallback<List<CTLSchemaMetaInfoDto>> callback) {
        rpcService.getSystemLevelCTLSchemas(
                new DataCallback<List<CTLSchemaMetaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<CTLSchemaMetaInfoDto> result) {
                    }
                });
    }
    
    public void getTenantLevelCTLSchemas(
            final AsyncCallback<List<CTLSchemaMetaInfoDto>> callback) {
        rpcService.getTenantLevelCTLSchemas(
                new DataCallback<List<CTLSchemaMetaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<CTLSchemaMetaInfoDto> result) {
                    }
                });
    }
    
    public void getApplicationLevelCTLSchemas(String applicationId,
            final AsyncCallback<List<CTLSchemaMetaInfoDto>> callback) {
        rpcService.getApplicationLevelCTLSchemas(applicationId, 
                new DataCallback<List<CTLSchemaMetaInfoDto>>(callback) {
                    @Override
                    protected void onResult(List<CTLSchemaMetaInfoDto> result) {
                    }
                });
    }
    
    public void getLatestCTLSchemaForm(String metaInfoId,
            final AsyncCallback<CtlSchemaFormDto> callback) {
        rpcService.getLatestCTLSchemaForm(metaInfoId,
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                    }
                });
    }
    
    public void getCTLSchemaFormByMetaInfoIdAndVer(String metaInfoId, Integer version,
            final AsyncCallback<CtlSchemaFormDto> callback) {
        rpcService.getCTLSchemaFormByMetaInfoIdAndVer(metaInfoId, version, 
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                    }
                });
    }
    
    public void createNewCTLSchemaFormInstance(String metaInfoId, Integer sourceVersion, 
            String applicationId,
            final AsyncCallback<CtlSchemaFormDto> callback) {
        rpcService.createNewCTLSchemaFormInstance(metaInfoId, sourceVersion, applicationId,
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                    }
        });
    }
    
    public void generateCtlSchemaForm(String fileItemName, String applicationId,
            final AsyncCallback<RecordField> callback) {
        rpcService.generateCtlSchemaForm(fileItemName, applicationId,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }
    
    public void editCTLSchemaForm(CtlSchemaFormDto ctlSchemaForm,
            final AsyncCallback<CtlSchemaFormDto> callback) {
        rpcService.saveCTLSchemaForm(ctlSchemaForm, 
                new DataCallback<CtlSchemaFormDto>(callback) {
                    @Override
                    protected void onResult(CtlSchemaFormDto result) {
                        eventBus.fireEvent(new DataEvent(CTLSchemaMetaInfoDto.class));
                    }
        });
    }
    
    public void checkFqnExists(CtlSchemaFormDto ctlSchemaForm,
            final AsyncCallback<Boolean> callback) {
        rpcService.checkFqnExists(ctlSchemaForm, 
                new DataCallback<Boolean>(callback) {
                    @Override
                    protected void onResult(Boolean result) {
                    }
        });
    }
    
    public void updateCtlSchemaScope(CTLSchemaMetaInfoDto metaInfo,
            final AsyncCallback<CTLSchemaMetaInfoDto> callback) {
        rpcService.updateCTLSchemaMetaInfoScope(metaInfo, 
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
        rpcService.deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(fqn, version, 
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
        rpcService.prepareCTLSchemaExport(ctlSchemaId, method, new DataCallback<String>(callback) {
                    @Override
                    protected void onResult(String result) {
                    }
                });
    }

    public void loadApplicationEventFamilyMaps(String applicationId,
            final AsyncCallback<List<ApplicationEventFamilyMapDto>> callback) {
        rpcService.getApplicationEventFamilyMapsByApplicationId(applicationId,
                new DataCallback<List<ApplicationEventFamilyMapDto>>(callback) {
                    @Override
                    protected void onResult(List<ApplicationEventFamilyMapDto> result) {
                    }
                });

    }

    public void editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap,
            final AsyncCallback<ApplicationEventFamilyMapDto> callback) {
        rpcService.editApplicationEventFamilyMap(applicationEventFamilyMap,
                new DataCallback<ApplicationEventFamilyMapDto>(callback) {
                    @Override
                    protected void onResult(ApplicationEventFamilyMapDto result) {
                        eventBus.fireEvent(new DataEvent(ApplicationEventFamilyMapDto.class));
                    }
                });
    }

    public void getApplicationEventFamilyMap(String applicationEventFamilyMapId,
            final AsyncCallback<ApplicationEventFamilyMapDto> callback) {
        rpcService.getApplicationEventFamilyMap(applicationEventFamilyMapId,
                new DataCallback<ApplicationEventFamilyMapDto>(callback) {
                    @Override
                    protected void onResult(ApplicationEventFamilyMapDto result) {
                    }
                });
    }

    public void getVacantEventClassFamilies(String applicationId,
            final AsyncCallback<List<EcfInfoDto>> callback) {
        rpcService.getVacantEventClassFamiliesByApplicationId(applicationId,
                new DataCallback<List<EcfInfoDto>>(callback) {
            @Override
            protected void onResult(List<EcfInfoDto> result) {
            }
        });
    }

    public void getAefMaps(String applicationId,
            final AsyncCallback<List<AefMapInfoDto>> callback) {
        rpcService.getEventClassFamiliesByApplicationId(applicationId,
                new DataCallback<List<AefMapInfoDto>>(callback) {
            @Override
            protected void onResult(List<AefMapInfoDto> result) {
            }
        });
    }

    public void loadEndpointGroups(String applicationId,
            final AsyncCallback<List<EndpointGroupDto>> callback) {
        rpcService.getEndpointGroupsByApplicationId(applicationId,
                new DataCallback<List<EndpointGroupDto>>(callback) {
                    @Override
                    protected void onResult(List<EndpointGroupDto> result) {
                    }
                });

    }

    public void deleteEndpointGroup(String endpointGroupId,
            final AsyncCallback<Void> callback) {
        rpcService.deleteEndpointGroup(endpointGroupId,
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
        rpcService.editEndpointGroup(endpointGroup,
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
        rpcService.getEndpointGroup(endpointGroupId,
                new DataCallback<EndpointGroupDto>(callback) {
                    @Override
                    protected void onResult(EndpointGroupDto result) {
                    }
                });
    }

    public void getEndpointProfileByGroupID(String groupID, String limit, String offset,
            AsyncCallback<EndpointProfilesPageDto> callback) {
        rpcService.getEndpointProfileByEndpointGroupId(groupID, limit, offset, callback);
    }

    public void getEndpointProfileByKeyHash(String endpointKeyHash,
            AsyncCallback<EndpointProfileDto> callback) {
        rpcService.getEndpointProfileByKeyHash(endpointKeyHash, callback);
    }

    public void getEndpointProfileViewByKeyHash(String endpointKeyHash,
            AsyncCallback<EndpointProfileViewDto> callback) {
        rpcService.getEndpointProfileViewByKeyHash(endpointKeyHash, callback);
    }

    public void updateServerProfile(String endpointKeyHash, int serverProfileVersion, RecordField serverProfileRecord,
                                      AsyncCallback<EndpointProfileDto> callback){
        rpcService.updateServerProfile(endpointKeyHash, serverProfileVersion, serverProfileRecord, callback);
    }

    public void loadProfileFilterRecords(String endpointGroupId, boolean includeDeprecated,
            final AsyncCallback<List<ProfileFilterRecordDto>> callback) {
        rpcService.getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated,
                new DataCallback<List<ProfileFilterRecordDto>>(callback) {
                    @Override
                    protected void onResult(List<ProfileFilterRecordDto> result) {
                    }
                });
    }

    public void getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId,
            final AsyncCallback<ProfileFilterRecordDto> callback) {
        rpcService.getProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId,
                new DataCallback<ProfileFilterRecordDto>(callback) {
            @Override
            protected void onResult(ProfileFilterRecordDto result) {
            }
        });
    }

    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, 
            String endpointGroupId,
            final AsyncCallback<Void> callback) {
        rpcService.deleteProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId,
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
        rpcService.editProfileFilter(profileFilter,
                new DataCallback<ProfileFilterDto>(callback) {
                    @Override
                    protected void onResult(ProfileFilterDto result) {
                        eventBus.fireEvent(new DataEvent(ProfileFilterDto.class));
                    }
        });
    }

    public void activateProfileFilter(String profileFilterId,
            final AsyncCallback<ProfileFilterDto> callback) {
        rpcService.activateProfileFilter(profileFilterId,
                new DataCallback<ProfileFilterDto>(callback) {
                    @Override
                    protected void onResult(ProfileFilterDto result) {
                        eventBus.fireEvent(new DataEvent(ProfileFilterDto.class));
                    }
        });
    }

    public void deactivateProfileFilter(String profileFilterId,
            final AsyncCallback<ProfileFilterDto> callback) {
        rpcService.deactivateProfileFilter(profileFilterId,
                new DataCallback<ProfileFilterDto>(callback) {
                    @Override
                    protected void onResult(ProfileFilterDto result) {
                        eventBus.fireEvent(new DataEvent(ProfileFilterDto.class));
                    }
        });
    }

    public void loadConfigurationRecords(String endpointGroupId, boolean includeDeprecated,
            final AsyncCallback<List<ConfigurationRecordDto>> callback) {
        rpcService.getConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated,
                new DataCallback<List<ConfigurationRecordDto>>(callback) {
                    @Override
                    protected void onResult(List<ConfigurationRecordDto> result) {
                    }
                });
    }

    public void getConfigurationRecordView(String schemaId, String endpointGroupId,
            final AsyncCallback<ConfigurationRecordViewDto> callback) {
        rpcService.getConfigurationRecordView(schemaId, endpointGroupId,
                new DataCallback<ConfigurationRecordViewDto>(callback) {
            @Override
            protected void onResult(ConfigurationRecordViewDto result) {
            }
        });
    }

    public void deleteConfigurationRecord(String schemaId, String endpointGroupId,
            final AsyncCallback<Void> callback) {
        rpcService.deleteConfigurationRecord(schemaId, endpointGroupId,
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
        rpcService.editConfigurationRecordForm(configuration,
                new DataCallback<ConfigurationRecordFormDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationRecordFormDto result) {
                        eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
                    }
        });
    }

    public void activateConfigurationRecordForm(String configurationId,
            final AsyncCallback<ConfigurationRecordFormDto> callback) {
        rpcService.activateConfigurationRecordForm(configurationId,
                new DataCallback<ConfigurationRecordFormDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationRecordFormDto result) {
                        eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
                    }
        });
    }

    public void deactivateConfigurationRecordForm(String configurationId,
            final AsyncCallback<ConfigurationRecordFormDto> callback) {
        rpcService.deactivateConfigurationRecordForm(configurationId,
                new DataCallback<ConfigurationRecordFormDto>(callback) {
                    @Override
                    protected void onResult(ConfigurationRecordFormDto result) {
                        eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
                    }
        });
    }

    public void getVacantProfileSchemas(String endpointGroupId,
            final AsyncCallback<List<ProfileVersionPairDto>> callback) {
        rpcService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId,
                new DataCallback<List<ProfileVersionPairDto>>(callback) {
            @Override
            protected void onResult(List<ProfileVersionPairDto> result) {
            }
        });
    }

    public void getVacantConfigurationSchemaInfos(String endpointGroupId,
            final AsyncCallback<List<SchemaInfoDto>> callback) {
        rpcService.getVacantConfigurationSchemaInfosByEndpointGroupId(endpointGroupId,
                new DataCallback<List<SchemaInfoDto>>(callback) {
            @Override
            protected void onResult(List<SchemaInfoDto> result) {
            }
        });
    }

    public void getUserNotificationSchemas(String applicationId,
            final AsyncCallback<List<VersionDto>> callback) {
        rpcService.getUserNotificationSchemasByApplicationId(applicationId,
                new DataCallback<List<VersionDto>>(callback) {
            @Override
            protected void onResult(List<VersionDto> result) {
            }
        });
    }

    public void getUserNotificationSchemaInfosByApplicationId(String applicationId,
            final AsyncCallback<List<SchemaInfoDto>> callback) {
        rpcService.getUserNotificationSchemaInfosByApplicationId(applicationId,
                new DataCallback<List<SchemaInfoDto>>(callback) {
            @Override
            protected void onResult(List<SchemaInfoDto> result) {
            }
        });
    }

    public void loadTopics(String applicationId,
            final AsyncCallback<List<TopicDto>> callback) {
        rpcService.getTopicsByApplicationId(applicationId,
                new DataCallback<List<TopicDto>>(callback) {
                    @Override
                    protected void onResult(List<TopicDto> result) {
                    }
                });
    }

    public void loadTopicsByEndpointGroupId(String endpointGroupId,
            final AsyncCallback<List<TopicDto>> callback) {
        rpcService.getTopicsByEndpointGroupId(endpointGroupId,
                new DataCallback<List<TopicDto>>(callback) {
                    @Override
                    protected void onResult(List<TopicDto> result) {
                    }
                });
    }

    public void loadVacantTopicsByEndpointGroupId(String endpointGroupId,
            final AsyncCallback<List<TopicDto>> callback) {
        rpcService.getVacantTopicsByEndpointGroupId(endpointGroupId,
                new DataCallback<List<TopicDto>>(callback) {
                    @Override
                    protected void onResult(List<TopicDto> result) {
                    }
                });
    }

    public void deleteTopic(String topicId,
            final AsyncCallback<Void> callback) {
        rpcService.deleteTopic(topicId,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                        eventBus.fireEvent(new DataEvent(
                                TopicDto.class));
                    }
                });
    }

    public void editTopic(
            TopicDto topic,
            final AsyncCallback<TopicDto> callback) {
        rpcService.editTopic(topic,
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
        rpcService.getTopic(topicId,
                new DataCallback<TopicDto>(callback) {
                    @Override
                    protected void onResult(TopicDto result) {
                    }
                });
    }

    public void addTopicToEndpointGroup(String endpointGroupId, String topicId,
            final AsyncCallback<Void> callback) {
        rpcService.addTopicToEndpointGroup(endpointGroupId, topicId,
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
        rpcService.removeTopicFromEndpointGroup(endpointGroupId, topicId,
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
        rpcService.getRecordDataFromFile(schema, fileItemName,
                new DataCallback<RecordField>(callback) {
                    @Override
                    protected void onResult(RecordField result) {
                    }
                });
    }

    public void sendNotification(
            NotificationDto notification, RecordField notificationData,
            final AsyncCallback<Void> callback) {
        rpcService.sendNotification(notification, notificationData,
                new DataCallback<Void>(callback) {
                    @Override
                    protected void onResult(Void result) {
                    }
                });
    }

    public void loadLogAppenders(String applicationId,
            final AsyncCallback<List<LogAppenderDto>> callback) {
        rpcService.getLogAppendersByApplicationId(applicationId,
                new DataCallback<List<LogAppenderDto>>(callback) {
            @Override
            protected void onResult(List<LogAppenderDto> result) {
            }
        });
    }

    public void getLogAppender(String appenderId, final AsyncCallback<LogAppenderDto> callback){
        rpcService.getRestLogAppender(appenderId,
                new DataCallback<LogAppenderDto>(callback) {
                    @Override
                    protected void onResult(LogAppenderDto result) {
                    }
                });
    }

    public void getLogAppenderForm(String appenderId,
            final AsyncCallback<LogAppenderDto> callback) {
        rpcService.getLogAppenderForm(appenderId,
                new DataCallback<LogAppenderDto>(callback) {
            @Override
            protected void onResult(LogAppenderDto result) {
            }
        });
    }

    public void editLogAppenderForm(LogAppenderDto dto,
            final AsyncCallback<LogAppenderDto> callback) {
        rpcService.editLogAppenderForm(dto,
                new DataCallback<LogAppenderDto>(callback) {
            @Override
            protected void onResult(LogAppenderDto result) {
            }
        });
    }

    public void removeLogAppender(String appenderId,
            final AsyncCallback<Void> callback) {
        rpcService.deleteLogAppender(appenderId,
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
            rpcService.getLogAppenderPluginInfos(new DataCallback<List<PluginInfoDto>>(callback) {
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
        rpcService.getUserVerifiersByApplicationId(applicationId,
                new DataCallback<List<UserVerifierDto>>(callback) {
            @Override
            protected void onResult(List<UserVerifierDto> result) {
            }
        });
    }

    public void getUserVerifierForm(String userVerifierId,
            final AsyncCallback<UserVerifierDto> callback) {
        rpcService.getUserVerifierForm(userVerifierId,
                new DataCallback<UserVerifierDto>(callback) {
                    @Override
                    protected void onResult(UserVerifierDto result) {
                    }
                });
    }

    public void getUserVerifier(String userVerifierId, final AsyncCallback<UserVerifierDto> callback){
        rpcService.getRestUserVerifier(userVerifierId,
                new DataCallback<UserVerifierDto>(callback) {
            @Override
            protected void onResult(UserVerifierDto result) {
            }
        });
    }

    public void editUserVerifierForm(UserVerifierDto dto,
            final AsyncCallback<UserVerifierDto> callback) {
        rpcService.editUserVerifierForm(dto,
                new DataCallback<UserVerifierDto>(callback) {
            @Override
            protected void onResult(UserVerifierDto result) {
            }
        });
    }

    public void removeUserVerifier(String userVerifierId,
            final AsyncCallback<Void> callback) {
        rpcService.deleteUserVerifier(userVerifierId,
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
            rpcService.getUserVerifierPluginInfos(new DataCallback<List<PluginInfoDto>>(callback) {
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
        rpcService.getUserConfigurationSchemaInfosByApplicationId(applicationId,
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
        rpcService.editUserConfiguration(endpointUserConfiguration, applicationId, configurationData,
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
        rpcService.createSdkProfile(sdkProfile, new DataCallback<SdkProfileDto>(callback) {
            @Override
            protected void onResult(SdkProfileDto callback) {
            }
        });
    }

    public void deleteSdkProfile(String sdkProfileId, final AsyncCallback<Void> callback) {
        rpcService.deleteSdkProfile(sdkProfileId, new DataCallback<Void>(callback) {
            @Override
            protected void onResult(Void result) {
                eventBus.fireEvent(new DataEvent(SdkProfileDto.class));
            }
        });
    }

    public void getSdkProfile(String sdkProfileId, final AsyncCallback<SdkProfileDto> callback) {
        rpcService.getSdkProfile(sdkProfileId, new DataCallback<SdkProfileDto>(callback) {
            @Override
            protected void onResult(SdkProfileDto result) {
            }
        });
    }

    public void getSdkProfileView(String sdkProfileId, final AsyncCallback<SdkProfileViewDto> callback) {
        rpcService.getSdkProfileView(sdkProfileId, new DataCallback<SdkProfileViewDto>(callback) {
            @Override
            protected void onResult(SdkProfileViewDto result) {
            }
        });
    }

    public void loadSdkProfiles(String applicationId, final AsyncCallback<List<SdkProfileDto>> callback) {
        rpcService.getSdkProfilesByApplicationId(applicationId, new DataCallback<List<SdkProfileDto>>(callback) {
            @Override
            protected void onResult(List<SdkProfileDto> result) {
            }
        });
    }
}
