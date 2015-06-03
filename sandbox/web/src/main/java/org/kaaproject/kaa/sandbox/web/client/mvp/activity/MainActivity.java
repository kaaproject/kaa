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
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEventHandler;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectFilterEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectFilterEventHandler;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.ChangeKaaHostPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.MainPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.ProjectPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.MainView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ChangeHostDialog;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ChangeHostDialog.Listener;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ConsoleDialog;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ConsoleDialog.ConsoleDialogListener;
import org.kaaproject.kaa.sandbox.web.client.servlet.ServletHelper;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataType;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class MainActivity extends AbstractActivity {

    private final ClientFactory clientFactory;
    private MainView view;
    
    private List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    public MainActivity(MainPlace place,
            ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        view = clientFactory.getMainView();
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
        registrations.add(view.getProjectsActionSource().addProjectActionHandler(new ProjectActionEventHandler() {
            @Override
            public void onProjectAction(ProjectActionEvent event) {
                switch(event.getAction()) {
                case GET_SOURCE_CODE:
                    getProjectSourceCode(event.getProject());
                    break;
                case GET_BINARY:
                    getProjectBinary(event.getProject());
                    break;
                case OPEN_DETAILS:
                    clientFactory.getPlaceController().goTo(new ProjectPlace(event.getProject().getId()));
                    break;
                default:
                    break;
                }
            }
        }));
        
        registrations.add(eventBus.addHandler(ProjectFilterEvent.getType(), new ProjectFilterEventHandler() {
            @Override
            public void onProjectFilter(ProjectFilterEvent event) {
                view.updateProjectFilter(event.getProjectFilter());
            }
        }));
        
        view.reset();
        fillView();
        showChangeKaaHostDialog();
    }

    private void fillView() {
        
        Sandbox.getSandboxService().getDemoProjects(new BusyAsyncCallback<List<Project>>() {
            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }

            @Override
            public void onSuccessImpl(List<Project> result) {
                view.setProjects(result);
            }
        });
    }
    
    private void showChangeKaaHostDialog() {
        Sandbox.getSandboxService().showChangeKaaHostDialog(new BusyAsyncCallback<Boolean>() {
            
            @Override
            public void onSuccessImpl(Boolean result) {
                if (result) {
                    ChangeHostDialog.showChangeHostDialog(new Listener() {
                        
                        @Override
                        public void onIgnore() {
                            Sandbox.getSandboxService().changeKaaHostDialogShown(new BusyAsyncCallback<Void>() {
                                @Override
                                public void onSuccessImpl(Void result) {}
                                @Override
                                public void onFailureImpl(Throwable caught) {}
                            });
                        }
                        
                        @Override
                        public void onChangeHost() {
                            Sandbox.getSandboxService().changeKaaHostDialogShown(new BusyAsyncCallback<Void>() {
                                @Override
                                public void onSuccessImpl(Void result) {
                                    clientFactory.getPlaceController().goTo(new ChangeKaaHostPlace());
                                }
                                @Override
                                public void onFailureImpl(Throwable caught) {
                                    view.setErrorMessage(Utils.getErrorMessage(caught));
                                }
                            });
                            
                        }
                    });
                }
            }
            
            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
    
    private void getProjectSourceCode(Project project) {
        getProjectData(project, ProjectDataType.SOURCE);
    }
    
    private void getProjectBinary(Project project) {
        getProjectData(project, ProjectDataType.BINARY);
    }
    
    private void getProjectData(final Project project, final ProjectDataType type) {
        view.clearError();
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
    }
    
    
}
