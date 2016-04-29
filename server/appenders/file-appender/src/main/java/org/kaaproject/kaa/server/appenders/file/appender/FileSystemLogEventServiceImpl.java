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

package org.kaaproject.kaa.server.appenders.file.appender;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Execute;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.file.config.gen.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemLogEventServiceImpl implements FileSystemLogEventService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogEventServiceImpl.class);
    private static final String DEFAULT_SYSTEM_USER = "kaa";
    
    private static final String CREATE_USER = "create_user";
    private static final String CREATE_ROOT_LOG_DIR = "create_root_log_dir";

    @Override
    public void createDirectory(String path) {
        LOG.debug("Starting create directory with path: {}", path);
        File directory = new File(path);
        if (!directory.exists()) {
            boolean result = directory.mkdir();
            LOG.debug("Creating directory result: {}", result);
        } else {
            LOG.debug("Directory/File with path: {} already exist", path);
        }
    }

    @Override
    public void createUserAndGroup(LogAppenderDto appender, FileConfig config, String path) {
        LOG.debug("Starting create user and group for application with id: {}", appender.getApplicationId());
        String userName = "kaa_log_user_" + appender.getApplicationToken();
        String groupName = "kaa_log_group_" + appender.getApplicationToken();
        String publicKey = config.getPublicKey();

        File tmpKeyFile = null;
        File createUserScript = null;
        try {
            tmpKeyFile = File.createTempFile("app_" + appender.getApplicationToken(), "_pub.key");
            PrintWriter out = new PrintWriter(tmpKeyFile);
            out.write(publicKey);
            out.close();
            
            createUserScript = prepareScriptFile(CREATE_USER);
            
            executeCommand(null, "sudo", createUserScript.getAbsolutePath(),
                    userName, groupName, path, tmpKeyFile.getAbsolutePath());
            
        } catch (IOException e) {
            LOG.error("Unexpected exception occurred while creating user", e);
        } finally {
            if (tmpKeyFile != null) {
                tmpKeyFile.delete();
            }
            if (createUserScript != null) {
                createUserScript.delete();
            }
        }
    }

    @Override
    public void createRootLogDirCommand(String logsRootPath) {
        LOG.info("Create root log directory...");
        File createRootLogDirScript = null;
        try {
            createRootLogDirScript = prepareScriptFile(CREATE_ROOT_LOG_DIR);
            executeCommand(null, "sudo", createRootLogDirScript.getAbsolutePath(),
                    logsRootPath, DEFAULT_SYSTEM_USER);
        } catch (IOException e) {
            LOG.error("Can't create root log dir: " + logsRootPath, e);
        } finally {
            if (createRootLogDirScript != null) {
                createRootLogDirScript.delete();
            }
        }
    }

    @Override
    public void removeAll(String path) {
        LOG.debug("Starting delete directory with path: {}", path);
        File directory = new File(path);
        try {
            FileUtils.deleteDirectory(directory);
            LOG.debug("Directory was successfully deleted");
        } catch (IOException e) {
            LOG.error("Unable to delete directory with path: {}, exception catched: {}", path, e);
        }
    }
    
    private static void executeCommand(File workingDir, String... command) throws IOException {
        Execute exec = new Execute();
        if (workingDir == null) {
            String homeDir = System.getProperty("user.home");
            if (homeDir != null) {
                workingDir = new File(homeDir); 
            }
        }
        if (workingDir != null) {
            exec.setWorkingDirectory(workingDir);
        }
        exec.setCommandline(command);
        exec.execute();
        if (exec.isFailure()) {
            throw new RuntimeException("Process returned bad exit value: " + exec.getExitValue());
        }
    }
    
    private File prepareScriptFile(String resourceName) throws IOException {
        
        File scriptFile = File.createTempFile(resourceName, ".sh");
        byte[] data = org.kaaproject.kaa.server.common.utils.FileUtils.readResourceBytes(resourceName);
        FileUtils.writeByteArrayToFile(scriptFile, data);
        
        executeCommand(null, "sudo", "chmod", "+x", scriptFile.getAbsolutePath());
        
        return scriptFile;
    }
    
    
}
