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

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.Renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A {@link Cell} used to render a drop-down list.
 */
public class ValueSelectionCell<T> extends AbstractInputCell<T, T> {

  interface Template extends SafeHtmlTemplates {
    @Template("<option value=\"{0}\">{0}</option>")
    SafeHtml deselected(String option);

    @Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
    SafeHtml selected(String option);
  }

  private static Template template;

  private HashMap<T, Integer> indexForOption = new HashMap<T, Integer>();

  private final List<T> options;
  
  private final Renderer<T> renderer;

  /**
   * Construct a new {@link ValueSelectionCell} with the specified options.
   *
   * @param options the options in the cell
   * @param renderer the renderer to render options in the cell
   */
  public ValueSelectionCell(List<T> options, Renderer<T> renderer) {
    super(BrowserEvents.CHANGE);
    if (template == null) {
      template = GWT.create(Template.class);
    }
    this.options = new ArrayList<T>(options);
    this.renderer = renderer;
    int index = 0;
    for (T option : options) {
      indexForOption.put(option, index++);
    }
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, T value,
      NativeEvent event, ValueUpdater<T> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    String type = event.getType();
    if (BrowserEvents.CHANGE.equals(type)) {
      Object key = context.getKey();
      SelectElement select = parent.getFirstChild().cast();
      T newValue = options.get(select.getSelectedIndex());
      //setViewData(key, newValue);
      finishEditing(parent, newValue, key, valueUpdater);
      if (valueUpdater != null) {
        valueUpdater.update(newValue);
      }
    }
  }

  @Override
  public void render(Context context, T value, SafeHtmlBuilder sb) {
    // Get the view data.
   // Object key = context.getKey();
//    T viewData = getViewData(key);
//    if (viewData != null && viewData.equals(value)) {
//      clearViewData(key);
//      viewData = null;
//    }

    //int selectedIndex = getSelectedIndex(viewData == null ? value : viewData);
    
    int selectedIndex = getSelectedIndex(value);
    sb.appendHtmlConstant("<select tabindex=\"-1\">");
    int index = 0;
    for (T option : options) {
      if (index++ == selectedIndex) {
        sb.append(template.selected(renderer.render(option)));
      } else {
        sb.append(template.deselected(renderer.render(option)));
      }
    }
    sb.appendHtmlConstant("</select>");
  }

  private int getSelectedIndex(T value) {
    Integer index = indexForOption.get(value);
    if (index == null) {
      return -1;
    }
    return index.intValue();
  }
}
