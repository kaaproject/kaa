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

import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class FlumeBalancingTypeListBox extends ValueListBox<FlumeBalancingTypeDto> {
    public FlumeBalancingTypeListBox() {
        super(new FlumeBalancingTypeListBoxRenderer());
    }

    public void reset() {
        List<FlumeBalancingTypeDto> emptyList = Collections.emptyList();
        setValue(null);
        setAcceptableValues(emptyList);
    }

    static class FlumeBalancingTypeListBoxRenderer implements Renderer<FlumeBalancingTypeDto> {

        @Override
        public String render(FlumeBalancingTypeDto object) {
            return object != null ? object.getLabel() : "";
        }

        @Override
        public void render(FlumeBalancingTypeDto object, Appendable appendable) throws IOException {
            appendable.append(render(object));
        }
    }
}
