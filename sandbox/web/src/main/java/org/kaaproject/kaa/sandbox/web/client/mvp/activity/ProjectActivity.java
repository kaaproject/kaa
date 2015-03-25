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

package org.kaaproject.kaa.sandbox.web.client.mvp.activity;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.Sandbox;
import org.kaaproject.kaa.sandbox.web.client.mvp.ClientFactory;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.MainPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.ProjectPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.ProjectView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ConsoleDialog;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ConsoleDialog.ConsoleDialogListener;
import org.kaaproject.kaa.sandbox.web.client.servlet.ServletHelper;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataType;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ProjectActivity extends AbstractActivity {

    private final ClientFactory clientFactory;
    private final ProjectPlace place;
    private ProjectView view;
    
    private List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    public ProjectActivity(ProjectPlace place,
            ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        view = clientFactory.getProjectView();
        bind(eventBus);
        containerWidget.setWidget(view.asWidget());
    }
    
    @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
          registration.removeHandler();
        }
        registrations.clear();
    }

    private void bind(final EventBus eventBus) {
        
        view.reset();
        
        registrations.add(view.getBackButton().addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
              clientFactory.getPlaceController().goTo(new MainPlace());
          }
        }));
        
        registrations.add(view.getSourceButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getProjectSourceCode(place.getProjectId());
            }
          }));
        
        registrations.add(view.getBinaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getProjectBinary(place.getProjectId());
            }
          }));
        
        fillView();
    }

    private void fillView() {
        
        Sandbox.getSandboxService().getDemoProject(place.getProjectId(), new BusyAsyncCallback<Project>() {
            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }

            @Override
            public void onSuccessImpl(Project result) {
                if (result.getIconBase64() != null && result.getIconBase64().length() > 0) {
                    view.getApplicationImage().setUrl("data:image/png;base64,"+result.getIconBase64());
                } else {
                    view.getApplicationImage().setResource(Utils.getPlatformIconBig(result.getPlatform()));
                }
                view.setProjectTitle(result.getName());
                view.setTargetPlatform(result.getPlatform());
                view.setFeatures(result.getFeatures());
                view.getDescription().setText(result.getDescription());
                view.getDetails().setHTML(result.getDetails());
                view.setBinaryButtonVisible(result.getDestBinaryFile() != null && 
                        result.getDestBinaryFile().length() > 0);
            }
        });
    }
    
    private void getProjectSourceCode(String projectId) {
        getProjectData(projectId, ProjectDataType.SOURCE);
    }
    
    private void getProjectBinary(String projectId) {
        getProjectData(projectId, ProjectDataType.BINARY);
    }
    
    private void getProjectData(final String projectId, final ProjectDataType type) {
        view.clearError();
        Sandbox.getSandboxService().checkProjectDataExists(projectId, type, new BusyAsyncCallback<Boolean>() {

            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }

            @Override
            public void onSuccessImpl(Boolean result) {
                if (result) {
                    ServletHelper.downloadProjectFile(projectId, type);
                }
                else {
                    ConsoleDialog.startConsoleDialog(new ConsoleDialogListener() {
                        @Override
                        public void onOk(boolean success) {
                            if (success) {
                                ServletHelper.downloadProjectFile(projectId, type);
                            }
                        }

                        @Override
                        public void onStart(String uuid, final ConsoleDialog dialog, final AsyncCallback<Void> callback) {
                            Sandbox.getSandboxService().buildProjectData(uuid, null, projectId, type, new AsyncCallback<Void>() {
                              @Override
                              public void onFailure(Throwable caught) {
                                  callback.onFailure(caught);
                              }
                    
                              @Override
                              public void onSuccess(Void result) {
                                  dialog.appendToConsoleAtFinish("Succesfully prepared project data!\n");
                                  dialog.appendToConsoleAtFinish("\n\n\n-------- CLICK OK TO START DOWNLOAD " + 
                                          (type==ProjectDataType.SOURCE ? "PROJECT SOURCES" : "BINARY FILE") + " --------\n\n\n");
                                  callback.onSuccess(result);
                              }
                            });
                        }
                    });
                }
            }
        });
    }
    
    
}
