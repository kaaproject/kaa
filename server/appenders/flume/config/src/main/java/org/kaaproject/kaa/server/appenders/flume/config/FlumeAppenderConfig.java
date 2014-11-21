package org.kaaproject.kaa.server.appenders.flume.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.log.shared.annotation.KaaAppenderConfig;
import org.kaaproject.kaa.server.common.log.shared.config.AppenderConfig;

@KaaAppenderConfig
public class FlumeAppenderConfig implements AppenderConfig {

    @Override
    public String getName() {
        return "Flume";
    }

    @Override
    public String getLogAppenderClass() {
        return "org.kaaproject.kaa.server.appenders.flume.appender.FlumeLogAppender";
    }

    @Override
    public Schema getConfigSchema() {
        return FlumeConfig.getClassSchema();
    }

}
