package org.kaaproject.kaa.server.appenders.cassandra.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;

@KaaPluginConfig(pluginType = PluginType.LOG_APPENDER)
public class CassandraAppenderConfig implements PluginConfig {

    @Override
    public String getPluginTypeName() {
        return "Cassandra";
    }

    @Override
    public String getPluginClassName() {
        return "org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender";
    }

    @Override
    public Schema getPluginConfigSchema() {
        return CassandraConfig.getClassSchema();
    }
}
