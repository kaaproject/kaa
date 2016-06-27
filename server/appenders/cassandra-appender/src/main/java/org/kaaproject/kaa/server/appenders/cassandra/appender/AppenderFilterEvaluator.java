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
