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

package org.kaaproject.kaa.server.admin.client.mvp.view.plugin;

import static org.kaaproject.kaa.server.admin.client.util.Utils.isNotBlank;

import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePluginView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.PluginInfoListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

public abstract class BasePluginViewImpl extends BaseDetailsViewImpl implements BasePluginView,                                                                        
                                                                        ValueChangeHandler<RecordField> {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();

    private SizedTextBox name;
    private PluginInfoListBox pluginInfo;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private RecordPanel configuration;

    public BasePluginViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {
        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false);
        createdUsername.setWidth(FULL_WIDTH);
        int idx = 0;
        detailsTable.setWidget(idx, 0, authorLabel);
        detailsTable.setWidget(idx, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false);
        createdDateTime.setWidth(FULL_WIDTH);
        
        idx++;        
        detailsTable.setWidget(idx, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(idx, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        name = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth(FULL_WIDTH);
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName(REQUIRED);
        idx++;
        detailsTable.setWidget(idx, 0, nameLabel);
        detailsTable.setWidget(idx, 1, name);
        name.addInputHandler(this);
        
        idx = initPluginDetails(idx); 

        description = new SizedTextArea(1024);
        description.setWidth(FULL_WIDTH);
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        idx++;
        detailsTable.setWidget(idx, 0, descriptionLabel);
        detailsTable.setWidget(idx, 1, description);
        detailsTable.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);
        description.addInputHandler(this);

        Label typeLabel = new Label(Utils.constants.type());
        pluginInfo = new PluginInfoListBox();
        pluginInfo.setEnabled(create);
        pluginInfo.addValueChangeHandler(new ValueChangeHandler<PluginInfoDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<PluginInfoDto> event) {
                updatePluginConfiguration(event.getValue());
            }
        });

        idx++;
        detailsTable.setWidget(idx, 0, typeLabel);
        detailsTable.setWidget(idx, 1, pluginInfo);

        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());
        getFooter().setWidth("1000px");

        configuration = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(900).createConfig(),
                Utils.constants.configuration(), this, !create, false);
        configuration.addValueChangeHandler(this);
        getFooter().add(configuration);
        name.setFocus(true);
    }
    
    protected abstract int initPluginDetails(int idx);

    @Override
    protected void resetImpl() {
        name.setValue("");
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        if (pluginInfo != null) {
            pluginInfo.setValue(null, true);
        }
        configuration.reset();
    }

    @Override
    protected boolean validate() {
        boolean result = isNotBlank(name.getValue());
        result &= configuration.validate();
        return result;
    }

    @Override
    public HasValue<String> getName() {
        return name;
    }

    @Override
    public RecordPanel getSchemaForm() {
        return configuration;
    }
    @Override
    public ValueListBox<PluginInfoDto> getPluginInfo() {
        return pluginInfo;
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
    public HasValue<RecordField> getConfiguration() {
        return configuration;
    }
    
    private void updatePluginConfiguration(PluginInfoDto value) {
        configuration.setValue(value != null ? value.getFieldConfiguration() : null);
        fireChanged();
    }

    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        fireChanged();
    }
}
