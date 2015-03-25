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

package org.kaaproject.kaa.sandbox.web.client.mvp.event.project;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.sandbox.demo.projects.Feature;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;

public class ProjectFilter {
    
    private final Set<Feature> enabledFeatures;
    private final Set<Platform> enabledPlatforms;
    
    private boolean useFeatureFilter = false;
    private boolean usePlatformFilter = false;
    
    public ProjectFilter() {
        enabledFeatures = new HashSet<>();
        enabledPlatforms = new HashSet<>();
    }
    
    public ProjectFilter(Set<Feature> enabledFeatures, Set<Platform> enabledPlatforms) {
        this.enabledFeatures = enabledFeatures;
        this.enabledPlatforms = enabledPlatforms;
        
        useFeatureFilter = !enabledFeatures.isEmpty();
        usePlatformFilter = !enabledPlatforms.isEmpty();
    }
    
    public boolean filter(Project project) {
        boolean hasFeature = !useFeatureFilter;
        boolean hasPlatform = !usePlatformFilter;
        if (useFeatureFilter) {
            List<Feature> features = project.getFeatures();
            for (Feature feature : features) {
                if (enabledFeatures.contains(feature)) {
                    hasFeature = true;
                    break;
                }
            }
        }
        if (usePlatformFilter) {
            hasPlatform = enabledPlatforms.contains(project.getPlatform());
        }
        return hasFeature && hasPlatform;
    }

}
