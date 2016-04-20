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

package org.kaaproject.kaa.server.admin.client.mvp.view.dialog;

import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.FormPopup;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaInfoListBox;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EditSchemaRecordDialog extends FormPopup implements ValueChangeHandler<SchemaInfoDto>, HasErrorMessage {

    private AlertPanel errorPanel;

    private SchemaInfoListBox schemaBox;
    private RecordPanel schemaRecordPanel;
    private Button saveButton;
    private Listener listener;

    public static void showEditSchemaRecordDialog(Listener listener, String title, List<SchemaInfoDto> schemas, int schemaVersion) {
        EditSchemaRecordDialog dialog = new EditSchemaRecordDialog(listener, title, schemas, schemaVersion);
        dialog.center();
        dialog.show();
    }

    public EditSchemaRecordDialog(Listener listener, String title, List<SchemaInfoDto> schemas, int schemaVersion) {
        setWidth("100%");
        setTitle(title);
        this.listener = listener;
        
        SchemaInfoDto currentValue = null;
        for (SchemaInfoDto schema : schemas) {
            if (schema.getVersion() == schemaVersion) {
                currentValue = schema;
                break;
            }
        }

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);
        table.addStyleName(Utils.avroUiStyle.fieldWidget());

        int row = 0;

        Widget schemaVersionLabel = new Label(Utils.constants.schemaVersion());
        schemaVersionLabel.addStyleName(Utils.avroUiStyle.requiredField());
        schemaBox = new SchemaInfoListBox();        
        schemaBox.setWidth("50px");        
        schemaBox.getElement().getStyle().setMarginLeft(10, Unit.PX);
        schemaBox.setValue(currentValue);
        schemaBox.setAcceptableValues(schemas);        
        schemaBox.addValueChangeHandler(this);
        
        HorizontalPanel versionPanel = new HorizontalPanel();
        versionPanel.add(schemaVersionLabel);
        versionPanel.add(schemaBox);
        
        table.setWidget(row, 0, versionPanel);
        
        schemaRecordPanel = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(700).createConfig(),
                Utils.constants.schema(), this, false, false);
        
        //schemaRecordPanel.setWidth("750px");
        schemaRecordPanel.getElement().getStyle().setPropertyPx("minWidth", 750);
        schemaRecordPanel.getRecordWidget().setForceNavigation(true);
        schemaRecordPanel.setPreferredHeightPx(250);
        schemaRecordPanel.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> event) {
                validate();
            }
        });
        
        table.setWidget(++row, 0, schemaRecordPanel);
        table.getFlexCellFormatter().setColSpan(row, 0, 2);
        
        dialogContents.add(table);

        saveButton = new Button(Utils.constants.save(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                performSave();
            }
        });

        Button cancelButton = new Button(Utils.constants.cancel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                EditSchemaRecordDialog.this.listener.onCancel();
            }
        });
        addButton(saveButton);
        addButton(cancelButton);

        updateValue(currentValue);

        saveButton.setEnabled(false);
    }

    @Override
    public void onValueChange(ValueChangeEvent<SchemaInfoDto> event) {
        updateValue(event.getValue());
    }
    
    private void updateValue(SchemaInfoDto schemaInfo) {
        schemaRecordPanel.setValue(schemaInfo.getSchemaForm());
        schemaRecordPanel.setTitle(schemaInfo.getSchemaName());
        validate();
    }
    
    private void validate() {
        boolean valid = schemaRecordPanel.validate();
        saveButton.setEnabled(valid);
    }

    private void performSave () {
        hide();
        listener.onSave(schemaBox.getValue());
    }

    @Override
    public void clearError() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        errorPanel.setVisible(true);
    }
    
    public interface Listener {

        public void onSave(SchemaInfoDto newValue);

        public void onCancel();

    }

}
