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
import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ExtendedPasswordTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAuthServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ChangePasswordDialog extends AvroUiDialog {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();

    private KaaAuthServiceAsync authService = KaaAuthServiceAsync.Util.getInstance();

    private AlertPanel errorPanel;

    private ExtendedPasswordTextBox oldPassword;
    private ExtendedPasswordTextBox newPassword;
    private ExtendedPasswordTextBox newPasswordAgain;

    private String username;

    private Button changePasswordButton;

    public static ChangePasswordDialog showChangePasswordDialog(Listener listener, String username, String message) {
        ChangePasswordDialog dialog = new ChangePasswordDialog(listener, username, message);
        dialog.center();
        dialog.show();
        return dialog;
    }

    public ChangePasswordDialog(final Listener listener, String username, String message) {
        super(false, true);

        this.username = username;

        InputChangeHandler handler = new InputChangeHandler(listener);

        setWidth("500px");

        setTitle(Utils.constants.changePassword());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        if (message != null) {
            AlertPanel warningPanel = new AlertPanel(AlertPanel.Type.WARNING);
            warningPanel.setMessage(message);
            dialogContents.add(warningPanel);
        }

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);

        int row=0;
        Widget label = new Label(Utils.constants.oldPassword());
        label.addStyleName(REQUIRED);
        oldPassword = new ExtendedPasswordTextBox();
        table.setWidget(row, 0, label);
        table.setWidget(row, 1, oldPassword);
        oldPassword.addInputHandler(handler);
        oldPassword.addKeyDownHandler(handler);

        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;
        label = new Label(Utils.constants.newPassword());
        label.addStyleName(REQUIRED);
        newPassword = new ExtendedPasswordTextBox();
        table.setWidget(row, 0, label);
        table.setWidget(row, 1, newPassword);
        newPassword.addInputHandler(handler);
        newPassword.addKeyDownHandler(handler);

        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;
        label = new Label(Utils.constants.newPasswordAgain());
        label.addStyleName(REQUIRED);
        newPasswordAgain = new ExtendedPasswordTextBox();
        table.setWidget(row, 0, label);
        table.setWidget(row, 1, newPasswordAgain);
        newPasswordAgain.addInputHandler(handler);
        newPasswordAgain.addKeyDownHandler(handler);

        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        dialogContents.add(table);

        changePasswordButton = new Button(Utils.constants.changePassword(), handler);

        Button cancelButton = new Button(Utils.constants.cancel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                listener.onCancel();
            }
        });
        addButton(changePasswordButton);
        addButton(cancelButton);

        changePasswordButton.setEnabled(false);
    }

    private boolean validate() {
        boolean result = !isEmpty(oldPassword.getValue());
        result &= !isEmpty(newPassword.getValue());
        result &= !isEmpty(newPasswordAgain.getValue());
        return result;
    }

    private boolean validatePasswords() {
        String oldPasswordText = oldPassword.getValue();
        String newPasswordText = newPassword.getValue();
        String newPasswordAgainText = newPasswordAgain.getValue();

        if (oldPasswordText.equals(newPasswordText)) {
            setError(Utils.messages.newPasswordShouldDifferent());
            return false;
        } else if (!newPasswordText.equals(newPasswordAgainText)) {
            setError(Utils.messages.newPasswordsNotMatch());
            return false;
        }
        return true;
    }

    private void performChangePassword (final AsyncCallback<ResultCode> callback) {
        if (validatePasswords()) {
            String oldPasswordText = oldPassword.getValue();
            String newPasswordText = newPassword.getValue();
            authService.changePassword(username, oldPasswordText, newPasswordText, new AsyncCallback<ResultCode>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(ResultCode result) {
                    if (ResultCode.OK != result) {
                        setError(Utils.constants.getString(result.getResourceKey()));
                    }
                    callback.onSuccess(result);
                }
            });
        }
    }

    private void setError(String error) {
        if (error!= null) {
            errorPanel.setMessage(error);
            errorPanel.setVisible(true);
        } else {
            errorPanel.setMessage("");
            errorPanel.setVisible(false);
        }
    }

    class InputChangeHandler implements ClickHandler, KeyDownHandler, InputEventHandler {

        final Listener listener;

        public InputChangeHandler(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(ClickEvent event) {
            performChangePassword(new AsyncCallback<ResultCode>() {
                @Override
                public void onFailure(Throwable caught) {
                }

                @Override
                public void onSuccess(ResultCode result) {
                    if (result == ResultCode.OK) {
                        hide();
                        listener.onChangePassword();
                    }
                }
            });
        }

        @Override
        public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && validate()) {
                onClick(null);
            }
        }

        @Override
        public void onInputChanged(InputEvent event) {
            boolean valid = validate();
            changePasswordButton.setEnabled(valid);
        }
    }

    public interface Listener {

        public void onChangePassword();

        public void onCancel();

    }

}
