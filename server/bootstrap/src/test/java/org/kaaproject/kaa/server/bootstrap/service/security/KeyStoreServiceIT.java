/*
 * Copyright 2014 CyberVision, Inc.
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
 * 
 */
package org.kaaproject.kaa.server.bootstrap.service.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Andrey Panasenko
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/bootstrapTestContext.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class KeyStoreServiceIT {
    static {
        System.setProperty("server_home_dir", ".");
    }
    @Autowired
    public KeyStoreService keyStore;
    
    
    @After
    public void After() {
        FileKeyStoreService fs = (FileKeyStoreService)keyStore;
        File privFile = new File(fs.getPrivateKeyLocation());
        if (privFile.exists()) {
            privFile.delete();
        }
        File pubFile = new File(fs.getPublicKeyLocation());
        if (pubFile.exists()) {
            pubFile.delete();
        }
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.security.FileKeyStoreService#getPrivateKey()()}.
     */
    @Test
    public void testGetPrivateKey() {
        assertNotNull("FileKeyStore service created sucessfully",keyStore);
        PrivateKey privateKey = keyStore.getPrivateKey();
        assertNotNull("PrivateKey generated",privateKey);
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.security.FileKeyStoreService#getPublicKey()()}.
     */
    @Test
    public void testGetPublicKey() {
        assertNotNull("FileKeyStore service created sucessfully",keyStore);
        PublicKey publicKey = keyStore.getPublicKey();
        assertNotNull("PrivateKey generated",publicKey);
    }
    
    @Test
    public void testGetLoadKeysPrivateFailed() {
        assertNotNull("FileKeyStore service created sucessfully",keyStore);
        FileKeyStoreService fs = (FileKeyStoreService)keyStore;
        String privFileName = fs.getPrivateKeyLocation();
        File privFile = new File(privFileName);
        if (privFile.exists() && privFile.canWrite()) {
            privFile.delete();
            try {
                if (privFile.createNewFile()) {
                    fs.loadKeys();
                    fail("testGetLoadKeysPrivateFailed failed, Privatekey file was removed but stil can read key from it");
                }
            } catch (IOException e) {
                fail("Failed test testGetLoadKeysPrivateFailed "+e.toString());
            } catch (RuntimeException re) {
                
            }
        }
    }
    
    @Test
    public void testGetLoadKeysPublicFailed() {
        assertNotNull("FileKeyStore service created sucessfully",keyStore);
        FileKeyStoreService fs = (FileKeyStoreService)keyStore;
        String pubFileName = fs.getPublicKeyLocation();
        File pubFile = new File(pubFileName);
        if (pubFile.exists() && pubFile.canWrite()) {
            pubFile.delete();
            try {
                if (pubFile.createNewFile()) {
                    fs.loadKeys();
                    fail("testGetLoadKeysPublicFailed failed, Publickey file was removed but stil can read key from it");
                }
            } catch (IOException e) {
                fail("Failed test testGetLoadKeysPublicFailed "+e.toString());
            } catch (RuntimeException re) {
                
            }
        }
    }
}
