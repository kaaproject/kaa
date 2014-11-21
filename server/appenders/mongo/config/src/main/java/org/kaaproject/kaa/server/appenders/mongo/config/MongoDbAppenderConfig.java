package org.kaaproject.kaa.server.appenders.mongo.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.log.shared.annotation.KaaAppenderConfig;
import org.kaaproject.kaa.server.common.log.shared.config.AppenderConfig;

@KaaAppenderConfig
public class MongoDbAppenderConfig implements AppenderConfig {
    
    public MongoDbAppenderConfig() {
    }

    @Override
    public String getName() {
        return "Mongo";
    }

    @Override
    public String getLogAppenderClass() {
        return "org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender";
    }

    @Override
    public Schema getConfigSchema() {
        return MongoDbConfig.getClassSchema();
    }

}
