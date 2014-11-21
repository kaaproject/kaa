package org.kaaproject.kaa.server.appenders.mongo.appender;

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.mongo.config.MongoDbConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.CustomLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDbLogAppender extends CustomLogAppender<MongoDbConfig> {
    
    private static final Logger LOG = LoggerFactory.getLogger(MongoDbLogAppender.class);

    private LogEventDao logEventDao;
    private String collectionName;
    private boolean closed = false;
    
    public MongoDbLogAppender() {
        super(MongoDbConfig.class);
    }
    
    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            LOG.debug("[{}] appending {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
            List<LogEventDto> dtos = generateLogEvent(logEventPack, header);
            LOG.debug("[{}] saving {} objects", collectionName, dtos.size());
            if (!dtos.isEmpty()) {
                logEventDao.save(dtos, collectionName);
                LOG.debug("[{}] appended {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender,
            MongoDbConfig configuration) {
        LOG.debug("Initializing new instance of MongoDB log appender");
        try {
            logEventDao = new LogEventMongoDao(configuration);
            createCollection(appender.getApplicationToken());
        } catch (Exception e) {
            LOG.error("Failed to init MongoDB log appender: ", e);
        }
    }
    
    private void createCollection(String applicationToken) {
        if (collectionName == null) {
            collectionName = "logs_" + applicationToken;
            logEventDao.createCollection(collectionName);
        } else {
            LOG.error("Appender is already initialized..");
        }
    }
    
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (logEventDao != null) {
                logEventDao.close();
                logEventDao = null;
            }
        }
        LOG.debug("Stoped MongoDB log appender.");
    }

}
