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

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHTML;
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
    private HTML projectDetailsPanel;
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
        flexTable.getFlexCellFormatter().setHeight(0, 1, "51px");
        
        VerticalPanel iconAndButtons = new VerticalPanel();
        iconAndButtons.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        
        applicationImage = new Image();
        iconAndButtons.add(applicationImage);
        
        VerticalPanel buttonsPanel = new VerticalPanel();
        buttonsPanel.getElement().getStyle().setPaddingTop(30, Unit.PX);
        buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        buttonsPanel.setWidth("100%");
        
        getSourceButton = new Button(Utils.constants.getSourceCode());
        getSourceButton.setSize("128px", "32px");
        getSourceButton.getElement().getStyle().setPaddingTop(0, Unit.PX);
        getSourceButton.getElement().getStyle().setPaddingBottom(0, Unit.PX);
        getSourceButton.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        getBinaryButton = new Button(Utils.constants.getBinary());
        getBinaryButton.setSize("128px", "32px");
        getBinaryButton.getElement().getStyle().setPaddingTop(0, Unit.PX);
        getBinaryButton.getElement().getStyle().setPaddingBottom(0, Unit.PX);
        getBinaryButton.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        
        buttonsPanel.add(getSourceButton);
        SimplePanel spacingPanel = new SimplePanel();
        spacingPanel.setHeight("10px");
        buttonsPanel.add(spacingPanel);
        buttonsPanel.add(getBinaryButton);
        
        iconAndButtons.add(buttonsPanel);

        flexTable.setWidget(1, 0, iconAndButtons);
        flexTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        
        FlexTable appDetailsPanel = new FlexTable();
        appDetailsPanel.getColumnFormatter().setWidth(0, "90px");
        appDetailsPanel.getColumnFormatter().setWidth(1, "610px");
        
        descriptionLabel = new Label();
        descriptionLabel.addStyleName(Utils.sandboxStyle.descriptionLabel());
        appDetailsPanel.setWidget(0, 0, descriptionLabel);
        appDetailsPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
        
        Label targetPlatformLabel = new Label(Utils.constants.targetPlatform());
        targetPlatformLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        appDetailsPanel.setWidget(1, 0, targetPlatformLabel);
        appDetailsPanel.getFlexCellFormatter().getElement(1, 0).getStyle().setPaddingTop(15, Unit.PX);
        appDetailsPanel.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        
        
        targetPlatformPanel = new HorizontalPanel();
        appDetailsPanel.setWidget(1, 1, targetPlatformPanel);
        appDetailsPanel.getFlexCellFormatter().getElement(1, 1).getStyle().setPaddingTop(15, Unit.PX);
        appDetailsPanel.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_MIDDLE);
        
        Label featuresLabel = new Label(Utils.constants.features());
        featuresLabel.addStyleName(Utils.sandboxStyle.contentLabel());
        appDetailsPanel.setWidget(2, 0, featuresLabel);
        appDetailsPanel.getFlexCellFormatter().getElement(2, 0).getStyle().setPaddingTop(10, Unit.PX);
        appDetailsPanel.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        
        featuresPanel = new HorizontalPanel();
        appDetailsPanel.setWidget(2, 1, featuresPanel);
        appDetailsPanel.getFlexCellFormatter().getElement(2, 1).getStyle().setPaddingTop(10, Unit.PX);
        appDetailsPanel.getFlexCellFormatter().setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_MIDDLE);

        projectDetailsPanel = new HTML();
        projectDetailsPanel.addStyleName(Utils.sandboxStyle.projectDetails());
        projectDetailsPanel.getElement().getStyle().setPaddingTop(15, Unit.PX);
        
        appDetailsPanel.setWidget(3, 0, projectDetailsPanel);
        appDetailsPanel.getFlexCellFormatter().setColSpan(3, 0, 2);

        flexTable.setWidget(1, 1, appDetailsPanel);
        flexTable.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        
        detailsPanel.add(flexTable);
    }

    @Override
    protected void resetImpl() {
        targetPlatformPanel.clear();
        featuresPanel.clear();
        descriptionLabel.setText("");
        projectDetailsPanel.setHTML("");
        projectTitleLabel.setText("");        
        applicationImage.setUrl("");
    }

    @Override
    public HasText getDescription() {
        return descriptionLabel;
    }

    @Override
    public HasHTML getDetails() {
        return projectDetailsPanel;
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
        image.setTitle(Utils.getPlatformText(platform));
        image.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        Label label = new Label(Utils.getPlatformText(platform));
        targetPlatformPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        targetPlatformPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        targetPlatformPanel.add(image);
        targetPlatformPanel.setCellWidth(image, "32px");
        targetPlatformPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        targetPlatformPanel.add(label);
        label.getElement().getStyle().setPaddingLeft(8, Unit.PX);
    }

    @Override
    public void setFeatures(List<Feature> features) {
        for (int i=0;i<features.size();i++) {
            Feature feature = features.get(i);
            Image image = new Image(Utils.getFeatureIcon(feature));
            image.setTitle(Utils.getFeatureText(feature));
            image.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
            Label label = new Label(Utils.getFeatureText(feature));
            featuresPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
            featuresPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            featuresPanel.add(image);
            featuresPanel.setCellWidth(image, "32px");
            featuresPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
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
