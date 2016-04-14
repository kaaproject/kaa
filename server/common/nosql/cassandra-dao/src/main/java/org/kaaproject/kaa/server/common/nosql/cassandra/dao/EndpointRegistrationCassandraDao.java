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
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointRegistrationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPRegistrationByCredentialsIDDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPRegistrationByCredentialsID;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointRegistration;
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
@Repository("endpointRegistrationDao")
public class EndpointRegistrationCassandraDao extends AbstractCassandraDao<CassandraEndpointRegistration, String> implements
        EndpointRegistrationDao<CassandraEndpointRegistration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointRegistrationCassandraDao.class);

    @Autowired
    private CassandraEPRegistrationByCredentialsIDDao byCredentialsID;

    @Override
    protected Class<CassandraEndpointRegistration> getColumnFamilyClass() {
        return CassandraEndpointRegistration.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_REGISTRATION_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointRegistration save(EndpointRegistrationDto endpointRegistration) {
        LOG.debug("Saving [{}]", endpointRegistration.toString());
        if (StringUtils.isBlank(endpointRegistration.getId())) {
            endpointRegistration.setId(endpointRegistration.getEndpointId());
        }
        return this.save(new CassandraEndpointRegistration(endpointRegistration));
    }

    @Override
    public CassandraEndpointRegistration save(CassandraEndpointRegistration endpointRegistration) {
        endpointRegistration = super.save(endpointRegistration);
        List<Statement> statements = new ArrayList<>();
        statements.add(this.getSaveQuery(endpointRegistration));
        if (endpointRegistration.getCredentialsId() != null) {
            statements.add(this.byCredentialsID.getSaveQuery(CassandraEPRegistrationByCredentialsID.fromEndpointRegistration(endpointRegistration)));
        }
        this.executeBatch(statements.toArray(new Statement[statements.size()]));
        return endpointRegistration;
    }

    @Override
    public Optional<CassandraEndpointRegistration> findByEndpointId(String endpointId) {
        LOG.debug("Searching for endpoint registration by endpoint ID [{}]", endpointId);
        Clause clause = QueryBuilder.eq(CassandraModelConstants.EP_REGISTRATION_ENDPOINT_ID_PROPERTY, endpointId);
        Statement statement = QueryBuilder.select().from(this.getColumnFamilyName()).where(clause);
        return Optional.ofNullable(this.findOneByStatement(statement));
    }

    @Override
    public Optional<CassandraEndpointRegistration> findByCredentialsId(String credentialsId) {
        LOG.debug("Searching for endpoint registration by credentials ID [{}]", credentialsId);
        String endpointId = this.byCredentialsID.getEndpointIdByCredentialsId(credentialsId);
        Clause clause = QueryBuilder.eq(CassandraModelConstants.EP_REGISTRATION_CREDENTIALS_ID_PROPERTY, endpointId);
        Statement statement = QueryBuilder.select().from(this.getColumnFamilyName()).where(clause);
        return Optional.ofNullable(this.findOneByStatement(statement));
    }

    @Override
    public void removeByEndpointId(String endpointId) {
        LOG.debug("Removing endpoint registration by endpoint ID", endpointId);
        Clause clause = QueryBuilder.eq(CassandraModelConstants.EP_REGISTRATION_ENDPOINT_ID_PROPERTY, endpointId);
        Statement statement = QueryBuilder.delete().from(this.getColumnFamilyName()).where(clause);
        this.execute(statement);
    }
}
