/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.widget;

import org.kaaproject.kaa.sandbox.demo.projects.Feature;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;

public class FeatureButton extends ToggleButton {

    private String text;
    private ImageResource upResource;
    private ImageResource downResource;
    private Image image;

    public FeatureButton(Feature feature) {
        super();
        setStyleName(Utils.sandboxStyle.featureButton());
        setText(Utils.getFeatureText(feature));        
        getElement().removeAttribute("tabIndex");
        upResource = Utils.getFeatureIcon(feature, false);
        downResource = Utils.getFeatureIcon(feature, true);
        image = new Image(upResource);
        String definedStyles = image.getElement().getAttribute("style");
        image.getElement().setAttribute("style",
                definedStyles + "; vertical-align:middle;");
        DOM.insertBefore(getElement(), image.getElement(),
                DOM.getFirstChild(getElement()));
    }
    
    @Override
    public int getTabIndex() {
        return 0;
    }

    @Override
    public void setTabIndex(int index) {
    }
    
    private void updateResource() {
        setResource(isDown() ? downResource : upResource);
    }
    
    @Override
    protected void onClick() {
        super.onClick();
        updateResource();
    }

    void setResource(ImageResource imageResource) {
        image.setResource(imageResource);
    }

    @Override
    public void setText(String text) {
        this.text = text;
        Element span = DOM.createElement("span");
        span.setInnerText(text);
        span.setAttribute("style", "padding-left:6px; vertical-align:middle;");

        DOM.insertChild(getElement(), span, 0);
    }

    @Override
    public String getText() {
        return this.text;
    }
}
