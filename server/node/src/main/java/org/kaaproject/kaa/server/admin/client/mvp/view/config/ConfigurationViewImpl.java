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

package org.kaaproject.kaa.server.admin.client.mvp.view.config;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.ConfigurationView;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.AbstractRecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.BaseRecordViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.VersionListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConfigurationViewImpl extends BaseRecordViewImpl<ConfigurationRecordFormDto, RecordField> 
                        implements ConfigurationView {

    private VersionListBox schema;
    private SizedTextBox schemaVersion;

    public ConfigurationViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected AbstractRecordPanel<ConfigurationRecordFormDto, RecordField> createRecordPanel() {
        return new ConfigurationPanel(this);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.configuration();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.configuration();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.configurationDetails();
    }

    @Override
    protected int initDetailsTableImpl() {
        int row = -1;
        Label schemaLabel = new Label(Utils.constants.schemaVersion());
        detailsTable.setWidget(++row, 0, schemaLabel);

        if (create) {
            schemaLabel.addStyleName(Utils.avroUiStyle.requiredField());
            schema = new VersionListBox();
            schema.setWidth("80px");
            VerticalPanel panel = new VerticalPanel();
            panel.setWidth("100%");
            panel.add(schema);
            panel.add(new HTML("&nbsp;"));
            detailsTable.setWidget(row, 1, panel);
            schema.addValueChangeHandler(this);
        } else {
            schemaVersion = new KaaAdminSizedTextBox(-1, false);
            schemaVersion.setWidth("100%");
            detailsTable.setWidget(row, 1, schemaVersion);
        }

        return row;
    }
    
    @Override
    protected void resetImpl() {
        super.resetImpl();
        if (create) {
            schema.reset();
        } else {
            schemaVersion.setValue("");
        }
    }

    @Override
    public VersionListBox getSchema() {
        return schema;
    }

    @Override
    public HasValue<String> getSchemaVersion() {
        return schemaVersion;
    }

}
