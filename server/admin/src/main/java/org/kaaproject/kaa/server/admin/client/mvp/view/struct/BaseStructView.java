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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import static org.kaaproject.kaa.server.admin.client.util.Utils.millisecondsToDateTimeString;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseStructView<T extends AbstractStructureDto, V> extends FlexTable implements InputEventHandler {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();
    
    private HasErrorMessage hasErrorMessage;
    
    private Label dateTimeCreatedLabel;
    private SizedTextBox createdDateTime;
    private Label authorLabel;
    private SizedTextBox createdUsername;
    private Label dateTimeModifiedLabel;
    private SizedTextBox modifiedDateTime;
    private Label modifiedByLabel;
    private SizedTextBox modifiedUsername;
    private Label dateTimeActivatedLabel;
    private SizedTextBox activatedDateTime;
    private Label activatedByLabel;
    private SizedTextBox activatedUsername;
    private Label dateTimeDeactivatedLabel;
    private SizedTextBox deactivatedDateTime;
    private Label deactivatedByLabel;
    private SizedTextBox deactivatedUsername;
    private SizedTextArea description;
    protected HasValue<V> body;
    private Button saveButton;
    private Button activateButton;
    private Button deactivateButton;

    private boolean active;

    private Label bodyLabel;

    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    public BaseStructView(HasErrorMessage hasErrorMessage) {
        this.hasErrorMessage = hasErrorMessage;
        init();
    }

    private void init() {
        FlexTable dateTable = new FlexTable();
        FlexTable userTable = new FlexTable();

        dateTable.getColumnFormatter().setWidth(0, "200px");
        dateTable.getColumnFormatter().setWidth(1, "200px");

        userTable.getColumnFormatter().setWidth(0, "200px");
        userTable.getColumnFormatter().setWidth(1, "200px");

        dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false, false);
        createdDateTime.setWidth("100%");
        dateTable.setWidget(0, 0, dateTimeCreatedLabel);
        dateTable.setWidget(0, 1, createdDateTime);

        dateTimeModifiedLabel = new Label(Utils.constants.dateTimeModified());
        modifiedDateTime = new KaaAdminSizedTextBox(-1, false, false);
        modifiedDateTime.setWidth("100%");
        dateTable.setWidget(1, 0, dateTimeModifiedLabel);
        dateTable.setWidget(1, 1, modifiedDateTime);

        dateTimeActivatedLabel = new Label(Utils.constants.dateTimeActivated());
        activatedDateTime = new KaaAdminSizedTextBox(-1, false, false);
        activatedDateTime.setWidth("100%");
        dateTable.setWidget(2, 0, dateTimeActivatedLabel);
        dateTable.setWidget(2, 1, activatedDateTime);

        dateTimeDeactivatedLabel = new Label(Utils.constants.dateTimeDectivated());
        deactivatedDateTime = new KaaAdminSizedTextBox(-1, false, false);
        deactivatedDateTime.setWidth("100%");
        dateTable.setWidget(3, 0, dateTimeDeactivatedLabel);
        dateTable.setWidget(3, 1, deactivatedDateTime);

        authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false, false);
        createdUsername.setWidth("100%");
        userTable.setWidget(0, 0, authorLabel);
        userTable.setWidget(0, 1, createdUsername);

        modifiedByLabel = new Label(Utils.constants.lastModifiedBy());
        modifiedUsername = new KaaAdminSizedTextBox(-1, false, false);
        modifiedUsername.setWidth("100%");
        userTable.setWidget(1, 0, modifiedByLabel);
        userTable.setWidget(1, 1, modifiedUsername);

        activatedByLabel = new Label(Utils.constants.activatedBy());
        activatedUsername = new KaaAdminSizedTextBox(-1, false, false);
        activatedUsername.setWidth("100%");
        userTable.setWidget(2, 0, activatedByLabel);
        userTable.setWidget(2, 1, activatedUsername);

        deactivatedByLabel = new Label(Utils.constants.deactivatedBy());
        deactivatedUsername = new KaaAdminSizedTextBox(-1, false, false);
        deactivatedUsername.setWidth("100%");
        userTable.setWidget(3, 0, deactivatedByLabel);
        userTable.setWidget(3, 1, deactivatedUsername);

        setWidget(0, 0, dateTable);
        setWidget(0, 1, userTable);

        FlexTable detailsTable = new FlexTable();
        setWidget(1, 0, detailsTable);
        getFlexCellFormatter().setColSpan(1, 0, 2);

        detailsTable.getColumnFormatter().setWidth(0, "200px");
        detailsTable.getColumnFormatter().setWidth(1, "300px");


        description = new SizedTextArea(1024);
        description.setWidth("500px");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(0, 0, descriptionLabel);
        detailsTable.setWidget(0, 1, description);

        detailsTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);

        body = createBody(hasErrorMessage);

        if (hasLabel()) {
            bodyLabel = new Label(Utils.constants.body());
            detailsTable.setWidget(1, 0, bodyLabel);
            detailsTable.setWidget(1, 1, (Widget)body);
            detailsTable.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        } else {
            detailsTable.setWidget(1, 0, (Widget)body);
            detailsTable.getFlexCellFormatter().setColSpan(1, 0, 2);
        }

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);

        detailsTable.setWidget(2, 0, buttonsPanel);
        detailsTable.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        detailsTable.getFlexCellFormatter().setColSpan(2, 0, 2);

        saveButton = new Button(Utils.constants.save());
        activateButton = new Button(Utils.constants.activate());
        deactivateButton = new Button(Utils.constants.deactivate());
        buttonsPanel.add(saveButton);
        buttonsPanel.add(activateButton);
        buttonsPanel.add(deactivateButton);

        description.setFocus(true);

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateSaveButton(false, false);
            }
        });
    }
    
    protected abstract HasValue<V> createBody(HasErrorMessage hasErrorMessage);
    
    protected abstract boolean hasLabel();
    
    protected abstract void setBodyReadOnly(boolean readOnly);
    
    protected abstract void setBodyValue(T struct);
    
    protected abstract HandlerRegistration addBodyChangeHandler();
    
    protected abstract boolean validateBody();

    public void reset() {
        dateTimeCreatedLabel.setVisible(false);
        createdDateTime.setVisible(false);
        authorLabel.setVisible(false);
        createdUsername.setVisible(false);
        dateTimeModifiedLabel.setVisible(false);
        modifiedDateTime.setVisible(false);
        modifiedByLabel.setVisible(false);
        modifiedUsername.setVisible(false);
        dateTimeActivatedLabel.setVisible(false);
        activatedDateTime.setVisible(false);
        activatedByLabel.setVisible(false);
        activatedUsername.setVisible(false);
        dateTimeDeactivatedLabel.setVisible(false);
        deactivatedDateTime.setVisible(false);
        deactivatedByLabel.setVisible(false);
        deactivatedUsername.setVisible(false);
        description.setValue("");
        body.setValue(null);
        saveButton.setVisible(false);
        activateButton.setVisible(false);
        deactivateButton.setVisible(false);        
        if (hasLabel()) {
            bodyLabel.removeStyleName(REQUIRED);
        }        
        description.getTextArea().setReadOnly(true);
        setBodyReadOnly(true);

        updateSaveButton(false, false);

        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
    }

    public void setReadOnly() {
        saveButton.setVisible(false);
        activateButton.setVisible(false);
        deactivateButton.setVisible(false);
        description.getTextArea().setReadOnly(true);
        setBodyReadOnly(true);

        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
    }

    public void setData(T struct) {
        this.active = struct.getStatus() != UpdateStatus.INACTIVE;
        if (!isEmpty(struct.getCreatedUsername())) {
            dateTimeCreatedLabel.setVisible(true);
            createdDateTime.setVisible(true);
            authorLabel.setVisible(true);
            createdUsername.setVisible(true);
            createdDateTime.setValue(millisecondsToDateTimeString(struct.getCreatedTime()));
            createdUsername.setValue(struct.getCreatedUsername());
        }
        if (!isEmpty(struct.getModifiedUsername())) {
            dateTimeModifiedLabel.setVisible(true);
            modifiedDateTime.setVisible(true);
            modifiedByLabel.setVisible(true);
            modifiedUsername.setVisible(true);
            modifiedDateTime.setValue(millisecondsToDateTimeString(struct.getLastModifyTime()));
            modifiedUsername.setValue(struct.getModifiedUsername());
        }
        if (!isEmpty(struct.getActivatedUsername())) {
            dateTimeActivatedLabel.setVisible(true);
            activatedDateTime.setVisible(true);
            activatedByLabel.setVisible(true);
            activatedUsername.setVisible(true);
            activatedDateTime.setValue(millisecondsToDateTimeString(struct.getActivatedTime()));
            activatedUsername.setValue(struct.getActivatedUsername());
        }
        if (!isEmpty(struct.getDeactivatedUsername())) {
            dateTimeDeactivatedLabel.setVisible(true);
            deactivatedDateTime.setVisible(true);
            deactivatedByLabel.setVisible(true);
            deactivatedUsername.setVisible(true);
            deactivatedDateTime.setValue(millisecondsToDateTimeString(struct.getDeactivatedTime()));
            deactivatedUsername.setValue(struct.getDeactivatedUsername());
        }
        description.setValue(struct.getDescription());
        setBodyValue(struct);

        if (this.active) {
            if (struct.getStatus()==UpdateStatus.ACTIVE) {
                deactivateButton.setVisible(true);
            }
        } else {
            description.getTextArea().setReadOnly(false);
            setBodyReadOnly(false);
            if (hasLabel()) {
                bodyLabel.addStyleName(REQUIRED);
            }
            registrations.add(description.addInputHandler(this));
            registrations.add(addBodyChangeHandler());
            saveButton.setVisible(true);
            if (isEmpty(struct.getId())) {
                saveButton.setText(Utils.constants.save());
            } else {
                activateButton.setVisible(true);
            }
        }
    }

    public void setSchemaSelected(boolean selected) {
        fireChanged();
    }

    public void setBodyLabelText(String text) {
        if (hasLabel()) {
            bodyLabel.setText(text);
        }
    }

    public HasValue<String> getDescription() {
        return description;
    }

    public HasValue<V> getBody() {
        return body;
    }

    public HasClickHandlers getSaveButton() {
        return saveButton;
    }

    public HasClickHandlers getActivateButton() {
        return activateButton;
    }

    public HasClickHandlers getDeactivateButton() {
        return deactivateButton;
    }

    @Override
    public void onInputChanged(InputEvent event) {
        fireChanged();
    }

    public void fireChanged() {
        if (!this.active) {
            boolean valid = validateBody();
            updateSaveButton(valid, !valid);
        }
    }

    private void updateSaveButton(boolean enabled, boolean invalid) {
        if (invalid) {
            saveButton.setText(Utils.constants.save());
        } else {
            saveButton.setText(enabled ? Utils.constants.save() : Utils.constants.saved());
        }
        saveButton.setEnabled(enabled);
        activateButton.setEnabled(!invalid && !enabled);
    }
}
