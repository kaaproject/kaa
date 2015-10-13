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

package org.kaaproject.kaa.server.admin.client.mvp.view.sdk;

import java.io.IOException;
import java.util.Arrays;

import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * An object of this class is used to select a target platform during SDK
 * generation.
 *
 * @see org.kaaproject.kaa.server.admin.client.mvp.activity.SdkProfilesActivity
 *
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 *
 */
public class TargetPlatformPopup extends PopupPanel {

    {
        VerticalPanel root = new VerticalPanel();

        root.add(this.getFields());
        root.add(this.getButtons());

        this.add(root);
        this.setGlassEnabled(true);
        this.center();
        this.hide();
    }

    /**
     * An object that handles the value selected.
     *
     * @author Bohdan Khablenko
     *
     * @since 0.8.0
     *
     */
    public interface Caller {

        void processValue(SdkPlatform targetPlatform);
    }

    private final Caller caller;

    public TargetPlatformPopup(Caller caller) {
        this.caller = caller;
    }

    private ValueListBox<SdkPlatform> targetPlatform;

    private HorizontalPanel getFields() {
        HorizontalPanel fields = new HorizontalPanel();

        Label label = new Label(Utils.constants.targetPlatform());
        label.setStyleName(Utils.avroUiStyle.requiredField());

        fields.add(label);

        targetPlatform = new ValueListBox<SdkPlatform>(new Renderer<SdkPlatform>() {

            @Override
            public String render(SdkPlatform object) {
                if (object != null) {
                    return Utils.constants.getString(object.getResourceKey());
                } else {
                    return "";
                }
            }

            @Override
            public void render(SdkPlatform object, Appendable appendable) throws IOException {
                appendable.append(this.render(object));
            }
        });

        // For some reason this adds a null value to the list box
        // targetPlatform.setAcceptableValues(Arrays.asList(SdkPlatform.values()));

        // TODO: Do something with this workaround
        for (SdkPlatform value : SdkPlatform.values()) {
            targetPlatform.setValue(value);
        }

        targetPlatform.setValue(SdkPlatform.ANDROID);

        fields.add(targetPlatform);

        return fields;
    }

    private HorizontalPanel getButtons() {
        HorizontalPanel buttons = new HorizontalPanel();

        Button generateSdk = new Button(Utils.constants.generateSdk());
        generateSdk.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                SdkPlatform targetPlatform = TargetPlatformPopup.this.targetPlatform.getValue();
                TargetPlatformPopup.this.caller.processValue(targetPlatform);
                TargetPlatformPopup.this.hide();
            }
        });

        buttons.add(generateSdk);

        Button cancel = new Button(Utils.constants.cancel());
        cancel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                TargetPlatformPopup.this.hide();
            }
        });

        buttons.add(cancel);

        return buttons;
    }
}
