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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProjectViewImpl extends BaseViewImpl implements ProjectView {
    
    private Label projectTitleLabel;
    private Image applicationImage;
    private Label descriptionLabel;
    private HorizontalPanel targetPlatformPanel;
    private HorizontalPanel featuresPanel;
    private Button backButton;
    private Button getSourceButton;
    private Button getBinaryButton;
    
    public ProjectViewImpl() {
        super(true);
        setBackEnabled(false);
    }

    @Override
    protected String getViewTitle() {
        return "";
    }

    @Override
    protected void initCenterPanel() {
        
        FlexTable flexTable = new FlexTable();
        
        flexTable.getColumnFormatter().setWidth(0, "60px");
        flexTable.getColumnFormatter().setWidth(1, "160px");
        flexTable.getColumnFormatter().setWidth(2, "700px");
        
        backButton = new Button();
        backButton.addStyleName(Utils.sandboxStyle.appBackButton());
        backButton.getElement().getStyle().setHeight(180, Unit.PX);
        setBackButton(backButton);
        
        flexTable.setWidget(0, 0, backButton);
        flexTable.getFlexCellFormatter().setRowSpan(0, 0, 2);
        flexTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        
        projectTitleLabel = new Label();
        projectTitleLabel.addStyleName(Utils.sandboxStyle.contentTitleLabel());
        projectTitleLabel.getElement().getStyle().setPaddingBottom(15, Unit.PX);
        
        flexTable.setWidget(0, 1, projectTitleLabel);
        flexTable.getFlexCellFormatter().setColSpan(0, 1, 2);
        
        applicationImage = new Image();
        flexTable.setWidget(1, 0, applicationImage);
        flexTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        
        VerticalPanel appDetailsPanel = new VerticalPanel();
        appDetailsPanel.setHeight("100%");
        
        Label detailsLabel = new Label(Utils.constants.description());
        detailsLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        appDetailsPanel.add(detailsLabel);
        
        descriptionLabel = new Label();
        descriptionLabel.addStyleName(Utils.sandboxStyle.descriptionLabel());
        appDetailsPanel.add(descriptionLabel);
        
        HorizontalPanel specPanel = new HorizontalPanel();
        specPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        specPanel.getElement().getStyle().setPaddingTop(10, Unit.PX);
        
        Label targetPlatformLabel = new Label(Utils.constants.targetPlatform());
        targetPlatformLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        specPanel.add(targetPlatformLabel);
        targetPlatformPanel = new HorizontalPanel();
        specPanel.add(targetPlatformPanel);
        targetPlatformPanel.getElement().getStyle().setPaddingRight(10, Unit.PX);
        
        Label featuresLabel = new Label(Utils.constants.features());
        featuresLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        featuresLabel.getElement().getStyle().setPaddingLeft(10, Unit.PX);
        specPanel.add(featuresLabel);
        featuresPanel = new HorizontalPanel();
        specPanel.add(featuresPanel);
        
        appDetailsPanel.add(specPanel);
        
        flexTable.setWidget(1, 1, appDetailsPanel);
        flexTable.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.getElement().getStyle().setPaddingTop(30, Unit.PX);
        
        getSourceButton = new Button(Utils.constants.getSourceCode());
        getBinaryButton = new Button(Utils.constants.getBinary());
        
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(getSourceButton);
        SimplePanel spacingPanel = new SimplePanel();
        spacingPanel.setWidth("50px");
        buttonsPanel.add(spacingPanel);
        buttonsPanel.add(getBinaryButton);
        
        flexTable.setWidget(2, 2, buttonsPanel);
        
        detailsPanel.add(flexTable);
    }

    @Override
    protected void resetImpl() {
        targetPlatformPanel.clear();
        featuresPanel.clear();
        descriptionLabel.setText("");
        projectTitleLabel.setText("");        
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
        Image image = new Image(Utils.getPlatformIcon(platform));
        Label label = new Label(Utils.getPlatformText(platform));
        targetPlatformPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        targetPlatformPanel.add(image);
        targetPlatformPanel.add(label);
        label.getElement().getStyle().setPaddingLeft(8, Unit.PX);
    }

    @Override
    public void setFeatures(List<Feature> features) {
        for (int i=0;i<features.size();i++) {
            Feature feature = features.get(i);
            Image image = new Image(Utils.getFeatureIcon(feature));
            Label label = new Label(Utils.getFeatureText(feature));
            featuresPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
            featuresPanel.add(image);
            featuresPanel.add(label);
            if (i < features.size()-1) {
                label.getElement().getStyle().setPaddingRight(10, Unit.PX);
            }
            label.getElement().getStyle().setPaddingLeft(8, Unit.PX);
            
        }
    }

    @Override
    public Image getApplicationImage() {
        return applicationImage;
    }

    @Override
    public void setProjectTitle(String title) {
        projectTitleLabel.setText(title);
        
    }

}
