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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.kaaproject.kaa.server.datamigration.model.EventClass;
import org.kaaproject.kaa.server.datamigration.model.EventSchemaVersion;
import org.kaaproject.kaa.server.datamigration.model.Schema;
import org.kaaproject.kaa.server.datamigration.utils.BaseSchemaIdCounter;
import org.kaaproject.kaa.server.datamigration.utils.datadefinition.Constraint;
import org.kaaproject.kaa.server.datamigration.utils.datadefinition.ReferenceOptions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CtlEventsMigration extends AbstractCtlMigration {
  private static final String EVENT_SCHEMA_VERSION_TABLE_NAME = "event_schems_versions";
  private static final String EVENT_CLASS_FAMILY_TABLE_NAME = "events_class_family";
  private static final String EVENT_CLASS_FAMILY_VERSION_TABLE_NAME =
      "events_class_family_versions";
  private static final String EVENT_CLASS_TABLE_NAME = "events_class";
  private static final String BASE_SCHEMA_TABLE_NAME = "base_schems";
  private static final String APPLICATION_EVENT_MAP_TABLE_NAME = "application_event_map";

  public CtlEventsMigration(Connection connection) {
    super(connection);
  }

  //actually not needed here
  @Override
  protected String getPrefixTableName() {
    return null;
  }

  @Override
  public void beforeTransform() throws SQLException {
    dd.dropUnnamedFk(EVENT_CLASS_TABLE_NAME, EVENT_CLASS_FAMILY_TABLE_NAME);
    dd.dropUnnamedFk(APPLICATION_EVENT_MAP_TABLE_NAME, EVENT_CLASS_TABLE_NAME);
    runner.update(
        connection, "ALTER TABLE "
            + BASE_SCHEMA_TABLE_NAME
            + " CHANGE application_id application_id bigint(20)");
  }

  @Override
  public void afterTransform() throws SQLException {
    dd.alterTable(EVENT_CLASS_TABLE_NAME)
        .add(Constraint.constraint("FK_events_class_family_versions_id")
            .foreignKey("events_class_family_versions_id")
            .references("events_class_family_versions", "id")
            .onDelete(ReferenceOptions.CASCADE)
            .onUpdate(ReferenceOptions.CASCADE))
        .execute();

    dd.alterTable(APPLICATION_EVENT_MAP_TABLE_NAME)
        .add(Constraint.constraint("FK_events_class_id")
            .foreignKey("events_class_id")
            .references("events_class", "id")
            .onDelete(ReferenceOptions.CASCADE))
        .execute();
  }


  @Override
  protected List<Schema> transform() throws SQLException {
    // fetch schemas of appropriate feature like configuration
    List<Schema> schemas = new ArrayList<>();

    final List<EventSchemaVersion> oldEsvs = runner.query(connection,
        "SELECT id, schems, created_time, created_username FROM "
            + EVENT_SCHEMA_VERSION_TABLE_NAME,
        new BeanListHandler<EventSchemaVersion>(EventSchemaVersion.class));
    final List<EventClass> oldECs = runner.query(connection,
        "SELECT id, schems, version FROM " + EVENT_CLASS_TABLE_NAME
        + " WHERE schems not like '{\"type\":\"enum\"%'",
        new BeanListHandler<>(EventClass.class));

    runner.update(
        connection, "ALTER TABLE " + EVENT_SCHEMA_VERSION_TABLE_NAME + " DROP COLUMN schems");

    runner.update(
        connection, "ALTER TABLE " + EVENT_SCHEMA_VERSION_TABLE_NAME + " RENAME "
            + EVENT_CLASS_FAMILY_VERSION_TABLE_NAME);

    runner.update(
        connection, "ALTER TABLE " + EVENT_CLASS_TABLE_NAME
            + " CHANGE events_class_family_id events_class_family_versions_id bigint(20)");

    runner.update(connection, "ALTER TABLE " + EVENT_CLASS_TABLE_NAME + " DROP COLUMN schems");
    runner.update(connection, "ALTER TABLE " + EVENT_CLASS_TABLE_NAME + " DROP COLUMN version");

    for (EventClass ec : oldECs) {
      updateFamilyVersionId(ec, oldEsvs, runner);
    }

    for (EventClass ec : oldECs) {
      EventSchemaVersion esv = findParent(ec, oldEsvs);

      Long id = ec.getId();
      Long createdTime = esv.getCreatedTime();
      String createUsername = esv.getCreatedUsername();
      String description = null;
      String name = parseName(ec.getSchems());
      String schems = ec.getSchems();
      Integer version = ec.getVersion();
      Long applicationId = null;
      String type = null; //fixme: what is type?

      Schema schema = new Schema(
          id, version, name, description, createUsername,
          createdTime, applicationId, schems, type);
      schemas.add(schema);
    }

    // shift ids in order to avoid PK constraint violation during adding record to base_schema
    Long shift = runner.query(
        connection, "select max(id) as max_id from " + EVENT_CLASS_TABLE_NAME,
        rs -> rs.next() ? rs.getLong("max_id") : null);
    Long idShift = BaseSchemaIdCounter.getInstance().getAndShift(shift);
    runner.update(
        connection,
        "update " + EVENT_CLASS_TABLE_NAME + " set id = id + " + idShift + " order by id desc");

    runner.update(
        connection,
        "update " + APPLICATION_EVENT_MAP_TABLE_NAME + " set events_class_id = events_class_id + "
            + idShift + " order by id desc");

    schemas.forEach(s -> s.setId(s.getId() + idShift));
    return schemas;
  }


  private EventSchemaVersion findParent(EventClass ec, List<EventSchemaVersion> versions) {
    for (EventSchemaVersion esv : versions) {
      if (ecBelongToThisFamilyVersion(ec, esv)) {
        return esv;
      }
    }
    return null;
  }

  private void updateFamilyVersionId(EventClass ec,
                                     List<EventSchemaVersion> versions,
                                     QueryRunner runner) throws SQLException {
    for (EventSchemaVersion esv : versions) {
      if (ecBelongToThisFamilyVersion(ec, esv)) {
        int updateCount = runner.update(this.connection,
            "UPDATE " + EVENT_CLASS_TABLE_NAME
                + " SET events_class_family_versions_id=? WHERE id=?",
            esv.getId(), ec.getId());
        if (updateCount != 1) {
          System.err.println("Error: failed to update event class's reference to ECFV: " + ec);
        }

        break;
      }
    }
  }

  private boolean ecBelongToThisFamilyVersion(EventClass ec, EventSchemaVersion esv) {
    try {
      JsonNode jsonNode = new ObjectMapper().readTree(ec.getSchems());
      String namespace = jsonNode.get("namespace").asText();
      String name = jsonNode.get("name").asText();

      return esv.getSchemas().contains(name) && esv.getSchemas().contains(namespace);
    } catch (IOException ex) {
      System.err.println("Failed to read EventClass schema: " + ec);
    }
    return false;
  }

  private String parseName(String body) {
    try {
      JsonNode jsonNode = new ObjectMapper().readTree(body);
      return jsonNode.get("name").asText();
    } catch (IOException ex) {
      System.err.println("Failed to parse name from schema: " + body);
    }
    return "";
  }
}
