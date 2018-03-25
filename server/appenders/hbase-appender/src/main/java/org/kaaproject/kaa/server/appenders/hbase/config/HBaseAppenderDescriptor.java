package org.kaaproject.kaa.server.appenders.hbase.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.HBaseAppenderConfiguration;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
/**
 * Sample descriptor for {@link org.HBaseLogAppender
 * .sample.appenders.hbase.appender.HBaseLogAppender} appender.
 *
 */

@KaaPluginConfig(pluginType = PluginType.LOG_APPENDER)
public class HBaseAppenderDescriptor implements PluginConfig {
  public HBaseAppenderDescriptor() {
  }
  
  /**
   * Name of the appender will be used in Administration UI.
   */
  
  @Override
  public String getPluginTypeName() {
    return "HBase";
  }
  /**
   * Returns name of the appender class.
   */
  
  @Override
  public String getPluginClassName() {
    return "org.kaaproject.kaa.server.appenders.hbase.appender.HBaseLogAppender";
  }           
  
  /**
   * Returns avro schema of the appender configuration.
   */
  
  @Override
  public Schema getPluginConfigSchema() {
    return HBaseAppenderConfiguration.getClassSchema();
  }
}
