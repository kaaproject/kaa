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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

public class TextAreaStructView<T extends AbstractStructureDto> extends BaseStructView<T, String> {

    public TextAreaStructView(HasErrorMessage hasErrorMessage) {
        super(hasErrorMessage);
    }

    @Override
    protected HasValue<String> createBody(HasErrorMessage hasErrorMessage) {
        SizedTextArea body = new SizedTextArea(-1);
        body.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 200);
        return body;
    }

    @Override
    protected boolean hasLabel() {
        return true;
    }

    @Override
    protected void setBodyReadOnly(boolean readOnly) {
        ((SizedTextArea)body).getTextArea().setReadOnly(readOnly);
    }

    @Override
    protected void setBodyValue(T struct) {
        body.setValue(struct.getBody());
    }

    @Override
    protected HandlerRegistration addBodyChangeHandler() {
        return ((SizedTextArea)body).addInputHandler(this);
    }

    @Override
    protected boolean validateBody() {
        return body.getValue().length()>0;
    }

    @Override
    protected void onShown() { 
    }

}
