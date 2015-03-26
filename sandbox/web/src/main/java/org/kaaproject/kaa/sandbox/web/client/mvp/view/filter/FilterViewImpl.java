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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.sandbox.demo.projects.Feature;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectFilter;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectFilterEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectFilterEventHandler;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.FilterView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.FilterPanel;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.FilterPanel.FilterItem;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.LeftPanelWidget;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FilterViewImpl extends LeftPanelWidget implements FilterView, ValueChangeHandler<Boolean> {

    private VerticalPanel filterPanel;
    private DemoProjectsFeatureFilter featureFilter;
    private DemoProjectsPlatformFilter platformFilter;
    
    public FilterViewImpl() {
        super(Unit.PX);
        
        setHeadTitle(Utils.constants.filter());
        
        filterPanel = new VerticalPanel();
        filterPanel.setWidth("100%");
        featureFilter = new DemoProjectsFeatureFilter();
        filterPanel.add(featureFilter);
        featureFilter.addValueChangeHandler(this);
        
        platformFilter = new DemoProjectsPlatformFilter();
        platformFilter.getElement().getStyle().setPaddingTop(40, Unit.PX);
        filterPanel.add(platformFilter);
        platformFilter.addValueChangeHandler(this);
        
        setContent(filterPanel);
        
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
        fireFilter();
    }
    
    private void fireFilter() {
        Set<Feature> enabledFeatures = new HashSet<Feature>();
        Set<Platform> enabledPlatforms = new HashSet<Platform>();

        List<FilterItem> featureFilterList = featureFilter.getFilterItems();
        List<FilterItem> platformFilterList = platformFilter.getFilterItems();

        for (Feature feature : Feature.values()) {
            if (featureFilterList.get(feature.ordinal()).getValue()) {
                enabledFeatures.add(feature);
            }
        }
        
        for (Platform platform : Platform.values()) {
            if (platformFilterList.get(platform.ordinal()).getValue()) {
                enabledPlatforms.add(platform);
            }
        }
        
        ProjectFilter projectFilter = new ProjectFilter(enabledFeatures, enabledPlatforms);
        ProjectFilterEvent filterEvent = new ProjectFilterEvent(projectFilter);
        fireEvent(filterEvent);
    }
    
    @Override
    public HandlerRegistration addProjectFilterHandler(
            ProjectFilterEventHandler handler) {
        return this.addHandler(handler, ProjectFilterEvent.getType());
    }
    
    private class DemoProjectsFeatureFilter extends FilterPanel {

        public DemoProjectsFeatureFilter() {
            super(Utils.constants.featuresFilter());
            setWidth("100%");
            for (Feature feature : Feature.values()) {
                addItem(Utils.getFeatureIcon(feature), Utils.getFeatureBackgroundClass(feature), Utils.getFeatureText(feature));
            }
        }
        
    }
    
    private class DemoProjectsPlatformFilter extends FilterPanel {

        public DemoProjectsPlatformFilter() {
            super(Utils.constants.platformsFilter());
            setWidth("100%");
            for (Platform platform : Platform.values()) {
                addItem(Utils.getFilterPlatformIcon(platform), Utils.getPlatformBackgroundClass(platform), Utils.getPlatformText(platform));
            }
        }
        
    }


}
