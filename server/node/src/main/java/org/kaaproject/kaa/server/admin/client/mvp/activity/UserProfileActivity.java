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

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.ChangePasswordDialog;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserProfileActivity
        extends
        AbstractDetailsActivity<UserDto, UserProfileView, UserProfilePlace> {

    public UserProfileActivity(UserProfilePlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.create = false;
    }

    @Override
    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
        registrations.add(detailsView.getChangePasswordButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showChangePasswordDialog();
            }
        }));
    }

    @Override
    protected String getEntityId(UserProfilePlace place) {
        return "";
    }

    @Override
    protected UserProfileView getView(boolean create) {
        return clientFactory.getUserProfileView();
    }

    @Override
    protected UserDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {
        detailsView.setTitle(entity.getUsername());
        detailsView.getAuthority().setValue(Utils.constants.getString(entity.getAuthority().getResourceKey()));
        detailsView.getFirstName().setValue(entity.getFirstName());
        detailsView.getLastName().setValue(entity.getLastName());
        detailsView.getEmail().setValue(entity.getMail());
    }

    @Override
    protected void onSave() {
        entity.setFirstName(detailsView.getFirstName().getValue());
        entity.setLastName(detailsView.getLastName().getValue());
        entity.setMail(detailsView.getEmail().getValue());
    }

    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        detailsView.clearError();

        checkEmail();
    }

    private void checkEmail() {
        final Long userId = Long.valueOf(entity.getExternalUid());
        KaaAdmin.getAuthService().checkEmailOccupied(entity.getMail(), userId,new BusyAsyncCallback<ResultCode>() {
            @Override
            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }

            @Override
            public void onSuccessImpl(ResultCode result) {
                if (result != ResultCode.OK) {
                    detailsView.setErrorMessage(Utils.constants.getString(result.getResourceKey()));
                } else {
                    performSave();
                }
            }
        });
    }

    private void performSave() {
        editEntity(entity,
                new BusyAsyncCallback<UserDto>() {
                    public void onSuccessImpl(UserDto result) {
                        if (place.getPreviousPlace() != null) {
                            goTo(place.getPreviousPlace());
                        }
                    }

                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, detailsView);
                    }
        });
    }

    @Override
    protected void getEntity(String id, AsyncCallback<UserDto> callback) {
        KaaAdmin.getDataSource().getUserProfile(callback);
    }

    @Override
    protected void editEntity(UserDto entity, AsyncCallback<UserDto> callback) {
        KaaAdmin.getDataSource().editUserProfile(new UserProfileUpdateDto(entity), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }

            @Override
            public void onSuccess(Void aVoid) {
                reload();
            }
        });
    }

    private void showChangePasswordDialog() {
        //change password
        ChangePasswordDialog.Listener listener = new ChangePasswordDialog.Listener() {
            @Override
            public void onChangePassword() {}
            @Override
            public void onCancel() {}
        };

        ChangePasswordDialog.showChangePasswordDialog(listener,
                KaaAdmin.getAuthInfo().getUsername(),
                null);
    }
}
