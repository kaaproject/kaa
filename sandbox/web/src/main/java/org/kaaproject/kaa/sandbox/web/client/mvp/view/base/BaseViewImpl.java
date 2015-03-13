/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.base;

import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel.Type;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources.SandboxStyle;
import org.kaaproject.kaa.sandbox.web.client.layout.SimpleWidgetPanel;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.BaseView;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseViewImpl extends Composite implements BaseView {

    interface BaseViewImplUiBinder extends UiBinder<Widget, BaseViewImpl> { }
    private static BaseViewImplUiBinder uiBinder = GWT.create(BaseViewImplUiBinder.class);

    @UiField public DockLayoutPanel dockPanel;
    @UiField public VerticalPanel headerPanel;
    @UiField public HorizontalPanel backButtonPanel;
    @UiField public Button backButton;
    @UiField public Label titleLabel;
    @UiField public SimpleWidgetPanel centerPanel;
    //@UiField public VerticalPanel detailsPanel;
    @UiField (provided = true) public final AlertPanel errorPanel;
    @UiField (provided = true) public final SandboxStyle sandboxStyle;
    
    protected VerticalPanel detailsPanel;
    
    public BaseViewImpl(boolean useDetailsPanel) {
        errorPanel = new AlertPanel(Type.ERROR);
        sandboxStyle = Utils.sandboxStyle;
        initWidget(uiBinder.createAndBindUi(this));
        setTitle(getViewTitle());
        if (useDetailsPanel) {
            detailsPanel = new VerticalPanel();
            detailsPanel.setWidth("100%");
            detailsPanel.addStyleName(sandboxStyle.contentPanel());
            ScrollPanel scroll = new ScrollPanel();
            scroll.setWidth("100%");
            scroll.add(detailsPanel);
            centerPanel.setWidget(scroll);
        }
        initCenterPanel();        
        clearError();
    }

    @Override
    public void reset() {
        clearError();
        resetImpl();
    }

    @Override
    public void setTitle(String title) {
        if (title == null || title.equals("")) {
            titleLabel.setVisible(false);
        } else {
            titleLabel.setVisible(true);
            titleLabel.setText(title);
        }
        updateHeaderHeight();
    }
    
    @Override
    public void clearError() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
        updateHeaderHeight();
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        errorPanel.setVisible(true);
        updateHeaderHeight();
    }
    
    @Override
    public void setBackEnabled(boolean enabled) {
        backButtonPanel.setVisible(enabled);
        updateHeaderHeight();
    }
    
    @Override
    public HasClickHandlers getBackButton() {
        return backButton;
    }
    
    protected void updateHeaderHeight() {
        dockPanel.setWidgetSize(headerPanel, headerPanel.getOffsetHeight());
    }
    
    protected abstract String getViewTitle();
    
    protected abstract void initCenterPanel();
    
    protected abstract void resetImpl();


}
