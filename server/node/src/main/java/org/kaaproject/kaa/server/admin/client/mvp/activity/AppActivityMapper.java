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

import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AefMapPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.AefMapsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationCtlSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.GeneralPropertiesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.AddSdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppenderPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogAppendersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.LogSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.MailPropertiesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.NotificationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.NotificationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileFilterPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SendNotificationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ServerProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ServerProfileSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SystemCtlSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantCtlSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TopicPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TopicsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UpdateUserConfigPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifierPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifiersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UsersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserProfilePlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class AppActivityMapper implements ActivityMapper {

    private final ClientFactory clientFactory;

    public AppActivityMapper(ClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {

        if (place != null) {
            Class<? extends Place> clazz = place.getClass();
            if (clazz == UserProfilePlace.class) {
                return new UserProfileActivity((UserProfilePlace)place, clientFactory);
            } else if (clazz == GeneralPropertiesPlace.class) {
                return new GeneralPropertiesActivity((GeneralPropertiesPlace)place, clientFactory);
            } else if (clazz == MailPropertiesPlace.class) {
                return new MailPropertiesActivity((MailPropertiesPlace)place, clientFactory);
            } else if (clazz == TenantsPlace.class) {
                return new TenantsActivity((TenantsPlace)place, clientFactory);
            } else if (clazz == TenantPlace.class) {
                return new TenantActivity((TenantPlace)place, clientFactory);
            } else if (clazz == ApplicationsPlace.class) {
                return new ApplicationsActivity((ApplicationsPlace) place, clientFactory);
            } else if (clazz == ApplicationPlace.class) {
                return new ApplicationActivity((ApplicationPlace) place, clientFactory);
            } else if (clazz == SdkProfilesPlace.class) {
                return new SdkProfilesActivity((SdkProfilesPlace) place, clientFactory);
            } else if (clazz == SdkProfilePlace.class) {
                return new SdkProfileActivity((SdkProfilePlace) place, clientFactory);
            } else if (clazz == AddSdkProfilePlace.class) {
                return new AddSdkProfileActivity((AddSdkProfilePlace) place, clientFactory);
            } else if (clazz == UsersPlace.class) {
                return new UsersActivity((UsersPlace) place, clientFactory);
            } else if (clazz ==  UserPlace.class) {
                return new UserActivity((UserPlace) place, clientFactory);
            } else if (clazz == EcfsPlace.class) {
                return new EcfsActivity((EcfsPlace) place, clientFactory);
            } else if (clazz ==  EcfPlace.class) {
                return new EcfActivity((EcfPlace) place, clientFactory);
            } else if (clazz ==  EcfSchemaPlace.class) {
                return new EcfSchemaActivity((EcfSchemaPlace) place, clientFactory);
            } else if (clazz == ProfileSchemasPlace.class) {
                return new ProfileSchemasActivity((ProfileSchemasPlace) place, clientFactory);
            } else if (clazz == ServerProfileSchemasPlace.class) {
                return new ServerProfileSchemasActivity((ServerProfileSchemasPlace) place, clientFactory);
            } else if (clazz == ProfileSchemaPlace.class) {
                return new ProfileSchemaActivity((ProfileSchemaPlace) place, clientFactory);
            } else if (clazz == ServerProfileSchemaPlace.class) {
                return new ServerProfileSchemaActivity((ServerProfileSchemaPlace) place, clientFactory);
            } else if (clazz == ConfigurationSchemasPlace.class) {
                return new ConfigurationSchemasActivity((ConfigurationSchemasPlace) place, clientFactory);
            } else if (clazz == ConfigurationSchemaPlace.class) {
                return new ConfigurationSchemaActivity((ConfigurationSchemaPlace) place, clientFactory);
            } else if (clazz == NotificationSchemasPlace.class) {
                return new NotificationSchemasActivity((NotificationSchemasPlace) place, clientFactory);
            } else if (clazz == NotificationSchemaPlace.class) {
                return new NotificationSchemaActivity((NotificationSchemaPlace) place, clientFactory);
            } else if (clazz == LogSchemasPlace.class) {
                return new LogSchemasActivity((LogSchemasPlace) place, clientFactory);
            } else if (clazz == LogSchemaPlace.class) {
                return new LogSchemaActivity((LogSchemaPlace) place, clientFactory);
            } else if (clazz == EndpointGroupsPlace.class) {
                return new EndpointGroupsActivity((EndpointGroupsPlace) place, clientFactory);
            } else if (clazz == EndpointGroupPlace.class) {
                return new EndpointGroupActivity((EndpointGroupPlace) place, clientFactory);
            } else if (clazz ==  EndpointProfilesPlace.class) {
                return new EndpointProfilesActivity((EndpointProfilesPlace) place, clientFactory);
            } else if (clazz ==  EndpointProfilePlace.class) {
                return new EndpointProfileActivity((EndpointProfilePlace) place, clientFactory);
            } else if (clazz == ProfileFilterPlace.class) {
                return new ProfileFilterActivity((ProfileFilterPlace) place, clientFactory);
            } else if (clazz == ConfigurationPlace.class) {
                return new ConfigurationActivity((ConfigurationPlace) place, clientFactory);
            } else if (clazz == TopicsPlace.class) {
                return new TopicsActivity((TopicsPlace) place, clientFactory);
            } else if (clazz == TopicPlace.class) {
                return new TopicActivity((TopicPlace) place, clientFactory);
            } else if (clazz == SendNotificationPlace.class) {
                return new SendNotificationActivity((SendNotificationPlace) place, clientFactory);
            } else if (clazz == AefMapsPlace.class) {
                return new AefMapsActivity((AefMapsPlace) place, clientFactory);
            } else if (clazz ==  AefMapPlace.class) {
                return new AefMapActivity((AefMapPlace) place, clientFactory);
            } else if (clazz ==  LogAppendersPlace.class) {
                return new LogAppendersActivity((LogAppendersPlace) place, clientFactory);
            } else if (clazz ==  LogAppenderPlace.class) {
                return new LogAppenderActivity((LogAppenderPlace) place, clientFactory);
            } else if (clazz ==  UserVerifiersPlace.class) {
                return new UserVerifiersActivity((UserVerifiersPlace) place, clientFactory);
            } else if (clazz ==  UserVerifierPlace.class) {
                return new UserVerifierActivity((UserVerifierPlace) place, clientFactory);
            } else if (clazz ==  UpdateUserConfigPlace.class) {
                return new UpdateUserConfigActivity((UpdateUserConfigPlace) place, clientFactory);
            } else if (clazz == SystemCtlSchemasPlace.class) {
                return new SystemCtlSchemasActivity((SystemCtlSchemasPlace) place, clientFactory);
            } else if (clazz == TenantCtlSchemasPlace.class) {
                return new TenantCtlSchemasActivity((TenantCtlSchemasPlace) place, clientFactory);
            } else if (clazz == ApplicationCtlSchemasPlace.class) {
                return new ApplicationCtlSchemasActivity((ApplicationCtlSchemasPlace) place, clientFactory);
            } else if (clazz == CtlSchemaPlace.class) {
                return new CtlSchemaActivity((CtlSchemaPlace) place, clientFactory);
            }
        }

        return null;
    }
}
