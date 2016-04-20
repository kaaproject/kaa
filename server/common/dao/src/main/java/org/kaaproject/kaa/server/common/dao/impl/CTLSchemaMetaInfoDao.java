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

package org.kaaproject.kaa.server.common.dao.impl;

import java.util.List;

/**
 * The interface CTL schema meta information dao.
 *
 * @param <T> the model type parameter.
 */
public interface CTLSchemaMetaInfoDao<T> extends SqlDao<T> {

    /**
     * Find a CTL schema meta info of the given fully qualified name, tenant and application identifiers.
     *
     * @param fqn     the fully qualified.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the CTL schema meta information object with given 
     * fully qualified name, tenant and application identifiers.
     */
    T findByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);
    
    /**
     * Find CTL schema meta infos which are the application level siblings to the CTL 
     * of the given fully qualified name, tenant and application identifiers.
     *
     * @param fqn     the fully qualified.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the CTL schema meta information objects which are the siblings to the given CTL.
     */
    List<T> findSiblingsByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);
    
    /**
     * Find a list of CTL schema meta infos of the given fully qualified name 
     * which is not in specified tenant or application scope.
     *
     * @param fqn     the fully qualified.
     * @param excludingTenantId the tenant identifier.
     * @param excludingApplicationId the application identifier.
     * @return the list of CTL schemas meta information object with given fully qualified name.
     */
    List<T> findExistingFqns(String fqn, String excludingTenantId, String excludingApplicationId);
    
    /**
     * Find others CTL schema meta infos with the given fully qualified name and tenant, excluding meta info id.
     *
     * @param fqn      the fully qualified name.
     * @param tenantId the tenant identifier.
     * @param excludingId the ctl schema meta info identifier to exclude.
     * @return the others CTL schema meta infos with the given fully qualified name and tenant identifier.
     */
    List<T> findOthersByFqnAndTenantId(String fqn, String tenantId, String excludingId);
    
    /**
     * Update scope of the given CTL schema meta information.
     *
     * @param ctlSchema the CTL schema meta information object.
     * @return the saved the CTL schema meta information object.
     */
    T updateScope(T ctlSchema);
    
    
 
}
