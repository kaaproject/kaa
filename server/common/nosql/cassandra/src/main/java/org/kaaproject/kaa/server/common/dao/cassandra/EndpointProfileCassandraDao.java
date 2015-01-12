package org.kaaproject.kaa.server.common.dao.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.cassandra.filter.CassandraEPByAccessTokenDao;
import org.kaaproject.kaa.server.common.dao.cassandra.filter.CassandraEPByAppIdDao;
import org.kaaproject.kaa.server.common.dao.cassandra.filter.CassandraEPByUserIdDao;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEPByAccessToken;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEPByAppId;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
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
import static org.kaaproject.kaa.server.common.dao.cassandra.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_APP_ID_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_APP_ID_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_EP_KEY_HASH_PROPERTY;

@Repository("endpointProfileDao")
public class EndpointProfileCassandraDao extends AbstractCassandraDao<CassandraEndpointProfile, ByteBuffer> implements EndpointProfileDao<CassandraEndpointProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointProfileCassandraDao.class);

    @Autowired
    private CassandraEPByAppIdDao cassandraEPByAppIdDao;
    @Autowired
    private CassandraEPByAccessTokenDao cassandraEPByAccessTokenDao;
    @Autowired
    private CassandraEPByUserIdDao cassandraEPByUserIdDao;

    @Override
    protected Class<CassandraEndpointProfile> getColumnFamilyClass() {
        return CassandraEndpointProfile.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointProfile save(EndpointProfileDto dto) {
        return save(new CassandraEndpointProfile(dto));
    }

    @Override
    public CassandraEndpointProfile save(CassandraEndpointProfile profile) {
        LOG.debug("Saving endpoint profile...");
        profile.setId(getStringId());
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
        LOG.debug("Try to find endpoint profile by key hash {}", endpointKeyHash);
        return (CassandraEndpointProfile) getMapper().get(getByteBuffer(endpointKeyHash));
    }

    @Override
    public long getCountByKeyHash(byte[] endpointKeyHash) {
        long count = 0;
        ResultSet resultSet = execute(select().countAll().from(getColumnFamilyName())
                .where(eq(EP_EP_KEY_HASH_PROPERTY, getByteBuffer(endpointKeyHash))));
        Row row = resultSet.one();
        if (row != null) {
            count = row.getLong(0);
        }
        return count;
    }

    @Override
    public void removeByKeyHash(byte[] endpointKeyHash) {
        getMapper().delete(getByteBuffer(endpointKeyHash));
    }

    @Override
    public void removeByAppId(String appId) {
        Statement deleteEps = delete().from(getColumnFamilyName()).where(in(EP_EP_KEY_HASH_PROPERTY, cassandraEPByAppIdDao.getEPIdsListByAppId(appId)));
        Statement deleteEpsByAppId = delete().from(EP_BY_APP_ID_COLUMN_FAMILY_NAME).where(eq(EP_BY_APP_ID_APPLICATION_ID_PROPERTY, appId));
        executeBatch(BatchStatement.Type.UNLOGGED, deleteEps, deleteEpsByAppId);
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
        List<ByteBuffer> keyHashList = cassandraEPByUserIdDao.findEPKeyHashListByUserId(endpointUserId);
        if (keyHashList != null) {
            Statement select = select().from(getColumnFamilyName()).where(in(EP_EP_KEY_HASH_PROPERTY, keyHashList));
            profileList = findListByStatement(select);
        }
        return profileList;
    }
}
