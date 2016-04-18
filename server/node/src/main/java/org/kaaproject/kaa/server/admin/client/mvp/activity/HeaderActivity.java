/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.GeneralPropertiesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.MailPropertiesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.HeaderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.ChangePasswordDialog;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ActionsLabel.ActionMenuItemListener;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class HeaderActivity extends AbstractActivity implements
        HeaderView.Presenter {

    private final ClientFactory clientFactory;
    private final HeaderView headerView;

    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    public HeaderActivity(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.headerView = clientFactory.getHeaderView();
        this.headerView.setPresenter(this);
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

    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    private void bind(final HeaderView headerView, final EventBus eventBus) {
        headerView.getUsernameLabel().setText(
                KaaAdmin.getAuthInfo().getDisplayName());
        registrations.add(headerView.getUsernameLabel().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                goTo(new UserProfilePlace());
            }
        }));
        registrations.add(headerView.getSignoutLabel().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                KaaAdmin.signOut();
            }
        }));
        headerView.getSettingsLabel().addMenuItem(Utils.constants.profile(), new ActionMenuItemListener() {
            @Override
            public void onMenuItemSelected() {
                goTo(new UserProfilePlace());
            }
        });
        headerView.getSettingsLabel().addMenuItem(Utils.constants.changePassword(), new ActionMenuItemListener() {
            @Override
            public void onMenuItemSelected() {
                showChangePasswordDialog();
            }
        });
        KaaAuthorityDto autority = KaaAdmin.getAuthInfo().getAuthority();
        if (autority == KaaAuthorityDto.KAA_ADMIN) {
            headerView.getSettingsLabel().addMenuItem(Utils.constants.generalSettings(), new ActionMenuItemListener() {
                @Override
                public void onMenuItemSelected() {
                    goTo(new GeneralPropertiesPlace());
                }
            });
            headerView.getSettingsLabel().addMenuItem(Utils.constants.outgoingMailSettings(), new ActionMenuItemListener() {
                @Override
                public void onMenuItemSelected() {
                    goTo(new MailPropertiesPlace());
                }
            });
        }
    }

    private void showChangePasswordDialog() {
        //change password
        ChangePasswordDialog.Listener listener = new ChangePasswordDialog.Listener() {
            @Override
            public void onChangePassword() {}
            @Override
            public void onCancel() {}
        };

        ChangePasswordDialog.showChangePasswordDialog(listener,
                KaaAdmin.getAuthInfo().getUsername(),
                null);
    }

    @Override
    public void goToHome() {
        clientFactory.getPlaceController().goTo(clientFactory.getHomePlace());
    }
}
