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

package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;

import java.util.Optional;

/**
 * The interface Endpoint specific configuration service.
 */
public interface EndpointSpecificConfigurationService {

  /**
   * Find currently active endpoint specific configuration by endpoint key hash.
   *
   * @param endpointKeyHash endpoint key hash
   * @return the endpoint specific configuration
   */
  Optional<EndpointSpecificConfigurationDto> findActiveConfigurationByEndpointKeyHash(byte[] endpointKeyHash);

  /**
   * Find endpoint specific configuration by endpoint key hash and configuration schema version.
   *
   * @param endpointKeyHash   endpoint key hash
   * @param confSchemaVersion configuration schema version
   * @return the endpoint specific configuration
   */
  Optional<EndpointSpecificConfigurationDto> findByEndpointKeyHashAndConfSchemaVersion(byte[] endpointKeyHash, Integer confSchemaVersion);

  /**
   * Find currently active endpoint specific configuration by endpoint profile.
   *
   * @param endpointProfileDto endpoint key hash
   * @return the endpoint specific configuration
   */
  Optional<EndpointSpecificConfigurationDto> findActiveConfigurationByEndpointProfile(EndpointProfileDto endpointProfileDto);

  /**
   * Delete currently active endpoint specific configuration by endpoint key hash.
   *
   * @param endpointKeyHash endpoint key hash
   * @return deleted endpoint specific configuration
   */
  Optional<EndpointSpecificConfigurationDto> deleteActiveConfigurationByEndpointKeyHash(byte[] endpointKeyHash);

  /**
   * Delete endpoint specific configuration by endpoint key hash and configuration schema version.
   *
   * @param endpointKeyHash   endpoint key hash
   * @param confSchemaVersion configuration schema version
   * @return deleted endpoint specific configuration
   */
  Optional<EndpointSpecificConfigurationDto> deleteByEndpointKeyHashAndConfSchemaVersion(byte[] endpointKeyHash, Integer confSchemaVersion);

  /**
   * Save endpoint specific configuration.
   *
   * @param configurationDto endpoint specific configuration
   * @return saved endpoint specific configuration
   */
  EndpointSpecificConfigurationDto save(EndpointSpecificConfigurationDto configurationDto);

}
