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

package org.kaaproject.kaa.server.operations.service.security;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.PostConstruct;

import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.server.common.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The implementation of {#link KeyStoreService KeyStoreService} based on file.
 * 
 * @author ashvayka
 */
@Service
public class OperationsFileKeyStoreService implements KeyStoreService {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OperationsFileKeyStoreService.class);

    /** The private key location. */
    @Value("#{properties[operations_keys_private_key_location]}")
    private String privateKeyLocation;
    
    /** The public key location. */
    @Value("#{properties[operations_keys_public_key_location]}")
    private String publicKeyLocation;

    /** The private key. */
    private PrivateKey privateKey;
    
    /** The public key. */
    private PublicKey publicKey;

    /**
     * Instantiates a new file key store service.
     */
    public OperationsFileKeyStoreService() {
        super();
    }

    /**
     * Load keys.
     */
    @PostConstruct
    public void loadKeys() {
        LOG.debug("Loading keys..");
        String privateKeyFullPath = Environment.getServerHomeDir() + "/" + this.privateKeyLocation;
        String publicKeyFullPath = Environment.getServerHomeDir() + "/" + this.publicKeyLocation;
        LOG.debug("Lookup private key: {}", privateKeyFullPath);
        LOG.debug("Lookup public key: {}", publicKeyFullPath);
        File f = new File(privateKeyFullPath);
        if (f.exists()) {
            try {
                privateKey = KeyUtil.getPrivate(f);
            } catch (Exception e) {
                LOG.debug("Error loading Private Key", e);
                throw new RuntimeException(e); //NOSONAR
            }
        }
        f = new File(publicKeyFullPath);
        if (f.exists()) {
            try {
                publicKey = KeyUtil.getPublic(f);
            } catch (Exception e) {
                LOG.debug("Error loading Public Key", e);
                throw new RuntimeException(e); //NOSONAR
            }
        }
        if (privateKey == null || publicKey == null) {
            KeyPair keyPair = generateKeyPair(privateKeyFullPath, publicKeyFullPath);
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.security.KeyStoreService#getPrivateKey()
     */
    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.security.KeyStoreService#getPublicKey()
     */
    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getPrivateKeyLocation() {
        return privateKeyLocation;
    }

    public String getPublicKeyLocation() {
        return publicKeyLocation;
    }

    public void setPrivateKeyLocation(String privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public void setPublicKeyLocation(String publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    /**
     * Generate key pair.
     *
     * @param privateKeyLocation the private key location
     * @param publicKeyLocation the public key location
     * @return the key pair
     */
    private KeyPair generateKeyPair(String privateKeyLocation, String publicKeyLocation) {
        LOG.debug("Generating Key pair");
        KeyPair kp = KeyUtil.generateKeyPair(privateKeyLocation, publicKeyLocation);
        LOG.debug("Private key location: {}", privateKeyLocation);
        LOG.debug("Public key location: {}", publicKeyLocation);
        return kp;
    }

}
