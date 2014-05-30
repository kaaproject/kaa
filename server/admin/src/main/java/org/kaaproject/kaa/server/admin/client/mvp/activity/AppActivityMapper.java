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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.NotificationSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.NotificationSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileFilterPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileSchemasPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TopicPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TopicsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserPlace;
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
            }
            else if (clazz == TenantsPlace.class) {
                return new TenantsActivity((TenantsPlace)place, clientFactory);
            }
            else if (clazz == TenantPlace.class) {
                return new TenantActivity((TenantPlace)place, clientFactory);
            }

            else if (clazz == ApplicationsPlace.class) {
                return new ApplicationsActivity((ApplicationsPlace) place, clientFactory);
            }
            else if (clazz == ApplicationPlace.class) {
                return new ApplicationActivity((ApplicationPlace) place, clientFactory);
            }

            else if (clazz == UsersPlace.class) {
                return new UsersActivity((UsersPlace) place, clientFactory);
            }
            else if (clazz ==  UserPlace.class) {
                return new UserActivity((UserPlace) place, clientFactory);
            }

            else if (clazz == ProfileSchemasPlace.class) {
                return new ProfileSchemasActivity((ProfileSchemasPlace) place, clientFactory);
            }
            else if (clazz == ProfileSchemaPlace.class) {
                return new ProfileSchemaActivity((ProfileSchemaPlace) place, clientFactory);
            }

            else if (clazz == ConfigurationSchemasPlace.class) {
                return new ConfigurationSchemasActivity((ConfigurationSchemasPlace) place, clientFactory);
            }
            else if (clazz == ConfigurationSchemaPlace.class) {
                return new ConfigurationSchemaActivity((ConfigurationSchemaPlace) place, clientFactory);
            }

            else if (clazz == NotificationSchemasPlace.class) {
                return new NotificationSchemasActivity((NotificationSchemasPlace) place, clientFactory);
            }
            else if (clazz == NotificationSchemaPlace.class) {
                return new NotificationSchemaActivity((NotificationSchemaPlace) place, clientFactory);
            }

            else if (clazz == EndpointGroupsPlace.class) {
                return new EndpointGroupsActivity((EndpointGroupsPlace) place, clientFactory);
            }
            else if (clazz == EndpointGroupPlace.class) {
                return new EndpointGroupActivity((EndpointGroupPlace) place, clientFactory);
            }

            else if (clazz == ProfileFilterPlace.class) {
                return new ProfileFilterActivity((ProfileFilterPlace) place, clientFactory);
            }
            else if (clazz == ConfigurationPlace.class) {
                return new ConfigurationActivity((ConfigurationPlace) place, clientFactory);
            }

            else if (clazz == TopicsPlace.class) {
                return new TopicsActivity((TopicsPlace) place, clientFactory);
            }
            else if (clazz == TopicPlace.class) {
                return new TopicActivity((TopicPlace) place, clientFactory);
            }

        }

        return null;
    }
}
