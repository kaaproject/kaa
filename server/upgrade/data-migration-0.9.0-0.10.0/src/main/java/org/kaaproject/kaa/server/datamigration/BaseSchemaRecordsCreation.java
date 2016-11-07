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

import org.apache.commons.dbutils.QueryRunner;
import org.kaaproject.kaa.server.datamigration.model.Ctl;
import org.kaaproject.kaa.server.datamigration.model.Schema;
import org.kaaproject.kaa.server.datamigration.utils.datadefinition.DataDefinition;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseSchemaRecordsCreation {
  protected Connection connection;
  protected QueryRunner runner;
  protected DataDefinition dd;

  /**
   * Create new instance of BaseSchemaRecordsCreation.
   *
   * @param connection the connection to relational database
   */
  public BaseSchemaRecordsCreation(Connection connection) {
    this.connection = connection;
    runner = new QueryRunner();
    dd = new DataDefinition(connection);
  }

  /**
   * Final phase of migration -- add created ctl based schemas to database.
   *
   * @param ctlToSchemas mapping of common type to a couple of schemas
   * @throws SQLException the sql exception
   */
  public void create(Map<Ctl, List<Schema>> ctlToSchemas) throws SQLException {
    List<Object[]> params = new ArrayList<>();
    int schemaCounter = 0;
    for (Ctl ctl : ctlToSchemas.keySet()) {
      for (Schema schema : ctlToSchemas.get(ctl)) {
        schemaCounter++;
        params.add(new Object[]{
            schema.getId(),
            schema.getCreatedTime(),
            schema.getCreatedUsername(),
            schema.getDescription(),
            schema.getName(),
            schema.getVersion(),
            ctl.getMetaInfo().getAppId(),
            ctl.getId()
        });
      }
    }

    runner.batch(connection, "insert into base_schems values(?, ?, ?, ?, ?, ?, ?, ?)",
        params.toArray(new Object[schemaCounter][]));
  }

}
