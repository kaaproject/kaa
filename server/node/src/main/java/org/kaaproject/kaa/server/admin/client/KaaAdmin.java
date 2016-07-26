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

package org.kaaproject.kaa.server.admin.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto.Result;
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
import org.kaaproject.kaa.server.admin.shared.services.KaaAuthServiceAsync;

public class KaaAdmin implements EntryPoint {

    private static AuthResultDto authInfo;

    private static DataSource dataSource;

    private static KaaAuthServiceAsync authService = KaaAuthServiceAsync.Util.getInstance();

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
                } else {
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
        Utils.injectKaaStyles();
        
        ClientFactory clientFactory = GWT.create(ClientFactory.class);
        EventBus eventBus = clientFactory.getEventBus();

        dataSource = new DataSource(eventBus);

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
            clientFactory.setHomePlace(new TenantsPlace());
            break;
        case TENANT_ADMIN:
            historyMapper = GWT.create(TenantAdminPlaceHistoryMapper.class);
            clientFactory.setHomePlace(new ApplicationsPlace());
            break;
        case TENANT_DEVELOPER:
            historyMapper = GWT.create(TenantDeveloperPlaceHistoryMapper.class);
            clientFactory.setHomePlace(new ApplicationsPlace());
            break;
        case TENANT_USER:
            historyMapper = GWT.create(TenantUserPlaceHistoryMapper.class);
            clientFactory.setHomePlace(new ApplicationsPlace());
            break;

        }

        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);

        Place place;
        if (authInfo.getAuthority()==KaaAuthorityDto.KAA_ADMIN) {
            place = new TenantsPlace();
        } else {
            place = new ApplicationsPlace();
        }

        historyHandler.register(placeController, eventBus, place);

        RootLayoutPanel.get().add(appWidget);

        // Goes to the place represented on URL else default place
        historyHandler.handleCurrentHistory();
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
        }
    }

    public static void redirectToModule(String module) {
        String path = Window.Location.getPath();
        if (!path.endsWith("/")) {
            if (path.endsWith(".html") || path.endsWith(".htm")) {
                int index = path.lastIndexOf('/');
                path = path.substring(0, index+1);
            } else {
                path += "/";
            }
        }
        String target = path + module + "/";
        Window.Location.assign(Window.Location.createUrlBuilder().setPath(target).buildString());
    }

}
