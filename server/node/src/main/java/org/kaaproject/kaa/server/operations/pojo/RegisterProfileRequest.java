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

import java.util.Arrays;

/**
 * The Class for modeling request of profile registration. It is used to
 * communicate with
 * {@link org.kaaproject.kaa.server.operations.service.profile.ProfileService
 * ProfileService}
 *
 * @author ashvayka
 */
public class RegisterProfileRequest {

  private final String appToken;


  private final byte[] endpointKey;


  private final byte[] profile;

  private final String sdkToken;

  private final String accessToken;

  /**
   * Instantiates a new register profile request.
   *
   * @param appToken    the app token
   * @param endpointKey the endpoint key
   * @param sdkToken    the sdk token
   * @param profile     the profile body
   */
  public RegisterProfileRequest(String appToken, byte[] endpointKey, String sdkToken,
                                byte[] profile) {
    this(appToken, endpointKey, sdkToken, profile, null);
  }

  /**
   * Instantiates a new register profile request.
   *
   * @param appToken    the app token
   * @param endpointKey the endpoint key
   * @param sdkToken    the sdk token
   * @param profile     the profile body
   * @param accessToken the access token
   */
  public RegisterProfileRequest(String appToken, byte[] endpointKey, String sdkToken,
                                byte[] profile, String accessToken) {
    super();
    this.appToken = appToken;
    this.endpointKey = Arrays.copyOf(endpointKey, endpointKey.length);
    this.sdkToken = sdkToken;
    this.profile = Arrays.copyOf(profile, profile.length);
    this.accessToken = accessToken;
  }


  public byte[] getEndpointKey() {
    return Arrays.copyOf(endpointKey, endpointKey.length);
  }

  public byte[] getProfile() {
    return Arrays.copyOf(profile, profile.length);
  }

  public String getAppToken() {
    return appToken;
  }

  public String getSdkToken() {
    return sdkToken;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
