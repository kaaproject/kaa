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

package org.kaaproject.kaa.server.operations.service.logs.aware;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the Application Id that it runs in.
 *
 */
public interface ApplicationAware {
    
    /**
     * Set the Application Id that this object runs in.
     * Normally this call will be used to initialize the Token.
     * @param applicationId the Application Id to be used by this object
     */
    public void setApplicationId(String applicationId);
}
