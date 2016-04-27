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

package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.kaaproject.kaa.server.common.dao.DBTestRunner;

import javax.sql.DataSource;
import java.sql.*;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MariaDBTestRunner extends DBTestRunner {

    @Override
    protected PreparedStatement prepareStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("show tables from kaa;");
    }

    @Override
    protected String getTrancateSql() {
        return "DELETE FROM ";
    }

    @Override
    protected void truncateTables(Set<String> tableNames, DataSource dataSource) throws SQLException {
        if (tableNames == null || tableNames.isEmpty()) {
            return;
        }
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for(int i = 0; i < tableNames.size(); i++) {
                for (String tableName : tableNames) {
                    try {
                        statement.execute(getTrancateSql() + tableName);
                    }catch (SQLException ex){
                        continue;
                    }
                }
            }
        }
    }

}
