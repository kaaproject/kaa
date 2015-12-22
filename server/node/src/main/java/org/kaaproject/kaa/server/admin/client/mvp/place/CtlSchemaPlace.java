/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class CtlSchemaPlace extends TreePlace {

    private String fqn;
    private Integer version;
    private String sourceFqn;
    private Integer sourceVersion;

    public CtlSchemaPlace(String fqn, Integer version, boolean create) {
        if (create) {
            this.sourceFqn = fqn;
            this.sourceVersion = version;
        } else {
            this.fqn = fqn;
            this.version = version;
        }
    }

    public String getFqn() {
        return fqn;
    }
    
    public Integer getVersion() {
        return version;
    }

    public String getSourceFqn() {
        return sourceFqn;
    }

    public Integer getSourceVersion() {
        return sourceVersion;
    }

    @Prefix(value = "ctlSchema")
    public static class Tokenizer implements PlaceTokenizer<CtlSchemaPlace>, PlaceConstants {

        @Override
        public CtlSchemaPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            String sourceFqn = PlaceParams.getParam(SOURCE_FQN);
            int sourceVersion = PlaceParams.getIntParam(SOURCE_VERSION);
            String fqn = PlaceParams.getParam(FQN);
            Integer version = null;
            if (PlaceParams.hasParam(VERSION)) {
                version = PlaceParams.getIntParam(VERSION);
            }
            if (sourceFqn != null && sourceVersion > 0) {
                return new CtlSchemaPlace(sourceFqn, sourceVersion, true);
            } else {
                return new CtlSchemaPlace(fqn, version, false);
            }
        }

        @Override
        public String getToken(CtlSchemaPlace place) {
            PlaceParams.clear();
            if (Utils.isNotBlank(place.getSourceFqn())) {
                PlaceParams.putParam(SOURCE_FQN, place.getSourceFqn());
            }
            if (place.getSourceVersion() != null) {
                PlaceParams.putIntParam(SOURCE_VERSION, place.getSourceVersion());
            }
            if (Utils.isNotBlank(place.getFqn())) {
                PlaceParams.putParam(FQN, place.getFqn());
            }
            if (place.getVersion() != null) {
                PlaceParams.putIntParam(VERSION, place.getVersion());
            }
            return PlaceParams.generateToken();
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
        CtlSchemaPlace other = (CtlSchemaPlace) obj;
        if (fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!fqn.equals(other.fqn)) {
            return false;
        }
        if (sourceFqn == null) {
            if (other.sourceFqn != null) {
                return false;
            }
        } else if (!sourceFqn.equals(other.sourceFqn)) {
            return false;
        }
        if (sourceVersion == null) {
            if (other.sourceVersion != null) {
                return false;
            }
        } else if (!sourceVersion.equals(other.sourceVersion)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public String getName() {
        return Utils.constants.commonType();
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new TenantCtlSchemasPlace();
    }


}
