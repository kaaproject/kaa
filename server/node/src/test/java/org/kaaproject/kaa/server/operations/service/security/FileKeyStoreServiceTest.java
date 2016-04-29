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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.Environment;
import org.kaaproject.kaa.server.operations.service.security.OperationsFileKeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileKeyStoreServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(FileKeyStoreServiceTest.class);
        
    @Test
    public void testKeysCreation() throws IOException{
        OperationsFileKeyStoreService keyStoreService = new OperationsFileKeyStoreService();
        String randomFolderName = "randomFolder" + new Random().nextInt(100000);
        LOG.info("Creating random folder: {}", randomFolderName);
        keyStoreService.setPrivateKeyLocation(randomFolderName + File.separator + "private.key");
        keyStoreService.setPublicKeyLocation(randomFolderName + File.separator + "public.key");
        try{
            keyStoreService.loadKeys();
            Assert.assertNotNull(keyStoreService.getPrivateKey());
            Assert.assertNotNull(keyStoreService.getPublicKey());
        }finally{
            removeFolder(Environment.getServerHomeDir() + File.separator + randomFolderName);
        }
    }
    
    @Test(expected=RuntimeException.class)
    public void testNotValidPrivateKey() throws IOException {
        OperationsFileKeyStoreService keyStoreService = new OperationsFileKeyStoreService();
        String randomFolderName = "randomFolder" + new Random().nextInt(100000);
        LOG.info("Creating random folder: {}", randomFolderName);
        keyStoreService.setPrivateKeyLocation(randomFolderName + File.separator + "private.key");
        keyStoreService.setPublicKeyLocation(randomFolderName + File.separator + "public.key");
        try {
            keyStoreService.loadKeys();
            Assert.assertNotNull(keyStoreService.getPrivateKey());
            Assert.assertNotNull(keyStoreService.getPublicKey());

            Files.delete(Paths.get(Environment.getServerHomeDir(), keyStoreService.getPrivateKeyLocation()));
            Files.createFile(Paths.get(Environment.getServerHomeDir(), keyStoreService.getPrivateKeyLocation()));
            keyStoreService.loadKeys();

        } finally {
            removeFolder(Environment.getServerHomeDir() + File.separator + randomFolderName);
        }
    }
    
    @Test(expected=RuntimeException.class)
    public void testNotValidPublicKey() throws IOException {
        OperationsFileKeyStoreService keyStoreService = new OperationsFileKeyStoreService();
        String randomFolderName = "randomFolder" + new Random().nextInt(100000);
        LOG.info("Creating random folder: {}", randomFolderName);
        keyStoreService.setPrivateKeyLocation(randomFolderName + File.separator + "private.key");
        keyStoreService.setPublicKeyLocation(randomFolderName + File.separator + "public.key");
        try {
            keyStoreService.loadKeys();
            Assert.assertNotNull(keyStoreService.getPrivateKey());
            Assert.assertNotNull(keyStoreService.getPublicKey());

            Files.delete(Paths.get(Environment.getServerHomeDir(), keyStoreService.getPrivateKeyLocation()));
            Files.createFile(Paths.get(Environment.getServerHomeDir(), keyStoreService.getPrivateKeyLocation()));
            keyStoreService.loadKeys();

        } finally {
            removeFolder(Environment.getServerHomeDir() + File.separator + randomFolderName);
        }
    }    

    protected void removeFolder(String randomFolderName) throws IOException {
        LOG.info("Deleting random folder: {}", Paths.get(randomFolderName).toAbsolutePath().toString());
        Files.walkFileTree(Paths.get(randomFolderName), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }   
    
}
