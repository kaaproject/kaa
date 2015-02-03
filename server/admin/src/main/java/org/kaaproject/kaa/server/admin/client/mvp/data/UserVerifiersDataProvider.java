package org.kaaproject.kaa.server.admin.client.mvp.data;

import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.util.List;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;

public class UserVerifiersDataProvider  extends AbstractDataProvider<UserVerifierDto>{
    private String applicationId;

    public UserVerifiersDataProvider(MultiSelectionModel<UserVerifierDto> selectionModel,
                                 HasErrorMessage hasErrorMessage,
                                 String applicationId) {
        super(selectionModel, hasErrorMessage);
        this.applicationId = applicationId;
    }

    @Override
    protected void loadData(final LoadCallback callback, final HasData<UserVerifierDto> display) {
        if (!isEmpty(applicationId)) {
            KaaAdmin.getDataSource().loadUserVerifiers(applicationId, new AsyncCallback<List<UserVerifierDto>>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }
                @Override
                public void onSuccess(List<UserVerifierDto> result) {
                    callback.onSuccess(result, display);
                }
            });
        }
    }
}
