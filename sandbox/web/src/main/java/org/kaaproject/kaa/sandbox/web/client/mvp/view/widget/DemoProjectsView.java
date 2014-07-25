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
package org.kaaproject.kaa.sandbox.web.client.mvp.view.widget;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.HasProjectActionEventHandlers;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectAction;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEventHandler;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class DemoProjectsView extends FlexTable implements HasProjectActionEventHandlers {
    
    private List<HandlerRegistration> registrations = new ArrayList<>();
    
    public DemoProjectsView() {
        setWidth("500px");
    }
    
    public void reset() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
        removeAllRows();
    }
    
    public void setProjects(List<Project> projects) {
        reset();
        int row = 0;
        for (final Project project : projects) {
            Label projectTitleLabel = new Label(project.getName());
            projectTitleLabel.addStyleName("b-app-content-title-label");
            if (row>0) {
                projectTitleLabel.getElement().getStyle().setPaddingTop(10, Unit.PX);
            }
            setWidget(row++, 0,  projectTitleLabel);
            Label platformLabel = new Label("Platform: " + project.getPlatform().name());
            platformLabel.getElement().getStyle().setFontWeight(FontWeight.NORMAL);
            setWidget(row++, 0,  platformLabel);
            HTML descLabel = new HTML(project.getDescription());
            setWidget(row++, 0,  descLabel);
            HorizontalPanel buttonsPanel = new HorizontalPanel();
            buttonsPanel.setSpacing(5);
            setWidget(row++, 0,  buttonsPanel);
            Button getSourceCodeButton = new Button("Get source code");
            buttonsPanel.add(getSourceCodeButton);
            Button getBinaryButton = new Button("Get binary");
            buttonsPanel.add(getBinaryButton);
            
            registrations.add(getSourceCodeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String projectId = project.getId();
                    ProjectActionEvent action = new ProjectActionEvent(projectId, ProjectAction.GET_SOURCE_CODE);
                    fireEvent(action);
                }
                
            }));
            
            registrations.add(getBinaryButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String projectId = project.getId();
                    ProjectActionEvent action = new ProjectActionEvent(projectId, ProjectAction.GET_BINARY);
                    fireEvent(action);
                }
            }));
        }
    }

    @Override
    public HandlerRegistration addProjectActionHandler(
            ProjectActionEventHandler handler) {
        return this.addHandler(handler, ProjectActionEvent.getType());
    }

}
