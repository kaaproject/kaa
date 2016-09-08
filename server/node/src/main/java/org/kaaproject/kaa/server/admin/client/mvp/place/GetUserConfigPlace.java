package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.Prefix;
import com.google.web.bindery.event.shared.EventBus;
import org.kaaproject.kaa.server.admin.client.util.Utils;

/**
 * Created by pyshankov on 07.09.16.
 */
public class GetUserConfigPlace extends EndpointUsersPlace {

    public GetUserConfigPlace(String applicationId) {
        super(applicationId);
    }

    @Prefix(value = "getUserConfig")
    public static class Tokenizer extends EndpointUsersPlace.Tokenizer<GetUserConfigPlace> {

        @Override
        protected GetUserConfigPlace getPlaceImpl(String applicationId) {
            return new GetUserConfigPlace(applicationId);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GetUserConfigPlace other = (GetUserConfigPlace) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return Utils.constants.getConfiguration();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        return null;
    }

}
