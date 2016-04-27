/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractPropertiesPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePropertiesView;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;

public abstract class AbstractPropertiesActivity<P extends AbstractPropertiesPlace>
        extends
        AbstractDetailsActivity<PropertiesDto, BasePropertiesView, P> {

    public AbstractPropertiesActivity(P place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    protected String getEntityId(P place) {
        return "";
    }

    @Override
    protected PropertiesDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {
        detailsView.getConfiguration().setValue(entity.getConfiguration());
    }

    @Override
    protected void onSave() {
        entity.setConfiguration(detailsView.getConfiguration().getValue());
    }

}
