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

package org.kaaproject.kaa.server.admin.client.mvp.view.schema;

import org.kaaproject.kaa.server.admin.client.mvp.view.BaseSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.FileUploadForm;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextArea;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.common.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.kaa.server.common.avro.ui.gwt.client.widget.SizedTextBox;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

public abstract class BaseSchemaViewImpl extends BaseDetailsViewImpl implements BaseSchemaView {

    private SizedTextBox version;
    private SizedTextBox name;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private SizedTextBox endpointCount;

    private SizedTextArea schema;
    private FileUploadForm schemaFileUpload;

    public BaseSchemaViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {

        Label versionLabel = new Label(Utils.constants.version());
        version = new KaaAdminSizedTextBox(-1, false);
        version.setWidth("100%");
        detailsTable.setWidget(0, 0, versionLabel);
        detailsTable.setWidget(0, 1, version);
        versionLabel.setVisible(!create);
        version.setVisible(!create);

        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(1, 0, authorLabel);
        detailsTable.setWidget(1, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(2, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(2, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        Label endpointCountLabel = new Label(Utils.constants.numberOfEndpoints());
        endpointCount = new KaaAdminSizedTextBox(-1, false);
        endpointCount.setWidth("100%");
        detailsTable.setWidget(3, 0, endpointCountLabel);
        detailsTable.setWidget(3, 1, endpointCount);

        endpointCountLabel.setVisible(!create);
        endpointCount.setVisible(!create);

        name = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth("100%");
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName("required");
        detailsTable.setWidget(4, 0, nameLabel);
        detailsTable.setWidget(4, 1, name);
        name.addInputHandler(this);

        description = new KaaAdminSizedTextArea(1024);
        description.setWidth("100%");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(5, 0, descriptionLabel);
        detailsTable.setWidget(5, 1, description);
        description.addInputHandler(this);

        detailsTable.getCellFormatter().setVerticalAlignment(5, 0, HasVerticalAlignment.ALIGN_TOP);

        Label schemaLabel = new Label(create ? Utils.constants.selectSchemaFile() : Utils.constants.schema());
        if (create) {
            schemaLabel.addStyleName("required");
        }
        detailsTable.setWidget(6, 0, schemaLabel);

        if (create) {
            schemaFileUpload = new FileUploadForm();
            schemaFileUpload.setWidth("500px");
            detailsTable.setWidget(6, 1, schemaFileUpload);
        }
        else {
            schema = new KaaAdminSizedTextArea(524288);
            schema.setWidth("500px");
            schema.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 300);
            schema.getTextArea().setReadOnly(true);
            detailsTable.setWidget(6, 1, schema);
            detailsTable.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);
        }


        if (create) {
            schemaFileUpload.addChangeHandler(this);
        }

        name.setFocus(true);
    }

    @Override
    protected void resetImpl() {
        version.setValue("");
        version.setValue("");
        name.setValue("");
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        endpointCount.setValue("");
        if (create) {
            schemaFileUpload.reset();
        }
        else {
            schema.setValue("");
        }
    }

    @Override
    public HasValue<String> getVersion() {
        return version;
    }

    @Override
    protected boolean validate() {
        boolean result = name.getValue().length()>0;
        if (create) {
            result &= schemaFileUpload.getFileName().length()>0;
        }
        return result;
    }

    @Override
    public HasValue<String> getSchema() {
        return schema;
    }

    @Override
    public FileUploadForm getSchemaFileUpload() {
        return schemaFileUpload;
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
    public HasValue<String> getEndpointCount() {
        return endpointCount;
    }

}
