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

package org.kaaproject.kaa.server.admin.client.mvp;

import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
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
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePropertiesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.ConfigurationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemasView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointGroupView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfilesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.HeaderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.NavigationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.ProfileFilterView;
import org.kaaproject.kaa.server.admin.client.mvp.view.SdkProfileView;
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
import org.kaaproject.kaa.server.admin.client.mvp.view.ctl.ApplicationCtlSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.ctl.ApplicationCtlSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.ctl.CtlSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.ctl.SystemCtlSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.ctl.TenantCtlSchemasViewImpl;
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
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ServerProfileSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ServerProfileSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.sdk.AddSdkProfileViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.sdk.SdkProfileViewImpl;
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

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class ClientFactoryImpl implements ClientFactory {

    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController placeController = new PlaceController(eventBus);

    private final HeaderView headerView = new HeaderViewImpl();
    private final NavigationView navigationView = new NavigationViewImpl();

    private final UserProfileView userProfileView = new UserProfileViewImpl();

    private final BasePropertiesView generalPropertiesView = new GeneralPropertiesViewImpl();

    private final BasePropertiesView mailPropertiesView = new MailPropertiesViewImpl();

    private final BaseListView<TenantDto> tenantsView = new TenantsViewImpl();
    private final TenantView createTenantView = new TenantViewImpl(true);
    private final TenantView tenantView = new TenantViewImpl(false);

    private final BaseListView<ApplicationDto> applicationsView = new ApplicationsViewImpl(KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));
    private final ApplicationView createApplicationView = new ApplicationViewImpl(true, KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));
    private final ApplicationView applicationView = new ApplicationViewImpl(false, KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));

    private final BaseListView<SdkProfileDto> sdkProfilesView = new SdkProfilesViewImpl();
    private final SdkProfileView sdkProfileView = new SdkProfileViewImpl();
    private final AddSdkProfileView generateSdkView = new AddSdkProfileViewImpl();

    private final BaseListView<UserDto> usersView = new UsersViewImpl();
    private final UserView createUserView = new UserViewImpl(true);
    private final UserView userView = new UserViewImpl(false);

    private final BaseListView<EndpointProfileSchemaDto> profileSchemasView = new ProfileSchemasViewImpl();
    private final BaseCtlSchemaView profileSchemaView = new ProfileSchemaViewImpl(false);
    private final BaseCtlSchemaView createProfileSchemaView = new ProfileSchemaViewImpl(true);

    private final BaseListView<ServerProfileSchemaDto> serverProfileSchemasView = new ServerProfileSchemasViewImpl();
    private final BaseCtlSchemaView serverProfileSchemaView = new ServerProfileSchemaViewImpl(false);
    private final BaseCtlSchemaView createServerProfileSchemaView = new ServerProfileSchemaViewImpl(true);

    private final BaseListView<ConfigurationSchemaDto> configurationSchemasView = new ConfigurationSchemasViewImpl();
    private final BaseCtlSchemaView configurationSchemaView = new ConfigurationSchemaViewImpl(false);
    private final BaseCtlSchemaView createConfigurationSchemaView = new ConfigurationSchemaViewImpl(true);

    private final BaseListView<NotificationSchemaDto> notificationSchemasView = new NotificationSchemasViewImpl();
    private final BaseCtlSchemaView notificationSchemaView = new NotificationSchemaViewImpl(false);
    private final BaseCtlSchemaView createNotificationSchemaView = new NotificationSchemaViewImpl(true);

    private final BaseListView<LogSchemaDto> logSchemasView = new LogSchemasViewImpl();
    private final BaseCtlSchemaView logSchemaView = new LogSchemaViewImpl(false);
    private final BaseCtlSchemaView createLogSchemaView = new LogSchemaViewImpl(true);

    private final BaseListView<EndpointGroupDto> endpointGroupsView = new EndpointGroupsViewImpl();
    private final EndpointGroupView endpointGroupView = new EndpointGroupViewImpl(false);
    private final EndpointGroupView createEndpointGroupView = new EndpointGroupViewImpl(true);

    private final EndpointProfilesView endpointProfilesView = new EndpointProfilesViewImpl();
    private final EndpointProfileView endpointProfileView = new EndpointProfileViewImpl();

    private final ProfileFilterView profileFilterView = new ProfileFilterViewImpl(false);
    private final ProfileFilterView createProfileFilterView = new ProfileFilterViewImpl(true);

    private final ConfigurationView configurationView = new ConfigurationViewImpl(false);
    private final ConfigurationView createConfigurationView = new ConfigurationViewImpl(true);

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
    
    private final CtlSchemasView systemCtlSchemasView = new SystemCtlSchemasViewImpl();
    private final CtlSchemasView tenantCtlSchemasView = new TenantCtlSchemasViewImpl();
    private final CtlSchemasView applicationCtlSchemasView = new ApplicationCtlSchemasViewImpl();
    
    private final CtlSchemaView createCtlSchemaView = new CtlSchemaViewImpl(true, true);
    private final CtlSchemaView editCtlSchemaView = new CtlSchemaViewImpl(false, true);
    private final CtlSchemaView editApplicationCtlSchemaView = new ApplicationCtlSchemaViewImpl(false, true);
    private final CtlSchemaView viewCtlSchemaView = new CtlSchemaViewImpl(false, false);

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
    public BaseListView<TenantDto> getTenantsView() {
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
    public SdkProfileView getSdkProfileView() {
        return sdkProfileView;
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
    public BaseListView<EndpointProfileSchemaDto> getProfileSchemasView() {
        return profileSchemasView;
    }

    @Override
    public BaseCtlSchemaView getProfileSchemaView() {
        return profileSchemaView;
    }

    @Override
    public BaseCtlSchemaView getCreateProfileSchemaView() {
        return createProfileSchemaView;
    }

    @Override
    public BaseListView<ServerProfileSchemaDto> getServerProfileSchemasView() {
        return serverProfileSchemasView;
    }

    @Override
    public BaseCtlSchemaView getServerProfileSchemaView() {
        return serverProfileSchemaView;
    }

    @Override
    public BaseCtlSchemaView getCreateServerProfileSchemaView() {
        return createServerProfileSchemaView;
    }

    @Override
    public BaseListView<ConfigurationSchemaDto> getConfigurationSchemasView() {
        return configurationSchemasView;
    }

    @Override
    public BaseCtlSchemaView getConfigurationSchemaView() {
        return configurationSchemaView;
    }

    @Override
    public BaseCtlSchemaView getCreateConfigurationSchemaView() {
        return createConfigurationSchemaView;
    }

    @Override
    public BaseListView<NotificationSchemaDto> getNotificationSchemasView() {
        return notificationSchemasView;
    }

    @Override
    public BaseCtlSchemaView getNotificationSchemaView() {
        return notificationSchemaView;
    }

    @Override
    public BaseCtlSchemaView getCreateNotificationSchemaView() {
        return createNotificationSchemaView;
    }

    @Override
    public BaseListView<LogSchemaDto> getLogSchemasView() {
        return logSchemasView;
    }

    @Override
    public BaseCtlSchemaView getLogSchemaView() {
        return logSchemaView;
    }

    @Override
    public BaseCtlSchemaView getCreateLogSchemaView() {
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
    public ProfileFilterView getProfileFilterView() {
        return profileFilterView;
    }

    @Override
    public ProfileFilterView getCreateProfileFilterView() {
        return createProfileFilterView;
    }

    @Override
    public ConfigurationView getConfigurationView() {
        return configurationView;
    }

    @Override
    public ConfigurationView getCreateConfigurationView() {
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

    @Override
    public CtlSchemaView getCreateCtlSchemaView() {
        return createCtlSchemaView;
    }

    @Override
    public CtlSchemaView getEditCtlSchemaView() {
        return editCtlSchemaView;
    }

    @Override
    public CtlSchemaView getEditApplicationCtlSchemaView() {
        return editApplicationCtlSchemaView;
    }

    @Override
    public CtlSchemaView getViewCtlSchemaView() {
        return viewCtlSchemaView;
    }

    @Override
    public CtlSchemasView getSystemCtlSchemasView() {
        return systemCtlSchemasView;
    }

    @Override
    public CtlSchemasView getTenantCtlSchemasView() {
        return tenantCtlSchemasView;
    }

    @Override
    public CtlSchemasView getApplicationCtlSchemasView() {
        return applicationCtlSchemasView;
    }

}