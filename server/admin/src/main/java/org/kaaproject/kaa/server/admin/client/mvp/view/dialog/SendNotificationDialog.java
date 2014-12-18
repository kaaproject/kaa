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

import java.util.List;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.AlertPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.FileUploadForm;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaListBox;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.datepicker.client.DateBox;

public class SendNotificationDialog extends KaaDialog implements ChangeHandler, ValueChangeHandler<SchemaDto>, HasErrorMessage {

    private AlertPanel errorPanel;

    private SchemaListBox notificationSchema;
    private DateBox expiredAt;
    private FileUploadForm notificationFileUpload;

    private String applicationId;
    private String topicId;
    private String fileItemName;

    private Button sendButton;

    public static void showSendNotificationDialog(final String applicationId, final String topicId, final AsyncCallback<SendNotificationDialog> callback) {
        KaaAdmin.getDataSource().getUserNotificationSchemas(applicationId, new AsyncCallback<List<SchemaDto>>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(List<SchemaDto> result) {
                SendNotificationDialog dialog = new SendNotificationDialog(applicationId, topicId, result);
                dialog.center();
                callback.onSuccess(dialog);
                dialog.show();
            }
        });
    }

    public SendNotificationDialog(String applicationId, String topicId, List<SchemaDto> notificationSchemas) {
        super(false, true);

        this.applicationId = applicationId;
        this.topicId = topicId;

        setWidth("500px");

        setTitle(Utils.constants.send_notification());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);

        int row=0;

        Widget label = new Label(Utils.constants.notificationSchema());
        label.addStyleName("required");
        notificationSchema = new SchemaListBox();
        notificationSchema.setWidth("80px");
        notificationSchema.setAcceptableValues(notificationSchemas);
        notificationSchema.addValueChangeHandler(this);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, notificationSchema);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        label = new Label(Utils.constants.expiresAt());
        expiredAt = new DateBox();
        expiredAt.setWidth("200px");

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, expiredAt);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;


        label = new Label(Utils.constants.selectNotificationFile());
        label.addStyleName("required");
        notificationFileUpload = new FileUploadForm();
        notificationFileUpload.setWidth("200px");

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, notificationFileUpload);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        dialogContents.add(table);

        notificationFileUpload.addChangeHandler(this);
        fileItemName = notificationFileUpload.getFileItemName();

        notificationFileUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                performSend();
            }
        });

        sendButton = new Button(Utils.constants.send(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                notificationFileUpload.submit();
            }
        });

        Button closeButton = new Button(Utils.constants.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        addButton(sendButton);
        addButton(closeButton);

        sendButton.setEnabled(false);
    }

    @Override
    public void onChange(ChangeEvent event) {
        fireChanged();
    }

    @Override
    public void onValueChange(ValueChangeEvent<SchemaDto> event) {
        fireChanged();
    }

    private void fireChanged() {
        boolean valid = validate();
        sendButton.setEnabled(valid);
    }

    private void performSend () {
          NotificationDto notification = new NotificationDto();
          notification.setApplicationId(applicationId);
          notification.setSchemaId(notificationSchema.getValue().getId());
          notification.setTopicId(topicId);
          notification.setExpiredAt(expiredAt.getValue());
          notification.setType(NotificationTypeDto.USER);

          KaaAdmin.getDataSource().sendNotification(notification, fileItemName,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Utils.handleException(caught, SendNotificationDialog.this);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        hide();
                    }
          });
    }

    private boolean validate() {
        boolean result = notificationSchema.getValue() != null;
        result &= notificationFileUpload.getFileName().length()>0;
        return result;
    }

    @Override
    public void clearError() {
        errorPanel.setText("");
        errorPanel.setVisible(false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setText(message);
        errorPanel.setVisible(true);
    }

}
