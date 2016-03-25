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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPRegistrationByCredentialsID;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Repository
public class CassandraEPRegistrationByCredentialsIDDao extends AbstractCassandraDao<CassandraEPRegistrationByCredentialsID, String> {

    @Override
    protected Class<CassandraEPRegistrationByCredentialsID> getColumnFamilyClass() {
        return CassandraEPRegistrationByCredentialsID.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_REGISTRATIONS_BY_CREDENTIALS_ID_COLUMN_FAMILY_NAME;
    }

    public String getEndpointIdByCredentialsId(String credentialsId) {
        Clause clause = QueryBuilder.eq(CassandraModelConstants.EP_REGISTRATION_BY_CREDENTIALS_ID_CREDENTIALS_ID_PROPERTY, credentialsId);
        Statement statement = QueryBuilder.select().from(this.getColumnFamilyName()).where(clause);
        return this.findOneByStatement(statement).getEndpointId();
    }
}
