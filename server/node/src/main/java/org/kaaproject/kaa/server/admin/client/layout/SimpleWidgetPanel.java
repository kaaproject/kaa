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

package org.kaaproject.kaa.server.admin.client.layout;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleWidgetPanel extends LayoutPanel implements AcceptsOneWidget {

    private Widget widget;

    @Override
    public void setWidget(IsWidget w) {
        setOneWidget(asWidgetOrNull(w));
    }

    private void setOneWidget(Widget w) {
        // validate
        if (w == widget) {
            return;
        }

        // remove the old widget
        if (widget != null) {
            super.remove(widget);
        }

        // logical attach
        widget = w;

        if (w != null) {
            super.add(w);
        }
        forceLayout();
    }
}