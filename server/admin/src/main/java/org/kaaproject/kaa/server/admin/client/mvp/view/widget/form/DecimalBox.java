package org.kaaproject.kaa.server.admin.client.mvp.view.widget.form;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.ValueBox;

public abstract class DecimalBox<T extends Number> extends ValueBox<T> {

    protected DecimalBox(Element element, NumberRenderer<T> renderer, Parser<T> parser) {
        super(element, renderer, parser);
        
        this.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                int keyCode = event.getCharCode();
                if ((keyCode == 46 || keyCode == 8 || keyCode == 9
                        || keyCode == 27 || keyCode == 13 || keyCode == 110 || keyCode == 190)
                        || (keyCode == 65 && event.isControlKeyDown())) {
                    return;
                }

                // Ensure that it is a number and stop the keypress
                if (keyCode < 48 || keyCode > 57) {
                    event.preventDefault();
                }
            }
        });
    }

    static class NumberRenderer<N extends Number> extends AbstractRenderer<N> {

        private NumberFormat numberFormat;

        public NumberRenderer(String numberFormatPattern) {
            numberFormat = NumberFormat.getFormat(numberFormatPattern);
        }

        public String render(N object) {
            if (null == object) {
                return "";
            }

            return numberFormat.format(object);
        }
    }    
}
