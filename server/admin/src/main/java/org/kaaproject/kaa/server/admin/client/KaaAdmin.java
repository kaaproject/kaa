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

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.client.layout.AppLayout;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.AppActivityMapper;
import org.kaaproject.kaa.server.admin.client.mvp.activity.HeaderActivityMapper;
import org.kaaproject.kaa.server.admin.client.mvp.activity.NavigationActivityMapper;
import org.kaaproject.kaa.server.admin.client.mvp.data.DataSource;
import org.kaaproject.kaa.server.admin.client.mvp.place.ApplicationsPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.KaaAdminPlaceHistoryMapper;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantAdminPlaceHistoryMapper;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantDeveloperPlaceHistoryMapper;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantUserPlaceHistoryMapper;
import org.kaaproject.kaa.server.admin.client.mvp.place.TenantsPlace;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.dto.AuthResultDto;
import org.kaaproject.kaa.server.admin.shared.dto.AuthResultDto.Result;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceAsync;
import org.kaaproject.kaa.server.admin.shared.services.KaaAuthServiceAsync;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class KaaAdmin implements EntryPoint {

    public static final String THEME = "clean"; //$NON-NLS-1$

    private static AuthResultDto authInfo;

    private static DataSource dataSource;

    private static KaaAuthServiceAsync authService = KaaAuthServiceAsync.Util.getInstance();

    private static KaaAdminServiceAsync adminService = KaaAdminServiceAsync.Util.getInstance();

    private AppLayout appWidget = new AppLayout();

    @Override
    public void onModuleLoad() {
        authService.checkAuth(new AsyncCallback<AuthResultDto>() {
            @Override
            public void onFailure(Throwable caught) {
                showLogin();
            }

            @Override
            public void onSuccess(AuthResultDto result) {
                if (result.getAuthResult()==Result.OK) {
                    authInfo = result;
                    init();
                }
                else {
                    showLogin();
                }
            }
        });

    }

    public static AuthResultDto getAuthInfo() {
        return authInfo;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static KaaAuthServiceAsync getAuthService() {
        return authService;
    }

    public static boolean checkAuthorities(KaaAuthorityDto... authorities) {
        for (KaaAuthorityDto authority : authorities) {
            if (authInfo.getAuthority() == authority) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDevMode() {
        return checkAuthorities(KaaAuthorityDto.TENANT_DEVELOPER,
                KaaAuthorityDto.TENANT_USER);
    }

    private void showLogin() {
        redirectToModule("..");
    }

    private void init() {
        injectThemeStyleSheet();
        Utils.resources.css().ensureInjected();

        KaaAdminServiceAsync rpcService = GWT.create(KaaAdminService.class);
        ClientFactory clientFactory = GWT.create(ClientFactory.class);
        EventBus eventBus = clientFactory.getEventBus();

        dataSource = new DataSource(rpcService, eventBus);

        PlaceController placeController = clientFactory.getPlaceController();

        ActivityMapper headerActivityMapper = new HeaderActivityMapper(clientFactory);
        ActivityManager headerActivityManager = new ActivityManager(headerActivityMapper, eventBus);
        headerActivityManager.setDisplay(appWidget.getAppHeaderHolder());

        ActivityMapper navigationActivityMapper = new NavigationActivityMapper(clientFactory, eventBus);
        ActivityManager navigationActivityManager = new ActivityManager(navigationActivityMapper, eventBus);
        navigationActivityManager.setDisplay(appWidget.getNavContentHolder());

        ActivityMapper appActivityMapper = new AppActivityMapper(clientFactory);
        ActivityManager appActivityManager = new ActivityManager(appActivityMapper, eventBus);
        appActivityManager.setDisplay(appWidget.getAppContentHolder());

        PlaceHistoryMapper historyMapper = null;
        switch (authInfo.getAuthority()) {
        case KAA_ADMIN:
            historyMapper = GWT.create(KaaAdminPlaceHistoryMapper.class);
            break;
        case TENANT_ADMIN:
            historyMapper = GWT.create(TenantAdminPlaceHistoryMapper.class);
            break;
        case TENANT_DEVELOPER:
            historyMapper = GWT.create(TenantDeveloperPlaceHistoryMapper.class);
            break;
        case TENANT_USER:
            historyMapper = GWT.create(TenantUserPlaceHistoryMapper.class);
            break;

        }

        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);

        Place place;
        if (authInfo.getAuthority()==KaaAuthorityDto.KAA_ADMIN) {
            place = new TenantsPlace();
        }
        else {
            place = new ApplicationsPlace();
        }

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

    public static void signOut() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL()+"j_spring_security_logout");
        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request,
                        Response response) {
                    redirectToModule("..");
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    redirectToModule("..");
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    public static void redirectToModule(String module) {
        String path = Window.Location.getPath();
        if (!path.endsWith("/")) {
            if (path.endsWith(".html") || path.endsWith(".htm")) {
                int index = path.lastIndexOf('/');
                path = path.substring(0, index+1);
            }
            else {
                path += "/";
            }
        }
        String target = path + module + "/";
        Window.Location.assign(Window.Location.createUrlBuilder().setPath(target).buildString());
    }

}
