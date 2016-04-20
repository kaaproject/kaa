/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.view.widget;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.StringField.InputType;

public class KaaAdminSizedTextBox extends SizedTextBox {

    public KaaAdminSizedTextBox(int maxChars) {
        super(maxChars, InputType.PLAIN, null);
    }
    
    public KaaAdminSizedTextBox(int maxChars, String prompt) {
        super(maxChars, InputType.PLAIN, prompt);
    }
    
    public KaaAdminSizedTextBox(int maxChars, boolean editable, boolean addNotes) {
        super(maxChars, InputType.PLAIN, null, editable, addNotes);
    }

    public KaaAdminSizedTextBox(int maxChars, String prompt, boolean editable, boolean addNotes) {
        super(maxChars, InputType.PLAIN, prompt, editable, addNotes);
    }

    public KaaAdminSizedTextBox(int maxChars, boolean editable) {
        super(maxChars, InputType.PLAIN, null, editable);
    }
    
    public KaaAdminSizedTextBox(int maxChars, String prompt, boolean editable) {
        super(maxChars, InputType.PLAIN, prompt, editable);
    }
    
}
