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

package org.kaaproject.kaa.server.admin.client.mvp;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.AddSdkProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.AefMapView;
import org.kaaproject.kaa.server.admin.client.mvp.view.ApplicationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePropertiesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointGroupView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfilesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.HeaderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.NavigationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.SendNotificationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TopicView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UpdateUserConfigView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserVerifierView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserView;
import org.kaaproject.kaa.server.admin.client.mvp.view.appender.LogAppenderViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.appender.LogAppendersViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.application.ApplicationViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.application.ApplicationsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.config.ConfigurationSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.config.ConfigurationSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.config.ConfigurationViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.endpoint.EndpointGroupViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.endpoint.EndpointGroupsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.endpoint.EndpointProfileViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.endpoint.EndpointProfilesViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.enduser.UpdateUserConfigViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.event.AefMapViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.event.AefMapsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.event.EcfSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.event.EcfViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.event.EcfsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.header.HeaderViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.log.LogSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.log.LogSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.navigation.NavigationViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.notification.NotificationSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.notification.NotificationSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ProfileFilterViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ProfileSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ProfileSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.sdk.AddSdkProfileViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.sdk.SdkProfilesViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.settings.GeneralPropertiesViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.settings.MailPropertiesViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.tenant.TenantViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.tenant.TenantsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.SendNotificationViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.user.UserProfileViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.user.UserViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.user.UsersViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.verifier.UserVerifierViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.verifier.UserVerifiersViewImpl;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;

public class ClientFactoryImpl implements ClientFactory {

    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController placeController = new PlaceController(eventBus);

    private final HeaderView headerView = new HeaderViewImpl();
    private final NavigationView navigationView = new NavigationViewImpl();

    private final UserProfileView userProfileView = new UserProfileViewImpl();

    private final BasePropertiesView generalPropertiesView = new GeneralPropertiesViewImpl();

    private final BasePropertiesView mailPropertiesView = new MailPropertiesViewImpl();

    private final BaseListView<TenantUserDto> tenantsView = new TenantsViewImpl();
    private final TenantView createTenantView = new TenantViewImpl(true);
    private final TenantView tenantView = new TenantViewImpl(false);

    private final BaseListView<ApplicationDto> applicationsView = new ApplicationsViewImpl(KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));
    private final ApplicationView createApplicationView = new ApplicationViewImpl(true, KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));
    private final ApplicationView applicationView = new ApplicationViewImpl(false, KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));

    private final BaseListView<SdkProfileDto> sdkProfilesView = new SdkProfilesViewImpl();
    private final AddSdkProfileView generateSdkView = new AddSdkProfileViewImpl();

    private final BaseListView<UserDto> usersView = new UsersViewImpl();
    private final UserView createUserView = new UserViewImpl(true);
    private final UserView userView = new UserViewImpl(false);

    private final BaseListView<ProfileSchemaDto> profileSchemasView = new ProfileSchemasViewImpl();
    private final BaseSchemaView profileSchemaView = new ProfileSchemaViewImpl(false);
    private final BaseSchemaView createProfileSchemaView = new ProfileSchemaViewImpl(true);

    private final BaseListView<ConfigurationSchemaDto> configurationSchemasView = new ConfigurationSchemasViewImpl();
    private final BaseSchemaView configurationSchemaView = new ConfigurationSchemaViewImpl(false);
    private final BaseSchemaView createConfigurationSchemaView = new ConfigurationSchemaViewImpl(true);

    private final BaseListView<NotificationSchemaDto> notificationSchemasView = new NotificationSchemasViewImpl();
    private final BaseSchemaView notificationSchemaView = new NotificationSchemaViewImpl(false);
    private final BaseSchemaView createNotificationSchemaView = new NotificationSchemaViewImpl(true);

    private final BaseListView<LogSchemaDto> logSchemasView = new LogSchemasViewImpl();
    private final BaseSchemaView logSchemaView = new LogSchemaViewImpl(false);
    private final BaseSchemaView createLogSchemaView = new LogSchemaViewImpl(true);

    private final BaseListView<EndpointGroupDto> endpointGroupsView = new EndpointGroupsViewImpl();
    private final EndpointGroupView endpointGroupView = new EndpointGroupViewImpl(false);
    private final EndpointGroupView createEndpointGroupView = new EndpointGroupViewImpl(true);

    private final EndpointProfilesView endpointProfilesView = new EndpointProfilesViewImpl();
    private final EndpointProfileView endpointProfileView = new EndpointProfileViewImpl();

    private final BaseRecordView<ProfileFilterDto, String> profileFilterView = new ProfileFilterViewImpl(false);
    private final BaseRecordView<ProfileFilterDto, String> createProfileFilterView = new ProfileFilterViewImpl(true);

    private final BaseRecordView<ConfigurationRecordFormDto, RecordField> configurationView = new ConfigurationViewImpl(false);
    private final BaseRecordView<ConfigurationRecordFormDto, RecordField> createConfigurationView = new ConfigurationViewImpl(true);

    private final BaseListView<TopicDto> topicsView = new TopicsViewImpl();
    private final TopicView topicView = new TopicViewImpl(false);
    private final TopicView createTopicView = new TopicViewImpl(true);

    private final SendNotificationView sendNotificationView = new SendNotificationViewImpl();

    private final BaseListView<LogAppenderDto> appendersView = new LogAppendersViewImpl();
    private final LogAppenderView appenderView = new LogAppenderViewImpl(false);
    private final LogAppenderView createAppenderView = new LogAppenderViewImpl(true);

    private final BaseListView<UserVerifierDto> userVerifiersView = new UserVerifiersViewImpl();
    private final UserVerifierView userVerifierView = new UserVerifierViewImpl(false);
    private final UserVerifierView createUserVerifierView = new UserVerifierViewImpl(true);

    private final BaseListView<EventClassFamilyDto> ecfsView = new EcfsViewImpl();
    private final EcfView ecfView = new EcfViewImpl(false);
    private final EcfView createEcfView = new EcfViewImpl(true);

    private final EcfSchemaView ecfSchemaView = new EcfSchemaViewImpl(false);
    private final EcfSchemaView createEcfSchemaView = new EcfSchemaViewImpl(true);

    private final BaseListView<ApplicationEventFamilyMapDto> aefMapsView = new AefMapsViewImpl();
    private final AefMapView aefMapView = new AefMapViewImpl(false);
    private final AefMapView createAefMapView = new AefMapViewImpl(true);

    private final UpdateUserConfigView updateUserConfigView = new UpdateUserConfigViewImpl();

    private Place homePlace;

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }

    @Override
    public HeaderView getHeaderView() {
        return headerView;
    }

    @Override
    public NavigationView getNavigationView() {
        return navigationView;
    }

    @Override
    public UserProfileView getUserProfileView() {
        return userProfileView;
    }

    @Override
    public BasePropertiesView getGeneralPropertiesView() {
        return generalPropertiesView;
    }

    @Override
    public BasePropertiesView getMailPropertiesView() {
        return mailPropertiesView;
    }

    @Override
    public BaseListView<TenantUserDto> getTenantsView() {
        return tenantsView;
    }

    @Override
    public TenantView getCreateTenantView() {
        return createTenantView;
    }

    @Override
    public TenantView getTenantView() {
        return tenantView;
    }

    @Override
    public BaseListView<ApplicationDto> getApplicationsView() {
        return applicationsView;
    }

    @Override
    public ApplicationView getCreateApplicationView() {
        return createApplicationView;
    }

    @Override
    public ApplicationView getApplicationView() {
        return applicationView;
    }

    @Override
    public BaseListView<SdkProfileDto> getSdkProfilesView() {
        return sdkProfilesView;
    }

    @Override
    public AddSdkProfileView getAddSdkProfileView() {
        return generateSdkView;
    }

    @Override
    public BaseListView<UserDto> getUsersView() {
        return usersView;
    }

    @Override
    public UserView getCreateUserView() {
        return createUserView;
    }

    @Override
    public UserView getUserView() {
        return userView;
    }

    @Override
    public BaseListView<ProfileSchemaDto> getProfileSchemasView() {
        return profileSchemasView;
    }

    @Override
    public BaseSchemaView getProfileSchemaView() {
        return profileSchemaView;
    }

    @Override
    public BaseSchemaView getCreateProfileSchemaView() {
        return createProfileSchemaView;
    }

    @Override
    public BaseListView<ConfigurationSchemaDto> getConfigurationSchemasView() {
        return configurationSchemasView;
    }

    @Override
    public BaseSchemaView getConfigurationSchemaView() {
        return configurationSchemaView;
    }

    @Override
    public BaseSchemaView getCreateConfigurationSchemaView() {
        return createConfigurationSchemaView;
    }

    @Override
    public BaseListView<NotificationSchemaDto> getNotificationSchemasView() {
        return notificationSchemasView;
    }

    @Override
    public BaseSchemaView getNotificationSchemaView() {
        return notificationSchemaView;
    }

    @Override
    public BaseSchemaView getCreateNotificationSchemaView() {
        return createNotificationSchemaView;
    }

    @Override
    public BaseListView<LogSchemaDto> getLogSchemasView() {
        return logSchemasView;
    }

    @Override
    public BaseSchemaView getLogSchemaView() {
        return logSchemaView;
    }

    @Override
    public BaseSchemaView getCreateLogSchemaView() {
        return createLogSchemaView;
    }

    @Override
    public BaseListView<EndpointGroupDto> getEndpointGroupsView() {
        return endpointGroupsView;
    }

    @Override
    public EndpointGroupView getEndpointGroupView() {
        return endpointGroupView;
    }

    @Override
    public EndpointGroupView getCreateEndpointGroupView() {
        return createEndpointGroupView;
    }

    @Override
    public EndpointProfilesView getEndpointProfilesView() {
        return endpointProfilesView;
    }

    @Override
    public EndpointProfileView getEndpointProfileView() {
        return endpointProfileView;
    }

    @Override
    public BaseRecordView<ProfileFilterDto, String> getProfileFilterView() {
        return profileFilterView;
    }

    @Override
    public BaseRecordView<ProfileFilterDto, String> getCreateProfileFilterView() {
        return createProfileFilterView;
    }

    @Override
    public BaseRecordView<ConfigurationRecordFormDto, RecordField> getConfigurationView() {
        return configurationView;
    }

    @Override
    public BaseRecordView<ConfigurationRecordFormDto, RecordField> getCreateConfigurationView() {
        return createConfigurationView;
    }

    @Override
    public BaseListView<TopicDto> getTopicsView() {
        return topicsView;
    }

    @Override
    public TopicView getTopicView() {
        return topicView;
    }

    @Override
    public TopicView getCreateTopicView() {
        return createTopicView;
    }

    @Override
    public SendNotificationView getSendNotificationView() {
        return sendNotificationView;
    }

    @Override
    public BaseListView<EventClassFamilyDto> getEcfsView() {
        return ecfsView;
    }

    @Override
    public EcfView getEcfView() {
        return ecfView;
    }

    @Override
    public EcfView getCreateEcfView() {
        return createEcfView;
    }

    @Override
    public EcfSchemaView getEcfSchemaView() {
        return ecfSchemaView;
    }

    @Override
    public EcfSchemaView getCreateEcfSchemaView() {
        return createEcfSchemaView;
    }

    @Override
    public BaseListView<ApplicationEventFamilyMapDto> getAefMapsView() {
        return aefMapsView;
    }

    @Override
    public AefMapView getAefMapView() {
        return aefMapView;
    }

    @Override
    public AefMapView getCreateAefMapView() {
        return createAefMapView;
    }

    @Override
    public Place getHomePlace() {
        return homePlace;
    }

    @Override
    public void setHomePlace(Place homePlace) {
        this.homePlace = homePlace;
    }

    @Override
    public BaseListView<LogAppenderDto> getAppendersView() {
        return appendersView;
    }

    @Override
    public LogAppenderView getAppenderView() {
        return appenderView;
    }

    @Override
    public LogAppenderView getCreateAppenderView() {
        return createAppenderView;
    }

    @Override
    public BaseListView<UserVerifierDto> getUserVerifiersView() {
        return userVerifiersView;
    }

    @Override
    public UserVerifierView getUserVerifierView() {
        return userVerifierView;
    }

    @Override
    public UserVerifierView getCreateUserVerifierView() {
        return createUserVerifierView;
    }

    @Override
    public UpdateUserConfigView getUpdateUserConfigView() {
        return updateUserConfigView;
    }

}