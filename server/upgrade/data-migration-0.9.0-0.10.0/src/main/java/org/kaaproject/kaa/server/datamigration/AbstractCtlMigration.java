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

import static java.util.stream.Collectors.joining;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.kaaproject.kaa.server.datamigration.model.Schema;
import org.kaaproject.kaa.server.datamigration.utils.BaseSchemaIdCounter;
import org.kaaproject.kaa.server.datamigration.utils.datadefinition.Constraint;
import org.kaaproject.kaa.server.datamigration.utils.datadefinition.DataDefinition;
import org.kaaproject.kaa.server.datamigration.utils.datadefinition.ReferenceOptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractCtlMigration {
  protected Connection connection;
  protected QueryRunner runner;
  protected DataDefinition dd;
  protected Long idShift;


  /**
   * Create entity that responsible for data migration from old tables to new ctl based ones.
   *
   * @param connection the connection to relational database
   */
  public AbstractCtlMigration(Connection connection) {
    this.connection = connection;
    runner = new QueryRunner();
    dd = new DataDefinition(connection);
  }


  /**
   * Do necessary operation to database before execution of {@link AbstractCtlMigration#transform()}
   * method.
   *
   * @throws SQLException the sql exception
   */
  public void beforeTransform() throws SQLException {
    // delete relation between <table_prefix>_schems to schems
    dd.dropUnnamedFk(getPrefixTableName() + "_schems", "schems");
  }


  /**
   * Do main part of data migration from old tables to new ctl based ones.
   *
   * @throws SQLException the sql exception
   */
  protected List<Schema> transform() throws SQLException {
    // fetch schemas of appropriate feature like configuration
    List<Schema> schemas = runner.query(connection, "select "
        + "f.id as id, created_time as createdTime, created_username as createdUsername, "
        + "description, name, schems, version, application_id as appId "
        + "from " + getPrefixTableName() + "_schems f join schems s on f.id = s.id",
        new BeanListHandler<>(Schema.class));

    // delete the fetched ids from schema table
    String toDelete = schemas.stream().map(s -> s.getId().toString()).collect(joining(", "));
    String notEmptyIdSet = "^[\\s]*([0-9]+(\\,\\s)?)+";
    if (toDelete.matches(notEmptyIdSet)) {
      runner.update(connection, "delete from schems where id in (" + toDelete + ")");
    }

    // shift ids in order to avoid PK constraint violation during adding record to base_schema
    Long shift = runner.query(connection, "select max(id) as max_id from " + getPrefixTableName()
        + "_schems",
        rs -> rs.next() ? rs.getLong("max_id") : null);
    idShift = BaseSchemaIdCounter.getInstance().getAndShift(shift);
    runner.update(connection, "update " + getPrefixTableName() + "_schems set id = id + "
        + idShift + " order by id desc");
    schemas.forEach(s -> s.setId(s.getId() + idShift));

    return schemas;
  }


  /**
   * Do necessary operation to database after execution of  {@link AbstractCtlMigration#transform()}
   * method.
   *
   * @throws SQLException the sql exception
   */
  public void afterTransform() throws SQLException {
    dd.alterTable(getPrefixTableName() + "_schems")
        .add(Constraint.constraint("FK_" + getPrefixTableName() + "_base_schems_id")
            .foreignKey("id")
            .references("base_schems", "id")
            .onDelete(ReferenceOptions.CASCADE)
            .onUpdate(ReferenceOptions.CASCADE)
        )
        .execute();
  }

  protected abstract String getPrefixTableName();

}
