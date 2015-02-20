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

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ApplicationDto;
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
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.AefMapView;
import org.kaaproject.kaa.server.admin.client.mvp.view.ApplicationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePropertiesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfView;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointGroupView;
import org.kaaproject.kaa.server.admin.client.mvp.view.GenerateSdkView;
import org.kaaproject.kaa.server.admin.client.mvp.view.HeaderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.NavigationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.SendNotificationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;
import org.kaaproject.kaa.server.admin.client.mvp.view.TopicView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserVerifierView;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserView;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public interface ClientFactory {
    EventBus getEventBus();
    PlaceController getPlaceController();

    HeaderView getHeaderView();
    NavigationView getNavigationView();

    UserProfileView getUserProfileView();
    
    BasePropertiesView getGeneralPropertiesView();
    
    BasePropertiesView getMailPropertiesView();

    BaseListView<TenantUserDto> getTenantsView();
    TenantView getCreateTenantView();
    TenantView getTenantView();

    BaseListView<ApplicationDto> getApplicationsView();
    ApplicationView getCreateApplicationView();
    ApplicationView getApplicationView();
    
    GenerateSdkView getGenerateSdkView();

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

    BaseRecordView<ProfileFilterDto, String> getProfileFilterView();
    BaseRecordView<ProfileFilterDto, String> getCreateProfileFilterView();

    BaseRecordView<ConfigurationRecordFormDto, RecordField> getConfigurationView();
    BaseRecordView<ConfigurationRecordFormDto, RecordField> getCreateConfigurationView();

    BaseListView<TopicDto> getTopicsView();
    TopicView getTopicView();
    TopicView getCreateTopicView();
    
    SendNotificationView getSendNotificationView();

    BaseListView<EventClassFamilyDto> getEcfsView();
    EcfView getEcfView();
    EcfView getCreateEcfView();

    EcfSchemaView getEcfSchemaView();
    EcfSchemaView getCreateEcfSchemaView();

    BaseListView<ApplicationEventFamilyMapDto> getAefMapsView();
    AefMapView getAefMapView();
    AefMapView getCreateAefMapView();

    Place getHomePlace();
    void setHomePlace(Place homePlace);

    BaseListView<LogAppenderDto> getAppendersView();
    LogAppenderView getAppenderView();
    LogAppenderView getCreateAppenderView();

    BaseListView<UserVerifierDto> getUserVerifiersView();
    UserVerifierView getUserVerifierView();
    UserVerifierView getCreateUserVerifierView();

}
