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

package org.kaaproject.kaa.server.admin.client.mvp.view.config;

import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.AbstractRecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.BaseRecordViewImpl;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class ConfigurationViewImpl extends BaseRecordViewImpl<ConfigurationDto> {

    public ConfigurationViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected AbstractRecordPanel<ConfigurationDto> createRecordPanel() {
        return new ConfigurationPanel();
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.configuration();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.configuration();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.configurationDetails();
    }

}
