package org.kaaproject.kaa.server.common.dao.cassandra;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;

import java.util.List;

public class EndpointProfileCassandraDao extends AbstractCassandraDao<CassandraEndpointProfile> implements EndpointProfileDao<CassandraEndpointProfile> {

    @Override
    protected Class<CassandraEndpointProfile> getColumnFamilyClass() {
        return CassandraEndpointProfile.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.ENDPOINT_PROFILE_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointProfile save(EndpointProfileDto dto) {
        return null;
    }

    @Override
    public CassandraEndpointProfile findByKeyHash(byte[] endpointKeyHash) {
        return null;
    }

    @Override
    public long getCountByKeyHash(byte[] endpointKeyHash) {
        return 0;
    }

    @Override
    public void removeByKeyHash(byte[] endpointKeyHash) {

    }

    @Override
    public void removeByAppId(String appId) {

    }

    @Override
    public CassandraEndpointProfile findByAccessToken(String endpointAccessToken) {
        return null;
    }

    @Override
    public List<CassandraEndpointProfile> findByEndpointUserId(String endpointUserId) {
        return null;
    }
}
