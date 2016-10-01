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

package org.kaaproject.kaa.server.datamigration.utils;


public final class Options {
  public static final String DEFAULT_USER_NAME = "sqladmin";
  public static final String DEFAULT_PASSWORD = "admin";
  public static final String DEFAULT_DB_NAME = "kaa";
  public static final String DEFAULT_HOST = "localhost";
  public static final String DEFAULT_NO_SQL = "mongo";
  public static final String DEFAULT_DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";
  public static final String DEFAULT_JDBC_URL = "jdbc:mysql:failover://localhost:3333/kaa";

  private String username = DEFAULT_USER_NAME;
  private String password = DEFAULT_PASSWORD;
  private String dbName = DEFAULT_DB_NAME;
  private String host = DEFAULT_HOST;
  private String noSql = DEFAULT_NO_SQL;
  private String driverClassName = DEFAULT_DRIVER_CLASS_NAME;
  private String jdbcUrl = DEFAULT_JDBC_URL;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getNoSql() {
    return noSql;
  }

  public void setNoSql(String noSql) {
    this.noSql = noSql;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  @Override
  public String toString() {
    return "Options{"
        + "username='" + username + '\''
        + ", password='" + password + '\''
        + ", dbName='" + dbName + '\''
        + ", host='" + host + '\''
        + ", driverClassName='" + driverClassName + '\''
        + ", jdbcUrl='" + jdbcUrl + '\'' + '}';
  }
}
