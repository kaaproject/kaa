package org.kaaproject.data_migration;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.kaaproject.data_migration.model.Ctl;
import org.kaaproject.data_migration.model.Schema;
import org.kaaproject.data_migration.utils.BaseSchemaIdCounter;
import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.kaaproject.data_migration.utils.DataSources.MARIADB;

public class MigrateData {

    private static Connection conn;

    public static void main(String[] args) {
        try {
            List<Schema> schemas = new ArrayList<>();
            conn = MARIADB.getDs().getConnection();
            QueryRunner runner = new QueryRunner();
            Long maxId = runner.query(conn, "select max(id) as max_id from base_schems", rs -> rs.next() ? rs.getLong("max_id") : null);
            BaseSchemaIdCounter.setInitValue(maxId);

            List<AbstractCTLMigration> migrationList = new ArrayList<>();
            migrationList.add(new CTLConfigurationMigration(conn));
//            migrationList.add(new CTLEventsMigration(conn));

            CTLAggregation aggregation = new CTLAggregation(conn);
            BaseSchemaRecordsCreation recordsCreation = new BaseSchemaRecordsCreation(conn);


            //before phase
            for (AbstractCTLMigration m : migrationList) {
                m.beforeTransform();
            }

            // transform phase
            for (AbstractCTLMigration m : migrationList) {
                schemas.addAll(m.transform());
            }

            //aggregation phase
            Map<Ctl, List<Schema>> ctlToSchemas = aggregation.aggregate(schemas);

            //base schema records creation phase
            recordsCreation.create(ctlToSchemas);


            //after phase
            for (AbstractCTLMigration m : migrationList) {
                m.afterTransform();
            }
;

        } catch (SQLException | IOException | ConfigurationGenerationException e) {
            DbUtils.rollbackAndCloseQuietly(conn);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }
}
