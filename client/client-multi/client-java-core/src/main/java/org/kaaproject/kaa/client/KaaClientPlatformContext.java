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

package org.kaaproject.kaa.client;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.client.util.Base64;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Represents platform specific context for Kaa client initialization.
 *
 * @author Andrew Shvayka
 */
public interface KaaClientPlatformContext {

  /**
   * Returns platform SDK properties.
   *
   * @return client SDK properties
   */
  KaaClientProperties getProperties();

  /**
   * Returns platform dependent implementation of http client.
   *
   * @param url             the url
   * @param privateKey      the private key
   * @param publicKey       the public key
   * @param remotePublicKey the remote public key
   * @return platform dependent implementation of http client
   */
  AbstractHttpClient createHttpClient(String url, PrivateKey privateKey, PublicKey publicKey,
                                      PublicKey remotePublicKey);

  /**
   * Returns platform dependent implementation of {@link PersistentStorage
   * persistent storage}.
   *
   * @return implementation of {@link PersistentStorage persistent storage}
   */
  PersistentStorage createPersistentStorage();

  /**
   * Returns platform dependent implementation of {@link Base64 base64}
   * algorithm.
   *
   * @return implementation of {@link Base64 base64} algorithm
   */
  Base64 getBase64();

  /**
   * Returns platform dependent implementation of {@link ConnectivityChecker}.
   *
   * @return implementation of {@link ConnectivityChecker}
   */
  ConnectivityChecker createConnectivityChecker();

  /**
   * Returns SDK thread execution context.
   *
   * @return SDK thread execution context
   */
  ExecutorContext getExecutorContext();

  /**
   * Determines if client state should be checked.
   *
   * @return true if client state should be checked
   */
  boolean needToCheckClientState();
}
