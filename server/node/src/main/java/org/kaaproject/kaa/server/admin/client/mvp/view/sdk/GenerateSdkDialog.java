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

package org.kaaproject.kaa.server.admin.client.mvp.view.sdk;

import java.io.IOException;

import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Bohdan Khablenko
 *
 * @see org.kaaproject.kaa.server.admin.client.mvp.activity.SdkProfilesActivity
 *
 * @since v0.8.0
 */
public class GenerateSdkDialog extends AvroUiDialog {

    private ValueListBox<SdkPlatform> targetPlatform;

    public static GenerateSdkDialog show(Listener listener) {
        GenerateSdkDialog instance = new GenerateSdkDialog(listener);

        instance.center();
        instance.show();

        return instance;
    }

    public GenerateSdkDialog(Listener listener) {
        super(false, true);

        this.listener = listener;

        this.setText(Utils.constants.generateSdk());

        VerticalPanel root = new VerticalPanel();
        this.setWidget(root);

        FlexTable grid = new FlexTable();
        grid.setCellSpacing(15);
        grid.getColumnFormatter().setWidth(0, "115px");
        grid.getColumnFormatter().setWidth(1, "115px");

        Label label = new Label(Utils.constants.targetPlatform());

        this.targetPlatform = new ValueListBox<>(new Renderer<SdkPlatform>() {

            @Override
            public String render(SdkPlatform object) {
                if (object != null) {
                    return Utils.constants.getString(object.getResourceKey());
                } else {
                    return null;
                }
            }

            @Override
            public void render(SdkPlatform object, Appendable appendable) throws IOException {
                appendable.append(this.render(object));
            }
        });

        // For some reason this adds a null value to the list box
        // targetPlatform.setAcceptableValues(Arrays.asList(SdkPlatform.values()));
        targetPlatform.setWidth("100%");

        // TODO: Do something with this workaround
        for (SdkPlatform value : SdkPlatform.values()) {
            targetPlatform.setValue(value);
        }

        targetPlatform.setValue(SdkPlatform.ANDROID);

        grid.setWidget(0, 0, label);
        grid.setWidget(0, 1, targetPlatform);
        root.add(grid);

        Button generateSdk = new Button(Utils.constants.generateSdk(), new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                GenerateSdkDialog.this.hide();

                if (GenerateSdkDialog.this.listener != null) {
                    SdkPlatform targetPlatform = GenerateSdkDialog.this.targetPlatform.getValue();
                    GenerateSdkDialog.this.listener.onGenerateSdk(targetPlatform);
                }
            }
        });

        Button cancel = new Button(Utils.constants.cancel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GenerateSdkDialog.this.hide();

                if (GenerateSdkDialog.this.listener != null) {
                    GenerateSdkDialog.this.listener.onCancel();
                }
            }
        });

        this.addButton(generateSdk);
        this.addButton(cancel);
    }

    public interface Listener {

        void onGenerateSdk(final SdkPlatform targetPlatform);

        void onCancel();
    }

    private Listener listener;
}
