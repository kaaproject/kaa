package org.kaaproject.kaa.server.admin.client.mvp.view.widget.form;

import org.kaaproject.kaa.server.admin.shared.form.RecordField;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class RecordFieldWidget extends AbstractFieldWidget<RecordField> {

    public RecordFieldWidget() {
        super();
    }

    @Override
    protected Widget constructForm() {
        FlexTable table = new FlexTable();
        constructFormData(table, value, registrations);
        return table;
    }
    
}
