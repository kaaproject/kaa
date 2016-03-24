package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.impl.CredentialsDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoCredentials;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoDaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;

import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants;

@Repository
public class CredentialsMongoDao extends AbstractVersionableMongoDao<MongoCredentials, ByteBuffer> implements CredentialsDao<MongoCredentials> {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsMongoDao.class);

    @Override
    protected String getCollectionName() {
        return MongoModelConstants.CREDENTIALS;
    }

    @Override
    protected Class<MongoCredentials> getDocumentClass() {
        return MongoCredentials.class;
    }

    @Override
    public MongoCredentials save(CredentialsDto credentialsDto) {
        LOG.debug("Saving{}", credentialsDto.toString());
        return this.save(new MongoCredentials(credentialsDto));
    }

    @Override
    public MongoCredentials findById(String id) {
        LOG.debug("Searching credential by ID[{}]", id);
        Query query = Query.query(Criteria.where(MongoModelConstants.CREDENTIALS_ID).is(id));
        return this.findOne(query);
    }

    @Override
    public MongoCredentials update(String id, CredentialsStatus status) {
        LOG.debug("Updating credentials status with ID[{}] to STATUS[{}]", id, status.toString());
        updateFirst(
                query(where(MongoModelConstants.CREDENTIALS_ID).is(id)),
                Update.update(MongoModelConstants.CREDENTIAL_STATUS, status));
        return findById(id);
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Deleting credential by ID[{}]", id);
        Query query = Query.query(Criteria.where(MongoModelConstants.CREDENTIALS_ID).is(id));
        this.remove(query);
    }
}
