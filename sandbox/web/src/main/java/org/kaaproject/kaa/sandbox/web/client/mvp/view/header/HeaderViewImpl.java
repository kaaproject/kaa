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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderViewImpl extends Composite implements HeaderView {

    interface HeaderViewImplUiBinder extends UiBinder<Widget, HeaderViewImpl> { }
    private static HeaderViewImplUiBinder uiBinder = GWT.create(HeaderViewImplUiBinder.class);

    @UiField Image logoImage;
    @UiField FlowPanel headerTitlePanel;
    @UiField HorizontalPanel buttonsPanel;
    @UiField(provided = true) public final SandboxStyle sandboxStyle;
    
    private ActionsLabel goToKaaAdminWeb;
    private ActionsLabel goToAvroUiSandboxWeb;
    private ActionsLabel settingsLabel;
    
    public HeaderViewImpl() {
        settingsLabel = new ActionsLabel(Utils.constants.settings(), true);
        settingsLabel.setVisible(false);
        
        sandboxStyle = Utils.sandboxStyle;

        initWidget(uiBinder.createAndBindUi(this));

        logoImage.setResource(Utils.resources.kaaLogo());
        logoImage.getElement().getStyle().setPaddingLeft(20, Unit.PX);
        
        InlineLabel headerTitle = new InlineLabel(Utils.constants.sandboxHeaderTitle());
        headerTitlePanel.add(headerTitle);
        
        goToKaaAdminWeb = new ActionsLabel(Utils.constants.kaaAdminWeb(), false);
        addButton(goToKaaAdminWeb);
        
        goToAvroUiSandboxWeb = new ActionsLabel(Utils.constants.avroUiSandboxWeb(), false);
        addButton(goToAvroUiSandboxWeb);
        
        addButton(settingsLabel);
        settingsLabel.addStyleName(Utils.sandboxStyle.buttonLast());
    }
    
    private void addButton(Label label) {
        label.addStyleName(Utils.sandboxStyle.button());
        buttonsPanel.add(label);
    }

    @Override
    public void setSettingsVisible(boolean visible) {
        if (visible) {
            settingsLabel.setVisible(true);
            goToAvroUiSandboxWeb.removeStyleName(Utils.sandboxStyle.buttonLast());
        } else {
            settingsLabel.setVisible(false);
            goToAvroUiSandboxWeb.addStyleName(Utils.sandboxStyle.buttonLast());
        }
    }

    @Override
    public ActionsLabel getSettings() {
        return settingsLabel;
    }

    @Override
    public HasClickHandlers getGoToKaaAdminWeb() {
        return goToKaaAdminWeb;
    }

    @Override
    public HasClickHandlers getGoToAvroUiSandboxWeb() {
        return goToAvroUiSandboxWeb;
    }

    
}
