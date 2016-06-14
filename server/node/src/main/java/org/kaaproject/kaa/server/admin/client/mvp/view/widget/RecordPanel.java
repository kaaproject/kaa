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

package org.kaaproject.kaa.server.admin.client.mvp.view.widget;


import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.RecordFieldWidget;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class RecordPanel extends SimplePanel implements HasValue<RecordField>, ChangeHandler {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();
    
    private CaptionPanel recordCaption;
    private RecordFieldWidget recordFieldWidget;
    private FlexTable uploadTable;
    private FileUploadForm recordFileUpload;
    private Button uploadButton;    
    private String recordFileItemName;
    private boolean readOnly;
    private HasErrorMessage hasErrorMessage;
    private FormDataLoader formDataLoader;
    private boolean optional;
    private boolean showCaption;
    
    public RecordPanel(String title, HasErrorMessage hasErrorMessage, boolean optional, boolean readOnly) {
        this(null, title, hasErrorMessage, optional, readOnly);
    }

    public RecordPanel(AvroWidgetsConfig config, String title, HasErrorMessage hasErrorMessage, boolean optional, boolean readOnly) {
        this(config, true, title, hasErrorMessage, optional, readOnly);
    }

    public RecordPanel(AvroWidgetsConfig config, boolean showCaption, String title, HasErrorMessage hasErrorMessage, boolean optional, boolean readOnly) {
        this.showCaption = showCaption;
        this.optional = optional;
        this.readOnly = readOnly;
        this.hasErrorMessage = hasErrorMessage;
        FlexTable table = new FlexTable();
        table.setWidth("100%");
        if (config == null) {
            config = new AvroWidgetsConfig.Builder().createConfig();
        }
        
        recordFieldWidget = new RecordFieldWidget(config, readOnly);
        if (showCaption) {
            recordCaption = new CaptionPanel();
            setTitle(title);            
            recordCaption.setContentWidget(recordFieldWidget);        
            table.setWidget(0, 0, recordCaption);
        } else {
            table.setWidget(0, 0, recordFieldWidget);
        }
        
        Label uploadLabel = new Label(Utils.constants.uploadFromFile());
        recordFileUpload = new FileUploadForm();
        recordFileUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                loadRecordFromFile();
            }
        });
        recordFileUpload.addChangeHandler(this);
        recordFileItemName = recordFileUpload.getFileItemName();
        uploadButton = new Button(Utils.constants.upload(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                recordFileUpload.submit();
            }
        });
        uploadButton.addStyleName(Utils.kaaAdminStyle.bAppButtonSmall());
        uploadButton.setEnabled(false);
        uploadTable = new FlexTable();
        uploadTable.setWidget(0, 0, uploadLabel);
        uploadTable.setWidget(0, 1, recordFileUpload);
        uploadTable.setWidget(0, 2, uploadButton);
        uploadTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        uploadTable.getFlexCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_MIDDLE);
        table.setWidget(1, 0, uploadTable);
        setWidget(table);
        setUploadVisible(!readOnly);
        formDataLoader = new DefaultFormDataLoader();
    }
    
    public void setPreferredHeightPx(int height) {
        recordFieldWidget.setPreferredHeightPx(height);
    }
    
    public RecordFieldWidget getRecordWidget() {
        return recordFieldWidget;
    }
    
    public void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;
            recordFieldWidget.setReadOnly(readOnly);
            setUploadVisible(!readOnly);
        }
    }
    
    public void setTitle(String title) {
        if (showCaption) {
            if (optional) {
                recordCaption.setCaptionText(title);
            } else {
                SpanElement span = Document.get().createSpanElement();
                span.appendChild(Document.get().createTextNode(title));
                span.addClassName("gwt-Label");
                span.addClassName(REQUIRED);
                recordCaption.setCaptionHTML(span.getString());
            }      
        }
    }
    
    private void setUploadVisible(boolean visible) {
        uploadTable.setVisible(visible);
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<RecordField> handler) {
        return recordFieldWidget.addValueChangeHandler(handler);
    }

    @Override
    public RecordField getValue() {
        return recordFieldWidget.getValue();
    }

    @Override
    public void setValue(RecordField value) {
        recordFieldWidget.setValue(value);
    }

    @Override
    public void setValue(RecordField value, boolean fireEvents) {
        recordFieldWidget.setValue(value, fireEvents);
    }
    
    public void reset() {
        recordFieldWidget.setValue(null);
        if (!readOnly) {
            recordFileUpload.reset();
            uploadButton.setEnabled(false);
        }
    }
    
    public boolean validate() {
        return recordFieldWidget.validate();
    }

    public void setFormDataLoader(FormDataLoader formDataLoader) {
        this.formDataLoader = formDataLoader;
    }
    
    private void loadRecordFromFile() {
        hasErrorMessage.clearError();
        formDataLoader.loadFormData(recordFileItemName, new BusyAsyncCallback<RecordField>() {
                    @Override
                    public void onSuccessImpl(RecordField result) {
                        setValue(result, true);
                        recordFileUpload.reset();
                        uploadButton.setEnabled(false);
                    }
                    
                    @Override
                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, hasErrorMessage);
                    }
        });
    }

    @Override
    public void onChange(ChangeEvent event) {
        boolean enabled = recordFileUpload.getFileName().length()>0 &&
                          recordFieldWidget.getValue() != null;
        uploadButton.setEnabled(enabled);
    }
    
    private class DefaultFormDataLoader implements FormDataLoader {

        @Override
        public void loadFormData(String fileItemName,
                final AsyncCallback<RecordField> callback) {
            String schema = recordFieldWidget.getValue().getSchema();
            KaaAdmin.getDataSource().getRecordDataFromFile(schema, fileItemName, 
                    new AsyncCallback<RecordField>() {
                        @Override
                        public void onSuccess(RecordField result) {
                            callback.onSuccess(result);
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure(caught);
                        }
                    });
        }
        
    }
    
    public static interface FormDataLoader {
        
        void loadFormData(String fileItemName, AsyncCallback<RecordField> callback);
        
    }

}
