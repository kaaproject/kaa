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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class FileKeyStoreServiceTest {

    private static final String SERVER_HOME_DIR = "/tmp";
    private static final String PRIVATE_KEY_LOCATION = "privateKey";
    private static final String PUBLIC_KEY_LOCATION = "publicKey";
    private static final String PRIVATE_KEY_PATH = SERVER_HOME_DIR + "/" + PRIVATE_KEY_LOCATION;
    private static final String PUBLIC_KEY_PATH = SERVER_HOME_DIR + "/" + PUBLIC_KEY_LOCATION;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("server_home_dir", SERVER_HOME_DIR);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.security.FileKeyStoreService#FileKeyStoreService()}.
     */
    @Test
    public void testFileKeyStoreService() {
        FileKeyStoreService ks = new FileKeyStoreService();
        assertNotNull(ks);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.security.FileKeyStoreService#loadKeys()}.
     */
    @Test
    public void testLoadKeys() {
        FileKeyStoreService ks = new FileKeyStoreService();
        assertNotNull(ks);
        ks.setPrivateKeyLocation(PRIVATE_KEY_LOCATION);
        ks.setPublicKeyLocation(PUBLIC_KEY_LOCATION);
        //Test file generation
        File pri = new File(PRIVATE_KEY_PATH);
        if (pri.exists()) {
            pri.delete();
        }
        File pub = new File(PUBLIC_KEY_PATH);
        if (pub.exists()) {
            pub.delete();
        }
        
        ks.loadKeys();
        
        if (!pri.exists() || !pri.canRead()) {
            fail("Private key file create failed.");
        }
        if (!pub.exists() || !pub.canRead()) {
            fail("Public key file create failed.");
        }
        
        //Test load incorrect files.
        
        if (pub.exists()) {
            pub.delete();
            FileWriter pubfw;
            try {
                pubfw = new FileWriter(pub);
                pubfw.write("ascasdcasdcasdc");
                pubfw.close();
            } catch (IOException e) {
                fail(e.toString());
            }
        }
        try {
            ks.loadKeys();
        } catch (Exception e) {
            assertNotNull(e.toString(),e);
        }
        
        if (pri.exists()) {
            pri.delete();
            FileWriter prifw;
            try {
                prifw = new FileWriter(pri);
                prifw.write("ascasdcasdcasdc");
                prifw.close();
            } catch (IOException e) {
                fail(e.toString());
            }
        }
        try {
            ks.loadKeys();
        } catch (Exception e) {
            assertNotNull(e.toString(),e);
        }
        
        
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.security.FileKeyStoreService#getPrivateKey()}.
     */
    @Test
    public void testGetPrivateKey() {
        File pri = new File(PRIVATE_KEY_PATH);
        if (pri.exists()) {
            pri.delete();
        }
        File pub = new File(PUBLIC_KEY_PATH);
        if (pub.exists()) {
            pub.delete();
        }
        
        FileKeyStoreService ks = new FileKeyStoreService();
        assertNotNull(ks);
        ks.setPrivateKeyLocation(PRIVATE_KEY_LOCATION);
        ks.setPublicKeyLocation(PUBLIC_KEY_LOCATION);
        ks.loadKeys();
        assertNotNull(ks.getPrivateKey());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.security.FileKeyStoreService#getPublicKey()}.
     */
    @Test
    public void testGetPublicKey() {
        File pri = new File(PRIVATE_KEY_PATH);
        if (pri.exists()) {
            pri.delete();
        }
        File pub = new File(PUBLIC_KEY_PATH);
        if (pub.exists()) {
            pub.delete();
        }
        FileKeyStoreService ks = new FileKeyStoreService();
        assertNotNull(ks);
        ks.setPrivateKeyLocation(PRIVATE_KEY_LOCATION);
        ks.setPublicKeyLocation(PUBLIC_KEY_LOCATION);
        ks.loadKeys();
        assertNotNull(ks.getPublicKey());
    }

}
