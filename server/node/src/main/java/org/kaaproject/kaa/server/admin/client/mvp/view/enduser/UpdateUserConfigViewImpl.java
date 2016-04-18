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

package org.kaaproject.kaa.server.admin.client.mvp.view.enduser;

import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.UpdateUserConfigView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaInfoListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

public class UpdateUserConfigViewImpl extends BaseDetailsViewImpl implements UpdateUserConfigView, ValueChangeHandler<RecordField> {

    private SizedTextBox userId;
    private SchemaInfoListBox configurationSchemaInfo;
    private RecordPanel configurationData;
    
    public UpdateUserConfigViewImpl() {
        super(true);
    }
    
    @Override
    protected void initDetailsTable() {
        
        int row=0;
        
        Label label = new Label(Utils.constants.userId());
        label.addStyleName(Utils.avroUiStyle.requiredField());
        userId = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        userId.setWidth(FULL_WIDTH);
        userId.addInputHandler(this);
        detailsTable.setWidget(row, 0, label);
        detailsTable.setWidget(row, 1, userId);
        row++;
        
        label = new Label(Utils.constants.configurationSchema());
        label.addStyleName(Utils.avroUiStyle.requiredField());
        configurationSchemaInfo = new SchemaInfoListBox();
        detailsTable.setWidget(row, 0, label);
        detailsTable.setWidget(row, 1, configurationSchemaInfo);
        configurationSchemaInfo.addValueChangeHandler(new ValueChangeHandler<SchemaInfoDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<SchemaInfoDto> event) {
                updateConfigurationData(event.getValue());
            }
        });
        
        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());
        
        configurationData = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(900).gridHeight(250).tableHeight(230).createConfig(),
                Utils.constants.configurationBody(), this, false, false);
        
        configurationData.addValueChangeHandler(this);
        getFooter().setWidth("1000px");
        getFooter().add(configurationData);
    }
    
    @Override
    protected String getCreateTitle() {
        return Utils.constants.updateConfiguration();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.updateConfiguration();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.configurationDetails();
    }
    
    private void updateConfigurationData(SchemaInfoDto value) {
        configurationData.setValue(value != null ? value.getSchemaForm() : null);
        fireChanged();
    }
    
    @Override
    public HasValue<String> getUserId() {
        return userId;
    }

    @Override
    public ValueListBox<SchemaInfoDto> getConfigurationSchemaInfo() {
        return configurationSchemaInfo;
    }

    @Override
    public HasValue<RecordField> getConfigurationData() {
        return configurationData;
    }

    @Override
    protected void resetImpl() {
        userId.setValue("");
        configurationSchemaInfo.reset();
        configurationData.reset();
    }

    @Override
    protected boolean validate() {
        boolean result = Utils.isNotBlank(userId.getValue());
        result &= configurationSchemaInfo.getValue() != null;
        result &= configurationData.validate();
        return result;
    }

    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        fireChanged();
    }
    
    @Override
    protected void updateSaveButton(boolean enabled, boolean invalid) {
        getSaveButtonWidget().setText(Utils.constants.update());
        getSaveButtonWidget().setEnabled(enabled);
    }

}
