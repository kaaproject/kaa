package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.UserVerifiersDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifierPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.UserVerifiersPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.MultiSelectionModel;

public class UserVerifiersActivity extends AbstractListActivity<UserVerifierDto, UserVerifiersPlace> {

    private String applicationId;

    public UserVerifiersActivity(UserVerifiersPlace place, ClientFactory clientFactory) {
        super(place, UserVerifierDto.class, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    protected BaseListView<UserVerifierDto> getView() {
        return clientFactory.getUserVerifiersView();
    }

    @Override
    protected AbstractDataProvider<UserVerifierDto> getDataProvider(MultiSelectionModel<UserVerifierDto> selectionModel) {
        return new UserVerifiersDataProvider(selectionModel, listView, applicationId);
    }

    @Override
    protected Place newEntityPlace() {
        return new UserVerifierPlace(applicationId, "");
    }

    @Override
    protected Place existingEntityPlace(String id) {
        return new UserVerifierPlace(applicationId, id);
    }

    @Override
    protected void deleteEntity(String id, AsyncCallback<Void> callback) {
        KaaAdmin.getDataSource().removeUserVerifier(id, callback);
    }

}
