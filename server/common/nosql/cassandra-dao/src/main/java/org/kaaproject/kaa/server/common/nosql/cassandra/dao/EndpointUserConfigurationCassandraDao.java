package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUserConfiguration;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_APP_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_USER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_VERSION_PROPERTY;

@Repository
public class EndpointUserConfigurationCassandraDao extends AbstractCassandraDao<CassandraEndpointUserConfiguration, String> implements EndpointUserConfigurationDao<CassandraEndpointUserConfiguration> {

    @Override
    protected Class<CassandraEndpointUserConfiguration> getColumnFamilyClass() {
        return CassandraEndpointUserConfiguration.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_USER_CONF_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointUserConfiguration save(EndpointUserConfigurationDto dto) {
        return save(new CassandraEndpointUserConfiguration(dto));
    }

    @Override
    public CassandraEndpointUserConfiguration findByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        Select.Where select = select().from(getColumnFamilyName()).where(eq(EP_USER_CONF_USER_ID_PROPERTY, userId))
                .and(eq(EP_USER_CONF_APP_TOKEN_PROPERTY, appToken)).and(eq(EP_USER_CONF_VERSION_PROPERTY, schemaVersion));
        return findOneByStatement(select);
    }

    @Override
    public List<CassandraEndpointUserConfiguration> findByUserId(String userId) {
        Select.Where select = select().from(getColumnFamilyName()).where(eq(EP_USER_CONF_USER_ID_PROPERTY, userId));
        return findListByStatement(select);
    }

    @Override
    public void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        Delete.Where delete = delete().from(getColumnFamilyName()).where(eq(EP_USER_CONF_USER_ID_PROPERTY, userId))
                .and(eq(EP_USER_CONF_APP_TOKEN_PROPERTY, appToken)).and(eq(EP_USER_CONF_VERSION_PROPERTY, schemaVersion));
        execute(delete);
    }
}
