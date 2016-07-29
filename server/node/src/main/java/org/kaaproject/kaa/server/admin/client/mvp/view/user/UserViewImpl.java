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

package org.kaaproject.kaa.server.admin.client.mvp.view.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.regexp.shared.RegExp;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

public class UserViewImpl extends BaseDetailsViewImpl implements UserView {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();
    
    private SizedTextBox userName;
    private SizedTextBox email;
    private ValueListBox<KaaAuthorityDto> authority;

    public UserViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNewUser();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.user();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.userDetails();
    }

    @Override
    protected void initDetailsTable() {

        userName = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE, create);
        userName.setWidth("100%");
        userName.addInputHandler(this);

        Label userLabel = new Label(Utils.constants.userName());
        if (create) {
            userLabel.addStyleName(REQUIRED);
        }
        detailsTable.setWidget(0, 0, userLabel);
        detailsTable.setWidget(0, 1, userName);

        email = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        email.setWidth("100%");
        email.addInputHandler(this);

        Label emailLabel = new Label(Utils.constants.email());
        emailLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(1, 0, emailLabel);
        detailsTable.setWidget(1, 1, email);

        Renderer<KaaAuthorityDto> authorityRenderer = new Renderer<KaaAuthorityDto>() {
            @Override
            public String render(KaaAuthorityDto object) {
                if (object != null) {
                    return Utils.constants.getString(object.getResourceKey());
                } else {
                    return "";
                }
            }

            @Override
            public void render(KaaAuthorityDto object, Appendable appendable)
                    throws IOException {
                appendable.append(render(object));
            }
        };

        authority = new ValueListBox<>(authorityRenderer);
        authority.setWidth("100%");
        authority.addValueChangeHandler(new ValueChangeHandler<KaaAuthorityDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<KaaAuthorityDto> event) {
                fireChanged();
            }
        });

        List<KaaAuthorityDto> possibleAuthorities = new ArrayList<KaaAuthorityDto>();

        if(KaaAdmin.getAuthInfo().getAuthority().equals(KaaAuthorityDto.TENANT_ADMIN)){
            possibleAuthorities.add(KaaAuthorityDto.TENANT_DEVELOPER);
            possibleAuthorities.add(KaaAuthorityDto.TENANT_USER);
        }
        if(KaaAdmin.getAuthInfo().getAuthority().equals(KaaAuthorityDto.KAA_ADMIN)){
            possibleAuthorities.add(KaaAuthorityDto.TENANT_ADMIN);
        }

        authority.setAcceptableValues(possibleAuthorities);

        Label authorityLabel = new Label(Utils.constants.accountRole());
        authorityLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(2, 0, authorityLabel);
        detailsTable.setWidget(2, 1, authority);

        userName.setFocus(true);
    }

    @Override
    protected void resetImpl() {
        authority.setValue(null);
        userName.setValue("");
        email.setValue("");
    }

    @Override
    protected boolean validate() {
        String pattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        RegExp regExp=RegExp.compile(pattern);
        return userName.getValue().length()>0 &&  authority.getValue() != null && regExp.test(email.getValue());
    }

    @Override
    public HasValue<String> getUserName() {
        return userName;
    }

    @Override
    public HasValue<String> getEmail() {
        return email;
    }

    @Override
    public HasValue<KaaAuthorityDto> getAuthority() {
        return authority;
    }

}
