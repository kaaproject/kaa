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

package org.kaaproject.kaa.server.admin.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface KaaAdminResources extends ClientBundle {

    public interface Css extends CssResource {

    }

    @NotStrict
    @Source("KaaAdmin.css")
    Css css();

    @ImageOptions(width = 14, height = 14)
    @Source("images/remove.png")
    ImageResource remove();

    @ImageOptions(width = 14, height = 14)
    @Source("images/send.png")
    ImageResource send();

    @ImageOptions(width = 14, height = 14)
    @Source("images/details.png")
    ImageResource details();

    @ImageOptions(width = 14, height = 14)
    @Source("images/plus.png")
    ImageResource plus();

    @ImageOptions(width = 14, height = 14)
    @Source("images/drop-down.png")
    ImageResource drop_down();

    @ImageOptions(width = 0, height = 0)
    @Source("images/circles.png")
    ImageResource circles();

    @ImageOptions(width = 0, height = 0)
    @Source("images/circles_ie6.png")
    ImageResource circles_ie6();
    
    @ImageOptions(width = 0, height = 0)
    @Source("images/vborder.png")
    ImageResource vborder();

    @ImageOptions(width = 0, height = 0)
    @Source("images/vborder_ie6.png")
    ImageResource vborder_ie6();

    @ImageOptions(width = 14, height = 14)
    @Source("images/download_icon_grey.png")
    ImageResource download();

}
