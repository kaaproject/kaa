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

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicDao;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPICS_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.WEIGHT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.NAME_PROPERTY;

@Repository
public class HibernateEndpointGroupDao extends HibernateAbstractDao<EndpointGroup> implements EndpointGroupDao<EndpointGroup> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateEndpointGroupDao.class);

    @Autowired
    private TopicDao<Topic> topicDao;

    @Override
    public List<EndpointGroup> findByApplicationId(String appId) {
        List<EndpointGroup> groups = Collections.emptyList();
        LOG.debug("Searching endpoint group by application id [{}] ", appId);
        if (isNotBlank(appId)) {
            groups = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(groups.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, groups.size());
        }
        return groups;
    }

    @Override
    public EndpointGroup findByAppIdAndWeight(String appId, int weight) {
        EndpointGroup group = null;
        LOG.debug("Searching endpoint group by application id [{}] and weight [{}]", appId, weight);
        if (isNotBlank(appId)) {
            group = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.eq(WEIGHT_PROPERTY, weight)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", appId, weight, group);
        } else {
            LOG.debug("[{},{}] Search result: {}.", appId, weight, group != null);
        }
        return group;
    }
    
    @Override
    public EndpointGroup findByAppIdAndName(String applicationId, String name) {
        EndpointGroup group = null;
        LOG.debug("Searching endpoint group by application id [{}] and name [{}]", applicationId, name);
        if (isNotBlank(applicationId)) {
            group = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)),
                            Restrictions.eq(NAME_PROPERTY, name)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", applicationId, name, group);
        } else {
            LOG.debug("[{},{}] Search result: {}.", applicationId, name, group != null);
        }
        return group;
    }

    @Override
    public EndpointGroup removeTopicFromEndpointGroup(String id, String topicId) {
        LOG.debug("Removing topic [{}] from endpoint group [{}]", topicId, id);
        EndpointGroup endpointGroup = findById(id);
        Set<Topic> topics = endpointGroup.getTopics();
        Iterator<Topic> it = topics.iterator();
        while (it.hasNext()) {
            Topic topic = it.next();
            if (topic.getId() == Long.parseLong(topicId)) {
                it.remove();
                topic.getEndpointGroups().remove(endpointGroup);
                topicDao.save(topic);
                break;
            }
        }
        return save(endpointGroup);
    }

    @Override
    public List<EndpointGroup> findEndpointGroupsByTopicIdAndAppId(String appId, String topicId) {
        List<EndpointGroup> groups = Collections.emptyList();
        LOG.debug("Searching endpoint group by application id [{}] and topic id [{}]", appId, topicId);
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS);
            criteria.createAlias(TOPICS_PROPERTY, TOPIC_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(TOPIC_REFERENCE, Long.valueOf(topicId))));
            groups = findListByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(groups.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, groups.size());
        }
        return groups;
    }

    //TODO: Check if it thread safe.
    @Override
    public EndpointGroup addTopicToEndpointGroup(String id, String topicId) {
        EndpointGroup endpointGroup = findById(id);
        if (endpointGroup != null) {
            addTopicToEndpointGroup(endpointGroup, topicId);
        }
        return endpointGroup;
    }

    @Override
    public EndpointGroup save(EndpointGroup endpointGroup) {
        LOG.debug("Saving endpoint group {}", endpointGroup);
        Set<Topic> topics = endpointGroup.getTopics();
        endpointGroup = super.save(endpointGroup);
        if (topics != null && !topics.isEmpty()) {
            for (Topic topic : topics) {
                addTopicToEndpointGroup(endpointGroup, topic.getStringId());
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Saving result: {}.", endpointGroup);
        } else {
            LOG.debug("Saving result: {}.", endpointGroup != null);
        }
        return endpointGroup;
    }

    private EndpointGroup addTopicToEndpointGroup(EndpointGroup endpointGroup, String topicId) {
        Topic topic = topicDao.findById(topicId);
        if (topic != null) {
            topic.getEndpointGroups().add(endpointGroup);
        }
        endpointGroup.getTopics().add(save(topic, Topic.class));
        return endpointGroup;
    }

    @Override
    protected Class<EndpointGroup> getEntityClass() {
        return EndpointGroup.class;
    }

    @Override
    public void removeById(String id) {
        EndpointGroup endpointGroup = findById(id);
        if (endpointGroup != null) {
            Set<Topic> topics = endpointGroup.getTopics();
            if (topics != null && !topics.isEmpty()) {
                for (Topic topic : topics) {
                    topic.getEndpointGroups().remove(endpointGroup);
                    topicDao.save(topic);
                }
            }
        }
        remove(endpointGroup);
        LOG.debug("Removed endpoint group by id [{}] ", id);
    }

}
