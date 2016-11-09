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
 * <p>Provides implementation of endpoint/user registration.</p>
 *
 * <p> In order to have ability to send and (or) receive events an application must be attached to
 * the user entity in Kaa ecosystem. User registration module gives API to manage endpoint-to-user
 * attachments.<br> <br> In scope of this java endpoint sdk provides next functionality: Attach
 * current endpoint to user entity; Attach other endpoint to user entity; Detach endpoint from user
 * entity. <br> If endpoint is assumed to be attached or detached by another application it is
 * possible to set up callback for notifications when this endpoint becomes attached or
 * detached.<br> <br> All operations being described on this page are run in asynchronous mode.
 * Thus, in order to retrieve result for each operation an appropriate callback should be
 * registered. </p> <h2>Usage</h2> <p> Attaching current endpoint to user:
 * <pre>
 *  {@code
 *      EndpointRegistrationManager registrationManager =
 *      kaaClient.getEndpointRegistrationManager();
 *      registrationManager.attachUser("userExternalId", "userAccessToken", new
 * UserAuthResultListener() { ... });
 *  }
 *  </pre>
 * To check if this endpoint is attached to user call {@link
 * org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager#isAttachedToUser()}.<br>
 * <br> Attaching any endpoint to user by its access token:
 * <pre>
 *  {@code
 * EndpointRegistrationManager registrationManager = kaaClient.getEndpointRegistrationManager();
 * registrationManager.attachEndpoint(new EndpointAccessToken("accessToken"), new
 * EndpointOperationResultListener() {...});
 * }
 * </pre>
 * Detaching endpoint from user by its {@link org.kaaproject.kaa.client.event.EndpointKeyHash}:
 * <pre>
 * {@code
 * EndpointRegistrationManager registrationManager = kaaClient.getEndpointRegistrationManager();
 * registrationManager.detachEndpoint((new EndpointKeyHash("keyHash"), new
 * EndpointOperationResultListener() {...});
 * }
 * </pre>
 * EndpointKeyHash for endpoint can be received with AttachEndpoint operation provided from
 * Operations server. <br> <br> If current endpoint is assumed to be attached or detached by another
 * endpoint, specific {@link
 * org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback}
 * and {@link org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback} may be
 * specified to receive notification about such event.<br> <br>
 */
package org.kaaproject.kaa.client.event.registration;

