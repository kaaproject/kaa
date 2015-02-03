package org.kaaproject.kaa.server.admin.client.mvp.view;

import com.google.gwt.user.client.ui.HasValue;

public interface UserVerifierView extends BasePluginView {

    HasValue<String> getVerifierToken();

}

