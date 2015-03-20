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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.widget;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.nav.ListItem;
import org.kaaproject.avro.ui.gwt.client.widget.nav.UnorderedList;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;

public class FilterPanel extends NavWidget implements HasValueChangeHandlers<Boolean> {

    private UnorderedList ul;
    
    private List<FilterItem> items = new ArrayList<>();
    
    public FilterPanel(String title) {
        super();
        addStyleName(Utils.sandboxStyle.navPrimary());

        ul = new UnorderedList();
        ul.addStyleName(Utils.sandboxStyle.nav());
        add(ul);
        
        FilterItem item = new FilterItem(null, null, title);
        item.setValue(true);
        ul.add(item);
    }
    
    public void addItem(ImageResource imageRes, String bgClass, String text) {
        FilterItem item = new FilterItem(imageRes, bgClass, text);
        ul.add(item);
        item.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fireEvent(event);
            }
        });
        items.add(item);
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Boolean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
    
    public List<FilterItem> getFilterItems() {
        return items;
    }
    
    public class FilterItem extends ListItem implements HasValue<Boolean> {
        
        private Anchor anchor;
        
        private boolean valueChangeHandlerInitialized;
        private boolean active = false;
        
        FilterItem(ImageResource imageRes, String bgClass, String text) {
            anchor = new Anchor();
            add(anchor);
            Element span = DOM.createElement("span");
            span.setInnerText(text);
            DOM.insertChild(anchor.getElement(), span, 0);
            
            span = DOM.createElement("span");
            DOM.insertChild(anchor.getElement(), span, 0);
            span.addClassName(Utils.sandboxStyle.icon());
            span.addClassName(Utils.sandboxStyle.fa());

            if (bgClass != null) {
                Element b = DOM.createElement("b");
                b.addClassName(bgClass);
                DOM.insertChild(span, b, 0);
            }

            if (imageRes != null) {
                Image image = new Image(imageRes);
                DOM.insertChild(span, image.getElement(), 0);
            }
        }
        
        protected void ensureDomEventHandlers() {
            anchor.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                setValue(!active, true);
              }
            });
        }

        @Override
        public HandlerRegistration addValueChangeHandler(
                ValueChangeHandler<Boolean> handler) {
            if (!valueChangeHandlerInitialized) {
                ensureDomEventHandlers();
                valueChangeHandlerInitialized = true;
              }
              return addHandler(handler, ValueChangeEvent.getType());
            }

        @Override
        public Boolean getValue() {
            return active;
        }

        @Override
        public void setValue(Boolean value) {
            setValue(value, false);
        }

        @Override
        public void setValue(Boolean value, boolean fireEvents) {
            if (this.active != value) {
                this.active = value;
                if (active) {
                    addStyleName(Utils.sandboxStyle.active());
                } else {
                    removeStyleName(Utils.sandboxStyle.active());
                }
            }
            if (fireEvents) {
                ValueChangeEvent.fire(this, value);
            }
        }
    }


    
}
