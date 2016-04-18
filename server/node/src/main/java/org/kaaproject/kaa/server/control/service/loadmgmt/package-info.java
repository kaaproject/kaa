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

/**
 * Operations Service Load Management Service.
 * 1. Get Operations servers list from ZK
 * 2. Gather Operations servers performance counters.
 * 3. Produce Operations servers list with priority information
 * 4. Propagate Operations servers list to every Bootstrap Servers
 * 5. Load balancing module, using Operations servers counters, produce redirection rules
 * 6. Set redirection rules to specific Operations servers.
 *
 * @author Andrey Panasenko apanasenko@cybervisiontech.com
 *
 */
package org.kaaproject.kaa.server.control.service.loadmgmt;