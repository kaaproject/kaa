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

package org.kaaproject.kaa.server.admin.client.mvp.view.ctl;

import org.kaaproject.avro.ui.gwt.client.widget.ActionsButton;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.IntegerListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CtlSchemaViewImpl extends BaseDetailsViewImpl implements CtlSchemaView, 
                                                                        ValueChangeHandler<RecordField> {
    
    private Label scope;
    private IntegerListBox version;
    private Button createNewSchemaVersionButton;
    private Button updateSchemaScopeButton;
    private Button deleteSchemaVersionButton;
    private ActionsButton exportActionsButton;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;

    private RecordPanel schemaForm;
    
    public CtlSchemaViewImpl(boolean create, boolean editable) {
        super(create, editable);
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
    protected Widget getBackButtonPanelWidget() {
        return backButtonPanelWidget;
    }
    
    @Override
    protected Button getBackButtonWidget() {
        return backButtonWidget;
    }
    
    @Override
    protected Label getTitileLabelWidget() {
        return titleLabelWidget;
    }

    @Override
    protected Button getSaveButtonWidget() {
        return saveButtonWidget;
    }

    @Override
    protected Button getCancelButtonWidget() {
        return cancelButtonWidget;
    }

    private Widget backButtonPanelWidget;
    private Button backButtonWidget;
    private Label titleLabelWidget;
    private Button saveButtonWidget;
    private Button cancelButtonWidget;
    
    @Override
    protected void constructTopPanel() {
        FlexTable flexTable = new FlexTable();
        flexTable.setCellSpacing(0);
        flexTable.setCellPadding(0);
        flexTable.setHeight("100%");
        topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        topPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        topPanel.add(flexTable);
        topPanel.setCellHeight(flexTable, "100%");
        
        HorizontalPanel backButtonPanel = new HorizontalPanel();
        backButtonPanel.setHeight("100%");
        backButtonPanel.addStyleName(Utils.kaaAdminStyle.bAppPaddedPanel());
        backButtonPanel.setVisible(false);  
        
        flexTable.setWidget(0, 0, backButtonPanel);
        flexTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        
        backButtonPanelWidget = backButtonPanel;
        
        Button backButton = new Button();
        backButton.addStyleName(Utils.kaaAdminStyle.bAppBackButton());        
        backButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        backButtonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        backButtonPanel.add(backButton);
        backButtonPanel.setCellHeight(backButton, "100%");
        
        backButtonWidget = backButton;
        
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHeight("100%");
        flexTable.setWidget(0, 1, verticalPanel);
        flexTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

        HorizontalPanel firstRowPanel = new HorizontalPanel();
        firstRowPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        firstRowPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        verticalPanel.add(firstRowPanel);

        HorizontalPanel secondRowPanel = new HorizontalPanel();
        secondRowPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        secondRowPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
        if (!create) {
            backButton.getElement().getStyle().setPaddingTop(25, Unit.PX);
            backButton.getElement().getStyle().setPaddingBottom(25, Unit.PX);
            firstRowPanel.setHeight("45px");
            firstRowPanel.getElement().getStyle().setPaddingBottom(5, Unit.PX);
            secondRowPanel.setHeight("45px");
            topPanel.setHeight("105px");
            topPanel.getElement().getStyle().setPaddingTop(10, Unit.PX);
            updateNorthSize(185);
        } else {
            firstRowPanel.setHeight("70px");
            secondRowPanel.setHeight("100%");
            topPanel.setHeight("80px");
            updateNorthSize(175);
        }
        verticalPanel.add(secondRowPanel);

        Label titleLabel = new Label();
        titleLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitle());
        firstRowPanel.add(titleLabel);
        firstRowPanel.setCellHeight(titleLabel, "100%");
        
        titleLabelWidget = titleLabel;        
        
        int horizontalMargin = 15;
        
        scope = new Label();
        scope.getElement().getStyle().setFontSize(16, Unit.PX);
        scope.getElement().getStyle().setFontWeight(FontWeight.NORMAL);
        scope.getElement().getStyle().setMarginLeft(horizontalMargin, Unit.PX);
        
        firstRowPanel.add(scope);
        firstRowPanel.setCellHeight(scope, "100%");
        
        updateSchemaScopeButton = new Button(Utils.constants.promote());
        updateSchemaScopeButton.setVisible(!create && editable);
        updateSchemaScopeButton.getElement().getStyle().setMarginLeft(horizontalMargin, Unit.PX);

        if (hasUpdateScopeOption()) {
            firstRowPanel.add(updateSchemaScopeButton);
            firstRowPanel.setCellHeight(updateSchemaScopeButton, "100%");
        }
        
        Label versionLabel = new Label(Utils.constants.version());
        versionLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitle());
        versionLabel.getElement().getStyle().setFontSize(16, Unit.PX);
        versionLabel.getElement().getStyle().setFontWeight(FontWeight.NORMAL);
        version = new IntegerListBox();
        version.getElement().getStyle().setPadding(5, Unit.PX);
        version.getElement().getStyle().setMarginLeft(10, Unit.PX);
        HorizontalPanel versionPanel = new HorizontalPanel();
        versionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        versionPanel.add(versionLabel);
        versionPanel.add(version);
        versionPanel.setVisible(!create);
        
        secondRowPanel.add(versionPanel);
        secondRowPanel.setCellHeight(versionPanel, "100%");
        
        createNewSchemaVersionButton = new Button(Utils.constants.createNewVersion());
        createNewSchemaVersionButton.setVisible(!create && editable);
        createNewSchemaVersionButton.getElement().getStyle().setMarginLeft(horizontalMargin, Unit.PX);

        if (!create && editable) {
            secondRowPanel.add(createNewSchemaVersionButton);
            secondRowPanel.setCellHeight(createNewSchemaVersionButton, "100%");
        }
        
        exportActionsButton = new ActionsButton(Utils.resources.export(), Utils.constants.export());
        exportActionsButton.setVisible(!create);
        exportActionsButton.getElement().getStyle().setMarginLeft(horizontalMargin, Unit.PX);

        if (!create) {
            secondRowPanel.add(exportActionsButton);
            secondRowPanel.setCellHeight(exportActionsButton, "100%");
        }
        
        Button saveButton = new Button();
        saveButton.getElement().getStyle().setMarginLeft(horizontalMargin, Unit.PX);
        
        if (create) {
            firstRowPanel.add(saveButton);
            firstRowPanel.setCellHeight(saveButton, "100%");
        } 
        
        saveButtonWidget = saveButton;
        
        Button cancelButton = new Button();
        cancelButton.setVisible(false);
        cancelButton.getElement().getStyle().setMarginLeft(horizontalMargin, Unit.PX);        
        
        if (create) {
            firstRowPanel.add(cancelButton);
            firstRowPanel.setCellHeight(cancelButton, "100%");
        } 
        
        cancelButtonWidget = cancelButton;
        
        deleteSchemaVersionButton = new Button(Utils.constants.delete());
        deleteSchemaVersionButton.addStyleName(Utils.kaaAdminStyle.deleteButton());
        deleteSchemaVersionButton.setVisible(!create && editable);
        deleteSchemaVersionButton.getElement().getStyle().setMarginLeft(horizontalMargin, Unit.PX);
        
        if (!create && editable) {
            secondRowPanel.add(deleteSchemaVersionButton);
            secondRowPanel.setCellHeight(deleteSchemaVersionButton, "100%");
        }
    }

    @Override
    protected void initDetailsTable() {
        if (create) {
            requiredFieldsNoteLabel.setVisible(true);
        } else {
            requiredFieldsNoteLabel.setVisible(false);
            requiredFieldsNoteLabel.getElement().getParentElement().getStyle().setHeight(0, Unit.PX);
        }
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
        
        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());

        schemaForm = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(900).createConfig(), false,
                null, this, !create, !create);
        
        if (create) {
            schemaForm.addValueChangeHandler(this);
        }
        getFooter().setWidth("1000px");
        getFooter().add(schemaForm);
    }
    
    protected boolean hasUpdateScopeOption() {
        return false;
    }

    @Override
    protected void resetImpl() {
        version.reset();
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
    public HasClickHandlers getUpdateSchemaScopeButton() {
        return updateSchemaScopeButton;
    }

    @Override
    public HasClickHandlers getDeleteSchemaVersionButton() {
        return deleteSchemaVersionButton;
    }

    @Override
    public ActionsButton getExportActionsButton() {
        return exportActionsButton;
    }

    @Override
    public Label getScope() {
        return scope;
    }

}
