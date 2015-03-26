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

import org.kaaproject.kaa.sandbox.web.client.mvp.ClientFactory;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectFilterEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectFilterEventHandler;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.MainPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.FilterView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LeftPanelActivity extends AbstractActivity {

    private final ClientFactory clientFactory;
    private final FilterView filterView;
    
    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    public LeftPanelActivity(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.filterView = clientFactory.getFilterView();
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, final EventBus eventBus) {
        registrations.add(filterView.addProjectFilterHandler(new ProjectFilterEventHandler() {
            @Override
            public void onProjectFilter(ProjectFilterEvent event) {
                clientFactory.getPlaceController().goTo(new MainPlace());
                eventBus.fireEvent(event);
            }
        }));
        containerWidget.setWidget(filterView.asWidget());
    }
    
    @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
    }

}
