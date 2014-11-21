package org.kaaproject.kaa.server.admin.client.mvp.view.widget.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.admin.shared.form.RecordField;
import org.kaaproject.kaa.server.admin.shared.form.UnionField;

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
