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
package org.kaaproject.kaa.sandbox.web.services.cache;

import org.kaaproject.kaa.common.dto.admin.SdkKey;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataKey;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceException;
import org.kaaproject.kaa.server.common.admin.FileData;

public interface CacheService {

       FileData getSdk(SdkKey key) throws SandboxServiceException;
       
       FileData getProjectFile(ProjectDataKey key);
       
       FileData putProjectFile(ProjectDataKey key, FileData data);
        
}
