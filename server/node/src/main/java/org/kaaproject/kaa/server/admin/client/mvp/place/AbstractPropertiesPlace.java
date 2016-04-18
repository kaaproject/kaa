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

import com.google.gwt.place.shared.PlaceTokenizer;

public abstract class AbstractPropertiesPlace extends TreePlace {

    public AbstractPropertiesPlace() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof AbstractPropertiesPlace);
    }

    public static abstract class Tokenizer<P extends AbstractPropertiesPlace> implements PlaceTokenizer<P>, PlaceConstants {

        @Override
        public String getToken(P place) {
            PlaceParams.clear();
            return PlaceParams.generateToken();
        }
    }
 
    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return null;
    }

}
