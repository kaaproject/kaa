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
package org.kaaproject.kaa.sandbox.web.services;

import org.kaaproject.kaa.common.dto.admin.SdkKey;
import org.kaaproject.kaa.sandbox.web.services.cache.CacheService;
import org.kaaproject.kaa.sandbox.web.services.rest.AdminClientProvider;
import org.kaaproject.kaa.sandbox.web.services.util.Utils;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataKey;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceException;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.admin.FileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger LOG = LoggerFactory.getLogger(CacheServiceImpl.class);
    
    private static final String SDK_CACHE = "sdkCache";
    private static final String FILE_CACHE = "fileCache";
    
    @Autowired
    private AdminClientProvider clientProvider;
    
    /** The thrift host. */
    @Value("#{properties[tenant_developer_user]}")
    private String tenantDeveloperUser;

    /** The thrift port. */
    @Value("#{properties[tenant_developer_password]}")
    private String tenantDeveloperPassword;
    
    @Override
    @Cacheable(SDK_CACHE)
    public FileData getSdk(SdkKey key) throws SandboxServiceException {
        AdminClient client = clientProvider.getClient();
        client.login(tenantDeveloperUser, tenantDeveloperPassword);
        FileData fileData;
        try {
            fileData = client.downloadSdk(key);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
        return fileData;
    }
    
    @Override
    @Cacheable(value = FILE_CACHE, key = "#key", unless="#result == null")
    public FileData getProjectFile(ProjectDataKey key) {
        return null;
    }
    
    @Override
    @CachePut(value = FILE_CACHE, key = "#key")
    public FileData putProjectFile(ProjectDataKey key, FileData data) {
        return data;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value=SDK_CACHE, allEntries=true),
            @CacheEvict(value=FILE_CACHE, allEntries=true)     
        })  
    public void flushAllCaches() throws SandboxServiceException {
        AdminClient client = clientProvider.getClient();
        client.login(tenantDeveloperUser, tenantDeveloperPassword);
        try {
            client.flushSdkCache();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }

        LOG.info("All caches have been completely flushed.");        
    }  

}
