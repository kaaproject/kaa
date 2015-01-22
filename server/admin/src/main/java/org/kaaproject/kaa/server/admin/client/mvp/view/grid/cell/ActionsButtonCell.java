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

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class ActionsButtonCell<T> extends AbstractCell<T> {

  interface Template extends SafeHtmlTemplates {
            @Template("<div class=\"gwt-ToggleButton\" tabindex=\"-1\" role=\"button\" style=\"; vertical-align:middle;\">"+
                      "<span>{0} </span>"+
                      "<span style='{1}'></span>"+
                      "</div>")
            SafeHtml actionsButtonUp(String text, SafeStyles style);

            @Template("<div class=\"gwt-ToggleButton gwt-ToggleButton-down\" tabindex=\"-1\" role=\"button\" style=\"; vertical-align:middle;\">"+
                      "<span>{0} </span>"+
                      "<span style='{1}'></span>"+
                      "</div>")
            SafeHtml actionsButtonDown(String text, SafeStyles style);

            @Template("<div tabindex=\"0\" style=\"vertical-align:middle;\">"+
                      "<span style='{0}'></span>"+
                      "<span>{1}</span>"+
                      "</div>")
            SafeHtml menuItemContent(SafeStyles style, String text);
  }

  private static Template template;

  private SafeHtml actionsButtonHtml;
  private SafeHtml actionsButtonHtmlDown;

  private PopupPanel actionsPopup;
  private HandlerRegistration popupCloseHandlerRegistration;
  private T currentValue;

  private final Map<Object, Boolean> viewDataMap = new HashMap<Object, Boolean>();

  private MenuBar menu = new MenuBar(true);

  public void clearViewData(Object key) {
        if (key != null) {
          viewDataMap.remove(key);
        }
  }

  public void clearViewData()
  {
      viewDataMap.clear();
  }

  public Boolean getViewData(Object key) {
        return (key == null) ? null : viewDataMap.get(key);
  }

  public void setViewData(Object key, Boolean viewData) {
        if (key == null) {
          return;
        }

        if (viewData == null) {
          clearViewData(key);
        } else {
          viewDataMap.put(key, viewData);
        }
 }

  public ActionsButtonCell(ImageResource imageResource, String text) {
    super(CLICK, KEYDOWN);
    if (template == null) {
        template = GWT.create(Template.class);
    }

    SafeUri uri = imageResource.getSafeUri();
    int width = imageResource.getWidth();
    int height = imageResource.getHeight();
    int paddingLeft = width + 16;

    String background = "url(\"" + uri.asString() + "\") no-repeat scroll right center";

    SafeStylesBuilder builder = new SafeStylesBuilder();
    builder
    .trustedNameAndValue("background", background)
    .width(width, Unit.PX)
    .height(height, Unit.PX)
    .paddingLeft(paddingLeft, Unit.PX);

    SafeStyles style = SafeStylesUtils.fromTrustedString(builder.toSafeStyles().asString());

    this.actionsButtonHtml = template.actionsButtonUp(text, style);
    this.actionsButtonHtmlDown = template.actionsButtonDown(text, style);

      actionsPopup = new PopupPanel(true, false);
      actionsPopup.addStyleName(Utils.kaaAdminStyle.actionPopup());
      actionsPopup.setWidget(menu);

  }

  public void addMenuItem(ImageResource image, String text, final ActionMenuItemListener<T> listener) {
        SafeUri uri = image.getSafeUri();
        int width = image.getWidth();
        int height = image.getHeight();
        int paddingRight = width + 16;

        String background = "url(\"" + uri.asString() + "\") no-repeat scroll left center";

        SafeStylesBuilder builder = new SafeStylesBuilder();
        builder
        .trustedNameAndValue("background", background)
        .width(width, Unit.PX)
        .height(height, Unit.PX)
        .paddingRight(paddingRight, Unit.PX);

        SafeStyles style = SafeStylesUtils.fromTrustedString(builder.toSafeStyles().asString());

        SafeHtml html = template.menuItemContent(style, text);

        MenuItem item = new MenuItem(html, new Command() {

             @Override
             public void execute() {
                 if (actionsPopup != null && actionsPopup.isVisible())
                     actionsPopup.hide();
                 listener.onMenuItemSelected(currentValue);
             }

         });

        menu.addItem(item);
  }

  @Override
  public boolean isEditing(Context context, Element parent, T value) {
    Boolean viewData = getViewData(context.getKey());
    return viewData == null ? false : viewData.booleanValue();
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, T value,
                                  NativeEvent event, ValueUpdater<T> valueUpdater) {
    int x = event.getClientX();
    int y = event.getClientY();
    Element child = parent.getFirstChildElement();
    if (x >= child.getAbsoluteLeft() && x <= child.getAbsoluteRight() &&
            y >= child.getAbsoluteTop() && y <= child.getAbsoluteBottom()) {

        Object key = context.getKey();
        Boolean viewData = getViewData(key);
        if (viewData != null && viewData.booleanValue()) {
          // Handle the edit event.
            setViewData(key, false);
            setValue(context, parent, value);
        } else  {
          String type = event.getType();
          int keyCode = event.getKeyCode();

          boolean enterPressed = KEYUP.equals(type)
              && keyCode == KeyCodes.KEY_ENTER;
          if (CLICK.equals(type) || enterPressed) {
            // Go into edit mode.
             clearViewData();
             setViewData(key, true);
             setValue(context, parent, value);
             showPopup(context, parent, value, valueUpdater);
          }
        }
    }
  }

  private void showPopup(final Context context, final Element parent, final T value, final ValueUpdater<T> valueUpdater) {

    currentValue = value;

    actionsPopup.getElement().getStyle().setZIndex(2000);

      actionsPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
          public void setPosition(int offsetWidth, int offsetHeight) {
            int left = parent.getFirstChildElement().getAbsoluteLeft();
            int top = parent.getFirstChildElement().getAbsoluteTop() + parent.getFirstChildElement().getOffsetHeight()+2;

            if (left + actionsPopup.getOffsetWidth()>Window.getClientWidth()) {
                left = parent.getFirstChildElement().getAbsoluteRight() - actionsPopup.getOffsetWidth();
            }

            actionsPopup.setPopupPosition(left, top);
          }
    });
      if (popupCloseHandlerRegistration != null) {
          popupCloseHandlerRegistration.removeHandler();
      }
      popupCloseHandlerRegistration = actionsPopup.addCloseHandler(new CloseHandler<PopupPanel> () {
        @Override
        public void onClose(CloseEvent<PopupPanel> event) {
             setViewData(context.getKey(), false);
             setValue(context, parent, value);
        }
      });



  }

  @Override
  public void render(Context context, T value, SafeHtmlBuilder sb) {

        Object key = context.getKey();
        Boolean viewData = getViewData(key);
        if (viewData != null && !viewData.booleanValue()) {
          clearViewData(key);
          viewData = null;
        }

        boolean down = viewData != null && viewData.booleanValue();

      if (down)
          sb.append(actionsButtonHtmlDown);
      else
          sb.append(actionsButtonHtml);

  }

  public static interface ActionMenuItemListener<T> {

      void onMenuItemSelected(T value);

  }


}

