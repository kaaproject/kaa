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

import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.FormPopup;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TestProfileFilterDialog extends FormPopup implements HasErrorMessage, 
                                                                     ErrorMessageCustomizer {

    private AlertPanel matchedPanel;
    private AlertPanel notMatchedPanel;
    private AlertPanel errorPanel;

    private RecordPanel endpointProfileRecordPanel;
    private RecordPanel serverProfileRecordPanel;
    private SizedTextArea filterPanel;
    private Button testFilterButton;
    
    private TestProfileFilterDialogListener listener;
    
    private String endpointProfileSchemaId;
    private String serverProfileSchemaId;
    
    private SchemaInfoDto endpointProfile = null;
    private SchemaInfoDto serverProfile = null;
    
    private TabPanel profileRecordsPanel;

    public static void showTestProfileFilterDialog(TestProfileFilterDialogListener listener,
            String endpointProfileSchemaId, String serverProfileSchemaId, String filterBody) {
        TestProfileFilterDialog dialog = new TestProfileFilterDialog(listener, endpointProfileSchemaId, serverProfileSchemaId, filterBody);
        dialog.center();
        dialog.show();
    }

    public TestProfileFilterDialog(TestProfileFilterDialogListener listener, 
            String endpointProfileSchemaId, String serverProfileSchemaId, String filterBody) {
        setWidth("100%");
        setTitle(Utils.constants.testProfileFilter());
        
        this.listener = listener;
        this.endpointProfileSchemaId = endpointProfileSchemaId;
        this.serverProfileSchemaId = serverProfileSchemaId;

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        dialogContents.getElement().getStyle().setOverflow(Overflow.AUTO);
        setWidget(dialogContents);
        
        VerticalPanel infoPanel = new VerticalPanel();

        matchedPanel = new AlertPanel(AlertPanel.Type.SUCCESS);
        matchedPanel.setVisible(false);
        matchedPanel.setWidth("720px");
        matchedPanel.setMessage(Utils.constants.filterMatched());
        infoPanel.add(matchedPanel);
        
        notMatchedPanel = new AlertPanel(AlertPanel.Type.WARNING);
        notMatchedPanel.setVisible(false);
        notMatchedPanel.setWidth("720px");
        notMatchedPanel.setMessage(Utils.constants.filterNotMatched());
        infoPanel.add(notMatchedPanel);
        
        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        errorPanel.setWidth("720px");
        infoPanel.add(errorPanel);        
        
        infoPanel.setHeight("50px");
        
        dialogContents.add(infoPanel);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);
        table.addStyleName(Utils.avroUiStyle.fieldWidget());

        int row = 0;
        
        profileRecordsPanel = new TabPanel();
        table.setWidget(++row, 0, profileRecordsPanel);
        
        endpointProfileRecordPanel = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(700).createConfig(),
                Utils.constants.schema(), this, false, false);
        endpointProfileRecordPanel.setWidth("750px");
        endpointProfileRecordPanel.getRecordWidget().setForceNavigation(true);
        endpointProfileRecordPanel.setPreferredHeightPx(200);
        endpointProfileRecordPanel.setHeight("320px");
        endpointProfileRecordPanel.getElement().getStyle().setPropertyPx("maxHeight", 320);
        endpointProfileRecordPanel.getElement().getStyle().setOverflow(Overflow.AUTO);

        endpointProfileRecordPanel.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> event) {
                validate();
            }
        });
        
        serverProfileRecordPanel = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(700).createConfig(),
                Utils.constants.schema(), this, false, false);
        
        serverProfileRecordPanel.setWidth("750px");
        serverProfileRecordPanel.getRecordWidget().setForceNavigation(true);
        serverProfileRecordPanel.setPreferredHeightPx(200);
        serverProfileRecordPanel.setHeight("320px");
        serverProfileRecordPanel.getElement().getStyle().setPropertyPx("maxHeight", 320);
        serverProfileRecordPanel.getElement().getStyle().setOverflow(Overflow.AUTO);

        serverProfileRecordPanel.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> event) {
                validate();
            }
        });
        
        filterPanel = new SizedTextArea(-1);
        filterPanel.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 200);
        filterPanel.getTextArea().getElement().getStyle().setPropertyPx("maxHeight", 200);
        filterPanel.getTextArea().setWidth("725px");
        filterPanel.getTextArea().getElement().getStyle().setPropertyPx("maxWidth", 725);
        filterPanel.addInputHandler(new InputEventHandler() {
            @Override
            public void onInputChanged(InputEvent event) {
                validate();
            }
        });
        
        SpanElement span = Document.get().createSpanElement();
        span.appendChild(Document.get().createTextNode(Utils.constants.filterBody()));
        span.addClassName("gwt-Label");
        
        CaptionPanel filterBodyPanel = new CaptionPanel(span.getString(), true);     
        filterBodyPanel.setWidth("737px");
        
        filterBodyPanel.add(filterPanel);
        
        table.setWidget(++row, 0, filterBodyPanel);
        table.getFlexCellFormatter().setColSpan(row, 0, 2);

        dialogContents.add(table);

        testFilterButton = new Button(Utils.constants.testFilter(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                performTest();
            }
        });
        testFilterButton.setEnabled(false);

        Button closeButton = new Button(Utils.constants.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        addButton(testFilterButton);
        addButton(closeButton);
        
        filterPanel.setValue(filterBody);

        load();
    }
    
    @Override
    public void hide() {
        super.hide();
        TestProfileFilterDialog.this.listener.onClose(filterPanel.getValue());
    }
    
    private void load() {
        clearError();
        processLoad();
    }
    
    private void processLoad() {
        if (Utils.isNotBlank(endpointProfileSchemaId) && endpointProfile == null) {
            KaaAdmin.getDataSource().getEndpointProfileSchemaInfo(endpointProfileSchemaId, new BusyAsyncCallback<SchemaInfoDto>() {
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, TestProfileFilterDialog.this);
                }

                @Override
                public void onSuccessImpl(SchemaInfoDto result) {
                    endpointProfile = result;
                    endpointProfileRecordPanel.setValue(result.getSchemaForm());
                    endpointProfileRecordPanel.setTitle(result.getSchemaName());
                    profileRecordsPanel.add(endpointProfileRecordPanel, Utils.constants.endpointProfile());
                    processLoad();
                }
            });
        } else if (Utils.isNotBlank(serverProfileSchemaId) && serverProfile == null) {
            KaaAdmin.getDataSource().getServerProfileSchemaInfo(serverProfileSchemaId, new BusyAsyncCallback<SchemaInfoDto>() {
                @Override
                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, TestProfileFilterDialog.this);
                }

                @Override
                public void onSuccessImpl(SchemaInfoDto result) {
                    serverProfile = result;
                    serverProfileRecordPanel.setValue(result.getSchemaForm());
                    serverProfileRecordPanel.setTitle(result.getSchemaName());
                    profileRecordsPanel.add(serverProfileRecordPanel, Utils.constants.serverProfile());
                    processLoad();
                }
            });
        } else {
            profileRecordsPanel.selectTab(0);
            if (endpointProfile != null) {
                endpointProfileRecordPanel.getRecordWidget().onShown();
            }
            if (serverProfile != null) {
                serverProfileRecordPanel.getRecordWidget().onShown();
            }
            center();
            validate();
        }
    }
    
    private void validate() {
        boolean valid = endpointProfile == null || endpointProfileRecordPanel.validate();
        valid &= serverProfile == null || serverProfileRecordPanel.validate();
        valid &= !Utils.isBlank(filterPanel.getValue());
        testFilterButton.setEnabled(valid);
    }

    private void performTest () {
        clearMessages();
        RecordField endpointProfileRecord = null;
        RecordField serverProfileRecord = null;
        if (endpointProfile != null) {
            endpointProfileRecord = endpointProfileRecordPanel.getValue();
        }
        if (serverProfile != null) {
            serverProfileRecord = serverProfileRecordPanel.getValue();
        }
        String filterBody = filterPanel.getValue();
        KaaAdmin.getDataSource().testProfileFilter(endpointProfileRecord, serverProfileRecord, filterBody, new BusyAsyncCallback<Boolean>() {

            @Override
            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, TestProfileFilterDialog.this, TestProfileFilterDialog.this);
            }

            @Override
            public void onSuccessImpl(Boolean result) {
                matchedPanel.setVisible(result);
                notMatchedPanel.setVisible(!result);
            }
        });
    }
    
    private void clearMessages() {
        clearError();
        matchedPanel.setVisible(false);
        notMatchedPanel.setVisible(false);
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

    @Override
    public String customizeErrorMessage(Throwable caught) {
        return caught.getLocalizedMessage();
    }
    
    public static interface TestProfileFilterDialogListener {
        
        void onClose(String filterBody);
        
    }

}
