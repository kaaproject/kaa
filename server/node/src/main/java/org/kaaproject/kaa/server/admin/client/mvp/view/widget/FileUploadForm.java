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

import org.kaaproject.kaa.server.admin.client.util.GUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;

public class FileUploadForm extends FormPanel {

    public final static String KAA_FILE_UPLOAD_SERVLET_PATH = "servlet/kaaFileUploadServlet";

    private FileUpload fu =  new FileUpload();

    public FileUploadForm() {
        this.setEncoding(FormPanel.ENCODING_MULTIPART);
        this.setMethod(FormPanel.METHOD_POST);
        fu.setName(GUID.get());
        fu.setHeight("30px");
        this.add(fu);
        addSubmitHandler(new FormPanel.SubmitHandler() {
            public void onSubmit(SubmitEvent event) {
                if ("".equalsIgnoreCase(fu.getFilename())) {
                    event.cancel();
                }
            }
        });
        this.setAction(GWT.getModuleBaseURL()+KAA_FILE_UPLOAD_SERVLET_PATH);
    }

    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return fu.addChangeHandler(handler);
    }

    public String getFileItemName() {
        return fu.getName();
    }

    public String getFileName() {
        return fu.getFilename();
    }

    public void reset() {
        fu.getElement().setPropertyString("value", "");
    }
}
