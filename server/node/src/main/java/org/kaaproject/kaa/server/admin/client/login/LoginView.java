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

package org.kaaproject.kaa.server.admin.client.login;

import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel.Type;
import org.kaaproject.kaa.server.admin.client.KaaAdminResources.KaaAdminStyle;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginView extends Composite implements HasErrorMessage {
    private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);

    interface LoginViewUiBinder extends UiBinder<Widget, LoginView> {
    }

    @UiField (provided=true) final AlertPanel errorPanel;
    @UiField (provided=true) final AlertPanel infoPanel;
    @UiField HTMLPanel loginTitle;
    @UiField FormPanel loginForm;
    @UiField FlexTable loginTable;
    @UiField(provided=true) final KaaAdminStyle kaaAdminStyle;

    private TextBox usernameBox;
    private PasswordTextBox passwordBox;
    private Button loginButton;
    private Label forgotPasswordLabel;

    public LoginView() {

        errorPanel = new AlertPanel(Type.ERROR);
        infoPanel = new AlertPanel(Type.INFO);
        kaaAdminStyle = Utils.kaaAdminStyle;

        initWidget(uiBinder.createAndBindUi(this));

        loginTitle.getElement().setInnerSafeHtml(SafeHtmlUtils.fromSafeConstant(Utils.messages.loginTitle()));

        Label loginLabel = new Label(Utils.constants.username());
        usernameBox = new TextBox();
        usernameBox.setName("j_username");
        usernameBox.setWidth("100%");

        Label passwordLabel = new Label(Utils.constants.password());
        passwordBox = new PasswordTextBox();
        passwordBox.setName("j_password");
        passwordBox.setWidth("100%");

        loginTable.setWidget(0, 0, loginLabel);
        loginTable.setWidget(0, 1, usernameBox);
        loginTable.setWidget(1, 0, passwordLabel);
        loginTable.setWidget(1, 1, passwordBox);
        
        forgotPasswordLabel = new Label(Utils.constants.forgotPassword());
        forgotPasswordLabel.addStyleName(Utils.kaaAdminStyle.linkLabel());
        loginTable.setWidget(2, 0, forgotPasswordLabel);

        loginTable.getFlexCellFormatter().setWidth(0, 0, "130px");
        loginTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        loginTable.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        loginTable.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        loginTable.getFlexCellFormatter().setColSpan(2, 0, 2);

        loginButton = new Button(Utils.constants.login());
        loginButton.addStyleName(Utils.kaaAdminStyle.loginButton());
        loginTable.setWidget(3, 2, loginButton);
        loginButton.getElement().getStyle().setMarginTop(15, Unit.PX);

        loginForm.setWidget(loginTable);
        loginForm.setAction("");

    }

    public Button getLoginButton() {
        return loginButton;
    }

    public TextBox getUsernameBox() {
        return usernameBox;
    }

    public PasswordTextBox getPasswordBox() {
        return passwordBox;
    }
    
    public Label getForgotPasswordLabel() {
        return forgotPasswordLabel;
    }

    @Override
    public void clearError() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        errorPanel.setVisible(true);
    }

    public void clearMessages() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
        infoPanel.setMessage("");
        infoPanel.setVisible(false);
    }

    public void setInfoMessage(String message) {
        infoPanel.setMessage(message);
        infoPanel.setVisible(true);
    }

}
