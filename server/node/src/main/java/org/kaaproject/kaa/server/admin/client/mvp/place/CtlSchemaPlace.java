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

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class CtlSchemaPlace extends TreePlace {

    private String metaInfoId;
    private Integer version;
    private CTLSchemaScopeDto scope;
    private String applicationId;
    private SchemaType schemaType;
    private boolean editable;
    private boolean create;

    public CtlSchemaPlace(String metaInfoId, 
            Integer version, 
            CTLSchemaScopeDto scope, 
            String applicationId, 
            boolean editable, 
            boolean create) {
        this.metaInfoId = metaInfoId;
        this.version = version;
        this.scope = scope;
        this.applicationId = applicationId;
        this.editable = editable;
        this.create = create;
    }

    public String getMetaInfoId() {
        return metaInfoId;
    }

    public Integer getVersion() {
        return version;
    }
    
    public CTLSchemaScopeDto getScope() {
		return scope;
	}
    
	public String getApplicationId() {
        return applicationId;
    }
	
    public boolean isEditable() {
        return editable;
    }

    public boolean isCreate() {
        return create;
    }
    
    public SchemaType getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(SchemaType schemaType) {
        this.schemaType = schemaType;
    }

    @Prefix(value = "ctlSchema")
    public static class Tokenizer implements PlaceTokenizer<CtlSchemaPlace>, PlaceConstants {

        @Override
        public CtlSchemaPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            String metaInfoId = PlaceParams.getParam(META_INFO_ID);
            Integer version = null;
            if (PlaceParams.hasParam(VERSION)) {
                version = PlaceParams.getIntParam(VERSION);
            }
            CTLSchemaScopeDto scope = null;
            if (PlaceParams.hasParam(SCOPE)) {
            	scope = CTLSchemaScopeDto.valueOf(PlaceParams.getParam(SCOPE));
            }
            SchemaType schemaType = null;
            if (PlaceParams.hasParam(SCHEMA_TYPE)) {
                schemaType = SchemaType.valueOf(PlaceParams.getParam(SCHEMA_TYPE));
            }
            String applicationId = PlaceParams.getParam(APPLICATION_ID);
            boolean editable = PlaceParams.getBooleanParam(EDITABLE);
            boolean create = PlaceParams.getBooleanParam(CREATE);
            CtlSchemaPlace place = new CtlSchemaPlace(metaInfoId, version, scope, applicationId, editable, create);
            place.setSchemaType(schemaType);
            return place;
        }

        @Override
        public String getToken(CtlSchemaPlace place) {
            PlaceParams.clear();
            if (Utils.isNotBlank(place.getMetaInfoId())) {
                PlaceParams.putParam(META_INFO_ID, place.getMetaInfoId());
            }
            if (place.getVersion() != null) {
                PlaceParams.putIntParam(VERSION, place.getVersion());
            }
            if (place.getScope() != null) {
            	PlaceParams.putParam(SCOPE, place.getScope().name());
            }
            if (place.getSchemaType() != null) {
                PlaceParams.putParam(SCHEMA_TYPE, place.getSchemaType().name());
            }
            if (Utils.isNotBlank(place.getApplicationId())) {
                PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            }
            PlaceParams.putBooleanParam(EDITABLE, place.isEditable());
            PlaceParams.putBooleanParam(CREATE, place.isCreate());
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
        CtlSchemaPlace other = (CtlSchemaPlace) obj;
        if (applicationId == null) {
            if (other.applicationId != null)
                return false;
        } else if (!applicationId.equals(other.applicationId))
            return false;
        if (create != other.create)
            return false;
        if (editable != other.editable)
            return false;
        if (metaInfoId == null) {
            if (other.metaInfoId != null)
                return false;
        } else if (!metaInfoId.equals(other.metaInfoId))
            return false;
        if (scope != other.scope)
            return false;
        if (schemaType != other.schemaType)
            return false;        
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
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
        if (Utils.isNotBlank(applicationId)) {
            if (schemaType != null) { 
                if (schemaType == SchemaType.ENDPOINT_PROFILE) {
                    return new ProfileSchemasPlace(applicationId);
                } else if (schemaType == SchemaType.CONFIGURATION) {
                    return new ConfigurationSchemasPlace(applicationId);
                } else if (schemaType == SchemaType.SERVER_PROFILE){
                    return new ServerProfileSchemasPlace(applicationId);
                } else if (schemaType == SchemaType.NOTIFICATION) {
                    return new NotificationSchemasPlace(applicationId);
                } else if (schemaType == SchemaType.LOG_SCHEMA) {
                    return new LogSchemasPlace(applicationId);
                }
            } else {    
                return new ApplicationCtlSchemasPlace(applicationId);
            }            
        } else {
            if (KaaAdmin.getAuthInfo().getAuthority() == KaaAuthorityDto.KAA_ADMIN) {
                return new SystemCtlSchemasPlace();
            } else {
                return new TenantCtlSchemasPlace();
            }
        }
        return null;
    }
    
    public enum SchemaType {

        CONFIGURATION,
        ENDPOINT_PROFILE,
        SERVER_PROFILE,
        NOTIFICATION,
        LOG_SCHEMA
        
    }

}
