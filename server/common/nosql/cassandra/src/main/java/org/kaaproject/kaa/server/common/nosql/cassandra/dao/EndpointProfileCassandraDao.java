package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPByAccessTokenDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPByAppIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPByUserIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByAccessToken;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByAppId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUser;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertKeyHashToString;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertStringToKeyHash;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;

@Repository
public class EndpointProfileCassandraDao extends AbstractCassandraDao<CassandraEndpointProfile, ByteBuffer> implements EndpointProfileDao<CassandraEndpointProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointProfileCassandraDao.class);

    @Autowired
    private CassandraEPByAppIdDao cassandraEPByAppIdDao;
    @Autowired
    private CassandraEPByAccessTokenDao cassandraEPByAccessTokenDao;
    @Autowired
    private CassandraEPByUserIdDao cassandraEPByUserIdDao;

    private EndpointUserCassandraDao endpointUserDao;

    @Override
    protected Class<CassandraEndpointProfile> getColumnFamilyClass() {
        return CassandraEndpointProfile.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointProfile save(EndpointProfileDto dto) {
        return save(new CassandraEndpointProfile(dto));
    }

    @Override
    public CassandraEndpointProfile save(CassandraEndpointProfile profile) {
        LOG.debug("Saving endpoint profile...");
        if (profile.getId() == null) {
            profile.setId(convertKeyHashToString(profile.getEndpointKeyHash()));
        }
        ByteBuffer epKeyHash = profile.getEndpointKeyHash();
        Statement saveByAppId = cassandraEPByAppIdDao.getSaveQuery(new CassandraEPByAppId(profile.getApplicationId(), epKeyHash));
        String accessToken = profile.getAccessToken();
        Statement saveByAccessToken = null;
        if (accessToken != null) {
            saveByAccessToken = cassandraEPByAccessTokenDao.getSaveQuery(new CassandraEPByAccessToken(accessToken, epKeyHash));
        }
        Statement saveProfile = getSaveQuery(profile);
        if (saveByAccessToken != null) {
            executeBatch(BatchStatement.Type.UNLOGGED, saveProfile, saveByAppId, saveByAccessToken);
        } else {
            executeBatch(BatchStatement.Type.UNLOGGED, saveProfile, saveByAppId);
        }
        LOG.debug("Endpoint profile saved");
        return profile;
    }

    @Override
    public CassandraEndpointProfile findByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Try to find endpoint profile by key hash [{}]", endpointKeyHash);
        CassandraEndpointProfile endpointProfile = (CassandraEndpointProfile) getMapper().get(getByteBuffer(endpointKeyHash));
        LOG.debug("{} endpoint profile by key hash [{}]", endpointKeyHash, endpointProfile != null ? "Found" : "No found");
        LOG.trace("Found endpoint profile {} by key hash [{}]", endpointKeyHash, endpointProfile);
        return endpointProfile;
    }

    @Override
    public long getCountByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Try to check if endpoint profile exists with key hash [{}]", endpointKeyHash);
        long count = 0;
        ResultSet resultSet = execute(select().countAll().from(getColumnFamilyName())
                .where(eq(CassandraModelConstants.EP_EP_KEY_HASH_PROPERTY, getByteBuffer(endpointKeyHash))));
        Row row = resultSet.one();
        if (row != null) {
            count = row.getLong(0);
        }
        LOG.debug("{} endpoint profile exists with key hash [{}]", count);
        return count;
    }

    @Override
    public void removeByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Remove endpoint profile by key hash [{}]", endpointKeyHash);
        getMapper().delete(getByteBuffer(endpointKeyHash));
    }

    @Override
    public void removeByAppId(String appId) {
        LOG.debug("Remove endpoint profile by application id [{}]", appId);
        Statement deleteEps = delete().from(getColumnFamilyName()).where(in(CassandraModelConstants.EP_EP_KEY_HASH_PROPERTY, cassandraEPByAppIdDao.getEPIdsListByAppId(appId)));
        Statement deleteEpsByAppId = delete().from(CassandraModelConstants.EP_BY_APP_ID_COLUMN_FAMILY_NAME).where(eq(CassandraModelConstants.EP_BY_APP_ID_APPLICATION_ID_PROPERTY, appId));
        executeBatch(BatchStatement.Type.UNLOGGED, deleteEps, deleteEpsByAppId);
        LOG.trace("Execute statements {}, {} like batch", deleteEps, deleteEpsByAppId);
    }

    @Override
    public CassandraEndpointProfile findByAccessToken(String endpointAccessToken) {
        CassandraEndpointProfile endpointProfile = null;
        ByteBuffer epKeyHash = cassandraEPByAccessTokenDao.findEPIdByAccessToken(endpointAccessToken);
        if (epKeyHash != null) {
            endpointProfile = (CassandraEndpointProfile) getMapper().get(epKeyHash);
        }
        return endpointProfile;
    }

    @Override
    public List<CassandraEndpointProfile> findByEndpointUserId(String endpointUserId) {
        List<CassandraEndpointProfile> profileList = Collections.emptyList();
        CassandraEndpointUser endpointUser = endpointUserDao.findById(endpointUserId);
        if (endpointUser != null) {
            List<String> ids = endpointUser.getEndpointIds();
            if (ids != null && !ids.isEmpty()) {
                Statement select = select().from(getColumnFamilyName()).where(QueryBuilder.in(CassandraModelConstants.EP_EP_KEY_HASH_PROPERTY, convertStringIds(ids)));
                profileList = findListByStatement(select);
            }
        }
        return profileList;
    }

    @Override
    public CassandraEndpointProfile findById(ByteBuffer key) {
        CassandraEndpointProfile profile = null;
        if (key != null) {
            profile = findByKeyHash(key.array());
        }
        return profile;
    }

    @Override
    public void removeById(ByteBuffer key) {
        if (key != null) {
            removeByKeyHash(key.array());
        }
    }

    private ByteBuffer[] convertStringIds(List<String> ids) {
        ByteBuffer[] keyHashArray = new ByteBuffer[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            keyHashArray[i] = convertStringToKeyHash(ids.get(i));
        }
        return keyHashArray;
    }

    public EndpointUserCassandraDao getEndpointUserDao() {
        return endpointUserDao;
    }

    public void setEndpointUserDao(EndpointUserCassandraDao endpointUserDao) {
        this.endpointUserDao = endpointUserDao;
    }
}
