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
package org.kaaproject.kaa.sandbox;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SandboxMain {
    
    private static final Logger logger = LoggerFactory.getLogger(SandboxMain.class);

    //1 Base path 
    //2 Sandbox image type (vbox)
    //3 Sandbox os type (ubuntu)
    //4 Sandbox base image url (//)
    //5 Box name
    //6 Sandbox output file (//)
    //7 SSH forward port
    //8 Web admin forward port
    
    public static void main(String[] args) throws Exception {
        File basePath = null;
        BoxType boxType = null;
        OsType osType = null;
        URL baseImageUrl = null;
        String boxName = null;
        File imageOutputFile = null;
        int sshForwardPort = -1;
        int webAdminForwardPort = -1;
        if (args.length < 8) {
            logger.error("Insufficient arguments!");
            throw new RuntimeException("Insufficient arguments!");
        }
        try {
            basePath = new File(args[0]);
            if (!basePath.exists() || !basePath.isDirectory()) {
                logger.error("Base path [{}] doesn't exists or not a directory!", args[0]);
                throw new RuntimeException("Invalid base path!");
            }
            boxType = BoxType.valueOf(args[1].toUpperCase());
            osType = OsType.valueOf(args[2].toUpperCase());
            try {
                baseImageUrl = new URL(args[3]);
            }
            catch (MalformedURLException e) {
                logger.error("Invalid base image url [{}]!", args[3]);
                throw new RuntimeException("Invalid base image url!");
            }
            boxName = args[4];

            imageOutputFile = new File(args[5]);
            
            try {
                sshForwardPort = Integer.valueOf(args[6]);
            }
            catch (NumberFormatException nfe) {
                logger.error("Invalid ssh forward port [{}]!", args[6]);
                throw new RuntimeException("Invalid ssh forward port!");
            }
            
            try {
                webAdminForwardPort = Integer.valueOf(args[7]);
            }
            catch (NumberFormatException nfe) {
                logger.error("Invalid web admin forward port [{}]!", args[7]);
                throw new RuntimeException("Invalid web admin forward port!");
            }
        }
        catch (Exception e) {
            logger.error("Unable to parse arguments!", e);
            throw new RuntimeException("Unable to parse arguments!");
        }
        logger.info("Going to build sandbox image with the following parameters:");
        logger.info("Base path: [{}]", basePath);
        logger.info("Box type: [{}]", boxType);
        logger.info("Os type: [{}]", osType);
        logger.info("Base image url: [{}]", baseImageUrl.toString());
        logger.info("Box name: [{}]", boxName);
        logger.info("Image output file: [{}]", imageOutputFile.getAbsolutePath());
        logger.info("Ssh forward port: [{}]", sshForwardPort);
        logger.info("Web admin forward port: [{}]", webAdminForwardPort);
        SandboxMain builder = new SandboxMain(basePath, boxType, osType, baseImageUrl, boxName, imageOutputFile, sshForwardPort, webAdminForwardPort);
        try {
            builder.buildSandboxImage();
        } catch (Exception e) {
            logger.error("Unable to build sandbox image!", e);
            throw new RuntimeException("Unable to build sandbox image!");
        }
    }
    
    private final File basePath;
    private final BoxType boxType;
    private final OsType osType;
    private final URL baseImageUrl;
    private final String boxName;
    private final File imageOutputFile;
    private final int sshForwardPort;
    private final int webAdminForwardPort;
    
    SandboxMain (File basePath, 
            BoxType boxType, 
            OsType osType, 
            URL baseImageUrl, 
            String boxName,
            File imageOutputFile, 
            int sshForwardPort,
            int webAdminForwardPort) {
        this.basePath = basePath;
        this.boxType = boxType;
        this.osType = osType;
        this.baseImageUrl = baseImageUrl;
        this.boxName = boxName;
        this.imageOutputFile = imageOutputFile;
        this.sshForwardPort = sshForwardPort;
        this.webAdminForwardPort = webAdminForwardPort;
    }
    
    void buildSandboxImage() throws Exception {
        SandboxBuilder builder = 
                SandboxBuilderFactory.createSandboxBuilder(basePath, 
                        boxType, 
                        osType, 
                        baseImageUrl, 
                        boxName, 
                        imageOutputFile, 
                        sshForwardPort, 
                        webAdminForwardPort);
        builder.buildSandboxImage();
    }
    
    
}
