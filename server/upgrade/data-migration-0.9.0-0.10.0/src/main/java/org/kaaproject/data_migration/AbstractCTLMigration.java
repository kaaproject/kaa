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

package org.kaaproject.data_migration;

import com.mongodb.MongoClient;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.kaaproject.data_migration.model.Schema;
import org.kaaproject.data_migration.utils.BaseSchemaIdCounter;
import org.kaaproject.data_migration.utils.datadefinition.DataDefinition;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.kaaproject.data_migration.utils.datadefinition.Constraint.constraint;
import static org.kaaproject.data_migration.utils.datadefinition.ReferenceOptions.CASCADE;

public abstract class AbstractCTLMigration {
    protected Connection connection;
    protected QueryRunner runner;
    protected DataDefinition dd;
    protected Long idShift;

    public AbstractCTLMigration(Connection connection) {
        this.connection = connection;
        runner = new QueryRunner();
        dd = new DataDefinition(connection);
    }

    public void beforeTransform() throws SQLException {
        // delete relation between <table_prefix>_schems to schems
        dd.dropUnnamedFK(getPrefixTableName() + "_schems", "schems");
    }


    protected List<Schema> transform() throws SQLException {
        // fetch schemas of appropriate feature like configuration
        List<Schema> schemas = runner.query(connection, "select " +
                "f.id as id, created_time as createdTime, created_username as createdUsername, " +
                "description, name, schems, version, application_id as appId " +
                "from " + getPrefixTableName() + "_schems f join schems s on f.id = s.id", new BeanListHandler<>(Schema.class));

        // delete the fetched ids from schema table
        String toDelete = schemas.stream().map(s -> s.getId().toString()).collect(joining(", "));
        runner.update(connection, "delete from schems where id in (" + toDelete + ")");

        // shift ids in order to avoid PK constraint violation during adding record to base_schema
        Long shift = runner.query(connection, "select max(id) as max_id from "+ getPrefixTableName() + "_schems", rs -> rs.next() ? rs.getLong("max_id") : null);
        idShift = BaseSchemaIdCounter.getInstance().getAndShift(shift);
        runner.update(connection, "update " + getPrefixTableName() + "_schems set id = id + " + idShift + " order by id desc");
        schemas.forEach(s -> s.setId(s.getId() + idShift));

        return schemas;
    }


    public void afterTransform() throws SQLException {
        dd.alterTable(getPrefixTableName() + "_schems")
                .add(constraint("FK_" + getPrefixTableName() + "_base_schems_id")
                        .foreignKey("id")
                        .references("base_schems", "id")
                        .onDelete(CASCADE)
                        .onUpdate(CASCADE)
                )
                .execute();
    }

    protected abstract String getPrefixTableName();

}
