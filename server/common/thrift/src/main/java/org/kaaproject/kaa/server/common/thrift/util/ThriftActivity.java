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

package org.kaaproject.kaa.server.common.thrift.util;

/**
 * The Interface ThriftActivity.
 *
 * @param <T> the generic type
 */
public interface ThriftActivity<T> {
    
    /**
     * Do in template.
     *
     * @param t the t
     */
    public void doInTemplate(T t);
    
    /**
     * Checks if is success.
     *
     * @param activitySuccess the activity success
     */
    public void isSuccess(boolean activitySuccess);
}
