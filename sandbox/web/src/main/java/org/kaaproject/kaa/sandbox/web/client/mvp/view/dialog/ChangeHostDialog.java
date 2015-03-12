/*
 * Copyright 2014-2015 CyberVision, Inc.
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
 
package org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog;

import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ChangeHostDialog extends AvroUiDialog {
    
    public static ChangeHostDialog showChangeHostDialog(Listener listener) {
        ChangeHostDialog dialog = new ChangeHostDialog(listener);
        dialog.center();
        return dialog;
    }
    
    public ChangeHostDialog(final Listener listener) {
        super(false, true);
        
        setWidth("500px");
        
        setTitle(Utils.constants.changeKaaHost());
        
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        add(dialogContents);
        
        AlertPanel warningPanel = new AlertPanel(AlertPanel.Type.WARNING);
        warningPanel.setMessage(Utils.messages.changeKaaHostDialogMessage());
        
        dialogContents.add(warningPanel);
        
        Button changeHostButton = new Button(Utils.constants.changeKaaHost(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                listener.onChangeHost();
            }
        });
        
        Button ignoreButton = new Button(Utils.constants.ignore(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                listener.onIgnore();
            }
        });
        addButton(changeHostButton);
        addButton(ignoreButton);
    }

    public interface Listener {
        
        public void onChangeHost();
        
        public void onIgnore();
        
    }   

}

