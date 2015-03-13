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

package org.kaaproject.kaa.sandbox.web.client.layout;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources.SandboxStyle;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;
import org.kaaproject.kaa.sandbox.web.shared.Version;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class AppLayout extends Composite {
        interface AppLayoutUiBinder extends UiBinder<Widget, AppLayout> { }
        private static AppLayoutUiBinder uiBinder = GWT.create(AppLayoutUiBinder.class);

        @UiField SimplePanel appHeader;
        @UiField SimpleWidgetPanel appContent;
        @UiField HTMLPanel footerPanel;
        @UiField (provided = true) public final AvroUiStyle avroUiStyle;
        @UiField (provided = true) public final SandboxStyle sandboxStyle;

        public AppLayout() {
            avroUiStyle = Utils.avroUiStyle;
            sandboxStyle = Utils.sandboxStyle;
            initWidget(uiBinder.createAndBindUi(this));
            footerPanel.getElement().setInnerHTML(Utils.messages.footerMessage(Version.PROJECT_VERSION));
        }

        public SimplePanel getAppHeaderHolder() {
            return this.appHeader;
        }

        public SimpleWidgetPanel getAppContentHolder() {
            return this.appContent;
        }

}


