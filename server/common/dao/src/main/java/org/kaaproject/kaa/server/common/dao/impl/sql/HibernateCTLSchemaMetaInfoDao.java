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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ID_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaMetaInfoDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HibernateCTLSchemaMetaInfoDao extends HibernateAbstractDao<CTLSchemaMetaInfo> implements CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaMetaInfoDao.class);

    public HibernateCTLSchemaMetaInfoDao() {
    }

    @Override
    protected Class<CTLSchemaMetaInfo> getEntityClass() {
        return CTLSchemaMetaInfo.class;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CTLSchemaMetaInfo save(CTLSchemaMetaInfo metaInfo) {
        String tenantId = metaInfo.getTenant() != null ? metaInfo.getTenant().getStringId() : null;
        String applicationId = metaInfo.getApplication() != null ? metaInfo.getApplication().getStringId() : null;
        LOG.debug("Try to save or find meta info with fqn [{}], tenantId [{}] and applicationId [{}]", metaInfo.getFqn(), 
                tenantId, applicationId);
        CTLSchemaMetaInfo uniqueMetaInfo = findByFqnTenantIdAndApplicationId(metaInfo.getFqn(), tenantId, applicationId);
        if (uniqueMetaInfo == null) {
            uniqueMetaInfo = super.save(metaInfo, true);
            LOG.debug("Save result: {}", uniqueMetaInfo);
        } else {
            LOG.debug("Search result: {}", uniqueMetaInfo);
        }
        return uniqueMetaInfo;
    }
    
    private Criterion buildSearchCriterion(String fqn, String tenantId, String applicationId) {
        List<Criterion> searchCriterions = new ArrayList<>();
        searchCriterions.add(Restrictions.eq(CTL_SCHEMA_META_INFO_FQN, fqn));
        if (isNotBlank(tenantId)) {
            searchCriterions.add(Restrictions.eq(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS, Long.valueOf(tenantId)));
        } else {
            searchCriterions.add(Restrictions.isNull(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS));
        }
        if (isNotBlank(applicationId)) {
            searchCriterions.add(Restrictions.eq(CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS, Long.valueOf(applicationId)));
        } else {
            searchCriterions.add(Restrictions.isNull(CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS));
        }
        return Restrictions.and(searchCriterions.toArray(new Criterion[searchCriterions.size()]));
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CTLSchemaMetaInfo findByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) {
        LOG.debug("Searching ctl metadata by fqn [{}], tenantId [{}] and applicationId [{}]", fqn, tenantId, applicationId);
        CTLSchemaMetaInfo ctlSchemaMetaInfo = findOneByCriterion(buildSearchCriterion(fqn, tenantId, applicationId));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{}] Search result: {}.", fqn, tenantId, applicationId, ctlSchemaMetaInfo);
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", fqn, tenantId, applicationId, ctlSchemaMetaInfo != null);
        }
        return ctlSchemaMetaInfo;
    }
    
    @Override
    public List<CTLSchemaMetaInfo> findSiblingsByFqnTenantIdAndApplicationId(String fqn, String tenantId,
            String applicationId) {
        LOG.debug("Searching siblings of ctl by fqn [{}], tenantId [{}] and applicationId [{}]", fqn, tenantId, applicationId);
        List<CTLSchemaMetaInfo> ctlSchemaMetaInfos;
        if (isNotBlank(fqn) && isNotBlank(tenantId) && isNotBlank(applicationId)) {
            ctlSchemaMetaInfos = findListByCriterion(
                    Restrictions.and(
                            Restrictions.eq(CTL_SCHEMA_META_INFO_FQN, fqn),
                            Restrictions.eq(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS, Long.valueOf(tenantId)),
                            Restrictions.isNotNull(CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS),
                            Restrictions.ne(CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS, Long.valueOf(applicationId))
                                    ));
        } else {
            ctlSchemaMetaInfos = Collections.emptyList();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}][{}][{}] Search result: [{}].", fqn, tenantId, applicationId, Arrays.toString(ctlSchemaMetaInfos.toArray()));
        } else {
            LOG.debug("[{}][{}][{}] Search result: [{}].", fqn, tenantId, applicationId, ctlSchemaMetaInfos.size());
        }
        return ctlSchemaMetaInfos;
    }
    
    private Criterion buildExludingSearchCriterion(String fqn, String excludingTenantId, String excludingApplicationId) {
        List<Criterion> searchCriterions = new ArrayList<>();
        if (isNotBlank(excludingTenantId)) {
            searchCriterions.add(Restrictions.isNull(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS));
            if (isNotBlank(excludingApplicationId)) {
                searchCriterions.add(Restrictions.and(
                        Restrictions.eq(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS, Long.valueOf(excludingTenantId)),
                        Restrictions.isNull(CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS)));
            } else {
                searchCriterions.add(Restrictions.and(
                        Restrictions.eq(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS, Long.valueOf(excludingTenantId)),
                        Restrictions.isNotNull(CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS)));
            }
        } else {
            searchCriterions.add(Restrictions.isNotNull(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS));
        }
        return Restrictions.and(Restrictions.eq(CTL_SCHEMA_META_INFO_FQN, fqn), 
                Restrictions.or(searchCriterions.toArray(new Criterion[searchCriterions.size()])));
    }

    @Override
    public List<CTLSchemaMetaInfo> findExistingFqns(String fqn, String excludingTenantId, String excludingApplicationId) {
        LOG.debug("Searching ctl metadata by fqn [{}], excludingTenantId [{}] and excludingApplicationId [{}]", fqn, excludingTenantId, excludingApplicationId);
        List<CTLSchemaMetaInfo> ctlSchemasMetaInfos = findListByCriterion(buildExludingSearchCriterion(fqn, excludingTenantId, excludingApplicationId));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{}] Search result: {}.", fqn, excludingTenantId, excludingApplicationId, Arrays.toString(ctlSchemasMetaInfos.toArray()));
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", fqn, excludingTenantId, excludingApplicationId, ctlSchemasMetaInfos.size());
        }
        return ctlSchemasMetaInfos;
    }
    
    @Override
    public List<CTLSchemaMetaInfo> findOthersByFqnAndTenantId(String fqn, String tenantId, String excludingId) {
        LOG.debug("Searching other ctl schema meta infos by fqn [{}], tenant id [{}] and excluding id [{}]", 
                fqn, tenantId, excludingId);
        List<CTLSchemaMetaInfo> availableSchemas = findListByCriterion(
                Restrictions.and(Restrictions.ne(ID_PROPERTY, Long.valueOf(excludingId)),
                                 Restrictions.eq(CTL_SCHEMA_META_INFO_FQN, fqn),
                                 Restrictions.or(
                                         Restrictions.eq(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS, Long.valueOf(tenantId)),
                                         Restrictions.isNull(CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS))));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}][{}][{}] Search result: [{}].", fqn, tenantId, excludingId, Arrays.toString(availableSchemas.toArray()));
        } else {
            LOG.debug("[{}][{}][{}] Search result: [{}].", fqn, tenantId, excludingId, availableSchemas.size());
        }
        return availableSchemas;
    }
    
    @Override
    public CTLSchemaMetaInfo updateScope(CTLSchemaMetaInfo ctlSchemaMetaInfo) {
        LOG.debug("Updating ctl meta info scope {}", ctlSchemaMetaInfo);
        CTLSchemaMetaInfo metaInfo = findById(ctlSchemaMetaInfo.getStringId());
        if (metaInfo != null) {
            metaInfo.setTenant(ctlSchemaMetaInfo.getTenant());
            metaInfo.setApplication(ctlSchemaMetaInfo.getApplication());
            metaInfo = super.save(metaInfo);
        }
        LOG.debug("Update result: {}", metaInfo != null);
        return metaInfo;
    }

}
