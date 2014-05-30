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

package org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;
import static com.google.gwt.dom.client.BrowserEvents.KEYUP;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;

public class ActionButtonCell<T> extends AbstractCell<T> {

    interface Template extends SafeHtmlTemplates {
        @Template("<div class=\"gwt-Button b-app-cell-button\" tabindex=\"-1\" role=\"button\" style=\"; vertical-align:middle;\">"+
                  "<span>{0} </span>"+
                  "<span style='{1}'></span>"+
                  "</div>")
        SafeHtml actionButton(String text, SafeStyles style);

      @Template("<div class=\"gwt-Button b-app-cell-button b-app-cell-button-small\" tabindex=\"-1\" role=\"button\" style=\"; vertical-align:middle;\">"+
                "<span>{0} </span>"+
                "<span style='{1}'></span>"+
                "</div>")
      SafeHtml actionButtonSmall(String text, SafeStyles style);

    }

    private static SafeHtml empty = SafeHtmlUtils.fromSafeConstant("&nbsp;");

    private static Template template;

    private SafeHtml actionButtonHtml;
    private ActionListener<T> listener;
    private ActionValidator<T> validator;

    public ActionButtonCell(ImageResource imageResource,
            String text,
            boolean small,
            ActionListener<T> listener,
            ActionValidator<T> validator) {
        super(CLICK, KEYDOWN);
        this.listener = listener;
        this.validator = validator;
        if (template == null) {
            template = GWT.create(Template.class);
        }
        SafeUri uri = imageResource.getSafeUri();
        int width = imageResource.getWidth();
        int height = imageResource.getHeight();
        int paddingLeft = width;
        //if (!small) {
        //    paddingLeft+=16;
        //}

        String background = "url(\"" + uri.asString() + "\") no-repeat scroll right center";

        SafeStylesBuilder builder = new SafeStylesBuilder();
        builder
        .trustedNameAndValue("background", background)
        .width(width, Unit.PX)
        .height(height, Unit.PX)
        .paddingLeft(paddingLeft, Unit.PX);

        SafeStyles style = SafeStylesUtils.fromTrustedString(builder.toSafeStyles().asString());
        if (small) {
            this.actionButtonHtml = template.actionButtonSmall(text, style);
        }
        else {
            this.actionButtonHtml = template.actionButton(text, style);
        }
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value,
                                  NativeEvent event, ValueUpdater<T> valueUpdater) {
       int x = event.getClientX();
       int y = event.getClientY();
       Element child = parent.getFirstChildElement();
       if (x >= child.getAbsoluteLeft() && x <= child.getAbsoluteRight() &&
                y >= child.getAbsoluteTop() && y <= child.getAbsoluteBottom()) {
          String type = event.getType();
          int keyCode = event.getKeyCode();
          boolean enterPressed = KEYUP.equals(type)
              && keyCode == KeyCodes.KEY_ENTER;
          if ((CLICK.equals(type) || enterPressed) && validator.canPerformAction(value)) {
             setValue(context, parent, value);
             itemClicked(value);
          }
       }
    }

    private void itemClicked(T value) {
        if (listener != null) {
            listener.onItemAction(value);
        }
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context,
            T value, SafeHtmlBuilder sb) {
        if (validator.canPerformAction(value)) {
            sb.append(actionButtonHtml);
        }
        else {
            sb.append(empty);
        }
    }

    public static interface ActionListener<T> {

        void onItemAction(T value);

    }

    public static interface ActionValidator<T> {

        boolean canPerformAction(T value);

    }

}
