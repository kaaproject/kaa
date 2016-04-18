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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;

public class ProfileFilterStructView extends TextAreaStructView<ProfileFilterDto> {
    
    private Button testFilterButton;

    public ProfileFilterStructView(HasErrorMessage hasErrorMessage) {
        super(hasErrorMessage);
        testFilterButton = new Button(Utils.constants.testFilter());
        prependButton(testFilterButton);
    }
    
    public void reset() {
        super.reset();
        testFilterButton.setVisible(false);
    }
    
    @Override
    public void setData(ProfileFilterDto struct) {
        super.setData(struct);
        testFilterButton.setVisible(struct.getStatus() == UpdateStatus.INACTIVE);
    }
    
    public HasClickHandlers getTestFilterButton() {
        return testFilterButton;
    }

}
