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

package org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class UneditableCheckboxCell extends AbstractEditableCell<Boolean, Boolean> {

    private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils
            .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled=\"disabled\"/>");

    private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>");


    @Override
    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
      // Get the view data.
      Object key = context.getKey();
      Boolean viewData = getViewData(key);
      if (viewData != null && viewData.equals(value)) {
        clearViewData(key);
        viewData = null;
      }

      if (value != null && ((viewData != null) ? viewData : value)) {
          sb.append(INPUT_CHECKED_DISABLED);
        } else {
          sb.append(INPUT_UNCHECKED_DISABLED);
        }
  }

    @Override
    public boolean isEditing(Context context, Element parent, Boolean value) {
        return false;
    }

}
