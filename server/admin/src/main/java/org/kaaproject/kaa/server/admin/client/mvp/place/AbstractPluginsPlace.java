package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.web.bindery.event.shared.EventBus;

public abstract class AbstractPluginsPlace extends TreePlace {

    protected String applicationId;

    public AbstractPluginsPlace(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public static abstract class Tokenizer<P extends AbstractPluginsPlace> implements PlaceTokenizer<P>, PlaceConstants {

        @Override
        public P getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return getPlaceImpl(PlaceParams.getParam(APPLICATION_ID));
        }

        protected abstract P getPlaceImpl(String applicationId);

        @Override
        public String getToken(P place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractPluginsPlace other = (AbstractPluginsPlace) obj;
        if (applicationId == null) {
            if (other.applicationId != null)
                return false;
        } else if (!applicationId.equals(other.applicationId))
            return false;
        return true;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new ApplicationPlace(applicationId);
    }
    
    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        return null;
    }
}
