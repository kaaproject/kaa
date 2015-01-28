package org.kaaproject.kaa.server.appenders.cassandra.appender;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraExecuteRequestType;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CassandraLogAppender extends AbstractLogAppender<CassandraConfig> {

    private static final String LOG_TABLE_PREFIX = "logs_";
    private static final Logger LOG = LoggerFactory.getLogger(CassandraLogAppender.class);

    private LogEventDao logEventDao;
    private String tableName;
    private boolean closed = false;
    private CassandraExecuteRequestType executeRequestType;

    public CassandraLogAppender() {
        super(CassandraConfig.class);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            LOG.debug("[{}] appending {} logs to cassandra collection", tableName, logEventPack.getEvents().size());
            List<LogEventDto> dtoList = generateLogEvent(logEventPack, header);
            LOG.debug("[{}] saving {} objects", tableName, dtoList.size());
            if (!dtoList.isEmpty()) {
                switch (executeRequestType) {
                    case ASYNC:
                        logEventDao.saveAsync(dtoList, tableName);
                        break;
                    case SYNC:
                        logEventDao.save(dtoList, tableName);
                        break;
                }
                LOG.debug("[{}] appended {} logs to cassandra collection", tableName, logEventPack.getEvents().size());
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, CassandraConfig configuration) {
        LOG.info("Initializing new instance of Cassandra log appender");
        try {
            checkExecuteRequestType(configuration);
            logEventDao = new CassandraLogEventDao(configuration);
            createTable(appender.getApplicationToken());
        } catch (Exception e) {
            LOG.error("Failed to init cassandra log appender: ", e);
        }
    }

    private void createTable(String applicationToken) {
        if (tableName == null) {
            tableName = LOG_TABLE_PREFIX + applicationToken;
            logEventDao.createTable(tableName);
        } else {
            LOG.error("Appender is already initialized..");
        }
    }

    @Override
    public void close() {
        LOG.info("Try to stop cassandra log appender...");
        if (!closed) {
            closed = true;
            if (logEventDao != null) {
                logEventDao.close();
                logEventDao = null;
            }
        }
        LOG.info("Cassandra log appender stoped.");
    }

    private void checkExecuteRequestType(CassandraConfig configuration) {
        CassandraExecuteRequestType requestType = configuration.getCassandraExecuteRequestType();
        if (requestType != null && CassandraExecuteRequestType.ASYNC.equals(requestType)) {
            executeRequestType = CassandraExecuteRequestType.ASYNC;
        } else {
            executeRequestType = CassandraExecuteRequestType.SYNC;
        }
    }
}
