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

import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MessageDialog extends AvroUiDialog {

    private Button okButton;
    
    private Listener listener;
    
    public static MessageDialog showMessageDialog(String title, String message) {
        return showMessageDialog(AlertPanel.Type.INFO, title, message);
    }
    
    public static MessageDialog showMessageDialog(AlertPanel.Type type, String title, String message) {
        return showMessageDialog(null, type, title, message);
    }
    
    public static MessageDialog showMessageDialog(Listener listener, AlertPanel.Type type, String title, String message) {
        MessageDialog dialog = new MessageDialog(listener, type, title, message);
        dialog.center();
        dialog.show();
        return dialog;
    }
    
    public MessageDialog(Listener listener, String title, String message) {
        this(listener, AlertPanel.Type.INFO, title, message);
    }

    public MessageDialog(Listener listener, AlertPanel.Type type, String title, String message) {
        super(false, true);
        setTitle(title);
        this.listener = listener;
        
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);
        
        AlertPanel messageLabel = new AlertPanel(type);
        messageLabel.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
        messageLabel.getElement().getStyle().setProperty("maxHeight", "400px");
        messageLabel.getElement().getStyle().setProperty("maxWidth", Window.getClientWidth()*2/3 + "px");
        messageLabel.getElement().getStyle().setOverflowY(Overflow.AUTO);
        messageLabel.setMessage(message);
        dialogContents.add(messageLabel);
        
        okButton = new Button(Utils.constants.ok(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        addButton(okButton);
        
        this.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (MessageDialog.this.listener != null) {
                    MessageDialog.this.listener.onOk();
                }
            }
        });
    }
    
    public interface Listener {
        
        public void onOk();
        
    }   
}
