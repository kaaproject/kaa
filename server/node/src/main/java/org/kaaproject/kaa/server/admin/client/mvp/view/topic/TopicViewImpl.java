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

package org.kaaproject.kaa.server.admin.client.mvp.view.topic;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.TopicView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

public class TopicViewImpl extends BaseDetailsViewImpl implements TopicView, ValueChangeHandler<Boolean> {

    private SizedTextBox name;
    private CheckBox mandatory;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private Button sendNotification;

    public TopicViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {

        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(0, 0, authorLabel);
        detailsTable.setWidget(0, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(1, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(1, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        name = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth("100%");
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName(Utils.avroUiStyle.requiredField());
        detailsTable.setWidget(2, 0, nameLabel);
        detailsTable.setWidget(2, 1, name);
        name.addInputHandler(this);

        mandatory = new CheckBox();
        mandatory.setWidth("100%");
        Label mandatoryLabel = new Label(Utils.constants.mandatory());
        detailsTable.setWidget(3, 0, mandatoryLabel);
        detailsTable.setWidget(3, 1, mandatory);
        mandatory.addValueChangeHandler(this);
        
        description = new SizedTextArea(1024);
        description.setWidth("100%");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(4, 0, descriptionLabel);
        detailsTable.setWidget(4, 1, description);
        description.addInputHandler(this);
        
        detailsTable.getCellFormatter().setVerticalAlignment(4, 0, HasVerticalAlignment.ALIGN_TOP);

        sendNotification = new Button(Utils.constants.sendNotification());
        detailsTable.setWidget(5, 0, sendNotification);
        sendNotification.setVisible(!create);

        if (!create) {
            getSaveButtonWidget().setVisible(false);
            name.setEnabled(false);
            mandatory.setEnabled(false);
            description.getTextArea().setEnabled(false);
        }

        name.setFocus(true);
    }

    @Override
    protected void resetImpl() {
        name.setValue("");
        mandatory.setValue(false);
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
    }

    @Override
    protected boolean validate() {
        boolean result = name.getValue().length()>0;
        return result;
    }

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
        fireChanged();
    }

    @Override
    public HasValue<String> getName() {
        return name;
    }

    @Override
    public HasValue<Boolean> getMandatory() {
        return mandatory;
    }

    @Override
    public HasValue<String> getDescription() {
        return description;
    }

    @Override
    public HasValue<String> getCreatedUsername() {
        return createdUsername;
    }

    @Override
    public HasValue<String> getCreatedDateTime() {
        return createdDateTime;
    }

    @Override
    public HasClickHandlers getSendNotificationButton() {
        return sendNotification;
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNotificationTopic();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.notificationTopic();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.notificationTopicDetails();
    }

}
