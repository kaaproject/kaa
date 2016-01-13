/*
 * Copyright 2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.service;

import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.dao.PluginContractInstanceService;
import org.kaaproject.kaa.server.common.dao.impl.PluginContractInstanceDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.PluginContractInstance;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.PluginInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class PluginContractInstanceServiceImpl implements PluginContractInstanceService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginContractInstanceServiceImpl.class);

    @Autowired
    private PluginContractInstanceDao<PluginContractInstance> pluginContractInstanceDao;

    @Override
    public Set<PluginInstanceDto> findPluginInstancesByPluginContractInstanceIds(List<String> pluginContractInstanceIds) {
        LOG.debug("Looking for plugin instances by plugin contract instance ids: {}", pluginContractInstanceIds);
        List<PluginContractInstance> pluginContractInstances = pluginContractInstanceDao.findByIds(pluginContractInstanceIds);
        Set<PluginInstance> pluginInstances = new HashSet<>();
        for (PluginContractInstance pluginContractInstance : pluginContractInstances) {
            pluginInstances.add(pluginContractInstance.getPluginInstance());
        }
        return ModelUtils.convertDtoSet(pluginInstances);
    }
}
