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

package org.kaaproject.kaa.server.admin.client.mvp.view.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;

@SuppressWarnings("deprecation")
public class ImageTextButton extends Button {
         private String text;

         public ImageTextButton(ImageResource imageResource, String text) {
                 this(imageResource, text, null);
         }

         public ImageTextButton(ImageResource imageResource, String text, ClickHandler clickHandler){
          super();
          if (clickHandler != null) {
            addClickHandler(clickHandler);
        }
          String definedStyles = getElement().getAttribute("style");
          getElement().setAttribute("style", definedStyles + "; vertical-align:middle;");
          this.text = text;
          Element span = DOM.createElement("span");
          span.setInnerText(text);
          DOM.insertChild(getElement(), span, 0);
          Element imageSpan = DOM.createElement("span");
          int spacing;
          if (this.text == null || this.text.trim().equals("")) {
                  spacing = 0;
          } else {
                  spacing = 16;
          }

          updateImageElementFromImageResource(imageSpan, imageResource, spacing);
          DOM.insertBefore(getElement(), imageSpan, DOM.getFirstChild(getElement()));
         }

         @Override
         public String getText() {
          return this.text;
         }

         private void updateImageElementFromImageResource(Element imageSpan, ImageResource res, int spacing) {
             SafeUri url = res.getSafeUri();
             int width = res.getWidth();
             int height = res.getHeight();
             int paddingRight = width + spacing;
         String style = "url(\"" + url.asString() + "\") no-repeat scroll left center";
         imageSpan.getStyle().setProperty("background", style);
         imageSpan.getStyle().setPropertyPx("width", width);
         imageSpan.getStyle().setPropertyPx("height", height);
         imageSpan.getStyle().setPropertyPx("paddingRight", paddingRight);
     }

}

