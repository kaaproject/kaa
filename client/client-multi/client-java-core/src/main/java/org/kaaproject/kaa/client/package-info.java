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
 * <p>Provides Kaa Client implementation.</p>
 *
 * <p>To start deal with Kaa, following steps should be done:</p>
 * <pre>
 * {@code
 * KaaDesktop kaaDesktop = new KaaDesktop();
 * KaaClient kaaClient = kaaDesktop.getClient();
 *
 * // OR
 *
 * KaaAndroid kaaAndroid = new KaaAndroid(androidContext);
 * KaaClient kaaClient = kaaAndroid.getClient();
 *
 * // Access to all Kaa submodules through 'kaaClient' object
 * }
 * </pre>
 *
 * @see org.kaaproject.kaa.client.Kaa
 * @see org.kaaproject.kaa.client.KaaClient
 */

package org.kaaproject.kaa.client;