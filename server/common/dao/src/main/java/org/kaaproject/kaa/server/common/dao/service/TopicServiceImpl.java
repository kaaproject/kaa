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

package org.kaaproject.kaa.server.common.dao.service;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.common.dao.impl.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicDao;
import org.kaaproject.kaa.server.common.dao.model.Notification;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TopicServiceImpl implements TopicService {

    private static final Logger LOG = LoggerFactory.getLogger(TopicServiceImpl.class);

    @Autowired
    private EndpointGroupDao<EndpointGroup> endpointGroupDao;

    @Autowired
    private EndpointService endpointService;

    @Autowired
    private TopicDao<Topic> topicDao;

    private NotificationDao<Notification> notificationDao;

    public void setNotificationDao(NotificationDao<Notification> notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public TopicDto saveTopic(TopicDto topicDto) {
        validateSqlObject(topicDto, "Can't save topic. Invalid topic object");
        if (StringUtils.isBlank(topicDto.getId())) {
            LOG.debug("Save new topic.");
            topicDto.setCreatedTime(System.currentTimeMillis());
            Topic topic = topicDao.findTopicByAppIdAndName(topicDto.getApplicationId(), topicDto.getName());
            if(topic != null){
                throw new IllegalArgumentException("Topic with the same name already present!");
            }
        }
        return getDto(topicDao.save(new Topic(topicDto)));
    }

    @Override
    public TopicDto findTopicById(String id) {
        validateId(id, "Can't find topic. Invalid topic id " + id);
        return getDto(topicDao.findById(id));
    }

    @Override
    public List<TopicDto> findTopicsByAppId(String appId) {
        validateId(appId, "Can't find topic. Invalid application id " + appId);
        return convertDtoList(topicDao.findTopicsByAppId(appId));
    }

    @Override
    public List<TopicDto> findTopicsByAppIdAndType(String appId, TopicTypeDto typeDto) {
        validateId(appId, "Can't find topics. Invalid application id " + appId);
        return convertDtoList(topicDao.findTopicsByAppIdAndType(appId, typeDto));
    }

    @Override
    public List<TopicDto> findTopicsByEndpointGroupId(String endpointGroupId) {
        validateId(endpointGroupId, "Can't find topics. Invalid endpoint group id " + endpointGroupId);
        EndpointGroup endpointGroup = endpointGroupDao.findById(endpointGroupId);
        Set<Topic> topics = endpointGroup.getTopics();
        return convertDtoList(topics);
    }

    @Override
    public List<TopicDto> findVacantTopicsByEndpointGroupId(String endpointGroupId) {
        validateId(endpointGroupId, "Can't find vacant topics. Invalid endpoint group id " + endpointGroupId);
        List<TopicDto> topics = Collections.emptyList();
        EndpointGroup endpointGroup = endpointGroupDao.findById(endpointGroupId);
        String applicationId = null;
        if (endpointGroup != null) {
            applicationId = endpointGroup.getApplicationId();
        }
        if (isNotBlank(applicationId)) {
            topics = convertDtoList(topicDao.findVacantTopicsByGroupId(applicationId, endpointGroupId));
        } else {
            LOG.warn("Can't get application id from endpoint group.");
        }
        return topics;
    }

    @Override
    public List<UpdateNotificationDto<EndpointGroupDto>> removeTopicById(String id) {
        validateId(id, "Can't remove topic. Invalid topic id " + id);
        TopicDto topic = findTopicById(id);
        List<UpdateNotificationDto<EndpointGroupDto>> notificationList = new LinkedList<>();
        if (topic != null) {
            List<EndpointGroup> groups = endpointGroupDao.findEndpointGroupsByTopicIdAndAppId(topic.getApplicationId(), id);
            if (groups != null && !groups.isEmpty()) {
                for (EndpointGroup eg : groups) {
                    notificationList.add(endpointService.removeTopicFromEndpointGroup(eg.getId().toString(), id));
                }
            }
            topicDao.removeById(id);
            notificationDao.removeNotificationsByTopicId(id);
        }
        return notificationList;
    }

    @Override
    public void removeTopicsByAppId(String appId) {
        validateId(appId, "Can't remove topics. Invalid application id " + appId);
        topicDao.removeTopicsByAppId(appId);
    }

}
