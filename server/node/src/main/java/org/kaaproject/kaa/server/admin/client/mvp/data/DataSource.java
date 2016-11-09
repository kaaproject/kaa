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
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey.RecordFiles;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CtlSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
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
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.LogSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.NotificationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.services.AdminUiService;
import org.kaaproject.kaa.server.admin.shared.services.AdminUiServiceAsync;
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

  private final ApplicationServiceAsync applicationRpcService =
      GWT.create(ApplicationService.class);

  private final ConfigurationServiceAsync configurationRpcService =
      GWT.create(ConfigurationService.class);

  private final CtlServiceAsync ctlRpcService = GWT.create(CtlService.class);

  private final DeviceManagementServiceAsync deviceManagementRpcService =
      GWT.create(DeviceManagementService.class);

  private final EventServiceAsync eventRpcService = GWT.create(EventService.class);

  private final GroupServiceAsync groupRpcService = GWT.create(GroupService.class);

  private final LoggingServiceAsync loggingRpcService = GWT.create(LoggingService.class);

  private final NotificationServiceAsync notificationRpcService =
      GWT.create(NotificationService.class);

  private final ProfileServiceAsync profileRpcService = GWT.create(ProfileService.class);

  private final SdkServiceAsync sdkRpcService = GWT.create(SdkService.class);

  private final TenantServiceAsync tenantRpcService = GWT.create(TenantService.class);

  private final UserServiceAsync userRpcService = GWT.create(UserService.class);

  private final VerifierServiceAsync verifierRpcService = GWT.create(VerifierService.class);

  private final AdminUiServiceAsync adminUirpcService = GWT.create(AdminUiService.class);

  private final EventBus eventBus;

  private List<TenantDto> tenants;

  private List<ApplicationDto> applications;

  private List<UserDto> users;

  private List<EventClassFamilyDto> ecfs;

  private List<PluginInfoDto> logAppenderPluginInfos;

  private List<PluginInfoDto> userVerifierPluginInfos;

  /**
   * All-args constructor.
   */
  public DataSource(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /**
   * Returns a user profile in async way.
   *
   * @param callback callback which should be called with a result
   */
  public void getUserProfile(
      final AsyncCallback<UserDto> callback) {
    userRpcService.getUserProfile(
        new DataCallback<UserDto>(callback) {
          @Override
          protected void onResult(UserDto result) {
          }
        });
  }

  /**
   * Loads a tenant admin list in async way.
   *
   * @param tenantId tenant id for which a list should be loaded
   * @param callback callback which should be called with a result
   */
  public void loadAllTenantAdminsByTenantId(String tenantId,
                                            final AsyncCallback<List<UserDto>> callback) {

    userRpcService.findAllTenantAdminsByTenantId(tenantId, new DataCallback<List<UserDto>>(
        callback) {
      @Override
      protected void onResult(List<UserDto> result) {
        eventBus.fireEvent(new DataEvent(UserDto.class, true));
      }
    });
  }

  /**
   * Returns a user configuration in async way.
   *
   * @param externalUserId   external user id
   * @param appId            application id
   * @param schemaVersion    schema version
   * @param callback         callback which should be called with a result
   */
  public void findUserConfigurationByExternalUIdAndAppIdAndSchemaVersion(
      String externalUserId,
      String appId,
      Integer schemaVersion,
      final AsyncCallback<EndpointUserConfigurationDto> callback) {

    configurationRpcService.findUserConfigurationByExternalUIdAndAppIdAndSchemaVersion(
        externalUserId, appId, schemaVersion,
        new DataCallback<EndpointUserConfigurationDto>(callback) {
          @Override
          protected void onResult(EndpointUserConfigurationDto result) {
            eventBus.fireEvent(new DataEvent(EndpointUserConfigurationDto.class, true));
          }
        });
  }

  /**
   * Edits a user profile in async way.
   *
   * @param userProfileUpdateDto new user profile
   * @param callback             callback which should be called with a result
   */
  public void editUserProfile(UserProfileUpdateDto userProfileUpdateDto,
                              final AsyncCallback<Void> callback) {
    userRpcService.editUserProfile(userProfileUpdateDto,
        new DataCallback<Void>(callback) {
          @Override
          protected void onResult(Void result) {
          }
        });
  }

  /**
   * Finds an endpoint configuration in async way.
   *
   * @param endpointKeyHash endpoint key hash
   * @param callback        callback which should be called with a result
   */
  public void findEndpointConfigurationByEndpointKeyHash(String endpointKeyHash,
                                                         final AsyncCallback<String> callback) {
    configurationRpcService.findEndpointConfigurationByEndpointKeyHash(
        endpointKeyHash,
        new DataCallback<String>(callback) {
          @Override
          protected void onResult(String result) {

          }
        });
  }


  /**
   * Returns mail properties.
   *
   * @param callback callback which should be called with a result
   */
  public void getMailProperties(
      final AsyncCallback<PropertiesDto> callback) {
    adminUirpcService.getMailProperties(
        new DataCallback<PropertiesDto>(callback) {
          @Override
          protected void onResult(PropertiesDto result) {
          }
        });
  }

  /**
   * Edits email properties.
   *
   * @param mailProperties new mail properties
   * @param callback       callback which should be called with a result
   */
  public void editMailProperties(PropertiesDto mailProperties,
                                 final AsyncCallback<PropertiesDto> callback) {
    adminUirpcService.editMailProperties(mailProperties,
        new DataCallback<PropertiesDto>(callback) {
          @Override
          protected void onResult(PropertiesDto result) {
          }
        });
  }

  /**
   * Returns general properties in async way.
   *
   * @param callback callback which should be called with a result
   */
  public void getGeneralProperties(
      final AsyncCallback<PropertiesDto> callback) {
    adminUirpcService.getGeneralProperties(
        new DataCallback<PropertiesDto>(callback) {
          @Override
          protected void onResult(PropertiesDto result) {
          }
        });
  }

  /**
   * Edits general properties.
   *
   * @param generalProperties new general properties
   * @param callback          callback which should be called with a result
   */
  public void editGeneralProperties(PropertiesDto generalProperties,
                                    final AsyncCallback<PropertiesDto> callback) {
    adminUirpcService.editGeneralProperties(generalProperties,
        new DataCallback<PropertiesDto>(callback) {
          @Override
          protected void onResult(PropertiesDto result) {
          }
        });
  }

  /**
   * Loads tenants in async way.
   *
   * @param callback callback which should be called with a result
   */
  public void loadTenants(final AsyncCallback<List<TenantDto>> callback) {
    loadTenants(callback, false);
  }


  /**
   * Loads tenants in async way.
   *
   * @param callback callback which should be called with a result
   * @param refresh indicate whether to clear an existing tenant list
   */
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

  /**
   * Deletes a tenant with a specified identifier.
   *
   * @param tenantId identifier of a tenant which should be deleted
   * @param callback callback which should be called with a result
   */
  public void deleteTenant(String tenantId, final AsyncCallback<Void> callback) {
    tenantRpcService.deleteTenant(tenantId, new DataCallback<Void>(callback) {
      @Override
      protected void onResult(Void result) {
        refreshTenants();
      }
    });
  }

  /**
   * Edits a tenant.
   *
   * @param tenant   new tenant
   * @param callback callback which should be called with a result
   */
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

  /**
   * Returns a tenant in async way.
   *
   * @param tenantId identifier of a tenant which should be returned
   * @param callback callback which should be called with a result
   */
  public void getTenant(String tenantId,
                        final AsyncCallback<TenantDto> callback) {
    tenantRpcService.getTenant(tenantId,
        new DataCallback<TenantDto>(callback) {
          @Override
          protected void onResult(TenantDto result) {
          }
        });
  }

  /**
   * Loads an application list in async way.
   *
   * @param callback callback which should be called with a result
   */
  public void loadApplications(
      final AsyncCallback<List<ApplicationDto>> callback) {
    loadApplications(callback, false);
  }

  /**
   * Loads an application list in async way.
   *
   * @param callback callback which should be called with a result
   * @param refresh  indicate whether to clear an existing application list
   */
  public void loadApplications(
      final AsyncCallback<List<ApplicationDto>> callback, boolean refresh) {
    if (applications == null || refresh) {
      applications = new ArrayList<>();
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

  /**
   * Edits an application.
   *
   * @param application new application
   * @param callback    callback which should be called with a result
   */
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

  /**
   * Returns an application in async way.
   *
   * @param applicationId identifier of an application which should be returned
   * @param callback      callback which should be called with a result
   */
  public void getApplication(String applicationId,
                             final AsyncCallback<ApplicationDto> callback) {
    applicationRpcService.getApplication(applicationId,
        new DataCallback<ApplicationDto>(callback) {
          @Override
          protected void onResult(ApplicationDto result) {
          }
        });
  }

  /**
   * Returns a schema version in async way.
   *
   * @param applicationId  identifier of an application whose schema version should be returned
   * @param callback       callback which should be called with a result
   */
  public void getSchemaVersionsByApplicationId(String applicationId,
                                               final AsyncCallback<SchemaVersions> callback) {
    sdkRpcService.getSchemaVersionsByApplicationId(applicationId,
        new DataCallback<SchemaVersions>(callback) {
          @Override
          protected void onResult(SchemaVersions result) {
          }
        });
  }

  /**
   * Generates a sdk.
   *
   * @param sdkProfile     sdk profile
   * @param targetPlatform target platform
   * @param callback       callback which should be called with a result
   */
  public void generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform,
                          final AsyncCallback<String> callback) {
    sdkRpcService.generateSdk(sdkProfile, targetPlatform,
        new DataCallback<String>(callback) {
          @Override
          protected void onResult(String result) {
          }
        });
  }

  /**
   * Returns a record data in async way.
   *
   * @param applicationId    application identifier
   * @param logSchemaVersion log schema version
   * @param fileType         file type
   * @param callback         callback which should be called with a result
   */
  public void getRecordData(String applicationId,
                            Integer logSchemaVersion, RecordFiles fileType,
                            final AsyncCallback<String> callback) {
    adminUirpcService.getRecordDataByApplicationIdAndSchemaVersion(
        applicationId, logSchemaVersion, fileType,
        new DataCallback<String>(callback) {
          @Override
          protected void onResult(String result) {
          }
        });
  }

  /**
   * Returns a record library in async way.
   *
   * @param applicationId    application identifier
   * @param logSchemaVersion log schema version
   * @param fileType         file type
   * @param callback         callback which should be called with a result
   */
  public void getRecordLibrary(String applicationId,
                               Integer logSchemaVersion, RecordFiles fileType,
                               final AsyncCallback<String> callback) {
    adminUirpcService.getRecordLibraryByApplicationIdAndSchemaVersion(
        applicationId, logSchemaVersion, fileType,
        new DataCallback<String>(callback) {
          @Override
          protected void onResult(String result) {
          }
        });
  }

  /**
   * Loads a user list in async way.
   *
   * @param callback callback which should be called with a result
   */
  public void loadUsers(final AsyncCallback<List<UserDto>> callback) {
    loadUsers(callback, false);
  }

  /**
   * Loads a user list in async way.
   *
   * @param callback callback which should be called with a result
   * @param refresh  indicate whether to clear an existing user list
   */
  public void loadUsers(final AsyncCallback<List<UserDto>> callback,
                        boolean refresh) {
    if (users == null || refresh) {
      users = new ArrayList<>();
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

  /**
   * Deletes a user with a specified identifier.
   *
   * @param userId   identifier of user which should be deleted
   * @param callback callback which should be called with a result
   */
  public void deleteUser(String userId, final AsyncCallback<Void> callback) {
    userRpcService.deleteUser(userId, new DataCallback<Void>(callback) {
      @Override
      protected void onResult(Void result) {
        refreshUsers();
      }
    });
  }

  /**
   * Edits a user.
   *
   * @param user     new user
   * @param callback callback which should be called with a result
   */
  public void editUser(UserDto user, final AsyncCallback<UserDto> callback) {
    userRpcService.editUser(user, true, new DataCallback<UserDto>(callback) {
      @Override
      protected void onResult(UserDto result) {
        if (KaaAdmin.getAuthInfo().getAuthority() == KaaAuthorityDto.TENANT_ADMIN) {
          refreshUsers();
        }
      }
    });
  }

  /**
   * Returns a user with a specified identifier in async way.
   *
   * @param userId   user identifier
   * @param callback callback which should be called with a result
   */
  public void getUser(String userId, final AsyncCallback<UserDto> callback) {
    userRpcService.getUser(userId, new DataCallback<UserDto>(callback) {
      @Override
      protected void onResult(UserDto result) {
      }
    });
  }

  /**
   * Creates a simple empty schema form.
   *
   * @param callback callback which should be called with a result
   */
  public void createSimpleEmptySchemaForm(final AsyncCallback<RecordField> callback) {
    adminUirpcService.createSimpleEmptySchemaForm(new DataCallback<RecordField>(callback) {
      @Override
      protected void onResult(RecordField result) {
      }
    });
  }

  /**
   * Creates a common empty schema form.
   *
   * @param callback callback which should be called with a result
   */
  public void createCommonEmptySchemaForm(final AsyncCallback<RecordField> callback) {
    adminUirpcService.createCommonEmptySchemaForm(new DataCallback<RecordField>(callback) {
      @Override
      protected void onResult(RecordField result) {
      }
    });
  }

  /**
   * Create a configuration schema.
   *
   * @param ctlSchemaForm CTL schema
   * @param callback      callback which should be called with a result
   */
  public void createConfigurationSchemaFormCtlSchema(
      CtlSchemaFormDto ctlSchemaForm,
      final AsyncCallback<ConfigurationSchemaViewDto> callback) {
    configurationRpcService.createConfigurationSchemaFormCtlSchema(
        ctlSchemaForm, new DataCallback<ConfigurationSchemaViewDto>(callback) {
          @Override
          protected void onResult(ConfigurationSchemaViewDto result) {
            eventBus.fireEvent(new DataEvent(ConfigurationSchemaViewDto.class));
          }
        });
  }

  /**
   * Creates the empty ECF schema form.
   *
   * @param callback callback which should be called with a result
   */
  public void createEcfEmptySchemaForm(final AsyncCallback<RecordField> callback) {
    eventRpcService.createEcfEmptySchemaForm(new DataCallback<RecordField>(callback) {
      @Override
      protected void onResult(RecordField result) {
      }
    });
  }

  /**
   * Generates a simple schema form.
   *
   * @param fileItemName file item name
   * @param callback     callback which should be called with a result
   */
  public void generateSimpleSchemaForm(String fileItemName,
                                       final AsyncCallback<RecordField> callback) {
    adminUirpcService.generateSimpleSchemaForm(fileItemName,
        new DataCallback<RecordField>(callback) {
          @Override
          protected void onResult(RecordField result) {
          }
        });
  }

  /**
   * Generates a common schema form.
   *
   * @param fileItemName file item name
   * @param callback     callback which should be called with a result
   */
  public void generateCommonSchemaForm(String fileItemName,
                                       final AsyncCallback<RecordField> callback) {
    adminUirpcService.generateCommonSchemaForm(fileItemName,
        new DataCallback<RecordField>(callback) {
          @Override
          protected void onResult(RecordField result) {
          }
        });
  }

  /**
   * Generates a configuration schema form.
   *
   * @param fileItemName file item name
   * @param callback     callback which should be called with a result
   */
  public void generateConfigurationSchemaForm(String fileItemName,
                                              final AsyncCallback<RecordField> callback) {
    configurationRpcService.generateConfigurationSchemaForm(fileItemName,
        new DataCallback<RecordField>(callback) {
          @Override
          protected void onResult(RecordField result) {
          }
        });
  }

  /**
   * Generates the ECF version form.
   *
   * @param fileItemName file item name
   * @param callback     callback which should be called with a result
   */
  public void generateEcfVersionForm(String fileItemName,
                                     final AsyncCallback<RecordField> callback) {
    eventRpcService.generateEcfSchemaForm(fileItemName,
        new DataCallback<RecordField>(callback) {
          @Override
          protected void onResult(RecordField result) {
          }
        });
  }

  /**
   * Loads the event class family list in async way.
   *
   * @param callback callback which should be called with a result
   */
  public void loadEcfs(
      final AsyncCallback<List<EventClassFamilyDto>> callback) {
    loadEcfs(callback, false);
  }

  /**
   * Loads the event class family list in async way.
   *
   * @param callback callback which should be called with a result
   * @param refresh  indicate whether to clear an existing ECF list
   */
  public void loadEcfs(
      final AsyncCallback<List<EventClassFamilyDto>> callback, boolean refresh) {
    if (ecfs == null || refresh) {
      ecfs = new ArrayList<>();
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

  /**
   * Edits an event class family.
   *
   * @param ecf      new event class family
   * @param callback callback which should be called with a result
   */
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

  /**
   * Loads an event class family in async way.
   *
   * @param ecfId    identifier of an event class family
   * @param callback callback which should be called with a result
   */
  public void getEcf(String ecfId,
                     final AsyncCallback<EventClassFamilyDto> callback) {
    eventRpcService.getEventClassFamily(ecfId,
        new DataCallback<EventClassFamilyDto>(callback) {
          @Override
          protected void onResult(EventClassFamilyDto result) {
          }
        });
  }

  /**
   * Loads an event class view in async way.
   *
   * @param eventClassId identifier of an event class view which should be loaded
   * @param callback     callback which should be called with a result
   */
  public void getEventClassView(String eventClassId,
                                final AsyncCallback<EventClassViewDto> callback) {
    eventRpcService.getEventClassView(eventClassId, new DataCallback<EventClassViewDto>(callback) {
      @Override
      protected void onResult(EventClassViewDto result) {
      }
    });

  }

  /**
   * Loads an event class view in async way.
   *
   * @param eventClassViewDto event class view
   * @param callback          callback which should be called with a result
   */
  public void getEventClassViewByCtlSchemaId(EventClassDto eventClassViewDto,
                                             final AsyncCallback<EventClassViewDto> callback) {
    eventRpcService.getEventClassViewByCtlSchemaId(
        eventClassViewDto, new DataCallback<EventClassViewDto>(callback) {
          @Override
          protected void onResult(EventClassViewDto result) {
          }
        });

  }

  /**
   * Save an event class view.
   *
   * @param eventClassViewDto event class view
   * @param callback          callback which should be called with a result
   */
  public void saveEventClassView(EventClassViewDto eventClassViewDto,
                                 final AsyncCallback<EventClassViewDto> callback) {
    eventRpcService.saveEventClassView(
        eventClassViewDto, new DataCallback<EventClassViewDto>(callback) {
          @Override
          protected void onResult(EventClassViewDto result) {
            eventBus.fireEvent(new DataEvent(EventClassViewDto.class));
          }
        });

  }

  /**
   * Creates an event class form.
   *
   * @param ctlSchemaFormDto CTL schema form
   * @param callback         callback which should be called with a result
   */
  public void createEventClassFormCtlSchema(CtlSchemaFormDto ctlSchemaFormDto,
                                            final AsyncCallback<EventClassViewDto> callback) {
    eventRpcService.createEventClassFormCtlSchema(ctlSchemaFormDto,
        new DataCallback<EventClassViewDto>(callback) {
          @Override
          protected void onResult(EventClassViewDto result) {
          }
        });

  }

  /**
   * Returns event class family versions.
   *
   * @param eventClassFamilyId event class family identifier
   * @param callback           callback which should be called with a result
   */
  public void getEventClassFamilyVersions(
      String eventClassFamilyId,
      final AsyncCallback<List<EventClassFamilyVersionDto>> callback) {
    eventRpcService.getEventClassFamilyVersions(eventClassFamilyId,
        new DataCallback<List<EventClassFamilyVersionDto>>(callback) {
          @Override
          protected void onResult(List<EventClassFamilyVersionDto> result) {
          }
        });

  }

  /**
   * Returns the last CTL schema reference.
   *
   * @param ctlSchemaId CTL schema identifier
   * @param callback    callback which should be called with a result
   */
  public void getLastCtlSchemaReferenceDto(String ctlSchemaId,
                                           final AsyncCallback<CtlSchemaReferenceDto> callback) {
    ctlRpcService.getLastCtlSchemaReferenceDto(
        ctlSchemaId, new DataCallback<CtlSchemaReferenceDto>(callback) {
          @Override
          protected void onResult(CtlSchemaReferenceDto result) {
          }
        });

  }

  /**
   * Adds an event class family version.
   *
   * @param eventClassFamilyId event class family identifier
   * @param eventClassViewDto  event class family view
   * @param callback           callback which should be called with a result
   */
  public void addEventClassFamilyVersionFromView(String eventClassFamilyId,
                                                 List<EventClassViewDto> eventClassViewDto,
                                                 final AsyncCallback<Void> callback) {
    eventRpcService.addEventClassFamilyVersionFromView(eventClassFamilyId, eventClassViewDto,
        new DataCallback<Void>(callback) {
          @Override
          protected void onResult(Void result) {

          }
        });
  }

  /**
   * Returns an event class family in async way.
   *
   * @param eventClassFamilyId event class family identifier
   * @param version            event class family version
   * @param type               event class family type
   * @param callback           callback which should be called with a result
   */
  public void getEventClassesByFamilyIdVersionAndType(
      String eventClassFamilyId,
      int version,
      EventClassType type,
      final AsyncCallback<List<EventClassDto>> callback) {
    eventRpcService.getEventClassesByFamilyIdVersionAndType(eventClassFamilyId, version, type,
        new DataCallback<List<EventClassDto>>(callback) {
          @Override
          protected void onResult(List<EventClassDto> result) {
          }
        });
  }

  /**
   * Adds an event class family version.
   *
   * @param eventClassFamilyId      event class family identifier
   * @param eventClassFamilyVersion event class family version
   * @param callback                callback which should be called with a result
   */
  public void addEventClassFamilyVersion(String eventClassFamilyId,
                                         EventClassFamilyVersionDto eventClassFamilyVersion,
                                         final AsyncCallback<Void> callback) {
    eventRpcService.addEventClassFamilyVersion(eventClassFamilyId, eventClassFamilyVersion,
        new DataCallback<Void>(callback) {
          @Override
          protected void onResult(Void result) {
          }
        });
  }

  /**
   * Validates an event class family list in the SDK profile.
   *
   * @param ecfList  event class family list
   * @param callback callback which should be called with a result
   */
  public void validateEcfListInSdkProfile(List<AefMapInfoDto> ecfList,
                                          final AsyncCallback<Void> callback) {
    eventRpcService.validateEcfListInSdkProfile(ecfList, new DataCallback<Void>(callback) {
      @Override
      protected void onResult(Void result) {
      }
    });
  }

  /**
   * Loads profile schemas.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadProfileSchemas(String applicationId,
                                 final AsyncCallback<List<EndpointProfileSchemaDto>> callback) {
    profileRpcService.getProfileSchemasByApplicationId(applicationId,
        new DataCallback<List<EndpointProfileSchemaDto>>(callback) {
          @Override
          protected void onResult(List<EndpointProfileSchemaDto> result) {
          }
        });

  }

  /**
   * Save profile schema view.
   *
   * @param profileSchemaView profile schema view
   * @param callback          callback which should be called with a result
   */
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

  /**
   * Creates a profile schema form the CTL schema.
   *
   * @param ctlSchemaForm CTL schema form
   * @param callback      callback which should be called with a result
   */
  public void createProfileSchemaFormCtlSchema(
      CtlSchemaFormDto ctlSchemaForm,
      final AsyncCallback<ProfileSchemaViewDto> callback) {
    profileRpcService.createProfileSchemaFormCtlSchema(ctlSchemaForm,
        new DataCallback<ProfileSchemaViewDto>(callback) {
          @Override
          protected void onResult(ProfileSchemaViewDto result) {
            eventBus.fireEvent(new DataEvent(EndpointProfileSchemaDto.class));
          }
        });
  }

  /**
   * Returns a profile schema view in async way.
   *
   * @param profileSchemaId profile schema identifier
   * @param callback        callback which should be called with a result
   */
  public void getProfileSchemaView(String profileSchemaId,
                                   final AsyncCallback<ProfileSchemaViewDto> callback) {
    profileRpcService.getProfileSchemaView(profileSchemaId,
        new DataCallback<ProfileSchemaViewDto>(callback) {
          @Override
          protected void onResult(ProfileSchemaViewDto result) {
          }
        });
  }

  /**
   * Loads server profile schemas.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadServerProfileSchemas(
      String applicationId,
      final AsyncCallback<List<ServerProfileSchemaDto>> callback) {
    profileRpcService.getServerProfileSchemasByApplicationId(applicationId,
        new DataCallback<List<ServerProfileSchemaDto>>(callback) {
          @Override
          protected void onResult(List<ServerProfileSchemaDto> result) {
          }
        });
  }

  /**
   * Returns a server profile schema info list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getServerProfileSchemaInfosByApplicationId(
      String applicationId,
      final AsyncCallback<List<SchemaInfoDto>> callback) {
    profileRpcService.getServerProfileSchemaInfosByApplicationId(applicationId,
        new DataCallback<List<SchemaInfoDto>>(callback) {
          @Override
          protected void onResult(List<SchemaInfoDto> result) {
          }
        });
  }

  /**
   * Returns a server profile schema info list.
   *
   * @param endpointKeyHash endpoint key hash
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Returns a server profile schema view.
   *
   * @param serverProfileSchemaId server profile schema identifier
   * @param callback              callback which should be called with a result
   */
  public void getServerProfileSchemaView(
      String serverProfileSchemaId,
      final AsyncCallback<ServerProfileSchemaViewDto> callback) {
    profileRpcService.getServerProfileSchemaView(serverProfileSchemaId,
        new DataCallback<ServerProfileSchemaViewDto>(callback) {
          @Override
          protected void onResult(ServerProfileSchemaViewDto result) {
          }
        });
  }

  /**
   * Save server profile schema view.
   *
   * @param serverProfileSchema server profile schema
   * @param callback            callback which should be called with a result
   */
  public void saveServerProfileSchemaView(
      ServerProfileSchemaViewDto serverProfileSchema,
      final AsyncCallback<ServerProfileSchemaViewDto> callback) {
    profileRpcService.saveServerProfileSchemaView(serverProfileSchema,
        new DataCallback<ServerProfileSchemaViewDto>(callback) {
          @Override
          protected void onResult(ServerProfileSchemaViewDto result) {
            eventBus.fireEvent(new DataEvent(ServerProfileSchemaDto.class));
          }
        });
  }

  /**
   * Creates a server profile schema.
   *
   * @param ctlSchemaForm CTL schema form
   * @param callback      callback which should be called with a result
   */
  public void createServerProfileSchemaFormCtlSchema(
      CtlSchemaFormDto ctlSchemaForm,
      final AsyncCallback<ServerProfileSchemaViewDto> callback) {
    profileRpcService.createServerProfileSchemaFormCtlSchema(ctlSchemaForm,
        new DataCallback<ServerProfileSchemaViewDto>(callback) {
          @Override
          protected void onResult(ServerProfileSchemaViewDto result) {
            eventBus.fireEvent(new DataEvent(ServerProfileSchemaViewDto.class));
          }
        });
  }

  /**
   * Returns an endpoint profile schema info.
   *
   * @param endpointProfileSchemaId endpoint profile schema identifier
   * @param callback                callback which should be called with a result
   */
  public void getEndpointProfileSchemaInfo(String endpointProfileSchemaId,
                                           final AsyncCallback<SchemaInfoDto> callback) {
    profileRpcService.getEndpointProfileSchemaInfo(endpointProfileSchemaId,
        new DataCallback<SchemaInfoDto>(callback) {
          @Override
          protected void onResult(SchemaInfoDto result) {
          }
        });
  }

  /**
   * Returns a server profile schema info.
   *
   * @param serverProfileSchemaId server profile identifier
   * @param callback              callback which should be called with a result
   */
  public void getServerProfileSchemaInfo(String serverProfileSchemaId,
                                         final AsyncCallback<SchemaInfoDto> callback) {
    profileRpcService.getServerProfileSchemaInfo(serverProfileSchemaId,
        new DataCallback<SchemaInfoDto>(callback) {
          @Override
          protected void onResult(SchemaInfoDto result) {
          }
        });
  }

  /**
   * Tests a profile filter.
   *
   * @param endpointProfile endpoint profile
   * @param serverProfile   server profile
   * @param filterBody      filter to be evaluated
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Returns an available application CTL schema reference list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getAvailableApplicationCtlSchemaReferences(
      String applicationId,
      final AsyncCallback<List<CtlSchemaReferenceDto>> callback) {
    ctlRpcService.getAvailableApplicationCtlSchemaReferences(applicationId,
        new DataCallback<List<CtlSchemaReferenceDto>>(callback) {
          @Override
          protected void onResult(List<CtlSchemaReferenceDto> result) {
          }
        });
  }

  /**
   * Returns a CTL schema reference list on a specified tenant level.
   *
   * @param ecfId                 event class family identifier
   * @param eventClassViewDtoList event class view list
   * @param callback              callback which should be called with a result
   */
  public void getTenantLevelCtlSchemaReferenceForEcf(
      String ecfId,
      List<EventClassViewDto> eventClassViewDtoList,
      final AsyncCallback<List<CtlSchemaReferenceDto>> callback) {
    ctlRpcService.getTenantLevelCtlSchemaReferenceForEcf(ecfId, eventClassViewDtoList,
        new DataCallback<List<CtlSchemaReferenceDto>>(callback) {
          @Override
          protected void onResult(List<CtlSchemaReferenceDto> result) {
          }
        });
  }

  /**
   * Loads a configuration schema list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadConfigurationSchemas(
      String applicationId,
      final AsyncCallback<List<ConfigurationSchemaDto>> callback) {
    configurationRpcService.getConfigurationSchemasByApplicationId(applicationId,
        new DataCallback<List<ConfigurationSchemaDto>>(callback) {
          @Override
          protected void onResult(List<ConfigurationSchemaDto> result) {
          }
        });

  }

  /**
   * Save a configuration schema view.
   *
   * @param configurationSchema configuration schema view
   * @param callback            callback which should be called with a result
   */
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

  /**
   * Returns configuration schema view for specified identifier.
   *
   * @param configurationSchemaId configuration schema identifier
   * @param callback              callback which should be called with a result
   */
  public void getConfigurationSchemaView(
      String configurationSchemaId,
      final AsyncCallback<ConfigurationSchemaViewDto> callback) {
    configurationRpcService.getConfigurationSchemaView(configurationSchemaId,
        new DataCallback<ConfigurationSchemaViewDto>(callback) {
          @Override
          protected void onResult(ConfigurationSchemaViewDto result) {
          }
        });
  }

  /**
   * Loads notification schema list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadNotificationSchemas(String applicationId,
                                      final AsyncCallback<List<NotificationSchemaDto>> callback) {
    notificationRpcService.getNotificationSchemasByApplicationId(applicationId,
        new DataCallback<List<NotificationSchemaDto>>(callback) {
          @Override
          protected void onResult(List<NotificationSchemaDto> result) {
          }
        });

  }

  /**
   * Save a notification schema view.
   *
   * @param notificationSchema notification schema view
   * @param callback           callback which should be called with a result
   */
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

  /**
   * Creates a notification schema.
   * @param ctlSchemaForm CTL schema form
   * @param callback      callback which should be called with a result
   */
  public void createNotificationSchemaFormCtlSchema(
      CtlSchemaFormDto ctlSchemaForm,
      final AsyncCallback<NotificationSchemaViewDto> callback) {
    notificationRpcService.createNotificationSchemaFormCtlSchema(ctlSchemaForm,
        new DataCallback<NotificationSchemaViewDto>(callback) {
          @Override
          protected void onResult(NotificationSchemaViewDto result) {
            eventBus.fireEvent(new DataEvent(NotificationSchemaViewDto.class));
          }
        });
  }

  /**
   * Returns a notification schema view.
   *
   * @param profileSchemaId profile schema identifier
   * @param callback         callback which should be called with a result
   */
  public void getNotificationSchemaView(
      String profileSchemaId,
      final AsyncCallback<NotificationSchemaViewDto> callback) {
    notificationRpcService.getNotificationSchemaView(profileSchemaId,
        new DataCallback<NotificationSchemaViewDto>(callback) {
          @Override
          protected void onResult(NotificationSchemaViewDto result) {
          }
        });
  }

  /**
   * Returns a notification schema.
   *
   * @param profileSchemaId profile schema identifier
   * @param callback        callback which should be called with a result
   */
  public void getNotificationSchema(String profileSchemaId,
                                    final AsyncCallback<NotificationSchemaDto> callback) {
    notificationRpcService.getNotificationSchema(profileSchemaId,
        new DataCallback<NotificationSchemaDto>(callback) {
          @Override
          protected void onResult(NotificationSchemaDto result) {
          }
        });
  }

  /**
   * Loads a log schema list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadLogSchemas(String applicationId,
                             final AsyncCallback<List<LogSchemaDto>> callback) {
    loggingRpcService.getLogSchemasByApplicationId(applicationId,
        new DataCallback<List<LogSchemaDto>>(callback) {
          @Override
          protected void onResult(List<LogSchemaDto> result) {
          }
        });

  }

  /**
   * Loads a log schema version list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadLogSchemasVersion(String applicationId,
                                    final AsyncCallback<List<VersionDto>> callback) {
    loggingRpcService.getLogSchemasVersions(applicationId,
        new DataCallback<List<VersionDto>>(callback) {
          @Override
          protected void onResult(List<VersionDto> result) {
          }
        });
  }

  /**
   * Save a log schema view.
   *
   * @param logSchema log schema view
   * @param callback  callback which should be called with a result
   */
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

  /**
   * Returns a log schema view.
   *
   * @param logSchemaId log schema identifier
   * @param callback    callback which should be called with a result
   */
  public void getLogSchemaView(String logSchemaId,
                               final AsyncCallback<LogSchemaViewDto> callback) {
    loggingRpcService.getLogSchemaView(logSchemaId,
        new DataCallback<LogSchemaViewDto>(callback) {
          @Override
          protected void onResult(LogSchemaViewDto result) {
          }
        });
  }

  /**
   * Creates a log schema.
   *
   * @param ctlSchemaForm CTL schema form
   * @param callback      callback which should be called with a result
   */
  public void createLogSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm,
                                           final AsyncCallback<LogSchemaViewDto> callback) {
    loggingRpcService.createLogSchemaFormCtlSchema(ctlSchemaForm,
        new DataCallback<LogSchemaViewDto>(callback) {
          @Override
          protected void onResult(LogSchemaViewDto result) {
          }
        });
  }

  /**
   * Returns the CTL schema list on a system level.
   *
   * @param callback callback which should be called with a result
   */
  public void getSystemLevelCtlSchemas(
      final AsyncCallback<List<CtlSchemaMetaInfoDto>> callback) {
    ctlRpcService.getSystemLevelCtlSchemas(
        new DataCallback<List<CtlSchemaMetaInfoDto>>(callback) {
          @Override
          protected void onResult(List<CtlSchemaMetaInfoDto> result) {
          }
        });
  }

  /**
   * Returns the CTL schema list on a tenant level.
   *
   * @param callback callback which should be called with a result
   */
  public void getTenantLevelCtlSchemas(
      final AsyncCallback<List<CtlSchemaMetaInfoDto>> callback) {
    ctlRpcService.getTenantLevelCtlSchemas(
        new DataCallback<List<CtlSchemaMetaInfoDto>>(callback) {
          @Override
          protected void onResult(List<CtlSchemaMetaInfoDto> result) {
          }
        });
  }

  /**
   * Returns CTL schema by a specified identifier.
   *
   * @param ctlSchemaId CTL schema identifier
   * @param callback    callback which should be called with a result
   */
  public void getCtlSchemaById(String ctlSchemaId,
                               final AsyncCallback<CTLSchemaDto> callback) {
    ctlRpcService.getCtlSchemaById(ctlSchemaId,
        new DataCallback<CTLSchemaDto>(callback) {
          @Override
          protected void onResult(CTLSchemaDto result) {
          }
        });
  }

  /**
   * Returns the CTL schema list on an application level.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getApplicationLevelCtlSchemas(
      String applicationId,
      final AsyncCallback<List<CtlSchemaMetaInfoDto>> callback) {
    ctlRpcService.getApplicationLevelCtlSchemas(applicationId,
        new DataCallback<List<CtlSchemaMetaInfoDto>>(callback) {
          @Override
          protected void onResult(List<CtlSchemaMetaInfoDto> result) {
          }
        });
  }

  /**
   * Returns the latest CTL schema form.
   *
   * @param metaInfoId meta information identifier
   * @param callback   callback which should be called with a result
   */
  public void getLatestCtlSchemaForm(String metaInfoId,
                                     final AsyncCallback<CtlSchemaFormDto> callback) {
    ctlRpcService.getLatestCtlSchemaForm(metaInfoId,
        new DataCallback<CtlSchemaFormDto>(callback) {
          @Override
          protected void onResult(CtlSchemaFormDto result) {
          }
        });
  }

  /**
   * Returns the CTL schema form.
   *
   * @param metaInfoId meta information identifier
   * @param version    CTL schema version
   * @param callback   callback which should be called with a result
   */
  public void getCtlSchemaFormByMetaInfoIdAndVer(String metaInfoId, Integer version,
                                                 final AsyncCallback<CtlSchemaFormDto> callback) {
    ctlRpcService.getCtlSchemaFormByMetaInfoIdAndVer(metaInfoId, version,
        new DataCallback<CtlSchemaFormDto>(callback) {
          @Override
          protected void onResult(CtlSchemaFormDto result) {
          }
        });
  }

  /**
   * Creates new CTK schema form instance.
   *
   * @param metaInfoId    meta information identifier
   * @param sourceVersion source version
   * @param applicationId application identifier
   * @param converterType converter type
   * @param callback      callback which should be called with a result
   */
  public void createNewCtlSchemaFormInstance(String metaInfoId, Integer sourceVersion,
                                             String applicationId, ConverterType converterType,
                                             final AsyncCallback<CtlSchemaFormDto> callback) {
    ctlRpcService.createNewCtlSchemaFormInstance(
        metaInfoId, sourceVersion, applicationId, converterType,
        new DataCallback<CtlSchemaFormDto>(callback) {
          @Override
          protected void onResult(CtlSchemaFormDto result) {
          }
        });
  }

  /**
   * Generates the CTL schema form.
   *
   * @param fileItemName  file item name
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void generateCtlSchemaForm(String fileItemName, String applicationId,
                                    final AsyncCallback<RecordField> callback) {
    ctlRpcService.generateCtlSchemaForm(fileItemName, applicationId,
        new DataCallback<RecordField>(callback) {
          @Override
          protected void onResult(RecordField result) {
          }
        });
  }

  /**
   * Edits the CTL schema form.
   *
   * @param ctlSchemaForm CTL schema form
   * @param converterType converter type
   * @param callback      callback which should be called with a result
   */
  public void editCtlSchemaForm(CtlSchemaFormDto ctlSchemaForm,
                                ConverterType converterType,
                                final AsyncCallback<CtlSchemaFormDto> callback) {
    ctlRpcService.saveCtlSchemaForm(ctlSchemaForm, converterType,
        new DataCallback<CtlSchemaFormDto>(callback) {
          @Override
          protected void onResult(CtlSchemaFormDto result) {
            eventBus.fireEvent(new DataEvent(CtlSchemaMetaInfoDto.class));
          }
        });
  }

  /**
   * Check whether fully qualified name exists.
   *
   * @param ctlSchemaForm CTL schema form
   * @param callback      callback which should be called with a result
   */
  public void checkFqnExists(CtlSchemaFormDto ctlSchemaForm,
                             final AsyncCallback<Boolean> callback) {
    ctlRpcService.checkFqnExists(ctlSchemaForm,
        new DataCallback<Boolean>(callback) {
          @Override
          protected void onResult(Boolean result) {
          }
        });
  }

  /**
   * Promotes a scope.
   *
   * @param applicationId application identifier
   * @param fqn           fully qualified name
   * @param callback      callback which should be called with a result
   */
  public void promoteScopeToTenant(String applicationId, String fqn,
                                   final AsyncCallback<CtlSchemaMetaInfoDto> callback) {
    ctlRpcService.promoteScopeToTenant(applicationId, fqn,
        new DataCallback<CtlSchemaMetaInfoDto>(callback) {
          @Override
          protected void onResult(CtlSchemaMetaInfoDto result) {
            eventBus.fireEvent(new DataEvent(CtlSchemaMetaInfoDto.class));
          }
        });
  }

  /**
   * Deletes the CTL schema.
   *
   * @param fqn           fully qualified name
   * @param version       fully qualified name version
   * @param tenantId      tenant identifier
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void deleteCtlSchemaByFqnVersionTenantIdAndApplicationId(
      String fqn, Integer version, String tenantId,
      String applicationId, final AsyncCallback<Void> callback) {
    ctlRpcService.deleteCtlSchemaByFqnVersionTenantIdAndApplicationId(fqn, version,
        tenantId, applicationId,
        new DataCallback<Void>(callback) {
          @Override
          protected void onResult(Void result) {
            eventBus.fireEvent(new DataEvent(CtlSchemaMetaInfoDto.class));
          }
        });
  }

  /**
   * Prepares the CTL schema to export.
   *
   * @param ctlSchemaId CTL schema identifier
   * @param method      CTL schema export method
   * @param callback    callback which should be called with a result
   */
  public void prepareCtlSchemaExport(String ctlSchemaId, CTLSchemaExportMethod method,
                                     final AsyncCallback<String> callback) {
    ctlRpcService.prepareCtlSchemaExport(ctlSchemaId, method, new DataCallback<String>(callback) {
      @Override
      protected void onResult(String result) {
      }
    });
  }

  /**
   * Loads application event family maps.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadApplicationEventFamilyMaps(
      String applicationId,
      final AsyncCallback<List<ApplicationEventFamilyMapDto>> callback) {
    eventRpcService.getApplicationEventFamilyMapsByApplicationId(applicationId,
        new DataCallback<List<ApplicationEventFamilyMapDto>>(callback) {
          @Override
          protected void onResult(List<ApplicationEventFamilyMapDto> result) {
          }
        });

  }

  /**
   * Edtis an application event family map.
   *
   * @param applicationEventFamilyMap application event family map
   * @param callback                  callback which should be called with a result
   */
  public void editApplicationEventFamilyMap(
      ApplicationEventFamilyMapDto applicationEventFamilyMap,
      final AsyncCallback<ApplicationEventFamilyMapDto> callback) {
    eventRpcService.editApplicationEventFamilyMap(applicationEventFamilyMap,
        new DataCallback<ApplicationEventFamilyMapDto>(callback) {
          @Override
          protected void onResult(ApplicationEventFamilyMapDto result) {
            eventBus.fireEvent(new DataEvent(ApplicationEventFamilyMapDto.class));
          }
        });
  }

  /**
   * Returns an application event family map.
   *
   * @param applicationEventFamilyMapId application event family map identifier
   * @param callback                    callback which should be called with a result
   */
  public void getApplicationEventFamilyMap(
      String applicationEventFamilyMapId,
      final AsyncCallback<ApplicationEventFamilyMapDto> callback) {
    eventRpcService.getApplicationEventFamilyMap(applicationEventFamilyMapId,
        new DataCallback<ApplicationEventFamilyMapDto>(callback) {
          @Override
          protected void onResult(ApplicationEventFamilyMapDto result) {
          }
        });
  }

  /**
   * Returns a list of vacant event class families.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getVacantEventClassFamilies(String applicationId,
                                          final AsyncCallback<List<EcfInfoDto>> callback) {
    eventRpcService.getVacantEventClassFamiliesByApplicationId(applicationId,
        new DataCallback<List<EcfInfoDto>>(callback) {
          @Override
          protected void onResult(List<EcfInfoDto> result) {
          }
        });
  }

  /**
   * Returns an attached class family list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getAefMaps(String applicationId,
                         final AsyncCallback<List<AefMapInfoDto>> callback) {
    eventRpcService.getEventClassFamiliesByApplicationId(applicationId,
        new DataCallback<List<AefMapInfoDto>>(callback) {
          @Override
          protected void onResult(List<AefMapInfoDto> result) {
          }
        });
  }

  /**
   * Loads an endpoint group list in async way.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadEndpointGroups(String applicationId,
                                 final AsyncCallback<List<EndpointGroupDto>> callback) {
    groupRpcService.getEndpointGroupsByApplicationId(applicationId,
        new DataCallback<List<EndpointGroupDto>>(callback) {
          @Override
          protected void onResult(List<EndpointGroupDto> result) {
          }
        });

  }

  /**
   * Deletes an endpoint group.
   *
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Edits an endpoint group.
   *
   * @param endpointGroup new endpoint group
   * @param callback      callback which should be called with a result
   */
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

  /**
   * Returns an endpoint group.
   *
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
  public void getEndpointGroup(String endpointGroupId,
                               final AsyncCallback<EndpointGroupDto> callback) {
    groupRpcService.getEndpointGroup(endpointGroupId,
        new DataCallback<EndpointGroupDto>(callback) {
          @Override
          protected void onResult(EndpointGroupDto result) {
          }
        });
  }

  /**
   * Returns en endpoint profile.
   *
   * @param groupId  endpoint group identifier
   * @param limit    limit
   * @param offset   offset
   * @param callback callback which should be called with a result
   */
  public void getEndpointProfileByGroupId(String groupId, String limit, String offset,
                                          AsyncCallback<EndpointProfilesPageDto> callback) {
    groupRpcService.getEndpointProfileByEndpointGroupId(groupId, limit, offset, callback);
  }

  /**
   * Returns en endpoint profile.
   *
   * @param endpointKeyHash endpoint key hash
   * @param callback        callback which should be called with a result
   */
  public void getEndpointProfileByKeyHash(String endpointKeyHash,
                                          AsyncCallback<EndpointProfileDto> callback) {
    profileRpcService.getEndpointProfileByKeyHash(endpointKeyHash, callback);
  }

  /**
   * Returns an endpoint profile view.
   *
   * @param endpointKeyHash endpoint key hash
   * @param callback        callback which should be called with a result
   */
  public void getEndpointProfileViewByKeyHash(String endpointKeyHash,
                                              AsyncCallback<EndpointProfileViewDto> callback) {
    profileRpcService.getEndpointProfileViewByKeyHash(endpointKeyHash, callback);
  }

  /**
   * Updates a server profile.
   *
   * @param endpointKeyHash      endpoint key hash
   * @param serverProfileVersion server profile version
   * @param serverProfileRecord  server profile record
   * @param callback             callback which should be called with a result
   */
  public void updateServerProfile(String endpointKeyHash,
                                  int serverProfileVersion,
                                  RecordField serverProfileRecord,
                                  AsyncCallback<EndpointProfileDto> callback) {
    profileRpcService.updateServerProfile(
        endpointKeyHash, serverProfileVersion, serverProfileRecord, callback);
  }

  /**
   * Loads a profile filter record list.
   *
   * @param endpointGroupId   endpoint group identifier
   * @param includeDeprecated indicate whether to include deprecated records
   * @param callback          callback which should be called with a result
   */
  public void loadProfileFilterRecords(
      String endpointGroupId,
      boolean includeDeprecated,
      final AsyncCallback<List<ProfileFilterRecordDto>> callback) {
    groupRpcService.getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated,
        new DataCallback<List<ProfileFilterRecordDto>>(callback) {
          @Override
          protected void onResult(List<ProfileFilterRecordDto> result) {
          }
        });
  }

  /**
   * Returns a profile filter record.
   *
   * @param endpointProfileSchemaId endpoint profile schema identifier
   * @param serverProfileSchemaId   server profile schema identifier
   * @param endpointGroupId         endpoint group identifier
   * @param callback                callback which should be called with a result
   */
  public void getProfileFilterRecord(String endpointProfileSchemaId,
                                     String serverProfileSchemaId,
                                     String endpointGroupId,
                                     final AsyncCallback<ProfileFilterRecordDto> callback) {
    groupRpcService.getProfileFilterRecord(
        endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId,
        new DataCallback<ProfileFilterRecordDto>(callback) {
          @Override
          protected void onResult(ProfileFilterRecordDto result) {
          }
        });
  }

  /**
   * Deletes a profile filter record.
   *
   * @param endpointProfileSchemaId endpoint profile schema identifier
   * @param serverProfileSchemaId   server profile schema identifier
   * @param endpointGroupId         endpoint group identifier
   * @param callback                callback which should be called with a result
   */
  public void deleteProfileFilterRecord(String endpointProfileSchemaId,
                                        String serverProfileSchemaId,
                                        String endpointGroupId,
                                        final AsyncCallback<Void> callback) {
    groupRpcService.deleteProfileFilterRecord(
        endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId,
        new DataCallback<Void>(callback) {
          @Override
          protected void onResult(Void result) {
            eventBus.fireEvent(new DataEvent(
                ProfileFilterDto.class));
          }
        });
  }

  /**
   * Edits a profile filter.
   *
   * @param profileFilter profile filter
   * @param callback      callback which should be called with a result
   */
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

  /**
   * Activates a profile filter.
   *
   * @param profileFilterId profile filter identifier
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Deactivates a profile filter.
   *
   * @param profileFilterId profile filter identifier
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Loads a configuration record list.
   *
   * @param endpointGroupId   endpoint group identifier
   * @param includeDeprecated indicate whether to include deprecated records
   * @param callback          callback which should be called with a result
   */
  public void loadConfigurationRecords(
      String endpointGroupId,
      boolean includeDeprecated,
      final AsyncCallback<List<ConfigurationRecordDto>> callback) {
    configurationRpcService.getConfigurationRecordsByEndpointGroupId(
        endpointGroupId, includeDeprecated,
        new DataCallback<List<ConfigurationRecordDto>>(callback) {
          @Override
          protected void onResult(List<ConfigurationRecordDto> result) {
          }
        });
  }

  /**
   * Returns a configuration record view.
   *
   * @param schemaId        schema identifier
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
  public void getConfigurationRecordView(
      String schemaId,
      String endpointGroupId,
      final AsyncCallback<ConfigurationRecordViewDto> callback) {
    configurationRpcService.getConfigurationRecordView(schemaId, endpointGroupId,
        new DataCallback<ConfigurationRecordViewDto>(callback) {
          @Override
          protected void onResult(ConfigurationRecordViewDto result) {
          }
        });
  }

  /**
   * Deletes a configuration record.
   *
   * @param schemaId        schema identifier
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Edits a configuration record form.
   *
   * @param configuration configuration
   * @param callback      callback which should be called with a result
   */
  public void editConfigurationRecordForm(
      ConfigurationRecordFormDto configuration,
      final AsyncCallback<ConfigurationRecordFormDto> callback) {
    configurationRpcService.editConfigurationRecordForm(configuration,
        new DataCallback<ConfigurationRecordFormDto>(callback) {
          @Override
          protected void onResult(ConfigurationRecordFormDto result) {
            eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
          }
        });
  }

  /**
   * Activates a configuration record form.
   *
   * @param configurationId configuration identifier
   * @param callback        callback which should be called with a result
   */
  public void activateConfigurationRecordForm(
      String configurationId,
      final AsyncCallback<ConfigurationRecordFormDto> callback) {
    configurationRpcService.activateConfigurationRecordForm(configurationId,
        new DataCallback<ConfigurationRecordFormDto>(callback) {
          @Override
          protected void onResult(ConfigurationRecordFormDto result) {
            eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
          }
        });
  }

  /**
   * Deactivates a configuration record form.
   *
   * @param configurationId configuration identifier
   * @param callback        callback which should be called with a result
   */
  public void deactivateConfigurationRecordForm(
      String configurationId,
      final AsyncCallback<ConfigurationRecordFormDto> callback) {
    configurationRpcService.deactivateConfigurationRecordForm(configurationId,
        new DataCallback<ConfigurationRecordFormDto>(callback) {
          @Override
          protected void onResult(ConfigurationRecordFormDto result) {
            eventBus.fireEvent(new DataEvent(ConfigurationRecordFormDto.class));
          }
        });
  }

  /**
   * Returns a vacant profile schema list.
   *
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
  public void getVacantProfileSchemas(String endpointGroupId,
                                      final AsyncCallback<List<ProfileVersionPairDto>> callback) {
    groupRpcService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId,
        new DataCallback<List<ProfileVersionPairDto>>(callback) {
          @Override
          protected void onResult(List<ProfileVersionPairDto> result) {
          }
        });
  }

  /**
   * Returns a vacant profile schema information list.
   *
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
  public void getVacantConfigurationSchemaInfos(
      String endpointGroupId,
      final AsyncCallback<List<SchemaInfoDto>> callback) {
    configurationRpcService.getVacantConfigurationSchemaInfosByEndpointGroupId(endpointGroupId,
        new DataCallback<List<SchemaInfoDto>>(callback) {
          @Override
          protected void onResult(List<SchemaInfoDto> result) {
          }
        });
  }

  /**
   * Returns a user notification schema list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getUserNotificationSchemas(String applicationId,
                                         final AsyncCallback<List<VersionDto>> callback) {
    notificationRpcService.getUserNotificationSchemasByApplicationId(applicationId,
        new DataCallback<List<VersionDto>>(callback) {
          @Override
          protected void onResult(List<VersionDto> result) {
          }
        });
  }

  /**
   * Returns a user notification schema info list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getUserNotificationSchemaInfosByApplicationId(
      String applicationId,
      final AsyncCallback<List<SchemaInfoDto>> callback) {
    notificationRpcService.getUserNotificationSchemaInfosByApplicationId(applicationId,
        new DataCallback<List<SchemaInfoDto>>(callback) {
          @Override
          protected void onResult(List<SchemaInfoDto> result) {
          }
        });
  }

  /**
   * Loads a topic list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadTopics(String applicationId,
                         final AsyncCallback<List<TopicDto>> callback) {
    notificationRpcService.getTopicsByApplicationId(applicationId,
        new DataCallback<List<TopicDto>>(callback) {
          @Override
          protected void onResult(List<TopicDto> result) {
          }
        });
  }

  /**
   * Loads a topic list.
   *
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
  public void loadTopicsByEndpointGroupId(String endpointGroupId,
                                          final AsyncCallback<List<TopicDto>> callback) {
    notificationRpcService.getTopicsByEndpointGroupId(endpointGroupId,
        new DataCallback<List<TopicDto>>(callback) {
          @Override
          protected void onResult(List<TopicDto> result) {
          }
        });
  }

  /**
   * Loads a vacant topic list.
   *
   * @param endpointGroupId endpoint group identifier
   * @param callback        callback which should be called with a result
   */
  public void loadVacantTopicsByEndpointGroupId(String endpointGroupId,
                                                final AsyncCallback<List<TopicDto>> callback) {
    notificationRpcService.getVacantTopicsByEndpointGroupId(endpointGroupId,
        new DataCallback<List<TopicDto>>(callback) {
          @Override
          protected void onResult(List<TopicDto> result) {
          }
        });
  }

  /**
   * Deletes a topic.
   *
   * @param topicId  topic identifier
   * @param callback callback which should be called with a result
   */
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

  /**
   * Removes an endpoint profile.
   *
   * @param endpointKeyHash endpoint key hash
   * @param callback        callback which should be called with a result
   */
  public void removeEndpointProfileByKeyHash(String endpointKeyHash,
                                             final AsyncCallback<Void> callback) {
    profileRpcService.removeEndpointProfileByKeyHash(endpointKeyHash,
        new DataCallback<Void>(callback) {
          @Override
          protected void onResult(Void result) {
            eventBus.fireEvent(new DataEvent(
                EndpointProfileDto.class));
          }
        });
  }

  /**
   * Edits a topic.
   *
   * @param topic    new topic
   * @param callback callback which should be called with a result
   */
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

  /**
   * Returns a topic.
   *
   * @param topicId  topic identifier
   * @param callback callback which should be called with a result
   */
  public void getTopic(String topicId,
                       final AsyncCallback<TopicDto> callback) {
    notificationRpcService.getTopic(topicId,
        new DataCallback<TopicDto>(callback) {
          @Override
          protected void onResult(TopicDto result) {
          }
        });
  }

  /**
   * Adds a topic to an endpoint group.
   *
   * @param endpointGroupId endpoint group identifier
   * @param topicId         topic identifier
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Removes a topic from endpoint group.
   *
   * @param endpointGroupId endpoint group identifier
   * @param topicId         topic identifier
   * @param callback        callback which should be called with a result
   */
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

  /**
   * Returns a record data.
   *
   * @param schema       schema
   * @param fileItemName file item name
   * @param callback     callback which should be called with a result
   */
  public void getRecordDataFromFile(String schema, String fileItemName,
                                    final AsyncCallback<RecordField> callback) {
    adminUirpcService.getRecordDataFromFile(schema, fileItemName,
        new DataCallback<RecordField>(callback) {
          @Override
          protected void onResult(RecordField result) {
          }
        });
  }

  /**
   * Returns a configuration record.
   *
   * @param schema       schema
   * @param fileItemName file item name
   * @param callback     callback which should be called with a result
   */
  public void getConfigurationRecordDataFromFile(String schema, String fileItemName,
                                                 final AsyncCallback<RecordField> callback) {
    configurationRpcService.getConfigurationRecordDataFromFile(schema, fileItemName,
        new DataCallback<RecordField>(callback) {
          @Override
          protected void onResult(RecordField result) {
          }
        });
  }

  /**
   * Sends a notification.
   *
   * @param notification      notification to be sent
   * @param notificationData  notification data
   * @param callback          callback which should be called with a result
   */
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

  /**
   * Sends a unicast notification.
   *
   * @param notification     notification to be sent
   * @param endpointKeyHash  endpoint key hash
   * @param notificationData notification data
   * @param callback         callback which should be called with a result
   */
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

  /**
   * Loads log appenders.
   *
   * @param applicationId application idetifier
   * @param callback      callback which should be called with a result
   */
  public void loadLogAppenders(String applicationId,
                               final AsyncCallback<List<LogAppenderDto>> callback) {
    loggingRpcService.getLogAppendersByApplicationId(applicationId,
        new DataCallback<List<LogAppenderDto>>(callback) {
          @Override
          protected void onResult(List<LogAppenderDto> result) {
          }
        });
  }

  /**
   * Returns a log appender.
   *
   * @param appenderId appender identifier
   * @param callback   callback which should be called with a result
   */
  public void getLogAppender(String appenderId, final AsyncCallback<LogAppenderDto> callback) {
    loggingRpcService.getRestLogAppender(appenderId,
        new DataCallback<LogAppenderDto>(callback) {
          @Override
          protected void onResult(LogAppenderDto result) {
          }
        });
  }

  /**
   * Returns a log appender form.
   *
   * @param appenderId log appender identifier
   * @param callback   callback which should be called with a result
   */
  public void getLogAppenderForm(String appenderId,
                                 final AsyncCallback<LogAppenderDto> callback) {
    loggingRpcService.getLogAppenderForm(appenderId,
        new DataCallback<LogAppenderDto>(callback) {
          @Override
          protected void onResult(LogAppenderDto result) {
          }
        });
  }

  /**
   * Edits a log appender.
   *
   * @param dto      new log appender
   * @param callback callback which should be called with a result
   */
  public void editLogAppenderForm(LogAppenderDto dto,
                                  final AsyncCallback<LogAppenderDto> callback) {
    loggingRpcService.editLogAppenderForm(dto,
        new DataCallback<LogAppenderDto>(callback) {
          @Override
          protected void onResult(LogAppenderDto result) {
          }
        });
  }

  /**
   * Removes a log appender.
   *
   * @param appenderId log appender identifier
   * @param callback   callback which should be called with a result
   */
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

  /**
   * Loads a log appender plugin info list.
   *
   * @param callback callback which should be called with a result
   */
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

  /**
   * Loads user verifiers.
   *
   * @param applicationId application idetifier
   * @param callback      callback which should be called with a result
   */
  public void loadUserVerifiers(String applicationId,
                                final AsyncCallback<List<UserVerifierDto>> callback) {
    verifierRpcService.getUserVerifiersByApplicationId(applicationId,
        new DataCallback<List<UserVerifierDto>>(callback) {
          @Override
          protected void onResult(List<UserVerifierDto> result) {
          }
        });
  }

  /**
   * Returns a user verifier form.
   *
   * @param userVerifierId user verifier identifier
   * @param callback       callback which should be called with a result
   */
  public void getUserVerifierForm(String userVerifierId,
                                  final AsyncCallback<UserVerifierDto> callback) {
    verifierRpcService.getUserVerifierForm(userVerifierId,
        new DataCallback<UserVerifierDto>(callback) {
          @Override
          protected void onResult(UserVerifierDto result) {
          }
        });
  }

  /**
   * Returns a user verifier.
   *
   * @param userVerifierId user verifier identifier
   * @param callback       callback which should be called with a result
   */
  public void getUserVerifier(String userVerifierId,
                              final AsyncCallback<UserVerifierDto> callback) {
    verifierRpcService.getRestUserVerifier(userVerifierId,
        new DataCallback<UserVerifierDto>(callback) {
          @Override
          protected void onResult(UserVerifierDto result) {
          }
        });
  }

  /**
   * Edits a user verifier form.
   *
   * @param dto      new user verifier
   * @param callback callback which should be called with a result
   */
  public void editUserVerifierForm(UserVerifierDto dto,
                                   final AsyncCallback<UserVerifierDto> callback) {
    verifierRpcService.editUserVerifierForm(dto,
        new DataCallback<UserVerifierDto>(callback) {
          @Override
          protected void onResult(UserVerifierDto result) {
          }
        });
  }

  /**
   * Removes a user verifier.
   *
   * @param userVerifierId user verifier identifier
   * @param callback       callback which should be called with a result
   */
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

  /**
   * Loads a user verifier plugin list.
   *
   * @param callback callback which should be called with a result
   */
  public void loadUserVerifierPluginInfos(
      final AsyncCallback<List<PluginInfoDto>> callback) {
    if (userVerifierPluginInfos == null) {
      userVerifierPluginInfos = new ArrayList<PluginInfoDto>();
      verifierRpcService.getUserVerifierPluginInfos(
          new DataCallback<List<PluginInfoDto>>(callback) {
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

  /**
   * Returns a user configuration schema info list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void getUserConfigurationSchemaInfosByApplicationId(
      String applicationId,
      final AsyncCallback<List<SchemaInfoDto>> callback) {
    configurationRpcService.getUserConfigurationSchemaInfosByApplicationId(applicationId,
        new DataCallback<List<SchemaInfoDto>>(callback) {
          @Override
          protected void onResult(List<SchemaInfoDto> result) {
          }
        });
  }

  /**
   * Edits a user configuration.
   *
   * @param endpointUserConfiguration endpoint user configuration
   * @param applicationId             application identifier
   * @param configurationData         configuratuon data
   * @param callback                  callback which should be called with a result
   */
  public void editUserConfiguration(
      EndpointUserConfigurationDto endpointUserConfiguration,
      String applicationId, RecordField configurationData,
      final AsyncCallback<Void> callback) {
    configurationRpcService.editUserConfiguration(
        endpointUserConfiguration, applicationId, configurationData,
        new DataCallback<Void>(callback) {
          @Override
          protected void onResult(Void result) {
          }
        });
  }

  /**
   * Adds the SDK profile.
   *
   * @param sdkProfile SDK profile
   * @param callback   callback which should be called with a result
   */
  public void addSdkProfile(SdkProfileDto sdkProfile,
                            final AsyncCallback<SdkProfileDto> callback) {
    sdkRpcService.createSdkProfile(sdkProfile, new DataCallback<SdkProfileDto>(callback) {
      @Override
      protected void onResult(SdkProfileDto callback) {
      }
    });
  }

  /**
   * Deletes the SDK profile.
   *
   * @param sdkProfileId SDK profile identifier
   * @param callback     callback which should be called with a result
   */
  public void deleteSdkProfile(String sdkProfileId, final AsyncCallback<Void> callback) {
    sdkRpcService.deleteSdkProfile(sdkProfileId, new DataCallback<Void>(callback) {
      @Override
      protected void onResult(Void result) {
        eventBus.fireEvent(new DataEvent(SdkProfileDto.class));
      }
    });
  }

  /**
   * Returns the SDK profile.
   *
   * @param sdkProfileId SDK profile identifier
   * @param callback     callback which should be called with a result
   */
  public void getSdkProfile(String sdkProfileId, final AsyncCallback<SdkProfileDto> callback) {
    sdkRpcService.getSdkProfile(sdkProfileId, new DataCallback<SdkProfileDto>(callback) {
      @Override
      protected void onResult(SdkProfileDto result) {
      }
    });
  }

  /**
   * Returns the SDK profile view.
   *
   * @param sdkProfileId SDK profile identifier
   * @param callback     callback which should be called with a result
   */
  public void getSdkProfileView(String sdkProfileId,
                                final AsyncCallback<SdkProfileViewDto> callback) {
    sdkRpcService.getSdkProfileView(sdkProfileId, new DataCallback<SdkProfileViewDto>(callback) {
      @Override
      protected void onResult(SdkProfileViewDto result) {
      }
    });
  }

  /**
   * Loads the SDK profile list.
   *
   * @param applicationId application identifier
   * @param callback      callback which should be called with a result
   */
  public void loadSdkProfiles(String applicationId,
                              final AsyncCallback<List<SdkProfileDto>> callback) {
    sdkRpcService.getSdkProfilesByApplicationId(
        applicationId, new DataCallback<List<SdkProfileDto>>(callback) {
          @Override
          protected void onResult(List<SdkProfileDto> result) {
          }
        });
  }

  /**
   * Returns a credential service name list.
   *
   * @param callback callback which should be called with a result
   */
  public void getCredentialsServiceNames(final AsyncCallback<List<String>> callback) {
    deviceManagementRpcService.getCredentialsServiceNames(
        new DataCallback<List<String>>(callback) {
          @Override
          protected void onResult(List<String> result) {
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
}
