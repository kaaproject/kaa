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

package org.kaaproject.kaa.server.common.avro.ui.gwt.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.common.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.kaa.server.common.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.kaa.server.common.avro.ui.shared.ArrayField;
import org.kaaproject.kaa.server.common.avro.ui.shared.BooleanField;
import org.kaaproject.kaa.server.common.avro.ui.shared.EnumField;
import org.kaaproject.kaa.server.common.avro.ui.shared.FieldType;
import org.kaaproject.kaa.server.common.avro.ui.shared.FormEnum;
import org.kaaproject.kaa.server.common.avro.ui.shared.FormField;
import org.kaaproject.kaa.server.common.avro.ui.shared.IntegerField;
import org.kaaproject.kaa.server.common.avro.ui.shared.LongField;
import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.common.avro.ui.shared.StringField;
import org.kaaproject.kaa.server.common.avro.ui.shared.UnionField;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractFieldWidget<T extends FormField> extends SimplePanel implements HasValue<T> {
    
    public interface Resources extends ClientBundle {
        
        @Source(Style.DEFAULT_CSS)
        Style fieldWidgetStyle();
        
    }
    
    public interface Style extends CssResource {
        
        String DEFAULT_CSS = "org/kaaproject/kaa/server/common/avro/ui/gwt/client/widget/FieldWidget.css";
        
        String requiredField();
        
        String buttonsPanel();
        
    }
    
    private static Resources DEFAULT_RESOURCES;
    private static final String DEFAULT_DECIMAL_FORMAT = "#";
    private static final String FULL_WIDTH = "100%";
    
    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    protected T value;
    
    protected final Style style;
    
    protected final SizedTextBox.Style sizedTextStyle;

    private static Style getDefaultStyle() {
        if (DEFAULT_RESOURCES == null) {
          DEFAULT_RESOURCES = GWT.create(Resources.class);
        }
        return DEFAULT_RESOURCES.fieldWidgetStyle();
    }
    
    public AbstractFieldWidget() {
        this(getDefaultStyle(), SizedTextBox.getDefaultStyle());
    }
    
    public AbstractFieldWidget(Style style, SizedTextBox.Style sizedTextStyle) {
        
        // Inject the stylesheet.
        this.style = style;
        this.style.ensureInjected();
        
        // Inject the stylesheet.
        this.sizedTextStyle = sizedTextStyle;
        this.sizedTextStyle.ensureInjected();
        
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }
        T before = this.value;
        this.value = value;
        updateFields();
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }
    
    public boolean validate() {
        return value != null && value.isValid();
    }
    
    protected void fireChanged() {
        ValueChangeEvent.fire(this, value);
    }

    private void updateFields() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
        setWidget(constructForm());
    }
    
    protected abstract Widget constructForm();

    protected void constructFormData(FlexTable table, RecordField formData, List<HandlerRegistration> handlerRegistrations) {
        table.removeAllRows();
        table.getColumnFormatter().setWidth(0, "200px");
        table.getColumnFormatter().setWidth(1, "300px");
        if (formData != null) {
            int row = 0;
            for (FormField field : formData.getValue()) {
                row = constructField(table, row, field, handlerRegistrations);
                row++;
            }
        }
    }
    
    private int constructField(FlexTable table, int row, final FormField field, List<HandlerRegistration> handlerRegistrations) {
        int column = 0;
        if (field.getFieldType()!=FieldType.RECORD &&
            field.getFieldType()!=FieldType.ARRAY &&
            field.getFieldType()!=FieldType.UNION) {
            constructLabel(table, field, row, column);
            column++;
        }
        constructWidget(table, field, row, column, handlerRegistrations);
        return row;
    }
    
    protected void constructLabel(FlexTable table, FormField field, int row, int column) {
        Label label = new Label(field.getDisplayName());
        if (!field.isOptional()) {
            label.addStyleName(style.requiredField());
        }        
        table.setWidget(row, column, label);
    }
    
    protected void constructWidget(FlexTable table, FormField field, int row, int column, List<HandlerRegistration> handlerRegistrations) {
        Widget widget = null;
        switch (field.getFieldType()) {
            case STRING:
                widget = constructStringWidget((StringField)field, handlerRegistrations);
            break;
            case INTEGER:
                widget = constructIntegerWidget((IntegerField)field, handlerRegistrations);
                break;
            case LONG:
                widget = constructLongWidget((LongField)field, handlerRegistrations);
                break;
            case BOOLEAN:
                widget = constructBooleanWidget((BooleanField)field, handlerRegistrations);
                break;
            case ENUM:
                widget = constructEnumWidget((EnumField)field, handlerRegistrations);
                break;
            case ARRAY:
                widget = constructArrayWidget((ArrayField)field, handlerRegistrations);
                break;
            case RECORD:
                widget = constructRecordWidget((RecordField)field, handlerRegistrations);
                break;
            case UNION:
                widget = constructUnionWidget((UnionField)field, handlerRegistrations);
                break;
        }
        widget.setWidth(FULL_WIDTH);
        table.setWidget(row, column, widget);
        if (field.getFieldType()==FieldType.ARRAY ||
                field.getFieldType()==FieldType.UNION) {
            table.getFlexCellFormatter().setColSpan(row, column, 3);
        }
    }
    
    private Widget constructStringWidget(final StringField field, List<HandlerRegistration> handlerRegistrations) {
        final SizedTextBox textBox = new SizedTextBox(sizedTextStyle, field.getMaxLength());
        textBox.setValue(field.getValue());
        handlerRegistrations.add(textBox.addInputHandler(new InputEventHandler() {
            @Override
            public void onInputChanged(InputEvent event) {
                field.setValue(textBox.getValue());
                fireChanged();
            }
        }));
        return textBox;
    }
    
    private Widget constructIntegerWidget(final IntegerField field, List<HandlerRegistration> handlerRegistrations) {
        final IntegerBox integerBox = new IntegerBox(DEFAULT_DECIMAL_FORMAT);
        integerBox.setValue(field.getValue());
        handlerRegistrations.add(integerBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                field.setValue(integerBox.getValue());
                fireChanged();                
            }
        }));       
        return integerBox;
    }

    private Widget constructLongWidget(final LongField field, List<HandlerRegistration> handlerRegistrations) {
        final LongBox longBox = new LongBox(DEFAULT_DECIMAL_FORMAT);
        longBox.setValue(field.getValue());
        handlerRegistrations.add(longBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                field.setValue(longBox.getValue());
                fireChanged();                
            }
        }));       
        return longBox;
    }
    
    private Widget constructBooleanWidget(final BooleanField field, List<HandlerRegistration> handlerRegistrations) {
        final CheckBox checkBox = new CheckBox();
        checkBox.setValue(field.getValue());
        handlerRegistrations.add(checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                field.setValue(event.getValue());
                fireChanged();  
            }
        }));
        return checkBox;
    }
    
    private Widget constructEnumWidget(final EnumField field, List<HandlerRegistration> handlerRegistrations) {
        final FormEnumListBox enumBox = new FormEnumListBox();
        enumBox.setAcceptableValues(field.getEnumValues());
        enumBox.setValue(field.getValue());
        handlerRegistrations.add(enumBox.addValueChangeHandler(new ValueChangeHandler<FormEnum>() {
            @Override
            public void onValueChange(ValueChangeEvent<FormEnum> event) {
                field.setValue(event.getValue());
                fireChanged();  
            }
        }));
        return enumBox;
    }
    
    private Widget constructArrayWidget(final ArrayField field, List<HandlerRegistration> handlerRegistrations) {
        final ArrayFieldWidget arrayWidget = new ArrayFieldWidget(style, sizedTextStyle);
        arrayWidget.setValue(field);
        handlerRegistrations.add(arrayWidget.addValueChangeHandler(new ValueChangeHandler<ArrayField>() {
            @Override
            public void onValueChange(ValueChangeEvent<ArrayField> event) {
                fireChanged();  
            }
        }));
        return arrayWidget;
    }
    
    private Widget constructRecordWidget(final RecordField field, List<HandlerRegistration> handlerRegistrations) {
        CaptionPanel recordWidget = new CaptionPanel();
        if (field.isOptional()) {
            recordWidget.setCaptionText(field.getDisplayName());
        }
        else {
            SpanElement span = Document.get().createSpanElement();
            span.appendChild(Document.get().createTextNode(field.getDisplayName()));
            span.addClassName("gwt-Label");
            span.addClassName(style.requiredField());
            recordWidget.setCaptionHTML(span.getString());
        }        
        
        FlexTable table = new FlexTable();
        constructFormData(table, field, handlerRegistrations);
        recordWidget.setContentWidget(table);
        
        return recordWidget;
    }
    
    private Widget constructUnionWidget(final UnionField field, List<HandlerRegistration> handlerRegistrations) {
        final UnionFieldWidget unionWidget = new UnionFieldWidget(style, sizedTextStyle);
        unionWidget.setValue(field);
        handlerRegistrations.add(unionWidget.addValueChangeHandler(new ValueChangeHandler<UnionField>() {
            @Override
            public void onValueChange(ValueChangeEvent<UnionField> event) {
                fireChanged();  
            }
        }));
        return unionWidget;
    }

}
