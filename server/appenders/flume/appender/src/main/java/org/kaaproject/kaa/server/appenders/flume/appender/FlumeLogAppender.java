package org.kaaproject.kaa.server.appenders.flume.appender;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.flume.appender.client.FlumeClientManager;
import org.kaaproject.kaa.server.appenders.flume.config.FlumeConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.CustomLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlumeLogAppender extends CustomLogAppender<FlumeConfig> {
    
    private static final Logger LOG = LoggerFactory.getLogger(FlumeLogAppender.class);

    private boolean closed = false;
    
    private FlumeEventBuilder flumeEventBuilder;
    private FlumeClientManager<?> flumeClientManger;
    
    public FlumeLogAppender() {
        super(FlumeConfig.class);
    }
    
    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            Event event = flumeEventBuilder.generateEvent(logEventPack, header, getApplicationToken());
            try {
                if (flumeClientManger != null) {
                    flumeClientManger.sendEventToFlume(event);
                } else {
                    LOG.warn("Flume client wasn't initialized. Invoke method init before.");
                }
            } catch (EventDeliveryException e) {
                LOG.warn("Can't send flume event.");
            }            
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender,
            FlumeConfig configuration) {
        LOG.debug("Initializing new instance of Flume log appender");
        try {
            flumeEventBuilder = new FlumeAvroEventBuilder();
            flumeClientManger = FlumeClientManager.getInstance(configuration);
        } catch (Exception e) {
            LOG.error("Failed to init Flume log appender: ", e);
        }
    }
    
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            flumeClientManger.cleanUp();
        }
        LOG.debug("Stoped Flume log appender.");
    }

}
