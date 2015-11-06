/*
 * Copyright 2014 CyberVision, Inc.
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
import org.kaaproject.kaa.server.operations.service.filter.el.GenericRecordPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * The Class DefaultFilter.
 */
public class DefaultFilter implements Filter {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultFilter.class);

    /** The schema body. */
    private String schemaBody;
    
    /** The avro converter. */
    private GenericAvroConverter<GenericRecord> avroConverter;
    
    /** The expression. */
    private Expression expression;

    /**
     * Instantiates a new default filter.
     *
     * @param filterBody the filter body
     * @param schemaBody the schema body
     */
    public DefaultFilter(String filterBody, String schemaBody) {
        this.schemaBody = schemaBody;

        this.expression = new SpelExpressionParser().parseExpression(filterBody);

        this.avroConverter = new GenericAvroConverter<GenericRecord>(
                new Schema.Parser().parse(this.schemaBody));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.filter.Filter#updateFilterBody(java.lang.String)
     */
    @Override
    public void updateFilterBody(String filterBody) {
        this.expression = new SpelExpressionParser().parseExpression(filterBody);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.filter.Filter#matches(java.lang.String)
     */
    @Override
    public boolean matches(String profileBody) {
        GenericRecord profileGenericRecord = null;
        try {
            profileGenericRecord = avroConverter.decodeJson(profileBody);
        } catch (IOException ioe) {
            LOG.error("Error decoding avro object from Json string", ioe);
            return false;
        }

        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(
                profileGenericRecord);
        evaluationContext.addPropertyAccessor(new GenericRecordPropertyAccessor());

        return expression.getValue(evaluationContext, Boolean.class);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder()
                .append(getClass().getName())
                .append(" [filterBody=\"").append(expression.getExpressionString()).append("\",")
                .append(" schemaBody=\"").append(schemaBody).append("\"]");
        return stringBuilder.toString();
    }
}