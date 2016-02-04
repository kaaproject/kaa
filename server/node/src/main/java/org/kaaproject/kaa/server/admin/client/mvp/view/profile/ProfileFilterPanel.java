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

package org.kaaproject.kaa.server.admin.client.mvp.view.profile;

import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.AbstractRecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.BaseStructView;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.ProfileFilterStructView;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;

public class ProfileFilterPanel extends AbstractRecordPanel<ProfileFilterDto, String>{

    public ProfileFilterPanel(HasErrorMessage hasErrorMessage) {
        super(hasErrorMessage);
    }

    @Override
    protected String bodyLabelText() {
        return Utils.constants.filterBody();
    }

    @Override
    public void setReadOnly() {
        setActiveReadOnly();
        setInactiveReadOnly();
    }

    @Override
    protected BaseStructView<ProfileFilterDto, String> createStructView(HasErrorMessage hasErrorMessage) {
        return new ProfileFilterStructView(hasErrorMessage);
    }
    
    public HasClickHandlers getTestFilterButton() {
        return ((ProfileFilterStructView)inactivePanel).getTestFilterButton();
    }

}
