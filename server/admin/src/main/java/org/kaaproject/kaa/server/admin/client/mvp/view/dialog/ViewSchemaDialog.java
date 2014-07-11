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

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewSchemaDialog extends KaaDialog {

    private TextArea schemaDetails;

    public static void showViewSchemaDialog(String schema) {
        ViewSchemaDialog dialog = new ViewSchemaDialog(schema);
        dialog.center();
        dialog.show();
    }

    public ViewSchemaDialog(String schema) {
        super(false, true);

        setWidth("500px");

        setTitle(Utils.constants.eventClassSchema());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);

        schemaDetails = new TextArea();
        schemaDetails.setReadOnly(true);
        schemaDetails.setWidth("400px");
        schemaDetails.getElement().getStyle().setPropertyPx("minHeight", 200);
        schemaDetails.setValue(schema);

        table.setWidget(0, 0, schemaDetails);

        dialogContents.add(table);

        Button closeButton = new Button(Utils.constants.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        addButton(closeButton);
    }

}
