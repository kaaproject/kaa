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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.sandbox.demo.projects.Feature;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources.SandboxStyle;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.HasProjectActionEventHandlers;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEventHandler;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DemoProjectsWidget extends Composite implements HasProjectActionEventHandlers, 
            ProjectActionEventHandler, ValueChangeHandler<Boolean> {
    
    interface DemoProjectsWidgetUiBinder extends UiBinder<Widget, DemoProjectsWidget> { }
    private static DemoProjectsWidgetUiBinder uiBinder = GWT.create(DemoProjectsWidgetUiBinder.class);
    
    private List<HandlerRegistration> registrations = new ArrayList<>();
    
    @UiField public DockLayoutPanel dockPanel;
    @UiField public HorizontalPanel headerPanel;
    @UiField public Label titleLabel;
    @UiField public HorizontalPanel filterPanel;
    @UiField public VerticalPanel demoProjectSectionsPanel;
    @UiField (provided = true) public final SandboxStyle sandboxStyle;
    
    private List<Project> projects;
    
    private Map<Platform, DemoProjectsPlatformSection> demoProjectPlatformSectionsMap;
    private DemoProjectsFeatureFilter filter;
    
    public DemoProjectsWidget() {
        super();
        sandboxStyle = Utils.sandboxStyle;
        initWidget(uiBinder.createAndBindUi(this));
        demoProjectPlatformSectionsMap = new HashMap<>();
        for (Platform platform : Platform.values()) {
            DemoProjectsPlatformSection section = new DemoProjectsPlatformSection(platform);
            demoProjectPlatformSectionsMap.put(platform, section);
            demoProjectSectionsPanel.add(section);
        }
        filter = new DemoProjectsFeatureFilter();
        filterPanel.add(filter);
        filter.addValueChangeHandler(this);
    }
    
    public void reset() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
        for (DemoProjectsPlatformSection section : demoProjectPlatformSectionsMap.values()) {
            section.reset();
        }
    }
    
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    
    public void setProjects(List<Project> projects) {
        this.projects = projects;
        updateProjects();
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
        updateProjects();
    }

    void updateProjects() {
        reset();
        Map<Feature, FeatureButton> filterMap = filter.getFilterMap();
        boolean useFilter = false;
        for (ToggleButton b : filterMap.values()) {
            useFilter |= b.getValue();
        }
        for (Project project : projects) {
            boolean hasFeature = !useFilter;
            if (useFilter) {
                List<Feature> features = project.getFeatures();
                for (Feature feature : features) {
                    if (filterMap.get(feature).getValue()) {
                        hasFeature = true;
                        break;
                    }
                }
            }
            if (hasFeature) {
                DemoProjectsPlatformSection section = demoProjectPlatformSectionsMap.get(project.getPlatform());
                section.addProject(project);
                registrations.add(section.addProjectActionHandler(this));
            }
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
    
    private class DemoProjectsPlatformSection extends VerticalPanel implements HasProjectActionEventHandlers, ProjectActionEventHandler {
        
        private List<Project> projects = new ArrayList<>();
        
        private FlowPanel demoProjectsPanel = new FlowPanel();
        
        private List<HandlerRegistration> registrations = new ArrayList<>();
        
        DemoProjectsPlatformSection(Platform platform) {
            setWidth("100%");
            Label title = new Label();
            title.addStyleName(sandboxStyle.platformSectionTitle());
            title.setText(Utils.getPlatformText(platform));
            add(title);
            demoProjectsPanel.addStyleName(sandboxStyle.demoProjectsWidget());
            add(demoProjectsPanel);
        }
        
        void addProject(Project project) {
            projects.add(project);
            DemoProjectWidget demoProjectWidget = new DemoProjectWidget();
            demoProjectWidget.setProject(project);
            demoProjectsPanel.add(demoProjectWidget);
            registrations.add(demoProjectWidget.addProjectActionHandler(this));
            setVisible(true);
        }
        
        public void reset() {
            for (HandlerRegistration registration : registrations) {
                registration.removeHandler();
            }
            demoProjectsPanel.clear();
            projects.clear();
            setVisible(false);
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
    
    private class DemoProjectsFeatureFilter extends HorizontalPanel implements HasValueChangeHandlers<Boolean>, ValueChangeHandler<Boolean> {
        
        Map<Feature, FeatureButton> filterMap = new HashMap<>();
        
        DemoProjectsFeatureFilter() {
            setSpacing(6);
            for (Feature feature : Feature.values()) {
                FeatureButton toggleButton = new FeatureButton(feature);
                filterMap.put(feature, toggleButton);
                add(toggleButton);
                toggleButton.addValueChangeHandler(this);
            }
        }

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            fireEvent(event);
        }

        @Override
        public HandlerRegistration addValueChangeHandler(
                ValueChangeHandler<Boolean> handler) {
            return addHandler(handler, ValueChangeEvent.getType());
        }
        
        Map<Feature, FeatureButton> getFilterMap() {
            return filterMap;
        }
        
    }


}
