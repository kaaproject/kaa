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

import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_STATUS_PROPERTY;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.impl.CredentialsDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.Optional;

@Repository
public class CredentialsCassandraDao extends AbstractCassandraDao<CassandraCredentials, ByteBuffer> implements CredentialsDao<CassandraCredentials> {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsCassandraDao.class);

    @Override
    protected Class<CassandraCredentials> getColumnFamilyClass() {
        return CassandraCredentials.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CREDENTIALS_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraCredentials save(String applicationId, CredentialsDto credentials) {
        LOG.debug("Saving {}", credentials.toString());
        return save(new CassandraCredentials(applicationId, credentials));
    }

    @Override
    public Optional<CassandraCredentials> find(String applicationId, String credentialsId) {
        LOG.debug("Searching credential by applicationID[{}] and credentialsID[{}]", applicationId, credentialsId);
        Select.Where query = select().from(getColumnFamilyName()).
                where(eq(CREDENTIALS_APPLICATION_ID_PROPERTY, applicationId)).
                and(eq(CREDENTIALS_ID_PROPERTY, credentialsId));
        return Optional.ofNullable(this.findOneByStatement(query));
    }

    @Override
    public Optional<CassandraCredentials> updateStatus(String applicationId, String credentialsId, CredentialsStatus status) {
        LOG.debug("Updating credentials status with applicationID[{}] and credentialsID[{}] to STATUS[{}]",
                applicationId, credentialsId, status.toString());
        Update.Assignments query = update(getColumnFamilyName()).where(eq(CREDENTIALS_ID_PROPERTY, credentialsId)).
                and(eq(CREDENTIALS_APPLICATION_ID_PROPERTY, applicationId)).
                with(set(CREDENTIALS_STATUS_PROPERTY, status.toString()));
        execute(query);
        return find(applicationId, credentialsId);
    }

    @Override
    public void remove(String applicationId, String credentialsId) {
        LOG.debug("Deleting credential by applicationID[{}] and credentialsID[{}]", applicationId, credentialsId);
        Delete.Where query = delete().from(getColumnFamilyName()).
                where(eq(CREDENTIALS_ID_PROPERTY, credentialsId)).
                and(eq(CREDENTIALS_APPLICATION_ID_PROPERTY, applicationId));
        execute(query);
    }
}
