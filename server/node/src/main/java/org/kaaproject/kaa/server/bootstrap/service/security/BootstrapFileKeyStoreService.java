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

package org.kaaproject.kaa.server.bootstrap.service.security;

import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.server.common.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.PostConstruct;

/**
 * The Class BootstrapFileKeyStoreService.
 */
@Service
public class BootstrapFileKeyStoreService implements KeyStoreService {
  private static final Logger LOG = LoggerFactory.getLogger(BootstrapFileKeyStoreService.class);

  @Value("#{properties[bootstrap_keys_private_key_location]}")
  private String privateKeyLocation;

  @Value("#{properties[bootstrap_keys_public_key_location]}")
  private String publicKeyLocation;
  private PrivateKey privateKey;
  private PublicKey publicKey;

  /**
   * Instantiates a new file key store service.
   */
  public BootstrapFileKeyStoreService() {
    super();
  }

  /**
   * Load keys.
   */
  @PostConstruct
  public void loadKeys() {
    privateKeyLocation = Environment.getServerHomeDir() + File.separator + privateKeyLocation;
    publicKeyLocation = Environment.getServerHomeDir() + File.separator + publicKeyLocation;
    LOG.debug("Loading private key from {}; public key from {}",
        privateKeyLocation, publicKeyLocation);
    File file = new File(privateKeyLocation);
    if (file.exists()) {
      try {
        privateKey = KeyUtil.getPrivate(file);
      } catch (Exception ex) {
        LOG.debug("Error loading private key", ex);
        throw new RuntimeException(ex); //NOSONAR
      }
    }
    file = new File(publicKeyLocation);
    if (file.exists()) {
      try {
        publicKey = KeyUtil.getPublic(file);
      } catch (Exception ex) {
        LOG.debug("Error loading public key", ex);
        throw new RuntimeException(ex); //NOSONAR
      }
    }
    if (privateKey == null || publicKey == null) {
      KeyPair keyPair = generateKeyPair(privateKeyLocation, publicKeyLocation);
      privateKey = keyPair.getPrivate();
      publicKey = keyPair.getPublic();
    }
  }

  /* (non-Javadoc)
   * @see org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService#getPrivateKey()
   */
  @Override
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  /* (non-Javadoc)
   * @see org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService#getPublicKey()
   */
  @Override
  public PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * Generate key pair.
   *
   * @param privateKeyLocation the private key location
   * @param publicKeyLocation  the public key location
   * @return the key pair
   */
  private KeyPair generateKeyPair(String privateKeyLocation, String publicKeyLocation) {
    LOG.debug("Generating key pair (private at {}; public at {})",
        privateKeyLocation, publicKeyLocation);
    return KeyUtil.generateKeyPair(privateKeyLocation, publicKeyLocation);
  }

  /**
   * PrivateKeyLocation getter.
   *
   * @return String the privateKeyLocation
   */
  public String getPrivateKeyLocation() {
    return privateKeyLocation;
  }

  /**
   * PrivateKeyLocation setter.
   *
   * @param privateKeyLocation String the privateKeyLocation to set
   */
  public void setPrivateKeyLocation(String privateKeyLocation) {
    this.privateKeyLocation = privateKeyLocation;
  }

  /**
   * PublicKeyLocation getter.
   *
   * @return String the publicKeyLocation
   */
  public String getPublicKeyLocation() {
    return publicKeyLocation;
  }

  /**
   * PublicKeyLocation setter.
   *
   * @param publicKeyLocation String the publicKeyLocation to set
   */
  public void setPublicKeyLocation(String publicKeyLocation) {
    this.publicKeyLocation = publicKeyLocation;
  }
}
