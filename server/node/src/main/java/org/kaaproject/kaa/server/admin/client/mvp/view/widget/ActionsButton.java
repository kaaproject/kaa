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

package org.kaaproject.kaa.server.admin.client.mvp.view.widget;

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ToggleButton;

public class ActionsButton extends ToggleButton {

    private static Template template;
    private PopupPanel actionsPopup;

    private MenuBar menu = new MenuBar(true);

    interface Template extends SafeHtmlTemplates {
        @Template("<div tabindex=\"0\" style=\"vertical-align:middle;\">"
                + "<span style='{0}'></span>" + "<span>{1}</span>" + "</div>")
        SafeHtml menuImageItemContent(SafeStyles style, String text);

        @Template("<div tabindex=\"0\" style=\"vertical-align:middle;\">"
                + "<span>{0}</span>" + "</div>")
        SafeHtml menuItemContent(String text);

    }

    @UiConstructor
    public ActionsButton(String text) {
        super();
        if (template == null) {
            template = GWT.create(Template.class);
        }
        getElement().getStyle().setPaddingRight(20, Unit.PX);

        Element caretSpan = DOM.createElement("span");
        caretSpan.setClassName(Utils.kaaAdminStyle.buttonCaret());
        DOM.insertChild(getElement(), caretSpan, 0);
        Element textElement = DOM.createElement("span");
        textElement.setInnerText(text);
        DOM.insertChild(caretSpan, textElement, 0);
        
        actionsPopup = new PopupPanel(true, false);
        actionsPopup.addStyleName(Utils.kaaAdminStyle.actionPopup());
        actionsPopup.setWidget(menu);
        actionsPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                 ActionsButton.this.setDown(false);
            }
        });

        actionsPopup.addAutoHidePartner(getElement());

        addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (!actionsPopup.isShowing()) {
                    // Instantiate the popup and show it.
                    final Element parent = ActionsButton.this.getElement();
                    actionsPopup
                            .setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                                public void setPosition(int offsetWidth,
                                        int offsetHeight) {
                                    int left = parent.getAbsoluteLeft();
                                    int top = parent.getAbsoluteTop()
                                            + parent.getOffsetHeight() + 2;
                                    if (left + actionsPopup.getOffsetWidth() > Window
                                            .getClientWidth()) {
                                        left = parent.getAbsoluteRight()
                                                - actionsPopup.getOffsetWidth();
                                    }
                                    actionsPopup.setPopupPosition(left, top);
                                }
                            });
                } else {
                    actionsPopup.hide();
                }
            }
        });
    }

    public HandlerRegistration addMenuItem(String text,
            final ActionMenuItemListener listener) {
        return addMenuItem(null, text, listener);
    }

    public HandlerRegistration addMenuItem(ImageResource image, String text,
            final ActionMenuItemListener listener) {

        SafeHtml html = null;
        if (image != null) {
            SafeUri uri = image.getSafeUri();
            int left = image.getLeft();
            int top = image.getTop();
            int width = image.getWidth();
            int height = image.getHeight();
            int paddingRight = width + 8;

            String background = "url(\"" + uri.asString() + "\") no-repeat "
                    + (-left + "px ") + (-top + "px");

            SafeStylesBuilder builder = new SafeStylesBuilder();
            builder.trustedNameAndValue("background", background)
                    .width(width, Unit.PX).height(height, Unit.PX)
                    .paddingRight(paddingRight, Unit.PX);

            SafeStyles style = SafeStylesUtils.fromTrustedString(builder
                    .toSafeStyles().asString());

            html = template.menuImageItemContent(style, text);
        }
        else {
            html = template.menuItemContent(text);
        }
        final MenuItem item = new MenuItem(html, new Command() {

            @Override
            public void execute() {
                if (actionsPopup != null && actionsPopup.isVisible())
                    actionsPopup.hide();
                listener.onMenuItemSelected();
            }

        });

        menu.addItem(item);
        HandlerRegistration registration = new HandlerRegistration() {
            
            @Override
            public void removeHandler() {
                menu.removeItem(item);
                
            }
        };
        return registration;
    }

    public void clearItems() {
        menu.clearItems();
    }

    public static interface ActionMenuItemListener {
        void onMenuItemSelected();
    }
}
