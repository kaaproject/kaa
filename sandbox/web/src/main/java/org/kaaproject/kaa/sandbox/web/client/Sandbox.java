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

package org.kaaproject.kaa.sandbox.web.client;

import org.kaaproject.kaa.sandbox.web.client.layout.AppLayout;
import org.kaaproject.kaa.sandbox.web.client.mvp.ClientFactory;
import org.kaaproject.kaa.sandbox.web.client.mvp.activity.HeaderActivityMapper;
import org.kaaproject.kaa.sandbox.web.client.mvp.activity.SandboxActivityMapper;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.MainPlace;
import org.kaaproject.kaa.sandbox.web.client.mvp.place.SandboxPlaceHistoryMapper;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceAsync;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;

public class Sandbox implements EntryPoint {

    public static final String THEME = "clean"; //$NON-NLS-1$

    private static SandboxServiceAsync sandboxService = SandboxServiceAsync.Util.getInstance();

    private AppLayout appWidget = new AppLayout();

    @Override
    public void onModuleLoad() {
        init();
    }
    
    public static SandboxServiceAsync getSandboxService() {
        return sandboxService;
    }

    private void init() {
        injectThemeStyleSheet();
        Utils.resources.css().ensureInjected();

        ClientFactory clientFactory = GWT.create(ClientFactory.class);
        EventBus eventBus = clientFactory.getEventBus();

        PlaceController placeController = clientFactory.getPlaceController();

        ActivityMapper headerActivityMapper = new HeaderActivityMapper(clientFactory);
        ActivityManager headerActivityManager = new ActivityManager(headerActivityMapper, eventBus);
        headerActivityManager.setDisplay(appWidget.getAppHeaderHolder());

        ActivityMapper appActivityMapper = new SandboxActivityMapper(clientFactory);
        ActivityManager appActivityManager = new ActivityManager(appActivityMapper, eventBus);
        appActivityManager.setDisplay(appWidget.getAppContentHolder());

        PlaceHistoryMapper historyMapper = GWT.create(SandboxPlaceHistoryMapper.class);

        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);

        Place place = new MainPlace();

        historyHandler.register(placeController, eventBus, place);

        RootLayoutPanel.get().add(appWidget);

        // Goes to the place represented on URL else default place
        historyHandler.handleCurrentHistory();
    }

    /**
     * Convenience method for getting the document's head element.
     *
     * @return the document's head element
     */
    private native HeadElement getHeadElement() /*-{
      return $doc.getElementsByTagName("head")[0];
    }-*/;


    private void injectThemeStyleSheet() {
        // Choose the name style sheet based on the locale.
        String styleSheet = "gwt/" + THEME + "/" + THEME; //$NON-NLS-1$ //$NON-NLS-2$
        styleSheet += LocaleInfo.getCurrentLocale().isRTL() ? "_rtl.css" : ".css"; //$NON-NLS-1$ //$NON-NLS-2$

        // Load the GWT theme style sheet
        String modulePath = GWT.getModuleBaseURL();
        LinkElement linkElem = Document.get().createLinkElement();
        linkElem.setRel("stylesheet"); //$NON-NLS-1$
        linkElem.setType("text/css"); //$NON-NLS-1$
        linkElem.setHref(modulePath + styleSheet);
        getHeadElement().appendChild(linkElem);
  }
 
    public static void redirectToModule(String module) {
        setWindowHref("/"+module);
    }
    
    private static native void setWindowHref(String url) /*-{
        $wnd.location.href = url;
    }-*/; 

}
