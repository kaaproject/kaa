/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointCredentialsDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEpCredsByAppIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointCredentials;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEpCredsByAppId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Repository("endpointCredentialsDao")
public class EndpointCredentialsCassandraDao extends AbstractCassandraDao<CassandraEndpointCredentials, String> implements
        EndpointCredentialsDao<CassandraEndpointCredentials> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointCredentialsCassandraDao.class);

    @Autowired
    private CassandraEpCredsByAppIdDao byApplicationID;

    @Override
    protected Class<CassandraEndpointCredentials> getColumnFamilyClass() {
        return CassandraEndpointCredentials.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_CREDS_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointCredentials save(EndpointCredentialsDto endpointCredentials) {
        LOG.debug("Saving {}", endpointCredentials.toString());
        if (StringUtils.isBlank(endpointCredentials.getId())) {
            String endpointId = endpointCredentials.getEndpointId();
            endpointCredentials.setId(endpointId);
        }
        return this.save(new CassandraEndpointCredentials(endpointCredentials));
    }

    @Override
    public CassandraEndpointCredentials save(CassandraEndpointCredentials endpointCredentials) {
        endpointCredentials = super.save(endpointCredentials);
        List<Statement> statements = new ArrayList<>();
        statements.add(this.getSaveQuery(endpointCredentials));
        statements.add(this.byApplicationID.getSaveQuery(this.prepareByApplicationIDTableEntry(endpointCredentials)));
        this.executeBatch(statements.toArray(new Statement[statements.size()]));
        return endpointCredentials;
    }

    @Override
    public List<CassandraEndpointCredentials> findByApplicationId(String applicationId) {
        LOG.debug("Searching for endpoint credentials by application ID [{}]", applicationId);
        List<CassandraEndpointCredentials> result = null;
        Clause clause = QueryBuilder.eq(CassandraModelConstants.EP_CREDS_APPLICATION_ID_PROPERTY, applicationId);
        Statement statement = QueryBuilder.select().from(this.getColumnFamilyName()).where(clause);
        List<String> endpointIDs = this.byApplicationID.getEndpointIDs(applicationId);
        if (endpointIDs != null) {
            clause = QueryBuilder.in(CassandraModelConstants.EP_CREDS_ENDPOINT_ID_PROPERTY, endpointIDs);
            statement = QueryBuilder.select().from(this.getColumnFamilyName()).where(clause);
            result = this.findListByStatement(statement);
        }
        return result;
    }

    @Override
    public CassandraEndpointCredentials findByEndpointId(String endpointId) {
        LOG.debug("Searching for endpoint credentials by endpoint ID [{}]", endpointId);
        Clause clause = QueryBuilder.eq(CassandraModelConstants.EP_CREDS_ENDPOINT_ID_PROPERTY, endpointId);
        Statement statement = QueryBuilder.select().from(this.getColumnFamilyName()).where(clause);
        return this.findOneByStatement(statement);
    }

    @Override
    public void removeByEndpointId(String endpointId) {
        LOG.debug("Removing endpoint credentials by endpoint ID [{}]", endpointId);
        Clause clause = QueryBuilder.eq(CassandraModelConstants.EP_CREDS_ENDPOINT_ID_PROPERTY, endpointId);
        Statement statement = QueryBuilder.delete().from(this.getColumnFamilyName()).where(clause);
        this.execute(statement);
    }

    private CassandraEpCredsByAppId prepareByApplicationIDTableEntry(CassandraEndpointCredentials endpointCredentials) {
        String applicationId = endpointCredentials.getApplicationId();
        String endpointId = endpointCredentials.getEndpointId();
        return new CassandraEpCredsByAppId(applicationId, endpointId);
    }
}
