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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint;

import akka.japi.Creator;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;

import java.util.UUID;

public abstract class EndpointActorCreator<T> implements Creator<T> {

  private static final long serialVersionUID = 1L;

  protected final AkkaContext context;

  protected final String actorKey;

  protected final String appToken;

  protected final EndpointObjectHash endpointKey;

  /**
   * Instantiates a new actor creator.
   *
   * @param context          the context
   * @param endpointActorKey the endpoint actor key
   * @param appToken         the app token
   * @param endpointKey      the endpoint key
   */
  public EndpointActorCreator(AkkaContext context, String endpointActorKey, String appToken,
                              EndpointObjectHash endpointKey) {
    super();
    this.context = context;
    this.actorKey = endpointActorKey;
    this.appToken = appToken;
    this.endpointKey = endpointKey;
  }

  public static String generateActorKey() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }
}