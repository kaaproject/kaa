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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UnauthorizedSessionDialog extends AvroUiDialog {
    
    public UnauthorizedSessionDialog(final Listener listener) {
        super(false, true);
        
        setWidth("500px");
        
        setTitle(Utils.constants.sessionExpired());
        
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        add(dialogContents);
        
        AlertPanel warningPanel = new AlertPanel(AlertPanel.Type.WARNING);
        warningPanel.setMessage(Utils.messages.sessionExpiredMessage());
        
        dialogContents.add(warningPanel);
        
        Button loginButton = new Button(Utils.constants.logInAgain(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                listener.onLogin();
            }
        });
        
        Button ignoreButton = new Button(Utils.constants.ignore(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                listener.onIgnore();
            }
        });
        addButton(loginButton);
        addButton(ignoreButton);
    }

    public interface Listener {
        
        public void onLogin();
        
        public void onIgnore();
        
    }   

}

