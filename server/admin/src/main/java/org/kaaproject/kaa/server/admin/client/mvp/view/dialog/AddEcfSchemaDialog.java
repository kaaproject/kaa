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

package org.kaaproject.kaa.server.admin.client.mvp.view.dialog;

import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.AlertPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.FileUploadForm;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddEcfSchemaDialog extends KaaDialog implements ChangeHandler, HasErrorMessage {

    private AlertPanel errorPanel;

    private FileUploadForm schemaFileUpload;

    private String ecfId;
    private String fileItemName;

    private Button addButton;
    
    private Listener listener;

    public static void showAddEcfSchemaDialog(String ecfId, Listener listener) {
        AddEcfSchemaDialog dialog = new AddEcfSchemaDialog(ecfId, listener);
        dialog.center();
        dialog.show();
    }

    public AddEcfSchemaDialog(String ecfId, Listener listener) {
        super(false, true);

        this.ecfId = ecfId;
        this.listener = listener;

        setWidth("500px");

        setTitle(Utils.constants.addEcfSchema());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);

        int row=0;

        Widget label = new Label(Utils.constants.selectSchemaFile());
        label.addStyleName(Utils.avroUiStyle.requiredField());
        schemaFileUpload = new FileUploadForm();
        schemaFileUpload.setWidth("200px");

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, schemaFileUpload);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        dialogContents.add(table);

        schemaFileUpload.addChangeHandler(this);
        fileItemName = schemaFileUpload.getFileItemName();

        schemaFileUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                performAdd();
            }
        });

        addButton = new Button(Utils.constants.add(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                schemaFileUpload.submit();
            }
        });

        Button closeButton = new Button(Utils.constants.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                AddEcfSchemaDialog.this.listener.onClose();
            }
        });
        addButton(addButton);
        addButton(closeButton);

        addButton.setEnabled(false);
    }

    @Override
    public void onChange(ChangeEvent event) {
        fireChanged();
    }

    private void fireChanged() {
        boolean valid = validate();
        addButton.setEnabled(valid);
    }

    private void performAdd () {
        KaaAdmin.getDataSource().addEcfSchema(ecfId, fileItemName, 
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Utils.handleException(caught, AddEcfSchemaDialog.this);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        hide();
                        listener.onAdd();
                    }
          });
    }

    private boolean validate() {
        return schemaFileUpload.getFileName().length()>0;
    }

    public interface Listener {

        public void onAdd();

        public void onClose();

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

}
