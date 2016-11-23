/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.datamigration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.kaaproject.kaa.server.datamigration.utils.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointSpecificConfigurationMigration {

  private static final Logger LOG = LoggerFactory.getLogger(EndpointSpecificConfigurationMigration.class);

  private Cluster cluster;
  private String dbName;
  private String nosql;
  private Session cassandraSession;

  /**
   *  Creates new  EndpointSpecificConfigurationMigration instance.
   */
  public EndpointSpecificConfigurationMigration(String host, String db, String nosql) {
    cluster = Cluster.builder()
        .addContactPoint(host)
        .build();
    dbName = db;
    this.nosql = nosql;
  }

  /**
   *  Runs transformations.
   */
  public void transform() {
    if (!Options.DEFAULT_NO_SQL.equalsIgnoreCase(nosql)) {
      try {
        cassandraSession = cluster.connect(dbName);
        addEndpointSpecificConfigurationTable();
        alterEndpointProfileTable();
      } finally {
        cassandraSession.close();
        cluster.close();
      }
    }
  }

  private void alterEndpointProfileTable() {
    try {
      cassandraSession.execute("ALTER TABLE ep_profile ADD eps_cf_hash blob;");
    } catch (InvalidQueryException ex) {
      LOG.warn("Failed to alter ep_profile table: {}", ex.getMessage());
    }
  }

  private void addEndpointSpecificConfigurationTable() {
    cassandraSession.execute("CREATE TABLE IF NOT EXISTS ep_specific_conf (\n"
        +        "    ep_key_hash blob,\n"
        +        "    cf_ver int,\n"
        +        "    body text,\n"
        +        "    opt_lock bigint,\n"
        +        "    PRIMARY KEY((ep_key_hash), cf_ver)\n"
        +        ") WITH CLUSTERING ORDER BY (cf_ver DESC);");
  }
}
