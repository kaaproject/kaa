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

package org.kaaproject.kaa.server.appenders.cassandra.appender;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.server.operations.service.filter.el.GenericRecordPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class AppenderFilterEvaluator {

    public static final String LOG_BODY_VAR = "log";

    private static final Logger LOG = LoggerFactory.getLogger(AppenderFilterEvaluator.class);

    private StandardEvaluationContext context;

    public AppenderFilterEvaluator(GenericRecord logRecord) {
        context = new StandardEvaluationContext(logRecord);
        context.addPropertyAccessor(new GenericRecordPropertyAccessor());
        context.setVariable(LOG_BODY_VAR, logRecord);
    }

    public boolean matches(String filter) {
        Expression expression = new SpelExpressionParser().parseExpression(filter);
        try {
            return expression.getValue(context, Boolean.class);
        } catch (EvaluationException e) {
            LOG.warn("Failed to process filter {}: due to evaluate exception.", filter, e);
        } catch (Exception e) {
            LOG.error("Failed to process filter {}: due to exception", filter, e);
        }
        return false;
    }
}
