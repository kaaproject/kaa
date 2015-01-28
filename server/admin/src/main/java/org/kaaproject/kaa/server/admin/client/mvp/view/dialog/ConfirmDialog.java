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

import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConfirmDialog extends KaaDialog {

    private Button noButton;
    private Button yesButton;

    private ConfirmListener listener;

    public ConfirmDialog(ConfirmListener listener, String title, String question) {
        super(false, true);
        setTitle(title);
        this.listener = listener;

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        AlertPanel questionLabel = new AlertPanel(AlertPanel.Type.WARNING);
        questionLabel.setMessage(question);
        dialogContents.add(questionLabel);

        noButton = new Button(Utils.constants.no(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                ConfirmDialog.this.listener.onNo();
            }
        });

        yesButton = new Button(Utils.constants.yes(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                ConfirmDialog.this.listener.onYes();
            }
        });

        addButton(yesButton);
        addButton(noButton);

    }

    public interface ConfirmListener {

        public void onNo();

        public void onYes();

    }
}
