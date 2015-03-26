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
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.HeaderMenuItems;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderViewImpl extends Composite implements HeaderView, ResizeHandler {

    interface HeaderViewImplUiBinder extends UiBinder<Widget, HeaderViewImpl> { }
    private static HeaderViewImplUiBinder uiBinder = GWT.create(HeaderViewImplUiBinder.class);

    @UiField Image logoImage;
    @UiField ResizeLayoutPanel centerResizePanel;
    @UiField HorizontalPanel centerPanel;
    @UiField FlowPanel headerTitlePanel;
    @UiField HeaderMenuItems headerMenuItems;
    @UiField(provided = true) public final SandboxStyle sandboxStyle;
    
    public HeaderViewImpl() {
        sandboxStyle = Utils.sandboxStyle;

        initWidget(uiBinder.createAndBindUi(this));

        logoImage.setResource(Utils.resources.kaaLogo());
        logoImage.getElement().getStyle().setPaddingLeft(20, Unit.PX);
        
        InlineLabel headerTitle = new InlineLabel(Utils.constants.sandboxHeaderTitle());
        headerTitlePanel.add(headerTitle);
        
        headerMenuItems.setCollapsed(false);

        centerResizePanel.addResizeHandler(this);
    }
    
    @Override
    public void onResize(ResizeEvent event) {
        collapseMenuItems(event.getWidth()<800);
    }
    
    private void collapseMenuItems(boolean collapse) {
        if (headerMenuItems.isCollapsed() != collapse) {            
            headerMenuItems.setCollapsed(collapse);
            if (collapse) {
                centerPanel.setCellWidth(headerMenuItems, "80px");
                headerTitlePanel.addStyleName(Utils.sandboxStyle.smaller());
            } else {
                centerPanel.setCellWidth(headerMenuItems, "600px");
                headerTitlePanel.removeStyleName(Utils.sandboxStyle.smaller());
            }
        }
    }

    @Override
    public HeaderMenuItems getHeaderMenuItems() {
        return headerMenuItems;
    }

}
