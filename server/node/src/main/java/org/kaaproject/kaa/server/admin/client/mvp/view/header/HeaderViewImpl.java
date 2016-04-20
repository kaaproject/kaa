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

package org.kaaproject.kaa.server.admin.client.mvp.view.header;

import org.kaaproject.kaa.server.admin.client.KaaAdminResources.KaaAdminStyle;
import org.kaaproject.kaa.server.admin.client.mvp.view.HeaderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ActionsLabel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class HeaderViewImpl extends Composite implements HeaderView {

    interface HeaderViewImplUiBinder extends UiBinder<Widget, HeaderViewImpl> { }
    private static HeaderViewImplUiBinder uiBinder = GWT.create(HeaderViewImplUiBinder.class);

    private Presenter presenter;

    @UiField Label usernameLabel;
    @UiField Label signoutLabel;
    @UiField(provided=true) final ActionsLabel settingsLabel;
    @UiField Label title;
    @UiField(provided=true) final KaaAdminStyle kaaAdminStyle;

    public HeaderViewImpl() {
        settingsLabel = new ActionsLabel(Utils.constants.settings());
        kaaAdminStyle = Utils.kaaAdminStyle;
        settingsLabel.setStyleName(kaaAdminStyle.bAppHeaderMenu());
        initWidget(uiBinder.createAndBindUi(this));
        signoutLabel.setText(Utils.constants.signOut());
        title.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                presenter.goToHome();
            }
        });
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Label getUsernameLabel() {
        return usernameLabel;
    }

    @Override
    public Label getSignoutLabel() {
        return signoutLabel;
    }

    @Override
    public ActionsLabel getSettingsLabel() {
        return settingsLabel;
    }

}
