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

package org.kaaproject.kaa.server.common.dao.service;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.dao.PluginService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.ContractDao;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.impl.PluginDao;
import org.kaaproject.kaa.server.common.dao.impl.PluginInstanceDao;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.Contract;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.Plugin;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.PluginContract;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.PluginInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BasePluginService implements PluginService {

    private static final Logger LOG = LoggerFactory.getLogger(BasePluginService.class);

    @Autowired
    private PluginDao<Plugin> pluginDao;

    @Autowired
    private ContractDao<Contract> contractDao;

    @Autowired
    private PluginInstanceDao<PluginInstance> pluginInstanceDao;

    @Override
    public PluginDto registerPlugin(PluginDto pluginDto) {
        LOG.debug("Registering plugin: {}", pluginDto);
        Plugin plugin = new Plugin(pluginDto);
        for (PluginContract pluginContract : plugin.getPluginContracts()) {
            Contract receivedContract = pluginContract.getContract();
            Contract foundContract = contractDao.findByNameAndVersion(receivedContract.getName(), receivedContract.getVersion());
            if (foundContract != null) {
                receivedContract.setId(foundContract.getId());
            }
        }
        Plugin savedPlugin = pluginDao.save(plugin);
        LOG.debug("Registered plugin: {}", plugin);
        return DaoUtil.getDto(savedPlugin);
    }

    @Override
    public PluginDto findPluginByNameAndVersion(String name, Integer version) {
        LOG.debug("Looking for a plugin by its name and version: [{}, {}]", name, version);
        Plugin plugin = pluginDao.findByNameAndVersion(name, version);
        LOG.debug("Found plugin: {}", plugin);
        return DaoUtil.getDto(plugin);
    }

    @Override
    public PluginDto findPluginByClassName(String className) {
        LOG.debug("Looking for a plugin by its class name: {}", className);
        Plugin plugin = pluginDao.findByClassName(className);
        LOG.debug("Found plugin: {}", plugin);
        return DaoUtil.getDto(plugin);
    }

    @Override
    public void unregisterPluginById(String id) {
        LOG.debug("Un-registering plugin by id: {}", id);
        pluginDao.removeById(id);
    }

    @Override
    public List<PluginDto> findAllPlugins() {
        LOG.debug("Looking for all plugins");
        return DaoUtil.convertDtoList(pluginDao.find());
    }

    @Override
    public PluginInstanceDto saveInstance(PluginInstanceDto pluginInstanceDto) {
        LOG.debug("Saving instance: {}", pluginInstanceDto);
        PluginDto pluginDto = pluginInstanceDto.getPluginDefinition();
        if (pluginDto == null) {
            throw new IncorrectParameterException("Plugin instance has no plugin, unable to save");
        }
        Plugin plugin = pluginDao.findByClassName(pluginDto.getClassName());
        PluginInstance pluginInstance = new PluginInstance(pluginInstanceDto);
        pluginInstance.setPlugin(plugin);
        plugin.addPluginInstance(pluginInstance);
        pluginDao.persist(plugin);
        PluginInstance savedInstance = pluginInstanceDao.save(pluginInstance);
        LOG.debug("Saved instance: {}", savedInstance);
        return DaoUtil.getDto(savedInstance);
    }

    @Override
    public PluginInstanceDto findInstanceById(String id) {
        LOG.debug("Looking for a plugin instance by id: {}", id);
        PluginInstance pluginInstance = pluginInstanceDao.findById(id);
        LOG.debug("Found plugin instance: {}", pluginInstance);
        return DaoUtil.getDto(pluginInstance);
    }

    @Override
    public Set<PluginInstanceDto> findInstancesByPluginId(String pluginId) {
        LOG.debug("Looking for the plugin instances by plugin id: {}", pluginId);
        Plugin plugin = pluginDao.findById(pluginId);
        LOG.trace("Found plugin instance: {}", plugin);
        Set<PluginInstanceDto> pluginInstanceDtos = DaoUtil.convertDtoSet(plugin != null ? plugin.getPluginInstances() : null);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found plugin instances: {}", pluginInstanceDtos);
        } else {
            int size = pluginInstanceDtos == null ? 0 : pluginInstanceDtos.size();
            LOG.debug("Found plugin instances: {}", size);
        }
        return pluginInstanceDtos;
    }

    @Override
    public void removeInstanceById(String id) {
        LOG.debug("Removing plugin instance by id: {}", id);
        pluginInstanceDao.removeById(id);
    }
}
