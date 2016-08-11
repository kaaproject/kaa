package org.kaaproject.data_migration;

import org.apache.commons.dbutils.DbUtils;
import org.kaaproject.data_migration.model.Ctl;
import org.kaaproject.data_migration.model.Schema;
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

    public static void main(String[] args)  {
        try {
            List<Schema> schemas = new ArrayList<>();
            conn = MARIADB.getDs().getConnection();
            CTLConfigurationMigration configurationMigration = new CTLConfigurationMigration(conn);
            CTLAggregation aggregation = new CTLAggregation(conn);
            BaseSchemaRecordsCreation recordsCreation = new BaseSchemaRecordsCreation(conn);

            //before phase
            configurationMigration.beforeTransform();

            // transform phase
            schemas.addAll(configurationMigration.transform());

            //aggregation phase
            Map<Ctl, List<Schema>> ctlToSchemas = aggregation.aggregate(schemas);

            //base schema records creation phase
            recordsCreation.create(ctlToSchemas);

            //after phase
            configurationMigration.afterTransform();


        } catch (SQLException | IOException | ConfigurationGenerationException e) {
            DbUtils.rollbackAndCloseQuietly(conn);
        }  finally {
            DbUtils.closeQuietly(conn);
        }
    }
}
