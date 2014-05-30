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

import static org.kaaproject.kaa.server.common.dao.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.NotificationDao;
import org.kaaproject.kaa.server.common.dao.TopicDao;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.mongo.model.Notification;
import org.kaaproject.kaa.server.common.dao.mongo.model.Topic;
import org.kaaproject.kaa.server.common.dao.mongo.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicServiceImpl implements TopicService {

    private static final Logger LOG = LoggerFactory.getLogger(TopicServiceImpl.class);

    @Autowired
    private EndpointGroupDao<EndpointGroup> endpointGroupDao;

    @Autowired
    private EndpointService endpointService;

    @Autowired
    private TopicDao<Topic> topicDao;

    @Autowired
    private NotificationDao<Notification> notificationDao;

    @Override
    public TopicDto saveTopic(TopicDto topicDto) {
        validateObject(topicDto, "Can't save topic. Invalid topic object");
        if (StringUtils.isBlank(topicDto.getId())) {
            LOG.debug("Save new topic.");
            topicDto.setCreatedTime(System.currentTimeMillis());
        }
        Topic topic = new Topic(topicDto);
        Update update = new Update();
        update.setSequenceNumber(topic.getSecNum());
        update.setStatus(ProcessingStatus.IDLE);
        topic.setUpdate(update);
        return getDto(topicDao.save(topic));
    }

    @Override
    public TopicDto findTopicById(String id) {
        validateId(id, "Can't find topic. Invalid topic id " + id);
        return getDto(topicDao.findById(id));
    }

    @Override
    public List<TopicDto> findTopicsByAppId(String appId) {
        validateId(appId, "Can't find topic. Invalid topic id " + appId);
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
        List<String> topicIds = endpointGroup.getTopics();
        return convertDtoList(topicDao.findTopicsByIds(topicIds));
    }

    @Override
    public List<TopicDto> findVacantTopicsByEndpointGroupId(String endpointGroupId) {
        validateId(endpointGroupId, "Can't find vacant topics. Invalid endpoint group id " + endpointGroupId);
        EndpointGroup endpointGroup = endpointGroupDao.findById(endpointGroupId);
        String applicationId = idToString(endpointGroup.getApplicationId());
        List<String> topicIds = endpointGroup.getTopics();
        return convertDtoList(topicDao.findVacantTopicsByAppId(applicationId, topicIds));
    }

    @Override
    public void removeTopicById(String id) {
        validateId(id, "Can't remove topic. Invalid topic id " + id);
        TopicDto topic = findTopicById(id);
        if (topic != null) {
            List<EndpointGroup> groups = endpointGroupDao.findEndpointGroupsByTopicIdAndAppId(topic.getApplicationId(), id);
            if (groups != null && !groups.isEmpty()) {
                for (EndpointGroup eg : groups) {
                    endpointService.removeTopicFromEndpointGroup(eg.getId(), id);
                }
            }
            notificationDao.removeNotificationsByTopicId(id);
            topicDao.removeById(id);
        }
    }

    @Override
    public void removeTopicsByAppId(String appId) {
        validateId(appId, "Can't remove topics. Invalid application id " + appId);
        topicDao.removeTopicsByAppId(appId);
    }

}
