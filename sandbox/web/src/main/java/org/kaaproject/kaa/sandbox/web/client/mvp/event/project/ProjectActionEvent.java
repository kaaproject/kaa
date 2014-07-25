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

package org.kaaproject.kaa.sandbox.web.client.mvp.event.project;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectActionEvent extends GwtEvent<ProjectActionEventHandler>{

  private static Type<ProjectActionEventHandler> TYPE;

  private final String projectId;
  private final ProjectAction action;

  public ProjectActionEvent(String projectId, ProjectAction action) {
    this.projectId = projectId;
    this.action = action;
  }

  public String getProjectId() {
      return projectId;
  }

  public ProjectAction getAction() {
      return action;
  }

  public static Type<ProjectActionEventHandler> getType() {
      if (TYPE == null) {
          TYPE = new Type<ProjectActionEventHandler>();
      }
    return TYPE;
  }

  @Override
  public Type<ProjectActionEventHandler> getAssociatedType() {
      return TYPE;
  }

  @Override
  protected void dispatch(ProjectActionEventHandler handler) {
    handler.onProjectAction(this);
  }
}

