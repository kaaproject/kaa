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

package org.kaaproject.kaa.server.admin.client.mvp.view.event;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

public class EcfViewImpl extends BaseDetailsViewImpl implements EcfView {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();
    
    private SizedTextBox name;
    private SizedTextBox namespace;
    private SizedTextBox className;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private EcfSchemasGrid ecfSchemasGrid;
    private Button addEcfSchemaButton;

    public EcfViewImpl(boolean create) {
        super(create);
    }

    @Override
    public HasValue<String> getName() {
        return name;
    }

    @Override
    public HasValue<String> getNamespace() {
        return namespace;
    }

    @Override
    public HasValue<String> getClassName() {
        return className;
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
    public AbstractGrid<EventSchemaVersionDto, Integer> getEcfSchemasGrid() {
        return ecfSchemasGrid;
    }

    @Override
    public HasClickHandlers getAddEcfSchemaButton() {
        return addEcfSchemaButton;
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNewEcf();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.ecf();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.ecfDetails();
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
        
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName(REQUIRED);
        name = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth("100%");
        detailsTable.setWidget(2, 0, nameLabel);
        detailsTable.setWidget(2, 1, name);
        name.addInputHandler(this);

        Label namespaceLabel = new Label(Utils.constants.namespace());
        Label classNameLabel = new Label(Utils.constants.className());
        if (create) {  
            namespaceLabel.addStyleName(REQUIRED);
            classNameLabel.addStyleName(REQUIRED);
            namespace = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
            namespace.addInputHandler(this);
            className = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
            className.addInputHandler(this);
        } else {
            namespace = new KaaAdminSizedTextBox(-1, false);
            className = new KaaAdminSizedTextBox(-1, false);
        }
        namespace.setWidth("100%");
        className.setWidth("100%");
        
        detailsTable.setWidget(3, 0, namespaceLabel);
        detailsTable.setWidget(3, 1, namespace);

        detailsTable.setWidget(4, 0, classNameLabel);
        detailsTable.setWidget(4, 1, className);

        description = new SizedTextArea(1024);
        description.setWidth("100%");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(5, 0, descriptionLabel);
        detailsTable.setWidget(5, 1, description);
        description.addInputHandler(this);

        detailsTable.getCellFormatter().setVerticalAlignment(5, 0, HasVerticalAlignment.ALIGN_TOP);
        
        ecfSchemasGrid = new EcfSchemasGrid();
        ecfSchemasGrid.setSize("700px", "200px");
        Label ecfSchemasLabel = new Label(Utils.constants.schemas());
        ecfSchemasLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());

        addEcfSchemaButton = new Button(Utils.constants.addSchema());
        addEcfSchemaButton.addStyleName(Utils.kaaAdminStyle.bAppButtonSmall());

        detailsTable.setWidget(6, 0, ecfSchemasLabel);
        ecfSchemasLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);

        detailsTable.setWidget(7, 0, ecfSchemasGrid);
        detailsTable.getFlexCellFormatter().setColSpan(7, 0, 3);

        detailsTable.setWidget(8, 2, addEcfSchemaButton);
        addEcfSchemaButton.getElement().getParentElement().getStyle().setPropertyPx("paddingTop", 15);
        detailsTable.getCellFormatter().setHorizontalAlignment(8, 2, HasHorizontalAlignment.ALIGN_RIGHT);
        
        ecfSchemasLabel.setVisible(!create);
        ecfSchemasGrid.setVisible(!create);
        addEcfSchemaButton.setVisible(!create);
        
        name.setFocus(true);
    }

    @Override
    protected void resetImpl() {
        name.setValue("");
        namespace.setValue("");
        className.setValue("");
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
    }

    @Override
    protected boolean validate() {
        boolean result = name.getValue().length()>0;
        result &= namespace.getValue().length()>0;
        result &= className.getValue().length()>0;
        return result;
    }

}
