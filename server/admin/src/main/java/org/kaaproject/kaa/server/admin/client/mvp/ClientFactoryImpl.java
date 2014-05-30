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

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.ApplicationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointGroupView;
import org.kaaproject.kaa.server.admin.client.mvp.view.HeaderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.NavigationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TopicView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserView;
import org.kaaproject.kaa.server.admin.client.mvp.view.application.ApplicationViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.application.ApplicationsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.config.ConfigurationSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.config.ConfigurationSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.config.ConfigurationViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.endpoint.EndpointGroupViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.endpoint.EndpointGroupsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.header.HeaderViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.navigation.NavigationViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.notification.NotificationSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.notification.NotificationSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ProfileFilterViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ProfileSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.profile.ProfileSchemasViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.tenant.TenantViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.tenant.TenantsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.user.UserProfileViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.user.UserViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.user.UsersViewImpl;
import org.kaaproject.kaa.server.admin.shared.dto.TenantUserDto;
import org.kaaproject.kaa.server.admin.shared.dto.UserDto;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;

public class ClientFactoryImpl implements ClientFactory {

    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController placeController = new PlaceController(eventBus);

    private final HeaderView headerView = new HeaderViewImpl();
    private final NavigationView navigationView = new NavigationViewImpl();

    private final UserProfileView userProfileView = new UserProfileViewImpl();

    private final BaseListView<TenantUserDto> tenantsView = new TenantsViewImpl();
    private final TenantView createTenantView = new TenantViewImpl(true);
    private final TenantView tenantView = new TenantViewImpl(false);

    private final BaseListView<ApplicationDto> applicationsView = new ApplicationsViewImpl(KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));
    private final ApplicationView createApplicationView = new ApplicationViewImpl(true, KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));
    private final ApplicationView applicationView = new ApplicationViewImpl(false, KaaAdmin.checkAuthorities(KaaAuthorityDto.TENANT_ADMIN));

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

    private final BaseListView<EndpointGroupDto> endpointGroupsView = new EndpointGroupsViewImpl();
    private final EndpointGroupView endpointGroupView = new EndpointGroupViewImpl(false);
    private final EndpointGroupView createEndpointGroupView = new EndpointGroupViewImpl(true);

    private final BaseRecordView<ProfileFilterDto> profileFilterView = new ProfileFilterViewImpl(false);
    private final BaseRecordView<ProfileFilterDto> createProfileFilterView = new ProfileFilterViewImpl(true);

    private final BaseRecordView<ConfigurationDto> configurationView = new ConfigurationViewImpl(false);
    private final BaseRecordView<ConfigurationDto> createConfigurationView = new ConfigurationViewImpl(true);

    private final BaseListView<TopicDto> topicsView = new TopicsViewImpl();
    private final TopicView topicView = new TopicViewImpl(false);
    private final TopicView createTopicView = new TopicViewImpl(true);

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
    public BaseRecordView<ProfileFilterDto> getProfileFilterView() {
        return profileFilterView;
    }

    @Override
    public BaseRecordView<ProfileFilterDto> getCreateProfileFilterView() {
        return createProfileFilterView;
    }

    @Override
    public BaseRecordView<ConfigurationDto> getConfigurationView() {
        return configurationView;
    }

    @Override
    public BaseRecordView<ConfigurationDto> getCreateConfigurationView() {
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


}