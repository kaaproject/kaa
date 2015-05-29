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
import org.kaaproject.kaa.sandbox.web.client.Sandbox;
import org.kaaproject.kaa.sandbox.web.client.mvp.ClientFactory;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.ChangeKaaHostPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.ChangeKaaHostView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ConsoleDialog;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog.ConsoleDialog.ConsoleDialogListener;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ChangeKaaHostActivity extends AbstractActivity {

    private final ChangeKaaHostPlace place;
    private final ClientFactory clientFactory;
    private ChangeKaaHostView view;

    private List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    public ChangeKaaHostActivity(ChangeKaaHostPlace place,
            ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        view = clientFactory.getChangeKaaHostView();
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

        registrations.add(view.getBackButton().addClickHandler(
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        clientFactory.getPlaceController().goTo(
                                place.getPreviousPlace());
                    }
                }));

        fillView();
    }

    private void fillView() {

        Sandbox.getSandboxService().changeKaaHostEnabled(
                new BusyAsyncCallback<Boolean>() {
                    @Override
                    public void onFailureImpl(Throwable caught) {
                        view.setErrorMessage(Utils.getErrorMessage(caught));
                    }

                    @Override
                    public void onSuccessImpl(Boolean enabled) {
                        view.setChangeKaaHostEnabled(enabled);
                        if (enabled) {
                            registrations.add(view.getChangeKaaHostButton()
                                    .addClickHandler(new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            changeKaaHost();
                                        }
                                    }));
                        }
                    }
                });
    }

    private void changeKaaHost() {
        final String host = view.getKaaHost().getValue();
        if (host != null && host.length() > 0) {
            view.clearError();
            ConsoleDialog.startConsoleDialog("Going to change kaa host to '"
                                            + host + "'...\n", new ConsoleDialogListener() {

                @Override
                public void onOk(boolean success) {
                }

                @Override
                public void onStart(String uuid, final ConsoleDialog dialog,
                        final AsyncCallback<Void> callback) {
                    Sandbox.getSandboxService().changeKaaHost(uuid, host,
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    callback.onFailure(caught);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    dialog.appendToConsoleAtFinish("Succesfully changed kaa host to '"
                                            + host + "'\n");
                                    callback.onSuccess(result);
                                }
                            });
                }
            });
        } else {
            view.setErrorMessage(Utils.messages.emptyKaaHostError());
        }
    }

}
