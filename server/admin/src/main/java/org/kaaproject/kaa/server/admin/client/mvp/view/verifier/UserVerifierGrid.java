package org.kaaproject.kaa.server.admin.client.mvp.view.verifier;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.plugin.BasePluginGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.user.cellview.client.DataGrid;

public class UserVerifierGrid extends BasePluginGrid<UserVerifierDto> {

    public UserVerifierGrid(boolean embedded) {
        super(embedded);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<UserVerifierDto> table) {
        float prefWidth = super.constructColumnsImpl(table);

        prefWidth += constructStringColumn(table, Utils.constants.verifierToken(),
                new StringValueProvider<UserVerifierDto>() {
            @Override
            public String getValue(UserVerifierDto item) {
                return item.getVerifierToken();
            }
        }, 80);
        return prefWidth;
    }

    @Override
    protected String deleteQuestion() {
        return Utils.messages.removeUserVerifierQuestion();
    }

    @Override
    protected String deleteTitle() {
        return Utils.messages.removeUserVerifierTitle();
    }

}
