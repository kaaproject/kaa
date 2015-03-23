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
package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.TopicDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUPS_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_TYPE_PROPERTY;

@Repository
public class HibernateTopicDao extends HibernateAbstractDao<Topic> implements TopicDao<Topic> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateTopicDao.class);

    @Override
    public List<Topic> findTopicsByAppId(String appId) {
        LOG.debug("Searching topics by application id [{}]", appId);
        List<Topic> topics = Collections.emptyList();
        if (isNotBlank(appId)) {
            topics = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(topics.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, topics.size());
        }
        return topics;
    }

    @Override
    public List<Topic> findTopicsByAppIdAndType(String appId, TopicTypeDto typeDto) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Searching topics by application id [{}] and type [{}]", appId, typeDto);
        if (isNotBlank(appId)) {
            topics = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.eq(TOPIC_TYPE_PROPERTY, typeDto)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", appId, typeDto, Arrays.toString(topics.toArray()));
        } else {
            LOG.debug("[{},{}] Search result: {}.", appId, typeDto, topics.size());
        }
        return topics;
    }

    @Override
    public List<Topic> findTopicsByIds(List<String> ids) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Searching topics by ids [{}]", ids);
        if (ids != null && !ids.isEmpty()) {
            List<Long> lids = toLongIds(ids);
            if (!lids.isEmpty()) {
                topics = findListByCriterion(Restrictions.in(ID_PROPERTY, lids));
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", ids, Arrays.toString(topics.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", ids, topics.size());
        }
        return topics;
    }

    @Override
    public List<Topic> findVacantTopicsByGroupId(String groupId) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Searching vacant topics for endpoint group with id [{}]", groupId);
        if (isNotBlank(groupId)) {
            topics = findListByCriterionWithAlias(ENDPOINT_GROUPS_PROPERTY, ENDPOINT_GROUP_ALIAS, JoinType.LEFT_OUTER_JOIN,
                    Restrictions.or(
                            Restrictions.ne(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.isNull(ENDPOINT_GROUP_REFERENCE)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", groupId, Arrays.toString(topics.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", groupId, topics.size());
        }
        return topics;
    }

    @Override
    public void removeTopicsByAppId(String appId) {
        if (isNotBlank(appId)) {
            List<Topic> topics = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
            removeList(topics);
        }
        LOG.debug("Removed topics by application id [{}]", appId);
    }

    @Override
    public Topic getNextSeqNumber(String topicId) {
        Topic topic = findById(topicId);
        topic.incrementSeqNumber();
        return save(topic);
    }

    @Override
    protected Class<Topic> getEntityClass() {
        return Topic.class;
    }
}
