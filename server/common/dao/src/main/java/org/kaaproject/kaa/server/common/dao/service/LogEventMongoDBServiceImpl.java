/*
 * Copyright 2014 CyberVision, Inc.
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

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.common.dto.logs.security.MongoPrivilegeDto;
import org.kaaproject.kaa.common.dto.logs.security.MongoResourceDto;
import org.kaaproject.kaa.common.dto.logs.security.MongoRoleDto;
import org.kaaproject.kaa.common.dto.logs.security.MongoUserDto;
import org.kaaproject.kaa.server.common.dao.LogEventService;
import org.kaaproject.kaa.server.common.dao.impl.LogEventDao;
import org.kaaproject.kaa.server.common.dao.impl.SecureRoleDao;
import org.kaaproject.kaa.server.common.dao.impl.SecureUserDao;
import org.kaaproject.kaa.server.common.dao.model.mongo.LogEvent;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureRole;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogEventMongoDBServiceImpl implements LogEventService {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventMongoDBServiceImpl.class);

    @Autowired
    private LogEventDao<LogEvent> logEventPackDao;

    @Autowired
    private SecureRoleDao<SecureRole> secureRoleDao;

    @Autowired
    private SecureUserDao<SecureUser> secureUserDao;

    @Override
    public void createCollection(String collectionName) {
        logEventPackDao.createCollection(collectionName);
    }

    @Override
    public void createRole(String name, String collectionName) {
        MongoRoleDto roleDto = new MongoRoleDto();
        MongoPrivilegeDto privilegeDto = new MongoPrivilegeDto();
        MongoResourceDto resourceDto = new MongoResourceDto();
        List<String> roles = new ArrayList<>();
        resourceDto.setDB(secureRoleDao.getDBName());
        resourceDto.setCollection(collectionName);
        List<String> actions = new ArrayList<>();
        actions.add("find");
        List<MongoPrivilegeDto> privileges = new ArrayList<>();
        privileges.add(privilegeDto);
        privilegeDto.setResource(resourceDto);
        privilegeDto.setActions(actions);
        roleDto.setPrivileges(privileges);
        roleDto.setRoleName(name);
        roleDto.setRoles(roles);
        secureRoleDao.saveRole(new SecureRole(roleDto));
    }

    @Override
    public void createUser(String name, String password, String roleName) {
        MongoUserDto userDto = new MongoUserDto();
        List<String> roles = new ArrayList<>();
        roles.add(roleName);
        userDto.setUserName(name);
        userDto.setPassword(password);
        userDto.setRoles(roles);
        secureUserDao.saveUser(new SecureUser(userDto));
    }

    @Override
    public List<LogEventDto> save(List<LogEventDto> logEventDtos, String collectionName) {
        List<LogEvent> logEvents = new ArrayList<>(logEventDtos.size());
        for (LogEventDto logEventDto : logEventDtos) {
            logEvents.add(new LogEvent(logEventDto));
        }
        LOG.debug("Saving {} log events", logEvents.size());
        List<LogEventDto> dtos = convertDtoList(logEventPackDao.save(logEvents, collectionName));
        return dtos;
    }

    @Override
    public void removeAll(String collectionName) {
        logEventPackDao.removeAll(collectionName);
    }

}
