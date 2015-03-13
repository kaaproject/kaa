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

import java.util.List;

import org.kaaproject.kaa.sandbox.demo.projects.Feature;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.ProjectView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.base.BaseViewImpl;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class ProjectViewImpl extends BaseViewImpl implements ProjectView {
    
    private Label descriptionLabel;
    private Label targetPlatform;
    private HorizontalPanel featuresPanel;
    private Button getSourceButton;
    private Button getBinaryButton;
    
    public ProjectViewImpl() {
        super(true);
        setBackEnabled(true);
    }

    @Override
    protected String getViewTitle() {
        return "";
    }

    @Override
    protected void initCenterPanel() {
        FlexTable infoTable = new FlexTable();
        infoTable.getColumnFormatter().setWidth(0, "150px");
        infoTable.getColumnFormatter().setWidth(1, "300px");
        
        Label targetPlatformLabel = new Label(Utils.constants.targetPlatform());
        targetPlatformLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        infoTable.setWidget(0, 0, targetPlatformLabel);
        targetPlatform = new Label();
        infoTable.setWidget(0, 1, targetPlatform);
        
        Label featuresLabel = new Label(Utils.constants.features());
        featuresLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        infoTable.setWidget(1, 0, featuresLabel);
        featuresPanel = new HorizontalPanel();
        infoTable.setWidget(1, 1, featuresPanel);
        
        detailsPanel.add(infoTable);
        
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
        featuresPanel.clear();
        descriptionLabel.setText("");
        setTitle("");
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

    @Override
    public void setTargetPlatform(Platform platform) {
        targetPlatform.setText(Utils.getPlatformText(platform));
        
    }

    @Override
    public void setFeatures(List<Feature> features) {
        for (Feature feature : features) {
            Image image = new Image(Utils.getFeatureIcon(feature, false));
            Label label = new Label(Utils.getFeatureText(feature));
            featuresPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
            featuresPanel.add(image);
            featuresPanel.add(label);
            label.getElement().getStyle().setPaddingRight(15, Unit.PX);
            label.getElement().getStyle().setPaddingLeft(8, Unit.PX);
            
        }
    }

}
