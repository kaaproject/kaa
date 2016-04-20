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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SimpleKeyProvider;

public class MultiValueListBox<T> extends Composite implements
    Focusable, HasValue<List<T>>, HasEnabled,
    IsEditor<TakesValueEditor<List<T>>> {

  private final List<T> values = new ArrayList<T>();
  private final Map<Object, Integer> valueKeyToIndex = new HashMap<Object, Integer>();
  private final Renderer<T> renderer;
  private final ProvidesKey<T> keyProvider;

  private TakesValueEditor<List<T>> editor;
  private List<T> value;

  public MultiValueListBox(Renderer<T> renderer) {
    this(renderer, new SimpleKeyProvider<T>());
  }

  public MultiValueListBox(Renderer<T> renderer, ProvidesKey<T> keyProvider) {
    this.keyProvider = keyProvider;
    this.renderer = renderer;
    ListBox listBox = new ListBox();
    listBox.setMultipleSelect(true);
    initWidget(listBox);
    getListBox().addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        List<T> newValue = new ArrayList<>();
        for (int i=0;i<values.size();i++) {
            if (getListBox().isItemSelected(i)) {
                newValue.add(values.get(i));
            }
        }
        setValue(newValue, true);
      }
    });
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<T>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  /**
   * Returns a {@link TakesValueEditor} backed by the ValueListBox.
   */
  public TakesValueEditor<List<T>> asEditor() {
    if (editor == null) {
      editor = TakesValueEditor.of(this);
    }
    return editor;
  }

  @Override
  public int getTabIndex() {
    return getListBox().getTabIndex();
  }

  public List<T> getValue() {
    return value;
  }

  @Override
  public boolean isEnabled() {
    return getListBox().isEnabled();
  }

  public void setAcceptableValues(Collection<T> newValues) {
    values.clear();
    valueKeyToIndex.clear();
    ListBox listBox = getListBox();
    listBox.clear();

    for (T nextNewValue : newValues) {
      addValue(nextNewValue);
    }

    updateListBox();
  }

  @Override
  public void setAccessKey(char key) {
    getListBox().setAccessKey(key);
  }

  @Override
  public void setEnabled(boolean enabled) {
    getListBox().setEnabled(enabled);
  }

  @Override
  public void setFocus(boolean focused) {
    getListBox().setFocus(focused);
  }

  @Override
  public void setTabIndex (int index) {
    getListBox().setTabIndex(index);
  }

  /**
   * Set the value and display it in the select element. Add the value to the
   * acceptable set if it is not already there.
   */
  public void setValue(List<T> value) {
    setValue(value, false);
  }

  public void setValue(List<T> value, boolean fireEvents) {
    if (value == this.value || (this.value != null && this.value.equals(value))) {
      return;
    }

    List<T> before = this.value;
    this.value = value;
    updateListBox();

    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, before, value);
    }
  }
  
  public List<T> getValues() {
      return values;
  }

  private void addValue(T value) {
    Object key = keyProvider.getKey(value);
    if (valueKeyToIndex.containsKey(key)) {
      throw new IllegalArgumentException("Duplicate value: " + value);
    }

    valueKeyToIndex.put(key, values.size());
    values.add(value);
    addListBoxItem(renderer.render(value));
    assert values.size() == getListBox().getItemCount();
  }
  
  private void addListBoxItem(String item) {
      addItemWithTitle(getListBox().getElement(), item, item);
  }
  
  private static native void addItemWithTitle(Element element, String name, String value) /*-{
      var opt = $doc.createElement("option");
      opt.title = name;
      opt.text = name;
      opt.value = value;
      element.options.add(opt);
  }-*/;

  private ListBox getListBox() {
    return (ListBox) getWidget();
  }

  private void updateListBox() {
      if (value != null) {
          for (T item : value) {
              Object key = keyProvider.getKey(item);
              Integer index = valueKeyToIndex.get(key);
              if (index == null) {
                  addValue(item);
              }
              index = valueKeyToIndex.get(key);
              getListBox().setItemSelected(index, true);
          }
      }
  }
}
