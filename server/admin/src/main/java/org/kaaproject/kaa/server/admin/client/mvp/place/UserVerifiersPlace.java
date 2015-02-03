package org.kaaproject.kaa.server.admin.client.mvp.place;

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.Prefix;

public class UserVerifiersPlace extends AbstractPluginsPlace {

    public UserVerifiersPlace(String applicationId) {
        super(applicationId);
    }

    @Override
    public String getName() {
        return Utils.constants.userVerifiers();
    }

    @Prefix(value = "userVerifiers")
    public static class Tokenizer extends AbstractPluginsPlace.Tokenizer<UserVerifiersPlace> {

        @Override
        protected UserVerifiersPlace getPlaceImpl(String applicationId) {
            return new UserVerifiersPlace(applicationId);
        }
    }

}
