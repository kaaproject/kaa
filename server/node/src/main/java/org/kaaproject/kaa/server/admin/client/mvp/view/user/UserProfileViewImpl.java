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

package org.kaaproject.kaa.server.admin.client.mvp.view.user;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class UserProfileViewImpl extends BaseDetailsViewImpl implements UserProfileView {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();

    private SizedTextBox authority;
    private SizedTextBox firstName;
    private SizedTextBox lastName;
    private SizedTextBox email;
    private Button changePasswordButton;

    public UserProfileViewImpl() {
        super(false);
    }

    @Override
    protected String getCreateTitle() {
        return "";
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.user();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.accountProfile();
    }

    @Override
    protected void initDetailsTable() {

        authority = new KaaAdminSizedTextBox(-1, false);
        authority.setWidth("100%");

        Label authorityLabel = new Label(Utils.constants.accountRole());
        detailsTable.setWidget(0, 0, authorityLabel);
        detailsTable.setWidget(0, 1, authority);

        firstName = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        firstName.setWidth("100%");
        firstName.addInputHandler(this);

        Label firstNameLabel = new Label(Utils.constants.firstName());
        firstNameLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(1, 0, firstNameLabel);
        detailsTable.setWidget(1, 1, firstName);

        lastName = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        lastName.setWidth("100%");
        lastName.addInputHandler(this);

        Label lastNameLabel = new Label(Utils.constants.lastName());
        lastNameLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(2, 0, lastNameLabel);
        detailsTable.setWidget(2, 1, lastName);

        email = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        email.setWidth("100%");
        email.addInputHandler(this);

        Label emailLabel = new Label(Utils.constants.email());
        emailLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(3, 0, emailLabel);
        detailsTable.setWidget(3, 1, email);

        changePasswordButton = new Button(Utils.constants.changePassword());

        detailsTable.setWidget(4, 0, changePasswordButton);

        firstName.setFocus(true);
    }

    @Override
    protected void resetImpl() {
        authority.setValue("");
        firstName.setValue("");
        lastName.setValue("");
        email.setValue("");
    }

    @Override
    protected boolean validate() {
        boolean result = firstName.getValue().length()>0;
        result &= lastName.getValue().length()>0;
        result &= Utils.validateEmail(email.getValue());
        return result;
    }

    @Override
    public HasValue<String> getAuthority() {
        return authority;
    }

    @Override
    public HasValue<String> getEmail() {
        return email;
    }

    @Override
    public HasValue<String> getFirstName() {
        return firstName;
    }

    @Override
    public HasValue<String> getLastName() {
        return lastName;
    }

    @Override
    public HasClickHandlers getChangePasswordButton() {
        return changePasswordButton;
    }


}
