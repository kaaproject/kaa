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

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

public final class DataSources {

  /**
   * Create data source based on passed options.
   *
   * @param opt options that used to build data source
   * @return the data source
   */
  public static DataSource getDataSource(Options opt) {
    BasicDataSource bds = new BasicDataSource();
    bds.setDriverClassName(opt.getDriverClassName());
    bds.setUrl(opt.getJdbcUrl());
    bds.setUsername(opt.getUsername());
    bds.setPassword(opt.getPassword());
    bds.setDefaultAutoCommit(false);
    return bds;
  }

}
