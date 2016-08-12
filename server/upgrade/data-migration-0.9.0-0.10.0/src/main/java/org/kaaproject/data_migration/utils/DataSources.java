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

package org.kaaproject.data_migration.utils;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

import static org.kaaproject.data_migration.utils.Constants.HOST;
import static org.kaaproject.data_migration.utils.Constants.PASSWORD;
import static org.kaaproject.data_migration.utils.Constants.USER_NAME;

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
        bds.setUrl("jdbc:postgresql://" + HOST + ":5432/" + DB_NAME);
        bds.setUsername(USER_NAME);
        bds.setPassword(PASSWORD);
        return bds;
    }


    private static DataSource getMariaDB() {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.mariadb.jdbc.Driver");
        bds.setUrl("jdbc:mysql://" + HOST + ":3306/" + DB_NAME);
        bds.setUsername(USER_NAME);
        bds.setPassword(PASSWORD);
//        bds.setDefaultAutoCommit(false);
        return bds;
    }
}
