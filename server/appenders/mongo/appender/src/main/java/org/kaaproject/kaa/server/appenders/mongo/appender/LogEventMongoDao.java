package org.kaaproject.kaa.server.appenders.mongo.appender;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.mongo.config.MongoDbConfig;
import org.kaaproject.kaa.server.appenders.mongo.config.MongoDbServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

public class LogEventMongoDao implements LogEventDao {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventMongoDao.class);
    
    private MongoClient mongoClient;
    private MongoTemplate mongoTemplate;
    
    @SuppressWarnings("deprecation")
    public LogEventMongoDao(MongoDbConfig configuration) throws Exception {
        
        List<ServerAddress> seeds = new ArrayList<>(configuration.getMongoServers().size());
        for (MongoDbServer server : configuration.getMongoServers()) {
            seeds.add(new ServerAddress(server.getHost(), server.getPort()));
        }
        
        MongoClientOptions.Builder optionsBuilder = new MongoClientOptions.Builder();
        if (configuration.getConnectionsPerHost() != null) {
            optionsBuilder.connectionsPerHost(configuration.getConnectionsPerHost());
        }
        if (configuration.getMaxWaitTime() != null) {
            optionsBuilder.maxWaitTime(configuration.getMaxWaitTime());
        }
        if (configuration.getConnectionTimeout() != null) {
            optionsBuilder.connectTimeout(configuration.getConnectionTimeout());
        }
        if (configuration.getSocketTimeout() != null) {
            optionsBuilder.socketTimeout(configuration.getSocketTimeout());
        }
        if (configuration.getSocketKeepalive() != null) {
            optionsBuilder.socketKeepAlive(configuration.getSocketKeepalive());
        }
        if (configuration.getAutoConnectRetry() != null) {
            optionsBuilder.autoConnectRetry(configuration.getAutoConnectRetry());
        }
        
        MongoClientOptions options = optionsBuilder.build();
        mongoClient = new MongoClient(seeds, options);
        
        MongoDbFactory dbFactory = new SimpleMongoDbFactory(mongoClient, configuration.getDbName());
        
        MappingMongoConverter converter = new MappingMongoConverter(dbFactory, new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        mongoTemplate = new MongoTemplate(dbFactory, converter);
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
    }
    
    @Override
    public void createCollection(String collectionName) {
        try{
            if(!mongoTemplate.collectionExists(collectionName)){
                mongoTemplate.createCollection(collectionName);
            }
        }catch(UncategorizedMongoDbException e){
            LOG.warn("Failed to create collection {} due to", collectionName, e.getMessage());
        }
    }
    
    @Override
    public List<LogEvent> save(List<LogEventDto> logEventDtos, String collectionName) {
        List<LogEvent> logEvents = new ArrayList<>(logEventDtos.size());
        for (LogEventDto logEventDto : logEventDtos) {
            logEvents.add(new LogEvent(logEventDto));
        }
        LOG.debug("Saving {} log events", logEvents.size());
        mongoTemplate.insert(logEvents, collectionName);
        return logEvents;
    }
    
    @Override
    public void removeAll(String collectionName) {
        LOG.debug("Remove all documents from [{}] collection.", collectionName);
        mongoTemplate.dropCollection(collectionName);
    }
    
    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
    
}
