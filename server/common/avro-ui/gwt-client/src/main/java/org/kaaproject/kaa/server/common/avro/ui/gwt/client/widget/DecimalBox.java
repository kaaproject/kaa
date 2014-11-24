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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.ValueBox;

public abstract class DecimalBox<T extends Number> extends ValueBox<T> {

    protected DecimalBox(Element element, NumberRenderer<T> renderer, Parser<T> parser) {
        super(element, renderer, parser);
        this.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp (KeyUpEvent event) {
                if (event.isShiftKeyDown() ||
                    event.getNativeKeyCode() > '9' || event.getNativeKeyCode() < '0') {
                    String text = getText();
                    for (int ii = 0; ii < text.length(); ii++) {
                        if (text.charAt(ii) > '9' || text.charAt(ii) < '0') {
                            text = text.substring(0, ii) + text.substring(ii+1);
                            ii--;
                        }
                    }
                    setText(text);
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
