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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.widget;

import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.HasProjectActionEventHandlers;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectAction;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEventHandler;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DemoProjectWidget extends VerticalPanel implements HasProjectActionEventHandlers {
    
    private Image platformImage;
    private Anchor projectTitle;
    private Anchor getSourceAnchor;
    private Anchor getBinaryAnchor;
    
    private Project project;

    public DemoProjectWidget() {
        super();
        addStyleName(Utils.sandboxStyle.demoProjectWidget());        
        
        VerticalPanel detailsPanel = new VerticalPanel();
        detailsPanel.addStyleName(Utils.sandboxStyle.details());
        detailsPanel.sinkEvents(Event.ONCLICK);
                
        detailsPanel.setWidth("100%");
        
        AbsolutePanel layoutPanel = new AbsolutePanel();
        
        
        VerticalPanel platformImagePanel  = new VerticalPanel();
        platformImagePanel.addStyleName(Utils.sandboxStyle.detailsInnerTop());
        platformImagePanel.setWidth("100%");
        platformImage = new Image();
        platformImagePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        platformImagePanel.add(platformImage);
        
        layoutPanel.add(platformImagePanel);
        SimplePanel platformImageHoverPanel = new SimplePanel();
        platformImageHoverPanel.addStyleName(Utils.sandboxStyle.platformImageHover());
        layoutPanel.add(platformImageHoverPanel);
        platformImageHoverPanel.setSize("100%", "100%");
        layoutPanel.setSize("100%", "100%");
        
        detailsPanel.add(layoutPanel);
        SimplePanel titlePanel = new SimplePanel();
        titlePanel.addStyleName(Utils.sandboxStyle.detailsInnerCenter());
        projectTitle = new Anchor();
        projectTitle.addStyleName(Utils.sandboxStyle.title());
        titlePanel.add(projectTitle);
        
        detailsPanel.add(titlePanel);

        add(detailsPanel);
        
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setWidth("100%");
        buttonsPanel.addStyleName(Utils.sandboxStyle.detailsInnerBottom());
        getSourceAnchor = new Anchor(Utils.constants.getSourceCode());
        getSourceAnchor.addStyleName(Utils.sandboxStyle.action());
        getSourceAnchor.getElement().getStyle().setMarginRight(20, Unit.PX);
        getBinaryAnchor = new Anchor(Utils.constants.getBinary());
        getBinaryAnchor.addStyleName(Utils.sandboxStyle.action());
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(getSourceAnchor);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        buttonsPanel.add(getBinaryAnchor);
        add(buttonsPanel);
 
        
        detailsPanel.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (project != null) {
                    ProjectActionEvent action = new ProjectActionEvent(project.getId(), ProjectAction.OPEN_DETAILS);
                    fireEvent(action);
                }
            }
        }, ClickEvent.getType());
        
        getSourceAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (project != null) {
                    ProjectActionEvent action = new ProjectActionEvent(project.getId(), ProjectAction.GET_SOURCE_CODE);
                    fireEvent(action);
                }
            }
        });

        getBinaryAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (project != null) {
                    ProjectActionEvent action = new ProjectActionEvent(project.getId(), ProjectAction.GET_BINARY);
                    fireEvent(action);
                }
            }
        });
    }
    
    public void setProject(Project project) {
        this.project = project;
        switch (project.getPlatform()) {
        case ANDROID:
            platformImage.setResource(Utils.resources.android());
            break;
        case JAVA:
            platformImage.setResource(Utils.resources.java());
            break;
        default:
            break;
        }
        projectTitle.setText(project.getName());
        projectTitle.setTitle(project.getName());
    }

    @Override
    public HandlerRegistration addProjectActionHandler(
            ProjectActionEventHandler handler) {
        return this.addHandler(handler, ProjectActionEvent.getType());
    }
    
}
