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

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;


public class LinkCell extends ClickableTextCell {

    String style;
    public LinkCell()
    {
        super();
        style = "LinkCell";
    }


     @Override
      protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
          sb.appendHtmlConstant("<div class=\""+style+"\">");
          sb.append(value);
          sb.appendHtmlConstant("</div>");
        }
      }

     public void addStyleName(String style)
     {
         this.style = style;
     }


}

