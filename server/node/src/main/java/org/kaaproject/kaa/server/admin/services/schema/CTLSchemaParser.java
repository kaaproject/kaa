/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
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
 * @see #parse(String, String)
 * @see #validate(CTLSchemaDto)
 */
public class CTLSchemaParser {

    private final Schema.Parser parser = new Schema.Parser();

    private final ControlService controlService;
    private final String tenantId;

    public CTLSchemaParser(ControlService controlService, String tenantId) {
        this.controlService = controlService;
        this.tenantId = tenantId;
    }
    
    public CTLSchemaDto parse(String body, String applicationId) throws ControlServiceException, JsonParseException, JsonMappingException, IOException {
        CTLSchemaDto schema = new CTLSchemaDto();
        CTLSchemaMetaInfoDto metaInfo = new CTLSchemaMetaInfoDto();
        String fqn = null;
        
        ObjectNode object = new ObjectMapper().readValue(body, ObjectNode.class);

        if (!object.has("type") || !object.get("type").isTextual() || !object.get("type").getTextValue().equals("record")) {
            throw new IllegalArgumentException("The data provided is not a record!");
        }

        if (!object.has("namespace") || !object.get("namespace").isTextual()) {
            throw new IllegalArgumentException("No namespace specified!");
        } else if (!object.has("name") || !object.get("name").isTextual()) {
            throw new IllegalArgumentException("No name specified!");
        } else {
            fqn = object.get("namespace").getTextValue() + "." + object.get("name").getTextValue();
        }
        metaInfo = new CTLSchemaMetaInfoDto(fqn, tenantId, applicationId);
        schema.setMetaInfo(metaInfo);

        if (!object.has("version") || !object.get("version").isInt()) {
            object.put("version", 1);
        } 
        schema.setVersion(object.get("version").asInt());

        Set<CTLSchemaDto> dependencies = new HashSet<>();
        List<FqnVersion> missingDependencies = new ArrayList<>();
        if (!object.has("dependencies")) {
            schema.setDependencySet(dependencies);
        } else if (!object.get("dependencies").isArray()) {
            throw new IllegalArgumentException("Illegal dependencies format!");
        } else {
            for (JsonNode child : object.get("dependencies")) {
                if (!child.isObject() || !child.has("fqn") || !child.get("fqn").isTextual() || !child.has("version")
                        || !child.get("version").isInt()) {
                    throw new IllegalArgumentException("Illegal dependency format!");
                } else {
                    String dependencyFqn = child.get("fqn").asText();
                    int dependencyVersion = child.get("version").asInt();
                    
                    CTLSchemaDto dependency = controlService.getAnyCTLSchemaByFqnVersionTenantIdAndApplicationId(dependencyFqn, dependencyVersion, tenantId, applicationId);
                    if (dependency != null) {
                        dependencies.add(dependency);
                    } else {
                        missingDependencies.add(new FqnVersion(dependencyFqn, dependencyVersion));
                    }
                }
            }
            if (!missingDependencies.isEmpty()) {
                String message = "The following dependencies are missing from the database: " + Arrays.toString(missingDependencies.toArray());
                throw new IllegalArgumentException(message);
            }
            schema.setDependencySet(dependencies);
        }
        body = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        schema.setBody(body);
        return schema;
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
    public Schema validate(CTLSchemaDto schema) throws KaaAdminServiceException {
        if (schema.getDependencySet() != null) {
            for (CTLSchemaDto dependency : schema.getDependencySet()) {
                try {
                    CTLSchemaDto dependencySchema = controlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(dependency.getMetaInfo().getFqn(),
                            dependency.getVersion(), dependency.getMetaInfo().getTenantId(), dependency.getMetaInfo().getApplicationId());
                    if (dependencySchema == null) {
                        String message = "Unable to locate dependency \"" + dependency.getMetaInfo().getFqn() + "\" (version " + dependency.getVersion() + ")";
                        throw new IllegalArgumentException(message);
                    }
                    validate(dependencySchema);
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
            throw new IllegalArgumentException("Unable to parse CTL schema \"" + schema.getMetaInfo().getFqn() + "\" (version " + schema.getVersion() + "): " + cause.getMessage());
        }
    }
}