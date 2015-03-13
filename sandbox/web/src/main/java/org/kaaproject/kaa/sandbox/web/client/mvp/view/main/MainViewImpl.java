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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.main;

import java.util.List;

import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.HasProjectActionEventHandlers;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.MainView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.base.BaseViewImpl;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.DemoProjectsWidget;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MainViewImpl extends BaseViewImpl implements MainView {

    protected static final int DEFAULT_TEXTBOX_SIZE = 255;

    private Anchor goToKaaAdminWeb;
    private Anchor goToAvroUiSandboxWeb;
    private DemoProjectsWidget demoProjectsView;
    
    public MainViewImpl() {
        super();
        setBackEnabled(false);
    }

    @Override
    protected String getViewTitle() {
        return "";
    }

    private Widget constructGotoLink (Anchor anchor) {
        FlowPanel panel = new FlowPanel();
        panel.getElement().getStyle().setDisplay(Display.BLOCK);
        anchor.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        anchor.getElement().getStyle().setFontSize(16, Unit.PX);
        panel.add(anchor);
        return panel;
    }
 
    @Override
    protected void initDetailsPanel() {
        HorizontalPanel linksPanel = new HorizontalPanel();
        linksPanel.setSpacing(6);
        linksPanel.getElement().getStyle().setMarginLeft(-6, Unit.PX);
        
        goToKaaAdminWeb = new Anchor(Utils.constants.kaaAdminWeb());
        Widget gotoLink = constructGotoLink(goToKaaAdminWeb);
        gotoLink.addStyleName(Utils.sandboxStyle.shadowPanel());
        linksPanel.add(gotoLink);
        
        SimplePanel spacing = new SimplePanel();
        spacing.setWidth("10px");
        linksPanel.add(spacing);
        
        goToAvroUiSandboxWeb = new Anchor(Utils.constants.avroUiSandboxWeb());
        gotoLink = constructGotoLink(goToAvroUiSandboxWeb);
        gotoLink.addStyleName(Utils.sandboxStyle.shadowPanel());
        linksPanel.add(gotoLink);
                
        detailsPanel.add(linksPanel);
        
        Label sampleApplicationsTitle = new Label(Utils.constants.sampleApplications());
        sampleApplicationsTitle.getElement().getStyle().setPaddingTop(10, Unit.PX);
        sampleApplicationsTitle.getElement().getStyle().setPaddingBottom(10, Unit.PX);
        sampleApplicationsTitle.addStyleName(Utils.sandboxStyle.contentTitleLabel());

        detailsPanel.add(sampleApplicationsTitle);
        
        demoProjectsView = new DemoProjectsWidget();
        detailsPanel.add(demoProjectsView);
    }

    @Override
    protected void resetImpl() {
        demoProjectsView.reset();
    }

    @Override
    public HasClickHandlers getGoToKaaAdminWeb() {
        return goToKaaAdminWeb;
    }
    
    @Override
    public HasClickHandlers getGoToAvroUiSandboxWeb() {
        return goToAvroUiSandboxWeb;
    }

    @Override
    public void setProjects(List<Project> projects) {
        demoProjectsView.setProjects(projects);
    }

    @Override
    public HasProjectActionEventHandlers getProjectsActionSource() {
        return demoProjectsView;
    }

}
