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

package org.kaaproject.kaa.server.admin.client.mvp.view;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;

public interface EndpointProfileView extends BaseDetailsView {

    SizedTextBox getKeyHash();
    SizedTextBox getId();
    SizedTextBox getAppId();
    SizedTextBox getProfileVersion();
    SizedTextBox getConfigurationHash();
    SizedTextBox getConfigurationVersion();
    SizedTextBox getNotificationVersion();
    SizedTextBox getSystemNfVersion();
    SizedTextBox getUserNfVersion();
    SizedTextBox getLogSchemaVer();
    SizedTextBox getServerHash();

//    BaseStructGrid<ProfileFilterDto> getProfileFiltersGrid();
//    BaseStructGrid<ConfigurationDto> getConfigurationsGrid();
}
