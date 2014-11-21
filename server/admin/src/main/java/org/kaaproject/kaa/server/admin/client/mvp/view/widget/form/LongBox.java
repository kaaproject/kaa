package org.kaaproject.kaa.server.admin.client.mvp.view.widget.form;

import com.google.gwt.dom.client.Document;
import com.google.gwt.text.client.LongParser;

public class LongBox extends DecimalBox<Long> {
    public LongBox(String numberFormatPattern) {
        super(Document.get().createTextInputElement(), new NumberRenderer<Long>(
                numberFormatPattern), LongParser.instance());
    }
}
