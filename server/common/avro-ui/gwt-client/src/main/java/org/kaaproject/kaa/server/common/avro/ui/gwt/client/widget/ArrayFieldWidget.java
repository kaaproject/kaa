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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.common.avro.ui.shared.ArrayField;
import org.kaaproject.kaa.server.common.avro.ui.shared.FieldType;
import org.kaaproject.kaa.server.common.avro.ui.shared.FormField;
import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ArrayFieldWidget extends AbstractFieldWidget<ArrayField> {

    private static ArrayFieldConstants constants = (ArrayFieldConstants) GWT.create(ArrayFieldConstants.class);
    
    public interface ArrayFieldConstants extends Constants {

        @DefaultStringValue("Add")
        String add();
     
        @DefaultStringValue("Remove")
        String remove();
     
    }
    
    public ArrayFieldWidget() {
        super();
    }
    
    public ArrayFieldWidget(Style style, SizedTextBox.Style sizedTextStyle) {
        super(style, sizedTextStyle);
    }
    
    @Override
    protected Widget constructForm() {
        CaptionPanel arrayWidget = new CaptionPanel();
        arrayWidget.setWidth("125%");
        
        if (value.isOptional()) {
            arrayWidget.setCaptionText(value.getDisplayName());
        }
        else {
            SpanElement span = Document.get().createSpanElement();
            span.appendChild(Document.get().createTextNode(value.getDisplayName()));
            span.addClassName("gwt-Label");
            span.addClassName(style.requiredField());            
            arrayWidget.setCaptionHTML(span.getString());
        }
        
        VerticalPanel verticalPanel = new VerticalPanel();
        ScrollPanel scroll = new ScrollPanel();
        final FlexTable table = new FlexTable();
        table.setWidth("95%");
        
        List<RecordField> records = value.getValue();
        final int minRowCount = value.getMinRowCount();
        final RecordField elementMetadata = value.getElementMetadata();
        
        float totalWeight = 0f;
        for (int column=0;column<elementMetadata.getValue().size();column++) {
            FormField metaField = elementMetadata.getValue().get(column);
            totalWeight += metaField.getWeight();
        }
        
        for (int column=0;column<elementMetadata.getValue().size();column++) {
            FormField metaField = elementMetadata.getValue().get(column);
            float weight = metaField.getWeight();
            String width = String.valueOf(weight/totalWeight*100f)+"%";
            table.getColumnFormatter().setWidth(column, width);
            table.setWidget(0, column, new Label(metaField.getDisplayName()));
        }
        final Map<RecordField, List<HandlerRegistration>> rowHandlerRegistrationMap = 
                new HashMap<>();
       
        for (int row=0;row<records.size();row++) {
            RecordField record = records.get(row);
            List<HandlerRegistration> rowHandlerRegistrations = new ArrayList<>();
            setRow(table, record, row+1, rowHandlerRegistrations);
            registrations.addAll(rowHandlerRegistrations);
            rowHandlerRegistrationMap.put(record, rowHandlerRegistrations);
        }
       
        scroll.setWidth("100%");
        scroll.setHeight("200px");
        scroll.add(table);

        verticalPanel.setWidth("100%");
        verticalPanel.add(scroll);
        
        Button addRow = new Button(constants.add());
        final Button removeRow = new Button(constants.remove());
        removeRow.setEnabled(value.getValue().size()>minRowCount);
        
        addRow.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RecordField newRecord = (RecordField) elementMetadata.clone();
                value.addArrayData(newRecord);
                List<HandlerRegistration> rowHandlerRegistrations = new ArrayList<>();
                setRow(table, newRecord, value.getValue().size(), rowHandlerRegistrations);
                rowHandlerRegistrationMap.put(newRecord, rowHandlerRegistrations);
                removeRow.setEnabled(value.getValue().size()>minRowCount);
                fireChanged();
            }
        });
       
        removeRow.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (value.getValue().size()>0) {
                    int row = value.getValue().size()-1;
                    RecordField toDelete = value.getValue().get(row);
                    List<HandlerRegistration> registrations = rowHandlerRegistrationMap.remove(toDelete);
                    if (registrations != null) {
                        for (HandlerRegistration registration : registrations) {
                            registration.removeHandler();
                        }
                        registrations.clear();
                    }
                    table.removeRow(row+1);
                    value.getValue().remove(row);
                    removeRow.setEnabled(value.getValue().size()>minRowCount);
                    fireChanged();
                }
            }
        });

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.addStyleName(style.buttonsPanel());
        buttonsPanel.add(addRow);
        buttonsPanel.add(removeRow);
        
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel.add(buttonsPanel);
        
        arrayWidget.setContentWidget(verticalPanel);
        
        return arrayWidget;
    }
    
    private void setRow(FlexTable table, RecordField record, int row, List<HandlerRegistration> handlerRegistrations) {
        for (int column=0;column<record.getValue().size();column++) {
            FormField cellField = record.getValue().get(column);
            if (cellField.getFieldType() != FieldType.ARRAY &&
                    cellField.getFieldType() != FieldType.RECORD) {
                constructWidget(table, cellField, row, column, handlerRegistrations);
            }
        }
    }

}
