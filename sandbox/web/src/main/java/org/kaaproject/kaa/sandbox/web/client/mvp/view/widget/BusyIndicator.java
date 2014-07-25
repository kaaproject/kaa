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
package org.kaaproject.kaa.sandbox.web.client.mvp.view.widget;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class BusyIndicator extends PopupPanel  {
    
    static BusyIndicator busy;

    @UiField FlowPanel view;

    public static void busy(){
        if (busy == null) busy = new BusyIndicator();
        busy.center();
    }
    
    public static void free(){
        if (busy == null) busy = new BusyIndicator();
        busy.hide();
    }
    
    public BusyIndicator(){
        super(false, true);
        this.setGlassEnabled(true);
        HTML widget = new HTML("Processing request... Please wait.");
        widget.getElement().getStyle().setPadding(20, Unit.PX);
        widget.getElement().getStyle().setFontSize(16, Unit.PX);
        widget.getElement().getStyle().setColor("#666666");
        this.add(widget);
    }
}
