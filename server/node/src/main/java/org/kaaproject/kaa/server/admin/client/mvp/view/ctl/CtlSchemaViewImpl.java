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

package org.kaaproject.kaa.server.admin.client.mvp.view.ctl;

import org.kaaproject.avro.ui.gwt.client.widget.ActionsButton;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.IntegerListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

public class CtlSchemaViewImpl extends BaseDetailsViewImpl implements CtlSchemaView, 
                                                                        ValueChangeHandler<RecordField> {
    
    private IntegerListBox version;
    private Button createNewSchemaVersionButton;
    private Button deleteSchemaVersionButton;
    private ActionsButton exportActionsButton;
    private SizedTextBox name;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;

    private RecordPanel schemaForm;
    
    public CtlSchemaViewImpl(boolean create) {
        super(create, true);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNewCtl();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.commonType();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.commonTypeDetails();
    }

    @Override
    protected void initDetailsTable() {

        Label versionLabel = new Label(Utils.constants.version());
        versionLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitle());
        version = new IntegerListBox();
        version.getElement().getStyle().setPadding(5, Unit.PX);
        version.getElement().getStyle().setMarginLeft(5, Unit.PX);
        
        createNewSchemaVersionButton = new Button(Utils.constants.createNewVersion());        
        exportActionsButton = new ActionsButton(Utils.resources.export(), Utils.constants.export());
        deleteSchemaVersionButton = new Button(Utils.constants.delete());
        
        HorizontalPanel versionPanel = new HorizontalPanel();
        versionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        versionPanel.add(versionLabel);
        versionPanel.add(version);
        
        prependToolbarWidget(versionPanel);
        appendToolbarWidget(createNewSchemaVersionButton);
        appendToolbarWidget(exportActionsButton);
        appendToolbarWidget(deleteSchemaVersionButton);
        
        versionPanel.setVisible(!create);
        createNewSchemaVersionButton.setVisible(!create);
        deleteSchemaVersionButton.setVisible(!create);
        exportActionsButton.setVisible(!create);

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
        detailsTable.setWidget(2, 0, nameLabel);
        detailsTable.setWidget(2, 1, name);
        name.addInputHandler(this);

        description = new SizedTextArea(1024);
        description.setWidth("100%");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 80);
        
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(3, 0, descriptionLabel);
        detailsTable.setWidget(3, 1, description);
        description.addInputHandler(this);

        detailsTable.getCellFormatter().setVerticalAlignment(5, 0, HasVerticalAlignment.ALIGN_TOP);
        
        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());

        schemaForm = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(900).createConfig(),
                Utils.constants.schema(), this, !create, !create);
        
        if (create) {
            schemaForm.addValueChangeHandler(this);
        }
        getFooter().setWidth("1000px");
        getFooter().add(schemaForm);

        name.setFocus(true);
    }

    @Override
    protected void resetImpl() {
        version.reset();
        name.setValue("");
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        schemaForm.reset();
    }

    @Override
    public ValueListBox<Integer> getVersion() {
        return version;
    }

    @Override
    protected boolean validate() {
        boolean result = !create || schemaForm.validate();
        return result;
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        fireChanged();
    }

    @Override
    public RecordPanel getSchemaForm() {
        return schemaForm;
    }
    
    @Override
    public HasValue<String> getName() {
        return name;
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
    public HasClickHandlers getCreateNewSchemaVersionButton() {
        return createNewSchemaVersionButton;
    }

    @Override
    public HasClickHandlers getDeleteSchemaVersionButton() {
        return deleteSchemaVersionButton;
    }

    @Override
    public ActionsButton getExportActionsButton() {
        return exportActionsButton;
    }

}
