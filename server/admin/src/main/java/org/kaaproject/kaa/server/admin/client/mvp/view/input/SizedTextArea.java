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

package org.kaaproject.kaa.server.admin.client.mvp.view.input;

import org.kaaproject.kaa.server.admin.client.mvp.event.input.HasInputEventHandlers;
import org.kaaproject.kaa.server.admin.client.mvp.event.input.InputEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.input.InputEventHandler;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SizedTextArea extends VerticalPanel implements HasValue<String>, HasInputEventHandlers {

    private final ExtendedTextArea text;
    private Label charactersLabel;
    private final int maxChars;

    public SizedTextArea(int maxChars) {
        this.maxChars = maxChars;
        text = new ExtendedTextArea();
        add(text);
        if (maxChars > -1) {
            charactersLabel = new Label();
            charactersLabel.setStyleName("b-app-field-notes");
            text.setMaxLength(maxChars);
            add(charactersLabel);
            updateCharactersLabel();
        }
        addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                fireInputEvent();
            }
        });
        text.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent  event) {
                updateCharactersLabel();
                fireInputEvent();
            }
        });
    }

    private void updateCharactersLabel() {
        if (maxChars > -1) {
            int currentLength = text.getText().length();
            charactersLabel.setText(Utils.messages.charactersLength(currentLength, maxChars));
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<String> handler) {
        return text.addValueChangeHandler(handler);
    }

    @Override
    public String getValue() {
        return text.getValue();
    }

    @Override
    public void setValue(String value) {
        text.setValue(value);
        updateCharactersLabel();
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        text.setValue(value, fireEvents);
    }

    public void setFocus(boolean focused) {
        text.setFocus(focused);
    }

    public void setWidth(String width) {
        super.setWidth(width);
        text.setWidth(width);
    }

    public void setHeight(String height) {
        super.setHeight(height);
        text.setHeight(height);
    }

    public TextArea getTextArea() {
        return text;
    }

    private class ExtendedTextArea extends TextArea {

        public ExtendedTextArea() {
            super();
            sinkEvents(Event.ONPASTE);
        }

        public void setMaxLength(int maxChars) {
            getElement().setAttribute("maxlength", ""+maxChars);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (DOM.eventGetType(event)) {
                case Event.ONPASTE:
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            ValueChangeEvent.fire(ExtendedTextArea.this, getText());
                        }
                    });
                    break;
            }
        }
    }

    private void fireInputEvent() {
        InputEvent event = new InputEvent(this);
        fireEvent(event);
    }

    @Override
    public HandlerRegistration addInputHandler(InputEventHandler handler) {
        return this.addHandler(handler, InputEvent.TYPE);
    }
}
