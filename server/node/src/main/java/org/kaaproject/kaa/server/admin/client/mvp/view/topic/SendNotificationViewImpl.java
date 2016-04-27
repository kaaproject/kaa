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

import java.util.Date;

import com.google.gwt.user.client.ui.TextBox;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.SendNotificationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaInfoListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.datepicker.client.DateBox;


public class SendNotificationViewImpl extends BaseDetailsViewImpl implements SendNotificationView, ValueChangeHandler<RecordField> {

    private SchemaInfoListBox notificationSchemaInfo;
    private DateBox expiredAt;
    private RecordPanel notificationData;
    private TextBox endpointKeyHashTextBox;
    
    public SendNotificationViewImpl() {
        super(true);
    }
    
    @Override
    protected void initDetailsTable() {
        
        int row=0;
        
        Label label = new Label(Utils.constants.notificationSchema());
        label.addStyleName(Utils.avroUiStyle.requiredField());
        notificationSchemaInfo = new SchemaInfoListBox();
        detailsTable.setWidget(row, 0, label);
        detailsTable.setWidget(row, 1, notificationSchemaInfo);
        notificationSchemaInfo.addValueChangeHandler(new ValueChangeHandler<SchemaInfoDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<SchemaInfoDto> event) {
                updateNotificationData(event.getValue());
            }
        });
        row++;
        
        label = new Label(Utils.constants.expiresAt());
        expiredAt = new DateBox();
        expiredAt.setWidth("200px");
        detailsTable.setWidget(row, 0, label);
        detailsTable.setWidget(row, 1, expiredAt);
        row++;

        label = new Label(Utils.constants.endpointKeyHash());
        endpointKeyHashTextBox = new TextBox();
        endpointKeyHashTextBox.setWidth("200px");
        detailsTable.setWidget(row, 0, label);
        detailsTable.setWidget(row, 1, endpointKeyHashTextBox);

        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());
        
        notificationData = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(900).gridHeight(250).tableHeight(230).createConfig(),
                Utils.constants.notificationBody(), this, false, false);
        
        notificationData.addValueChangeHandler(this);
        getFooter().setWidth("1000px");
        getFooter().add(notificationData);
    }
    
    @Override
    protected String getCreateTitle() {
        return Utils.constants.sendNotification();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.sendNotification();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.notificationDetails();
    }

    @Override
    public HasValue<String> getEndpointKeyHash(){
        return endpointKeyHashTextBox;
    }
    
    private void updateNotificationData(SchemaInfoDto value) {
        notificationData.setValue(value != null ? value.getSchemaForm() : null);
        fireChanged();
    }

    @Override
    public ValueListBox<SchemaInfoDto> getNotificationSchemaInfo() {
        return notificationSchemaInfo;
    }

    @Override
    public HasValue<Date> getExpiredAt() {
        return expiredAt;
    }

    @Override
    public HasValue<RecordField> getNotificationData() {
        return notificationData;
    }

    @Override
    protected void resetImpl() {
        notificationSchemaInfo.reset();
        expiredAt.setValue(null);
        notificationData.reset();
    }

    @Override
    protected boolean validate() {
        boolean result = notificationSchemaInfo.getValue() != null;
        result &= notificationData.validate();
        return result;
    }

    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        fireChanged();
    }
    
    @Override
    protected void updateSaveButton(boolean enabled, boolean invalid) {
        getSaveButtonWidget().setText(Utils.constants.send());
        getSaveButtonWidget().setEnabled(enabled);
    }

}
