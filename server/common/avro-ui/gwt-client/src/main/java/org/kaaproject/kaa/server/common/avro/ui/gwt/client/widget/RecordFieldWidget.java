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

import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class RecordFieldWidget extends AbstractFieldWidget<RecordField> {

    public RecordFieldWidget() {
        super();
    }
    
    public RecordFieldWidget(Style style, SizedTextBox.Style sizedTextStyle) {
        super(style, sizedTextStyle);
    }

    @Override
    protected Widget constructForm() {
        FlexTable table = new FlexTable();
        constructFormData(table, value, registrations);
        return table;
    }
    
}
