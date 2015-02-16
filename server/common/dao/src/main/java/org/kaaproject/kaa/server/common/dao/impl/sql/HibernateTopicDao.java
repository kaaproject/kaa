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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ENDPOINT_GROUP_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ENDPOINT_GROUPS_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ENDPOINT_GROUP_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.TOPIC_TYPE_PROPERTY;

import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.TopicDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateTopicDao extends HibernateAbstractDao<Topic> implements TopicDao<Topic> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateTopicDao.class);

    @Override
    public List<Topic> findTopicsByAppId(String appId) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Find topics by application id {}", appId);
        if (isNotBlank(appId)) {
            topics = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        return topics;
    }

    @Override
    public List<Topic> findTopicsByAppIdAndType(String appId, TopicTypeDto typeDto) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Find topics by application id {} and type", appId, typeDto);
        if (isNotBlank(appId)) {
            topics = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.eq(TOPIC_TYPE_PROPERTY, typeDto)));
        }
        return topics;
    }

    @Override
    public List<Topic> findTopicsByIds(List<String> ids) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Find topics by ids {} and type", ids);
        if (ids != null && !ids.isEmpty()) {
            List<Long> lids = toLongIds(ids);
            if (!lids.isEmpty()) {
                topics = findListByCriterion(Restrictions.in(ID_PROPERTY, lids));
            }
        }
        return topics;
    }

    @Deprecated
    @Override
    public List<Topic> findVacantTopicsByAppId(String appId, List<String> excludeIds) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Find topics by application id {} and type", appId);
        if (isNotBlank(appId)) {
            List<Long> lids = toLongIds(excludeIds);
            topics = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.not(Restrictions.in(ID_PROPERTY, lids))));
        }
        return topics;
    }

    @Override
    public List<Topic> findVacantTopicsByGroupId(String groupId) {
        List<Topic> topics = Collections.emptyList();
        LOG.debug("Find vacant topics for endpoint group with id {}", groupId);
        if (isNotBlank(groupId)) {
            topics = findListByCriterionWithAlias(ENDPOINT_GROUPS_PROPERTY, ENDPOINT_GROUP_ALIAS, JoinType.LEFT_OUTER_JOIN,
                    Restrictions.or(
                            Restrictions.ne(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.isNull(ENDPOINT_GROUP_REFERENCE)));
        }
        return topics;
    }

    @Override
    public void removeTopicsByAppId(String appId) {
        LOG.debug("Remove topics by application id {}", appId);
        if (isNotBlank(appId)) {
            List<Topic> topics = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
            removeList(topics);
        }
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
