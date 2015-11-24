/*
 * Copyright 2015 CyberVision, Inc.
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


import org.kaaproject.kaa.server.common.dao.impl.ServerProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HibernateServerProfileSchemaDao extends HibernateAbstractDao<ServerProfileSchema> implements ServerProfileSchemaDao<ServerProfileSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateServerProfileSchemaDao.class);

    @Override
    protected Class<ServerProfileSchema> getEntityClass() {
        return ServerProfileSchema.class;
    }

    @Override
    public ServerProfileSchema findLatestByAppId(String appId) {
        return null;
    }

    @Override
    public List<ServerProfileSchema> findByAppId(String appId) {
        return null;
    }

    @Override
    public void removeByAppId(String appId) {

    }
}
