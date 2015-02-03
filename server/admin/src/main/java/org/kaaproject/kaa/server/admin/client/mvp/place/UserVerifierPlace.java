package org.kaaproject.kaa.server.admin.client.mvp.place;

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.Prefix;

public class UserVerifierPlace extends AbstractPluginPlace {

    public UserVerifierPlace(String applicationId, String pluginId) {
        super(applicationId, pluginId);
    }

    @Override
    public String getName() {
        return Utils.constants.userVerifier();
    }

    @Prefix(value = "userVerifier")
    public static class Tokenizer extends AbstractPluginPlace.Tokenizer<UserVerifierPlace> {

        @Override
        protected UserVerifierPlace getPlaceImpl(String applicationId,
                String schemaId) {
            return new UserVerifierPlace(applicationId, schemaId);
        }
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new UserVerifiersPlace(applicationId);
    }
}
