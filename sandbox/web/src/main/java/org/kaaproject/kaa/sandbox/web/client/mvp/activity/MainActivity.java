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

package org.kaaproject.kaa.sandbox.web.client.mvp.activity;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.Sandbox;
import org.kaaproject.kaa.sandbox.web.client.mvp.ClientFactory;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEventHandler;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.MainPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.MainView;
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
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class MainActivity extends AbstractActivity implements MainView.Presenter {

    private final ClientFactory clientFactory;
    private MainPlace place;
    private MainView view;
    
    private List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    public MainActivity(MainPlace place,
            ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        view = clientFactory.getMainView();
        view.setPresenter(this);
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
    
    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    private void bind(final EventBus eventBus) {
        registrations.add(view.getGoToKaaAdminWeb().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                gotoKaaAdminWeb();
            }
          }));
        
        registrations.add(view.getProjectsActionSource().addProjectActionHandler(new ProjectActionEventHandler() {
            @Override
            public void onProjectAction(ProjectActionEvent event) {
                switch(event.getAction()) {
                case GET_SOURCE_CODE:
                    getProjectSourceCode(event.getProjectId());
                    break;
                case GET_BINARY:
                    getProjectBinary(event.getProjectId());
                    break;
                }
            }
        }));
        
        view.reset();
        fillView();
    }

    private void fillView() {
        
        Sandbox.getSandboxService().changeKaaHostEnabled(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }

            @Override
            public void onSuccess(Boolean enabled) {
                view.setChangeKaaHostEnabled(enabled);
                if (enabled) {
                  registrations.add(view.getChangeKaaHostButton().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        changeKaaHost();
                    }
                  }));
                }
            }
        });
        
        Sandbox.getSandboxService().getDemoProjects(new AsyncCallback<List<Project>>() {
            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }

            @Override
            public void onSuccess(List<Project> result) {
                view.setProjects(result);
            }
        });
    }
    
    private void gotoKaaAdminWeb() {
        Sandbox.redirectToModule("kaaAdmin");
    }

    private void changeKaaHost() {
    	final String host = view.getKaaHost().getValue();
    	if (host != null && host.length()>0) { 
    		ConsoleDialog.startConsoleDialog(new ConsoleDialogListener() {

				@Override
				public void onOk(boolean success) {}

				@Override
				public void onStart(String uuid, final ConsoleDialog dialog, final AsyncCallback<Void> callback) {
			        Sandbox.getSandboxService().changeKaaHost(uuid, host, new AsyncCallback<Void>() {
			          @Override
			          public void onFailure(Throwable caught) {
			              callback.onFailure(caught);
			          }
			
			          @Override
			          public void onSuccess(Void result) {
			        	  dialog.appendToConsoleAtFinish("Succesfully changed kaa host to '" + host + "'\n");
			        	  callback.onSuccess(result);
			          }
			        });
				}
    		});
    	}
    	else {
    		view.setErrorMessage("Kaa host field can not be empty!");
    	}
    }
    
    private void getProjectSourceCode(String projectId) {
        getProjectData(projectId, ProjectDataType.SOURCE);
    }
    
    private void getProjectBinary(String projectId) {
        getProjectData(projectId, ProjectDataType.BINARY);
    }
    
    private void getProjectData(final String projectId, final ProjectDataType type) {
        view.clearMessages();
        Sandbox.getSandboxService().checkProjectDataExists(projectId, type, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }

            @Override
            public void onSuccess(Boolean result) {
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
