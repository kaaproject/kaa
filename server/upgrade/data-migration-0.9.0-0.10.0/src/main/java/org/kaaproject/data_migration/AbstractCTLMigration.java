package org.kaaproject.data_migration;


import org.apache.commons.dbutils.DbUtils;
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

    public AbstractCTLMigration(Connection connection) {
        this.connection = connection;
        runner = new QueryRunner();
        dd = new DataDefinition(connection);
    }

    public void beforeTransform() throws SQLException {
        // delete relation between <feature>_schems to schems
        dd.dropUnnamedFK(getName() + "_schems", "schems");
    }


    protected List<Schema> transform() throws SQLException {
        // fetch schemas of appropriate feature like configuration
        List<Schema> schemas = runner.query(connection, "select " +
                "f.id as id, created_time as createdTime, created_username as createdUsername, " +
                "description, name, schems, version, application_id as appId " +
                "from " + getName() + "_schems f join schems s on f.id = s.id", new BeanListHandler<>(Schema.class));

        // delete the fetched ids from schema table
        String toDelete = schemas.stream().map(s -> s.getId().toString()).collect(joining(", "));
        runner.update(connection, "delete from schems where id in (" + toDelete + ")");

        // shift ids in order to avoid PK constraint violation during adding record to base_schema
        Long shift = runner.query(connection, "select max(id) as max_id from "+ getName() + "_schems", rs -> rs.next() ? rs.getLong("max_id") : null);
        Long idShift = BaseSchemaIdCounter.getInstance().getAndShift(shift);
        runner.update(connection, "update " + getName() + "_schems set id = id + " + idShift + " order by id desc");
        schemas.forEach(s -> s.setId(s.getId() + idShift));

        return schemas;
    }


    public void afterTransform() throws SQLException {
        dd.alterTable(getName() + "_schems")
                .add(constraint("FK_" + getName() + "_base_schems_id")
                        .foreignKey("id")
                        .references("base_schems", "id")
                        .onDelete(CASCADE)
                        .onUpdate(CASCADE)
                )
                .execute();
    }

    protected abstract String getName();

}
