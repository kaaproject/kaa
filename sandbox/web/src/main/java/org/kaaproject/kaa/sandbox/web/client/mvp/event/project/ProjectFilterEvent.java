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

import com.google.gwt.event.shared.GwtEvent;

public class ProjectFilterEvent extends GwtEvent<ProjectFilterEventHandler>{

    private static Type<ProjectFilterEventHandler> TYPE;
    
    private final ProjectFilter projectFilter;
    
    public ProjectFilterEvent(ProjectFilter projectFilter) {
        this.projectFilter = projectFilter;
    }
    
    public ProjectFilter getProjectFilter() {
        return projectFilter;
    }
    
    public static Type<ProjectFilterEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<ProjectFilterEventHandler>();
        }
      return TYPE;
    }

    @Override
    public Type<ProjectFilterEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProjectFilterEventHandler handler) {
        handler.onProjectFilter(this);
    }

}
