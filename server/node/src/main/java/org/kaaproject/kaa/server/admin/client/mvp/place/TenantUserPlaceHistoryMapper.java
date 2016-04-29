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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({UserProfilePlace.Tokenizer.class,
    ApplicationsPlace.Tokenizer.class,
    ApplicationPlace.Tokenizer.class,
    SdkProfilesPlace.Tokenizer.class,
    SdkProfilePlace.Tokenizer.class,
    AddSdkProfilePlace.Tokenizer.class,
    ProfileSchemasPlace.Tokenizer.class,
    ServerProfileSchemasPlace.Tokenizer.class,
    ProfileSchemaPlace.Tokenizer.class,
    ServerProfileSchemaPlace.Tokenizer.class,
    ConfigurationSchemasPlace.Tokenizer.class,
    ConfigurationSchemaPlace.Tokenizer.class,
    NotificationSchemasPlace.Tokenizer.class,
    NotificationSchemaPlace.Tokenizer.class,
    LogSchemasPlace.Tokenizer.class,
    LogSchemaPlace.Tokenizer.class,
    EndpointGroupsPlace.Tokenizer.class,
    EndpointGroupPlace.Tokenizer.class,
    EndpointProfilesPlace.Tokenizer.class,
    EndpointProfilePlace.Tokenizer.class,
    ProfileFilterPlace.Tokenizer.class,
    ConfigurationPlace.Tokenizer.class,
    TopicsPlace.Tokenizer.class,
    TopicPlace.Tokenizer.class,
    SendNotificationPlace.Tokenizer.class,
    AefMapsPlace.Tokenizer.class,
    AefMapPlace.Tokenizer.class,
    LogAppendersPlace.Tokenizer.class,
    LogAppenderPlace.Tokenizer.class,
    UserVerifiersPlace.Tokenizer.class,
    UserVerifierPlace.Tokenizer.class,
    UpdateUserConfigPlace.Tokenizer.class,
    TenantCtlSchemasPlace.Tokenizer.class,
    ApplicationCtlSchemasPlace.Tokenizer.class,
    CtlSchemaPlace.Tokenizer.class})
public interface TenantUserPlaceHistoryMapper extends PlaceHistoryMapper
{
}
