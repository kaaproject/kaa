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

package org.kaaproject.kaa.server.operations.service.filter;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.operations.service.filter.el.GenericRecordPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * The Class DefaultFilter.
 */
public class DefaultFilterEvaluator implements FilterEvaluator {

    public static final String EP_KEYHASH_VARIABLE_NAME = "ekh";
    public static final String SERVER_PROFILE_VARIABLE_NAME = "sp";
    public static final String CLIENT_PROFILE_VARIABLE_NAME = "cp";

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFilterEvaluator.class);

    private String epKey;
    private GenericRecord serverProfileGenericRecord;
    private GenericRecord clientProfileGenericRecord;

    /**
     * Instantiates a new default filter.
     *
     */
    public DefaultFilterEvaluator() {
        super();
    }

    @Override
    public void init(EndpointProfileDto profile, String profileSchemaBody, String serverProfileSchemaBody) {
        GenericAvroConverter<GenericRecord> endpointProfileConverter = new GenericAvroConverter<>(
                new Schema.Parser().parse(profileSchemaBody));
        GenericAvroConverter<GenericRecord> serverProfileConverter = new GenericAvroConverter<>(
                new Schema.Parser().parse(serverProfileSchemaBody));
        this.epKey = Base64Util.encode(profile.getEndpointKeyHash());
        try {
            if (profile.getServerProfileBody() != null) {
                serverProfileGenericRecord = serverProfileConverter.decodeJson(profile.getServerProfileBody());
            }
            if (profile.getClientProfileBody() != null) {
                clientProfileGenericRecord = endpointProfileConverter.decodeJson(profile.getClientProfileBody());
            }
        } catch (IOException ioe) {
            LOG.error("Error decoding avro object from Json string", ioe);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.filter.Filter#matches(java
     * .lang.String)
     */
    @Override
    public boolean matches(ProfileFilterDto filter) {
        Expression expression = new SpelExpressionParser().parseExpression(filter.getBody());
        StandardEvaluationContext evaluationContext;
        if (filter.getEndpointProfileSchemaVersion() != null) {
            evaluationContext = new StandardEvaluationContext(clientProfileGenericRecord);
            evaluationContext.setVariable(CLIENT_PROFILE_VARIABLE_NAME, clientProfileGenericRecord);
        } else {
            evaluationContext = new StandardEvaluationContext();
        }
        evaluationContext.addPropertyAccessor(new GenericRecordPropertyAccessor());
        evaluationContext.setVariable(EP_KEYHASH_VARIABLE_NAME, epKey);
        if (filter.getServerProfileSchemaVersion() != null) {
            evaluationContext.setVariable(SERVER_PROFILE_VARIABLE_NAME, serverProfileGenericRecord);
        }

        return expression.getValue(evaluationContext, Boolean.class);
    }

}
