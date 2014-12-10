package org.kaaproject.kaa.server.common.dao.cassandra;

import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointUser;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserDao;

public class EndpointUserCassandraDao extends AbstractCassandraDao<CassandraEndpointUser> implements EndpointUserDao<CassandraEndpointUser> {

    @Override
    protected Class<?> getColumnFamilyClass() {
        return null;
    }

    @Override
    protected String getColumnFamilyName() {
        return null;
    }

    @Override
    public CassandraEndpointUser save(EndpointUserDto dto) {
        return null;
    }

    @Override
    public CassandraEndpointUser findByExternalIdAndTenantId(String externalId, String tenantId) {
        return null;
    }

    @Override
    public void removeByExternalIdAndTenantId(String externalId, String tenantId) {

    }

    @Override
    public String generateAccessToken(String externalUid, String tenantId) {
        return null;
    }

    @Override
    public boolean checkAccessToken(String externalUid, String tenantId, String accessToken) {
        return false;
    }
}
