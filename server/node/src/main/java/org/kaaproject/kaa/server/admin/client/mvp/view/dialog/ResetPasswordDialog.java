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

import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAuthServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ResetPasswordDialog extends AvroUiDialog implements HasErrorMessage {

    private KaaAuthServiceAsync authService = KaaAuthServiceAsync.Util.getInstance();

    private AlertPanel errorPanel;
    private AlertPanel messagePanel;

    private SizedTextBox usernameOrEmail;
    private Button sendResetPasswordLinkButton;
    private Listener listener;

    public static ResetPasswordDialog showResetPasswordDialog(Listener listener) {
        ResetPasswordDialog dialog = new ResetPasswordDialog(listener);
        dialog.center();
        dialog.show();
        return dialog;
    }

    public ResetPasswordDialog(Listener listener) {
        super(false, true);
        
        this.listener = listener;

        setWidth("500px");

        setTitle(Utils.constants.resetPassword());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        messagePanel = new AlertPanel(AlertPanel.Type.WARNING);
        messagePanel.setMessage(Utils.messages.resetPasswordMessage());
        dialogContents.add(messagePanel);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);

        int row = 0;
        Widget label = new Label(Utils.constants.usernameOrEmail());
        label.addStyleName(Utils.avroUiStyle.requiredField());
        usernameOrEmail = new KaaAdminSizedTextBox(255);
        table.setWidget(row, 0, label);
        table.setWidget(row, 1, usernameOrEmail);
        usernameOrEmail.addInputHandler(new InputEventHandler() {
            @Override
            public void onInputChanged(InputEvent event) {
                boolean valid = validate();
                sendResetPasswordLinkButton.setEnabled(valid);
            }
        });

        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        dialogContents.add(table);

        sendResetPasswordLinkButton = new Button(Utils.constants.sendResetPasswordLink(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                validateUsernameOrEmail();
            }
        });

        Button cancelButton = new Button(Utils.constants.cancel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                ResetPasswordDialog.this.listener.onCancel();
            }
        });
        addButton(sendResetPasswordLinkButton);
        addButton(cancelButton);

        sendResetPasswordLinkButton.setEnabled(false);
    }

    private boolean validate() {
        return !isEmpty(usernameOrEmail.getValue());
    }

    private void validateUsernameOrEmail() {
        String usernameOrEmailText = usernameOrEmail.getValue();
        authService.checkUsernameOrEmailExists(usernameOrEmailText, new AsyncCallback<ResultCode>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, ResetPasswordDialog.this);
            }

            @Override
            public void onSuccess(ResultCode result) {
                if (ResultCode.OK != result) {
                    setErrorMessage(Utils.constants.getString(result.getResourceKey()));
                } else {
                    sendResetPasswordLink();
                }
            }
            
        });
    }

    private void sendResetPasswordLink () {
        String usernameOrEmailText = usernameOrEmail.getValue();
        authService.sendPasswordResetLinkByEmail(usernameOrEmailText, new AsyncCallback<ResultCode>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, ResetPasswordDialog.this);
            }

            @Override
            public void onSuccess(ResultCode result) {
                if (ResultCode.OK != result) {
                    setErrorMessage(Utils.constants.getString(result.getResourceKey()));
                } else {
                    MessageDialog dialog = new MessageDialog(new MessageDialog.Listener() {
                        @Override
                        public void onOk() {
                            hide();
                            listener.onSendResetPasswordLink();
                        }
                    },
                    Utils.constants.resetLinkWasSent(),
                    Utils.messages.resetPasswordLinkWasSent());
                    dialog.show();
                    dialog.center();
                }
            }
            
        });
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

        public void onSendResetPasswordLink();
        public void onCancel();

    }

}
