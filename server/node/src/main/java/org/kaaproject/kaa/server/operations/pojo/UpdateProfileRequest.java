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

package org.kaaproject.kaa.server.operations.pojo;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import java.util.Arrays;


/**
 * The Class for modeling request of profile update. It is used to communicate
 * with
 * {@link org.kaaproject.kaa.server.operations.service.profile.ProfileService
 * ProfileService}
 *
 * @author ashvayka
 */
public class UpdateProfileRequest {

  private final EndpointObjectHash endpointKeyHash;

  private final byte[] profile;

  private final String appToken;

  private final String sdkToken;

  private final String accessToken;

  /**
   * Instantiates a new update profile request.
   *
   * @param appToken        the app token
   * @param endpointKeyHash the endpoint key hash
   * @param accessToken     the access token
   * @param profile         the profile
   * @param sdkToken        the sdk token
   */
  public UpdateProfileRequest(String appToken, EndpointObjectHash endpointKeyHash,
                              String accessToken, byte[] profile, String sdkToken) {
    super();
    this.appToken = appToken;
    this.endpointKeyHash = endpointKeyHash;
    this.accessToken = accessToken;
    this.profile = Arrays.copyOf(profile, profile.length);
    this.sdkToken = sdkToken;
  }


  public EndpointObjectHash getEndpointKeyHash() {
    return endpointKeyHash;
  }

  public byte[] getProfile() {
    return Arrays.copyOf(profile, profile.length);
  }

  public String getApplicationToken() {
    return appToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getSdkToken() {
    return sdkToken;
  }
}
