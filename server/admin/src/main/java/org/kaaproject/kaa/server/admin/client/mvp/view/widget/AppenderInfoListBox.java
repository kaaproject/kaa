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

package org.kaaproject.kaa.server.admin.client.mvp.view.widget;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderInfoDto;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class AppenderInfoListBox extends ValueListBox<LogAppenderInfoDto> {
    public AppenderInfoListBox() {
        super(new AppenderInfoListBoxRenderer());
    }

    public void reset() {
        List<LogAppenderInfoDto> emptyList = Collections.emptyList();
        setValue(null);
        setAcceptableValues(emptyList);
    }

    static class AppenderInfoListBoxRenderer implements Renderer<LogAppenderInfoDto> {

        @Override
        public String render(LogAppenderInfoDto object) {
            return object != null ? object.getName() : "";
        }

        @Override
        public void render(LogAppenderInfoDto object, Appendable appendable) throws IOException {
            appendable.append(render(object));
        }
    }
}
