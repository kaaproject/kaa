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
    private Project project;
    
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
                getProjectSourceCode();
            }
          }));
        
        registrations.add(view.getBinaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getProjectBinary();
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
                project = result;
                if (project.getIconBase64() != null && project.getIconBase64().length() > 0) {
                    view.getApplicationImage().setUrl("data:image/png;base64,"+project.getIconBase64());
                } else {
                    view.getApplicationImage().setResource(Utils.getPlatformIconBig(project.getPlatform()));
                }
                view.setProjectTitle(project.getName());
                view.setPlatform(project.getPlatform());
                view.setFeatures(project.getFeatures());
                view.getDescription().setText(project.getDescription());
                view.getDetails().setHTML(project.getDetails());
                view.setBinaryButtonVisible(project.getDestBinaryFile() != null && 
                        project.getDestBinaryFile().length() > 0);
            }
        });
    }
    
    private void getProjectSourceCode() {
        getProjectData(ProjectDataType.SOURCE);
    }
    
    private void getProjectBinary() {
        getProjectData(ProjectDataType.BINARY);
    }
    
    private void getProjectData(final ProjectDataType type) {
        view.clearError();
        if (project != null) {
            Sandbox.getSandboxService().checkProjectDataExists(project.getId(), type, new BusyAsyncCallback<Boolean>() {
    
                @Override
                public void onFailureImpl(Throwable caught) {
                    view.setErrorMessage(Utils.getErrorMessage(caught));
                }
    
                @Override
                public void onSuccessImpl(Boolean result) {
                    if (result) {
                        ServletHelper.downloadProjectFile(project.getId(), type);
                    }
                    else {
                        String initialMessage = "Assembling ";
                        if (type == ProjectDataType.SOURCE) {
                            initialMessage += "sources";
                        } else {
                            initialMessage += "binary";
                        }
                        initialMessage += " for '" + project.getName() + "' project...\n";
                        ConsoleDialog.startConsoleDialog(initialMessage, new ConsoleDialogListener() {
                            @Override
                            public void onOk(boolean success) {
                                if (success) {
                                    ServletHelper.downloadProjectFile(project.getId(), type);
                                }
                            }
    
                            @Override
                            public void onStart(String uuid, final ConsoleDialog dialog, final AsyncCallback<Void> callback) {
                                Sandbox.getSandboxService().buildProjectData(uuid, null, project.getId(), type, new AsyncCallback<Void>() {
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
        } else {
            view.setErrorMessage("Unable to retrieve project data!");
        }
    }
    
    
}
