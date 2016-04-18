/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.appenders.mongo.appender;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.mongo.config.gen.MongoDBCredential;
import org.kaaproject.kaa.server.appenders.mongo.config.gen.MongoDbConfig;
import org.kaaproject.kaa.server.appenders.mongo.config.gen.MongoDbServer;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;
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
import com.mongodb.MongoCredential;
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

        List<MongoCredential> credentials = new ArrayList<>();
        if (configuration.getMongoCredentials() != null) {
            for (MongoDBCredential credential : configuration.getMongoCredentials()) {
                credentials.add(MongoCredential.createMongoCRCredential(credential.getUser(), configuration.getDbName(), 
                        credential.getPassword().toCharArray()));
            }
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
        
        MongoClientOptions options = optionsBuilder.build();
        mongoClient = new MongoClient(seeds, credentials, options);

        MongoDbFactory dbFactory = new SimpleMongoDbFactory(mongoClient, configuration.getDbName());

        MappingMongoConverter converter = new MappingMongoConverter(dbFactory, new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        mongoTemplate = new MongoTemplate(dbFactory, converter);
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
    }

    @Override
    public void createCollection(String collectionName) {
        try {
            if (!mongoTemplate.collectionExists(collectionName)) {
                mongoTemplate.createCollection(collectionName);
            }
        } catch (UncategorizedMongoDbException e) {
            LOG.warn("Failed to create collection {} due to", collectionName, e);
        }
    }

    @Override
    public List<LogEvent> save(List<LogEventDto> logEventDtos, ProfileInfo clientProfile, ProfileInfo serverProfile, String collectionName) {
        List<LogEvent> logEvents = new ArrayList<>(logEventDtos.size());
        for (LogEventDto logEventDto : logEventDtos) {
            logEvents.add(new LogEvent(logEventDto, clientProfile, serverProfile));
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
