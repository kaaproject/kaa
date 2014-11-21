package org.kaaproject.kaa.server.admin.client.mvp.view.widget.form;

import com.google.gwt.dom.client.Document;
import com.google.gwt.text.client.IntegerParser;

public class IntegerBox extends DecimalBox<Integer> {
    public IntegerBox(String numberFormatPattern) {
        super(Document.get().createTextInputElement(), new NumberRenderer<Integer>(
                numberFormatPattern), IntegerParser.instance());
    }
}
