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
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEventHandler;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;

public class DemoProjectsWidget extends FlowPanel implements HasProjectActionEventHandlers, ProjectActionEventHandler {
    
    private List<HandlerRegistration> registrations = new ArrayList<>();
    
    public DemoProjectsWidget() {
        super();
        addStyleName(Utils.sandboxStyle.demoProjectsWidget());
    }
    
    public void reset() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
        clear();
    }
    
    public void setProjects(List<Project> projects) {
        reset();
        for (Project project : projects) {
            DemoProjectWidget demoProjectWidget = new DemoProjectWidget();
            demoProjectWidget.setProject(project);
            add(demoProjectWidget);
            registrations.add(demoProjectWidget.addProjectActionHandler(this));
        }
    }

    @Override
    public HandlerRegistration addProjectActionHandler(
            ProjectActionEventHandler handler) {
        return this.addHandler(handler, ProjectActionEvent.getType());
    }

    @Override
    public void onProjectAction(ProjectActionEvent event) {
        fireEvent(event);
    }

}
