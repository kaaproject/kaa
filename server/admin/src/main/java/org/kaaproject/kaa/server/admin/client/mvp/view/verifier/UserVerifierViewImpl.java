package org.kaaproject.kaa.server.admin.client.mvp.view.verifier;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.UserVerifierView;
import org.kaaproject.kaa.server.admin.client.mvp.view.plugin.BasePluginViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class UserVerifierViewImpl extends BasePluginViewImpl implements UserVerifierView {

    private SizedTextBox verifierToken;

    public UserVerifierViewImpl(boolean create) {
        super(create);
    }
    
    @Override
    protected int initPluginDetails(int idx) {
        Label verifierTokenLabel = new Label(Utils.constants.verifierToken());
        verifierToken = new KaaAdminSizedTextBox(-1, false);
        verifierToken.setWidth(FULL_WIDTH);
        idx++;
        detailsTable.setWidget(idx, 0, verifierTokenLabel);
        detailsTable.setWidget(idx, 1, verifierToken);
        
        verifierTokenLabel.setVisible(!create);
        verifierToken.setVisible(!create);
        
        return idx;
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addUserVerifier();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.userVerifier();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.userVerifierDetails();
    }
    
    @Override
    protected void resetImpl() {
        super.resetImpl();
        verifierToken.setValue("");
    }

    @Override
    public HasValue<String> getVerifierToken() {
        return verifierToken;
    }

}

