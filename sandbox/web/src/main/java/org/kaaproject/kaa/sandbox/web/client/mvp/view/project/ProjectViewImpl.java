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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.project;

import org.kaaproject.kaa.sandbox.web.client.mvp.view.ProjectView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.base.BaseViewImpl;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class ProjectViewImpl extends BaseViewImpl implements ProjectView {
    
    private Label descriptionLabel;
    private Label targetPlatform;
    private Button getSourceButton;
    private Button getBinaryButton;
    
    public ProjectViewImpl() {
        super();
        setBackEnabled(true);
    }

    @Override
    protected String getViewTitle() {
        return "";
    }

    @Override
    protected void initDetailsPanel() {
        HorizontalPanel targetPlatformPanel = new HorizontalPanel();
        Label targetPlatformLabel = new Label(Utils.constants.targetPlatform());
        targetPlatformLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        targetPlatformPanel.add(targetPlatformLabel);
        targetPlatform = new Label();
        targetPlatformPanel.add(targetPlatform);
        detailsPanel.add(targetPlatformPanel);
        
        descriptionLabel = new Label();
        descriptionLabel.addStyleName(Utils.sandboxStyle.descriptionLabel());
        detailsPanel.add(descriptionLabel);
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.getElement().getStyle().setPaddingTop(30, Unit.PX);
        
        getSourceButton = new Button(Utils.constants.getSourceCode());
        getBinaryButton = new Button(Utils.constants.getBinary());
        
        buttonsPanel.add(getSourceButton);
        
        SimplePanel spacingPanel = new SimplePanel();
        spacingPanel.setWidth("50px");
        buttonsPanel.add(spacingPanel);
        
        buttonsPanel.add(getBinaryButton);
        
        detailsPanel.add(buttonsPanel);
    }

    @Override
    protected void resetImpl() {
        targetPlatform.setText("");
        descriptionLabel.setText("");
        setTitle("");
    }

    @Override
    public HasText getTargetPlatform() {
        return targetPlatform;
    }

    @Override
    public HasText getDescription() {
        return descriptionLabel;
    }

    @Override
    public HasClickHandlers getSourceButton() {
        return getSourceButton;
    }

    @Override
    public HasClickHandlers getBinaryButton() {
        return getBinaryButton;
    }

}
