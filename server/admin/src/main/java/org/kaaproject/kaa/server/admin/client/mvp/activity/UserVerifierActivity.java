package org.kaaproject.kaa.server.admin.client.mvp.activity;

import java.util.List;

import org.kaaproject.kaa.common.dto.plugin.PluginInfoDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifierPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserVerifierView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserVerifierActivity extends AbstractPluginActivity<UserVerifierDto, UserVerifierView, UserVerifierPlace> {

    private String applicationId;

    public UserVerifierActivity(UserVerifierPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected UserVerifierView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateUserVerifierView();
        } else {
            return clientFactory.getUserVerifierView();
        }
    }

    @Override
    protected UserVerifierDto newEntity() {
        UserVerifierDto dto = new UserVerifierDto();
        dto.setApplicationId(applicationId);
        return dto;
    }

    @Override
    protected void loadPluginInfos(AsyncCallback<List<PluginInfoDto>> callback) {
        KaaAdmin.getDataSource().loadUserVerifierPluginInfos(callback);
    }

    @Override
    protected void onEntityRetrieved() {
        super.onEntityRetrieved();
        if (!create) {
            detailsView.getVerifierToken().setValue(entity.getVerifierToken());
        }
    }

    @Override
    protected void getEntity(String id, AsyncCallback<UserVerifierDto> callback) {
        KaaAdmin.getDataSource().getUserVerifierForm(id, callback);
    }

    @Override
    protected void editEntity(UserVerifierDto entity, AsyncCallback<UserVerifierDto> callback) {
        KaaAdmin.getDataSource().editUserVerifierForm(entity, callback);
    }


}