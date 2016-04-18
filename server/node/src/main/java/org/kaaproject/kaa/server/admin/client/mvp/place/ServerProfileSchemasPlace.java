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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.Prefix;
import com.google.web.bindery.event.shared.EventBus;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class ServerProfileSchemasPlace extends SchemasPlace {

    public ServerProfileSchemasPlace(String applicationId) {
        super(applicationId);
    }

    @Prefix(value = "serverProfSchemas")
    public static class Tokenizer extends SchemasPlace.Tokenizer<ServerProfileSchemasPlace> {

        @Override
        protected ServerProfileSchemasPlace getPlaceImpl(String applicationId) {
            return new ServerProfileSchemasPlace(applicationId);
        }
    }

    @Override
    public String getName() {
        return Utils.constants.serverProfile();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ServerProfileSchemasPlace that = (ServerProfileSchemasPlace) o;

        return !(applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        return result;
    }
}
