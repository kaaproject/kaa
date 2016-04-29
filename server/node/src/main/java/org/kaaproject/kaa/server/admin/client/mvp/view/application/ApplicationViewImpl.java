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

package org.kaaproject.kaa.server.admin.client.mvp.view.application;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.ApplicationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

public class ApplicationViewImpl extends BaseDetailsViewImpl implements ApplicationView {

    private SizedTextBox applicationName;
    private SizedTextBox applicationToken;

    private ValueListBox<String> credentialsServiceName;

    private Button generateSdkButton;

    public ApplicationViewImpl(boolean create, boolean editable) {
        super(create, editable);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNewApplication();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.application();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.applicationDetails();
    }

    @Override
    protected void initDetailsTable() {
        applicationName = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE, editable);
        applicationName.setWidth("100%");
        Label titleLabel = new Label(Utils.constants.title());
        if (editable) {
            titleLabel.addStyleName(avroUiStyle.requiredField());
        }
        detailsTable.setWidget(0, 0, titleLabel);
        detailsTable.setWidget(0, 1, applicationName);

        applicationName.addInputHandler(this);
        applicationName.setFocus(true);

//        applicationKey = new SizedTextBox(DEFAULT_TEXTBOX_SIZE * 2, editable);
//        applicationKey.setWidth("100%");
//
//        Label keyLabel = new Label(Utils.constants.publicKey());
//        detailsTable.setWidget(1, 0, keyLabel);
//        detailsTable.setWidget(1, 1, applicationKey);

        if (!create) {
            applicationToken = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE * 2, editable);
            applicationToken.setWidth("100%");
            applicationToken.setEnabled(false);

            Label tokenLabel = new Label(Utils.constants.appToken());
            detailsTable.setWidget(2, 0, tokenLabel);
            detailsTable.setWidget(2, 1, applicationToken);
        }

//        applicationKey.addInputHandler(this);

        if (KaaAdmin.isDevMode()) {
            generateSdkButton = new Button(Utils.constants.generateSdk());
            detailsTable.setWidget(3, 0, generateSdkButton);
        } else {
            this.credentialsServiceName = new ValueListBox<String>();
            this.credentialsServiceName.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    ApplicationViewImpl.this.fireChanged();
                }
            });
            this.credentialsServiceName.setWidth("100%");

            Label label = new Label(Utils.constants.credentialsService());
            label.addStyleName(this.avroUiStyle.requiredField());

            this.detailsTable.setWidget(3, 0, label);
            this.detailsTable.setWidget(3, 1, this.credentialsServiceName);
        }
    }

    @Override
    protected void resetImpl() {
        applicationName.setValue("");

        if (this.credentialsServiceName != null) {
            this.credentialsServiceName.setValue("");
        }
    }

    @Override
    protected boolean validate() {
        return applicationName.getValue().length()>0 && credentialsServiceName.getValue().length() > 0;
    }

    @Override
    public HasValue<String> getApplicationName() {
        return applicationName;
    }

    @Override
    public ValueListBox<String> getCredentialsServiceName() {
        return this.credentialsServiceName;
    }

    @Override
    public HasClickHandlers getGenerateSdkButton() {
        return generateSdkButton;
    }

//    @Override
//    public HasValue<String> getApplicationKey() {
//        return applicationKey;
//    }

    @Override
    public HasValue<String> getApplicationToken() {
        return applicationToken;
    }

}
