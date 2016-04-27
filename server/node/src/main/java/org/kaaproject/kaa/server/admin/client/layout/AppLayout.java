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

package org.kaaproject.kaa.server.admin.client.layout;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.kaa.server.admin.client.KaaAdminResources.KaaAdminStyle;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.common.Version;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class AppLayout extends Composite {
    interface AppLayoutUiBinder extends UiBinder<Widget, AppLayout> {
    }

    private static AppLayoutUiBinder uiBinder = GWT.create(AppLayoutUiBinder.class);
    private static final String OPEN = "<<";
    private static final String CLOSE = ">>";
    private static final int PANEL_WIDTH = 200;
    private static final String PANEL_WIDTH_IN_PX = PANEL_WIDTH + "px";
    private static final double DELTA_FACTOR = 2.0;

    private int clickCount = 1;

    @UiField
    FlowPanel mainLayoutPanel;
    @UiField
    SimplePanel appHeader;
    @UiField
    FlowPanel navPanel;
    @UiField
    SimpleWidgetPanel appContent;
    CustomDeckLayoutPanel navContent;
    @UiField 
    HTMLPanel footerPanel;
    
    @UiField(provided=true) 
    final KaaAdminStyle kaaAdminStyle;
    @UiField(provided=true) 
    final AvroUiStyle avroUiStyle;

    public AppLayout() {
        kaaAdminStyle = Utils.kaaAdminStyle;
        avroUiStyle = Utils.avroUiStyle;
        initWidget(uiBinder.createAndBindUi(this));
        init();
    }

    public SimplePanel getAppHeaderHolder() {
        return this.appHeader;
    }

    public CustomDeckLayoutPanel getNavContentHolder() {
        return this.navContent;
    }

    public SimpleWidgetPanel getAppContentHolder() {
        return this.appContent;
    }

    private void incr() {
        ++clickCount;
    }

    private void init() {
        final SimplePanel emptyPanel = new SimplePanel();
        navContent = new CustomDeckLayoutPanel();
        navContent.setAnimationVertical(false);

        navContent.setAnimationDuration(0);
        navContent.setStyleName(kaaAdminStyle.bNavContent());
        navContent.setSize("200px", "100%");

        navPanel.add(navContent);
        final Label back = new Label(OPEN);
        back.setStyleName(kaaAdminStyle.bNavLabel());

        navPanel.add(back);
        appHeader.setSize("100%", "60px");

        appContent.setStyleName(avroUiStyle.bAppContent());
        navPanel.setStyleName(kaaAdminStyle.bNavPanel());
        
        footerPanel.getElement().setInnerHTML(Utils.messages.footerMessage(Version.PROJECT_VERSION));

        back.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (clickCount == 1) {
                    navContent.add(emptyPanel);
                    navContent.setAnimationDuration(100);
                    navContent.showWidget(clickCount, new CustomAnimationCallback(true));
                    back.setText(CLOSE);
                } else if (clickCount % 2 == 0) {
                    navContent.showWidget(0, new CustomAnimationCallback(false));
                    back.setText(OPEN);
                } else {
                    navContent.showWidget(1, new CustomAnimationCallback(true));
                    back.setText(CLOSE);
                }
                incr();
            }
        });
    }

    class CustomAnimationCallback implements AnimationCallback {

        private boolean isOpen;

        public CustomAnimationCallback(boolean isOpen) {
            this.isOpen = isOpen;
        }

        @Override
        public void onAnimationComplete() {
            if (isOpen) {
                navContent.setSize("0", "100%");
            } else {
                navContent.setSize(PANEL_WIDTH_IN_PX, "100%");
            }
        }

        @Override
        public void onLayout(Layer arg0, double progres) {
            navContent.setSize(getNewSize(progres) + "px", "100%");
        }

        private double getNewSize(double progres) {
            double delta = getDelta(progres);
            if (isOpen) {
                double newSize = PANEL_WIDTH - delta;
                if (newSize < 0) {
                    return 0;
                } else {
                    return newSize;
                }
            } else {
                double newSize = delta;
                if (newSize > PANEL_WIDTH) {
                    return PANEL_WIDTH;
                } else {
                    return newSize;
                }
            }
        }

        private double getDelta(double progres) {
            if (isOpen) {
                return PANEL_WIDTH * progres * DELTA_FACTOR;
            } else {
                return PANEL_WIDTH * progres;
            }

        }
    }

}
