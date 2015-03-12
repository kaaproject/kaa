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

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.sandbox.web.client.Sandbox;
import org.kaaproject.kaa.sandbox.web.client.mvp.ClientFactory;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.ChangeKaaHostPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.HeaderView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.ActionsLabel.ActionMenuItemListener;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class HeaderActivity extends AbstractActivity {

    private final Place place;
    private final ClientFactory clientFactory;
    private final HeaderView headerView;

    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    public HeaderActivity(Place place, ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.headerView = clientFactory.getHeaderView();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        bind(headerView, eventBus);
        containerWidget.setWidget(headerView.asWidget());
    }

    @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
        headerView.getSettingsLabel().clearItems();
    }

    private void bind(final HeaderView headerView, final EventBus eventBus) {
        headerView.getSettingsLabel().setVisible(false);
        
        Sandbox.getSandboxService().changeKaaHostEnabled(new BusyAsyncCallback<Boolean>() {
            @Override
            public void onFailureImpl(Throwable caught) {
                headerView.getSettingsLabel().setVisible(false);
            }

            @Override
            public void onSuccessImpl(Boolean enabled) {
                headerView.getSettingsLabel().setVisible(enabled);
                if (enabled) {
                    headerView.getSettingsLabel().addMenuItem(Utils.constants.changeKaaHost(), new ActionMenuItemListener() {
                        @Override
                        public void onMenuItemSelected() {
                            clientFactory.getPlaceController().goTo(new ChangeKaaHostPlace(place));
                        }
                    });
                }
            }
        });
        
    }

}
