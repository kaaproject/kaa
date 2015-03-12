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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.header;

import org.kaaproject.kaa.sandbox.web.client.SandboxResources.SandboxStyle;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.HeaderView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.ActionsLabel;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderViewImpl extends Composite implements HeaderView {

    interface HeaderViewImplUiBinder extends UiBinder<Widget, HeaderViewImpl> { }
    private static HeaderViewImplUiBinder uiBinder = GWT.create(HeaderViewImplUiBinder.class);

    @UiField HTMLPanel headerTitlePanel;
    @UiField(provided=true) final ActionsLabel settingsLabel;
    @UiField(provided = true) public final SandboxStyle sandboxStyle;
    
    public HeaderViewImpl() {
        settingsLabel = new ActionsLabel(Utils.constants.settings());
        sandboxStyle = Utils.sandboxStyle;
        settingsLabel.setStyleName(sandboxStyle.bAppHeaderMenu());
        initWidget(uiBinder.createAndBindUi(this));
        headerTitlePanel.getElement().setInnerHTML(Utils.constants.sandboxHeaderTitle());
    }

    @Override
    public ActionsLabel getSettingsLabel() {
        return settingsLabel;
    }

}
