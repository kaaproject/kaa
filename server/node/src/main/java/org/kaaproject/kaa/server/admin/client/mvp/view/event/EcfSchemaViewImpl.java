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

import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class EcfSchemaViewImpl extends BaseDetailsViewImpl implements EcfSchemaView, ValueChangeHandler<RecordField> {

    private SizedTextBox version;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    
    private RecordPanel ecfSchemaForm;

    public EcfSchemaViewImpl(boolean create) {
        super(create, create);
    }

    @Override
    public HasValue<String> getVersion() {
        return version;
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
    public RecordPanel getEcfSchemaForm() {
        return ecfSchemaForm;
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addEcfSchema();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.ecfSchema();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.ecfSchemaDetails();
    }

    @Override
    protected void initDetailsTable() {
        
        Label versionLabel = new Label(Utils.constants.version());
        version = new KaaAdminSizedTextBox(-1, false);
        version.setWidth("100%");
        detailsTable.setWidget(0, 0, versionLabel);
        detailsTable.setWidget(0, 1, version);
        
        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(1, 0, authorLabel);
        detailsTable.setWidget(1, 1, createdUsername);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(2, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(2, 1, createdDateTime);
        
        Label schemaLabel = new Label(Utils.constants.schema());
        detailsTable.setWidget(3, 0, schemaLabel);
        
        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());
        
        ecfSchemaForm = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(900).createConfig(),
                Utils.constants.schema(), this, !create, !create);
        
        if (create) {
            ecfSchemaForm.addValueChangeHandler(this);
        }
        getFooter().setWidth("1000px");
        getFooter().add(ecfSchemaForm);
    }

    @Override
    protected void resetImpl() {
        version.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        ecfSchemaForm.reset();
    }

    @Override
    protected boolean validate() {
        if (create) {
            if (!ecfSchemaForm.validate()) {
                return false;
            }
            RecordField value = ecfSchemaForm.getValue();
            if (value == null) {
                return false;
            }
            if (value.getValue().isEmpty()) {
                return false;
            }
            FormField field = value.getValue().get(0);
            if (field instanceof ArrayField) {
                return !((ArrayField)field).getValue().isEmpty();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        fireChanged();
    }


}
