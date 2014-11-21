package org.kaaproject.kaa.server.admin.client.mvp.view.widget.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.form.ArrayField;
import org.kaaproject.kaa.server.admin.shared.form.FieldType;
import org.kaaproject.kaa.server.admin.shared.form.FormField;
import org.kaaproject.kaa.server.admin.shared.form.RecordField;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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

    public ArrayFieldWidget() {
        super();
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
            span.addClassName("required");            
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
        
        Button addRow = new Button(Utils.constants.add());
        final Button removeRow = new Button(Utils.constants.remove());
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
        buttonsPanel.addStyleName("b-app-buttons-panel");
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
