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
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.AefMapView;
import org.kaaproject.kaa.server.admin.client.mvp.view.ApplicationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointGroupView;
import org.kaaproject.kaa.server.admin.client.mvp.view.HeaderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.NavigationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TopicView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserView;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;

public interface ClientFactory {
    EventBus getEventBus();
    PlaceController getPlaceController();

    HeaderView getHeaderView();
    NavigationView getNavigationView();

    UserProfileView getUserProfileView();

    BaseListView<TenantUserDto> getTenantsView();
    TenantView getCreateTenantView();
    TenantView getTenantView();

    BaseListView<ApplicationDto> getApplicationsView();
    ApplicationView getCreateApplicationView();
    ApplicationView getApplicationView();

    BaseListView<UserDto> getUsersView();
    UserView getCreateUserView();
    UserView getUserView();

    BaseListView<ProfileSchemaDto> getProfileSchemasView();
    BaseSchemaView getProfileSchemaView();
    BaseSchemaView getCreateProfileSchemaView();

    BaseListView<ConfigurationSchemaDto> getConfigurationSchemasView();
    BaseSchemaView getConfigurationSchemaView();
    BaseSchemaView getCreateConfigurationSchemaView();

    BaseListView<NotificationSchemaDto> getNotificationSchemasView();
    BaseSchemaView getNotificationSchemaView();
    BaseSchemaView getCreateNotificationSchemaView();

    BaseListView<LogSchemaDto> getLogSchemasView();
    BaseSchemaView getLogSchemaView();
    BaseSchemaView getCreateLogSchemaView();

    BaseListView<EndpointGroupDto> getEndpointGroupsView();
    EndpointGroupView getEndpointGroupView();
    EndpointGroupView getCreateEndpointGroupView();

    BaseRecordView<ProfileFilterDto> getProfileFilterView();
    BaseRecordView<ProfileFilterDto> getCreateProfileFilterView();

    BaseRecordView<ConfigurationDto> getConfigurationView();
    BaseRecordView<ConfigurationDto> getCreateConfigurationView();

    BaseListView<TopicDto> getTopicsView();
    TopicView getTopicView();
    TopicView getCreateTopicView();

    BaseListView<EventClassFamilyDto> getEcfsView();
    EcfView getEcfView();
    EcfView getCreateEcfView();
    
    EcfSchemaView getEcfSchemaView();
    
    BaseListView<ApplicationEventFamilyMapDto> getAefMapsView();
    AefMapView getAefMapView();
    AefMapView getCreateAefMapView();

}
