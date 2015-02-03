package org.kaaproject.kaa.server.admin.client.mvp.view.verifier;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseListViewImpl;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class UserVerifiersViewImpl extends BaseListViewImpl<UserVerifierDto> {

    public UserVerifiersViewImpl() {
        super(true);
    }

    @Override
    protected AbstractGrid<UserVerifierDto, String> createGrid() {
        return new UserVerifierGrid(false);
    }

    @Override
    protected String titleString() {
        return Utils.constants.userVerifiers();
    }

    @Override
    protected String addButtonString() {
        return Utils.constants.addNewUserVerifier();
    }

}
