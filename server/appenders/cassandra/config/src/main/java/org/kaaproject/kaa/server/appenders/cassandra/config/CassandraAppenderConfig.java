package org.kaaproject.kaa.server.appenders.cassandra.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.common.log.shared.annotation.KaaAppenderConfig;
import org.kaaproject.kaa.server.common.log.shared.config.AppenderConfig;

@KaaAppenderConfig
public class CassandraAppenderConfig implements AppenderConfig {

    @Override
    public String getName() {
        return "Cassandra";
    }

    @Override
    public String getLogAppenderClass() {
        return "org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender";
    }

    @Override
    public Schema getConfigSchema() {
        return CassandraConfig.getClassSchema();
    }
}
