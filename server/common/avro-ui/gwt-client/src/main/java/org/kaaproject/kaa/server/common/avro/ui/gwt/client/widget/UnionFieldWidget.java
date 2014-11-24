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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.common.avro.ui.shared.UnionField;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

public class UnionFieldWidget extends AbstractFieldWidget<UnionField> implements ValueChangeHandler<RecordField> {

    private List<HandlerRegistration> recordTableRegistrations = new ArrayList<HandlerRegistration>();
    private FlexTable recordTable;
    
    public UnionFieldWidget() {
        super();
    }
    
    public UnionFieldWidget(Style style, SizedTextBox.Style sizedTextStyle) {
        super(style, sizedTextStyle);
    }

    @Override
    protected Widget constructForm() {
        FlexTable table = new FlexTable();
        table.getColumnFormatter().setWidth(0, "200px");
        table.getColumnFormatter().setWidth(1, "300px");
        constructLabel(table, value, 0, 0);
        
        RecordValuesListBox recordValuesBox = new RecordValuesListBox();
        recordValuesBox.setAcceptableValues(value.getAcceptableValues());
        recordValuesBox.setWidth("100%");
        registrations.add(recordValuesBox.addValueChangeHandler(this));
        
        table.setWidget(0, 1, recordValuesBox);

        recordTable = new FlexTable();
        table.setWidget(1, 0, recordTable);
        table.getFlexCellFormatter().setColSpan(1, 0, 3);
        recordValuesBox.setValue(value.getValue(), true);
        return table;
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        for (HandlerRegistration registration : recordTableRegistrations) {
            registration.removeHandler();
        }
        recordTableRegistrations.clear();
        
        RecordField recordField = event.getValue();
        value.setValue(recordField);
        
        if (recordField == null) {
            recordTable.clear();
        }
        else {
            constructFormData(recordTable, recordField, recordTableRegistrations);
        }
        fireChanged();
    }
    
    static class RecordValuesListBox extends ValueListBox<RecordField> {

        public RecordValuesListBox() {
            super(new RecordFieldRenderer(), new RecordFieldKeyProvider());
        }
        
    }
    
    static class RecordFieldKeyProvider implements ProvidesKey<RecordField> {

        @Override
        public Object getKey(RecordField item) {
            return item != null ? item.getTypeFullname() : null;
        }
        
    }
    
    static class RecordFieldRenderer implements Renderer<RecordField> {

        @Override
        public String render(RecordField object) {
            return object != null ? object.getDisplayName() : "";
        }

        @Override
        public void render(RecordField object, Appendable appendable)
                throws IOException {
            appendable.append(render(object));
        }
    }

}
