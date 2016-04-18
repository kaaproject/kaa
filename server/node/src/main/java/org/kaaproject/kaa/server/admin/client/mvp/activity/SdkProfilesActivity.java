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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.SdkProfilesDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.AddSdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.KaaRowAction;
import org.kaaproject.kaa.server.admin.client.mvp.view.sdk.GenerateSdkDialog;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
public class SdkProfilesActivity extends AbstractListActivity<SdkProfileDto, SdkProfilesPlace> {

    private final String applicationId;

    public SdkProfilesActivity(SdkProfilesPlace place, ClientFactory clientFactory) {
        super(place, SdkProfileDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<SdkProfileDto> getView() {
        return clientFactory.getSdkProfilesView();
    }

    @Override
    protected AbstractDataProvider<SdkProfileDto, String> getDataProvider(AbstractGrid<SdkProfileDto, String> dataGrid) {
        return new SdkProfilesDataProvider(dataGrid, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new AddSdkProfilePlace(applicationId);
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new SdkProfilePlace(applicationId, id);
    }

    @Override
    protected void deleteEntity(final String id, final AsyncCallback<Void> callback) {
        SdkProfilesActivity.this.getView().clearError();

        BusyPopup.showPopup();
        KaaAdmin.getDataSource().deleteSdkProfile(id, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                BusyPopup.hidePopup();
                Utils.handleException(caught, SdkProfilesActivity.this.getView());
            }

            @Override
            public void onSuccess(Void result) {
                BusyPopup.hidePopup();
            }
        });
    }

    @Override
    protected void onCustomRowAction(RowActionEvent<String> event) {
        if (event.getAction() == KaaRowAction.GENERATE_SDK) {

            KaaAdmin.getDataSource().getSdkProfile(event.getClickedId(), new AsyncCallback<SdkProfileDto>() {

                @Override
                public void onFailure(Throwable caught) {
                    Utils.handleException(caught, SdkProfilesActivity.this.getView());
                }

                @Override
                public void onSuccess(final SdkProfileDto sdkProfile) {

                    GenerateSdkDialog.show(new GenerateSdkDialog.Listener() {

                        @Override
                        public void onGenerateSdk(SdkPlatform targetPlatform) {
                            BusyPopup.showPopup();
                            KaaAdmin.getDataSource().generateSdk(sdkProfile, targetPlatform, new AsyncCallback<String>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    BusyPopup.hidePopup();
                                    Utils.handleException(caught, SdkProfilesActivity.this.getView());
                                }

                                @Override
                                public void onSuccess(String key) {
                                    BusyPopup.hidePopup();
                                    SdkProfilesActivity.this.getView().clearError();
                                    ServletHelper.downloadSdk(key);
                                }
                            });
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
            });
        }
    }
}
