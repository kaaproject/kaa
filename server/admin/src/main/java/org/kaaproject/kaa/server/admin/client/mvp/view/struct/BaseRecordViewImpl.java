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

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class BaseRecordViewImpl<T extends AbstractStructureDto,V> extends BaseDetailsViewImpl implements BaseRecordView<T,V>, ValueChangeHandler<SchemaDto> {

    private SchemaListBox schema;
    private SizedTextBox schemaVersion;
    private AbstractRecordPanel<T,V> recordPanel;

    public BaseRecordViewImpl(boolean create) {
        super(create);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        backButtonPanel.setVisible(true);
    }

    @Override
    protected void initDetailsTable() {

        detailsTable.getColumnFormatter().setWidth(0, "200px");
        detailsTable.getColumnFormatter().setWidth(1, "500px");

        Label schemaLabel = new Label(Utils.constants.schemaVersion());
        detailsTable.setWidget(0, 0, schemaLabel);

        if (create) {
            schemaLabel.addStyleName(Utils.avroUiStyle.requiredField());
            schema = new SchemaListBox();
            schema.setWidth("80px");
            VerticalPanel panel = new VerticalPanel();
            panel.setWidth("100%");
            panel.add(schema);
            panel.add(new HTML("&nbsp;"));
            detailsTable.setWidget(0, 1, panel);
            schema.addValueChangeHandler(this);
        }
        else {
            schemaVersion = new KaaAdminSizedTextBox(-1, false);
            schemaVersion.setWidth("100%");
            detailsTable.setWidget(0, 1, schemaVersion);
        }

        recordPanel = createRecordPanel();
        detailsTable.setWidget(1, 0, recordPanel);
        detailsTable.getFlexCellFormatter().setColSpan(1, 0, 2);

    }

    @Override
    public void onValueChange(ValueChangeEvent<SchemaDto> event) {
        recordPanel.setSchemaSelected(schema.getValue() != null);
    }

    protected abstract AbstractRecordPanel<T,V> createRecordPanel();

    @Override
    protected void resetImpl() {
        recordPanel.reset();
        if (create) {
            schema.reset();
        }
        else {
            schemaVersion.setValue("");
        }
    }

    @Override
    protected boolean validate() {
        return false;
    }

    @Override
    public SchemaListBox getSchema() {
        return schema;
    }

    @Override
    public HasValue<String> getSchemaVersion() {
        return schemaVersion;
    }

    @Override
    public AbstractRecordPanel<T,V> getRecordPanel() {
        return recordPanel;
    }


}
