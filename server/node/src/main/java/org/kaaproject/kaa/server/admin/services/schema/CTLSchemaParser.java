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
package org.kaaproject.kaa.server.admin.services.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;

/**
 * This class is used to parse and validate CTL schemas on save.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 *
 * @see #parse(String, CTLSchemaScopeDto, String)
 * @see #validate(CTLSchemaInfoDto)
 */
public class CTLSchemaParser {

    private final Schema.Parser parser = new Schema.Parser();

    private final ControlService controlService;
    private final String tenantId;

    public CTLSchemaParser(ControlService controlService, String tenantId) {
        this.controlService = controlService;
        this.tenantId = tenantId;
    }
    
    public CTLSchemaInfoDto parse(String body, CTLSchemaScopeDto scope, String applicationId) throws JsonParseException, JsonMappingException, IOException {
        CTLSchemaInfoDto schema = new CTLSchemaInfoDto();

        schema.setTenantId(tenantId);
        schema.setApplicationId(applicationId);
        schema.setScope(detectScope(scope, applicationId));
        
        ObjectNode object = new ObjectMapper().readValue(body, ObjectNode.class);

        if (!object.has("type") || !object.get("type").isTextual() || !object.get("type").getTextValue().equals("record")) {
            throw new IllegalArgumentException("The data provided is not a record!");
        }

        if (!object.has("namespace") || !object.get("namespace").isTextual()) {
            throw new IllegalArgumentException("No namespace specified!");
        } else if (!object.has("name") || !object.get("name").isTextual()) {
            throw new IllegalArgumentException("No name specified!");
        } else {
            schema.setFqn(object.get("namespace").getTextValue() + "." + object.get("name").getTextValue());
        }

        if (!object.has("version") || !object.get("version").isInt()) {
            object.put("version", 1);
        } 
        schema.setVersion(object.get("version").asInt());

        Set<CTLSchemaMetaInfoDto> dependencies = new HashSet<>();
        if (!object.has("dependencies")) {
            schema.setDependencies(dependencies);
        } else if (!object.get("dependencies").isArray()) {
            throw new IllegalArgumentException("Illegal dependencies format!");
        } else {
            for (JsonNode child : object.get("dependencies")) {
                if (!child.isObject() || !child.has("fqn") || !child.get("fqn").isTextual() || !child.has("version")
                        || !child.get("version").isInt()) {
                    throw new IllegalArgumentException("Illegal dependency format!");
                } else {
                    dependencies.add(new CTLSchemaMetaInfoDto(child.get("fqn").asText(), child.get("version").asInt()));
                }
                schema.setDependencies(dependencies);
            }
        }
        body = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        schema.setBody(body);
        return schema;
    }
    
    private CTLSchemaScopeDto detectScope(CTLSchemaScopeDto scope, String applicationId) {
        if (scope != null) {
            if (scope.getLevel() >= CTLSchemaScopeDto.APPLICATION.getLevel() && applicationId == null) {
                throw new IllegalArgumentException("Missing application identifier for provided scope " + scope.name() + "!");
            } else if (scope.getLevel() < CTLSchemaScopeDto.APPLICATION.getLevel() && applicationId != null) {
                throw new IllegalArgumentException("Application identifier can't be specified for provided scope " + scope.name() + "!");
            }
            if (scope == CTLSchemaScopeDto.SYSTEM && tenantId != null) {
                throw new IllegalArgumentException("You do not have permission to perform this operation!");
            }
            return scope;
        } else {
            if (tenantId != null && applicationId != null) {
                return CTLSchemaScopeDto.APPLICATION;
            } else if (tenantId != null && applicationId == null) {
                return CTLSchemaScopeDto.TENANT;
            } else if (tenantId == null && applicationId == null) {
                return CTLSchemaScopeDto.SYSTEM;
            } else {
                /*
                 * The Kaa administrator is trying to save an application CTL
                 * schema.
                 */
                throw new IllegalArgumentException("You do not have permission to perform this operation!");
            }
        }
    }

    public Set<CTLSchemaDto> fetchDependencies(CTLSchemaInfoDto schema) throws ControlServiceException {
        // Check if the schema dependencies are present in the database
        List<CTLSchemaMetaInfoDto> missingDependencies = new ArrayList<>();
        Set<CTLSchemaDto> dependencies = new HashSet<>();
        if (schema.getDependencies() != null) {
            for (CTLSchemaMetaInfoDto dependency : schema.getDependencies()) {
                CTLSchemaDto schemaFound = controlService.getCTLSchemaByFqnVersionAndTenantId(dependency.getFqn(), dependency.getVersion(),
                        schema.getTenantId());
                if (schemaFound == null) {
                    missingDependencies.add(dependency);
                } else {
                    dependencies.add(schemaFound);
                }
            }
        }
        if (!missingDependencies.isEmpty()) {
            String message = "The following dependencies are missing from the database: " + Arrays.toString(missingDependencies.toArray());
            throw new IllegalArgumentException(message);
        }
        return dependencies;
    }

    /**
     * Parses the given CTL schema along with its dependencies as an
     * {@link org.apache.avro.Schema Avro schema}.
     *
     * @param schema
     *            A CTL schema to parse
     *
     * @return A parsed CTL schema as an Avro schema
     *
     * @throws KaaAdminServiceException
     *             - if the given CTL schema is invalid and thus cannot be
     *             parsed.
     */
    public Schema validate(CTLSchemaInfoDto schema) throws KaaAdminServiceException {
        if (schema.getDependencies() != null) {
            for (CTLSchemaMetaInfoDto dependency : schema.getDependencies()) {
                try {
                    CTLSchemaDto dependencySchema = controlService.getCTLSchemaByFqnVersionAndTenantId(dependency.getFqn(),
                            dependency.getVersion(), tenantId);
                    if (dependencySchema == null) {
                        String message = "Unable to locate dependency \"" + dependency.getFqn() + "\" (version " + dependency.getVersion() + ")";
                        throw new IllegalArgumentException(message);
                    }
                    validate(dependencySchema.toCTLSchemaInfoDto());
                } catch (Exception cause) {
                    throw Utils.handleException(cause);
                }
            }
        }

        try {
            /*
             * Parsed schemas are automatically added to the set of types known
             * to the parser.
             */
            return parser.parse(schema.getBody());
        } catch (Exception cause) {
            throw new IllegalArgumentException("Unable to parse CTL schema \"" + schema.getFqn() + "\" (version " + schema.getVersion() + "): " + cause.getMessage());
        }
    }
}