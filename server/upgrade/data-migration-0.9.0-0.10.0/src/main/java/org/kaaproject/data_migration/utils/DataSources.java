package org.kaaproject.data_migration.utils;


import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

import static org.kaaproject.data_migration.utils.Constants.*;

public enum DataSources {

    MARIADB(getMariaDB()), POSTGRES(getPostgreSQL());

    private final DataSource ds;
    private static final String DB_NAME = "kaa";


     DataSources(DataSource ds) {
        this.ds = ds;
    }

    public DataSource getDs() {
        return ds;
    }

    private static DataSource getPostgreSQL() {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.postgresql.Driver");
        bds.setUrl("jdbc:postgresql://" + HOST +":5432/" + DB_NAME);
        bds.setUsername(USER_NAME);
        bds.setPassword(PASSWORD);
        return bds;
    }


    private static DataSource getMariaDB() {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.mariadb.jdbc.Driver");
        bds.setUrl("jdbc:mysql://" + HOST +":3306/" + DB_NAME);
        bds.setUsername(USER_NAME);
        bds.setPassword(PASSWORD);
//        bds.setDefaultAutoCommit(false);
        return bds;
    }
}
