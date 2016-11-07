/**
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointRegistrationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEpRegistrationByEndpointIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointRegistration;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEpRegistrationByEndpointId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Repository("endpointRegistrationDao")
public class EndpointRegistrationCassandraDao
    extends AbstractCassandraDao<CassandraEndpointRegistration, String>
    implements EndpointRegistrationDao<CassandraEndpointRegistration> {

  private static final Logger LOG =
      LoggerFactory.getLogger(EndpointRegistrationCassandraDao.class);

  @Autowired
  private CassandraEpRegistrationByEndpointIdDao byEndpointId;

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
    return this.save(new CassandraEndpointRegistration(endpointRegistration));
  }

  @Override
  public CassandraEndpointRegistration save(CassandraEndpointRegistration object) {
    if (isBlank(object.getId())) {
      object.generateId();
    }
    object = super.save(object);
    List<Statement> statements = new ArrayList<>();
    statements.add(this.getSaveQuery(object));
    if (object.getEndpointId() != null) {
      statements.add(
          this.byEndpointId.getSaveQuery(
              CassandraEpRegistrationByEndpointId.fromEndpointRegistration(object)));
    }
    this.executeBatch(statements.toArray(new Statement[statements.size()]));
    return object;
  }

  @Override
  public Optional<CassandraEndpointRegistration> findByEndpointId(String endpointId) {
    LOG.debug("Searching for endpoint registration by endpoint ID [{}]", endpointId);
    Optional<String> credentialsId = this.byEndpointId.getCredentialsIdByEndpointId(
        endpointId);
    if (credentialsId.isPresent()) {
      LOG.debug("[{}] Endpoint credentials ID by endpoint ID: {}",
          endpointId, credentialsId.get());
      Clause clause = QueryBuilder.eq(
          CassandraModelConstants.EP_REGISTRATION_CREDENTIALS_ID_PROPERTY,
          credentialsId.get());
      Statement statement = QueryBuilder.select().from(this.getColumnFamilyName())
          .where(clause);
      return Optional.ofNullable(this.findOneByStatement(statement));
    } else {
      LOG.debug("[{}] No credentials ID found by endpoint ID: {}", endpointId);
      return Optional.empty();
    }
  }

  @Override
  public Optional<CassandraEndpointRegistration> findByCredentialsId(String credentialsId) {
    LOG.debug("Searching for endpoint registration by credentials ID [{}]", credentialsId);
    Clause clause = QueryBuilder.eq(
        CassandraModelConstants.EP_REGISTRATION_CREDENTIALS_ID_PROPERTY, credentialsId);
    Statement statement = QueryBuilder.select().from(this.getColumnFamilyName())
        .where(clause);
    return Optional.ofNullable(this.findOneByStatement(statement));
  }

  @Override
  public void removeByEndpointId(String endpointId) {
    LOG.debug("Removing endpoint registration by endpoint ID", endpointId);
    Optional<String> credentialsId = this.byEndpointId.getCredentialsIdByEndpointId(
        endpointId);
    if (credentialsId.isPresent()) {
      Clause clause = QueryBuilder.eq(
          CassandraModelConstants.EP_REGISTRATION_BY_ENDPOINT_ID_ENDPOINT_ID_PROPERTY,
          endpointId);
      Statement statement = QueryBuilder.delete()
          .from(this.byEndpointId.getColumnFamilyName())
          .where(clause);
      this.execute(statement);
      clause = QueryBuilder.eq(
          CassandraModelConstants.EP_REGISTRATION_CREDENTIALS_ID_PROPERTY,
          credentialsId.get());
      statement = QueryBuilder.delete().from(this.getColumnFamilyName()).where(clause);
      this.execute(statement);
    } else {
      LOG.debug("[{}] No credentials ID found by endpoint ID: {}", endpointId);
    }
  }
}
