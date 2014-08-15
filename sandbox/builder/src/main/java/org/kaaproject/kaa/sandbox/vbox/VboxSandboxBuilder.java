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
package org.kaaproject.kaa.sandbox.vbox;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.sandbox.AbstractSandboxBuilder;
import org.kaaproject.kaa.sandbox.OsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VboxSandboxBuilder extends AbstractSandboxBuilder {

    private static final Logger logger = LoggerFactory.getLogger(VboxSandboxBuilder.class);
    
    private static final Pattern vmNamePattern = Pattern.compile("^\"(.+?)\" \\{(.+?)\\}$");
    private static final Pattern sharedFolderPattern = Pattern.compile("^SharedFolderNameMachineMapping\\d+=\"(.+?)\"$");
    private static final Pattern vmStatePattern = Pattern.compile("^VMState=\"(.+?)\"$");
    private static final Pattern portForwardingPattern = Pattern.compile("^Forwarding.+?=\"(.+?),.+?,.*?,(.+?),.*?,(.+?)\"$");
    
    private String vboxManagePath;
    
    public VboxSandboxBuilder(File basePath,
            OsType osType,
            URL baseImageUrl,
            String boxName,
            File imageOutputFile,
            int sshForwardPort,
            int webAdminForwardPort) throws Exception {
        super(basePath, osType, baseImageUrl, boxName, imageOutputFile, sshForwardPort, webAdminForwardPort);
        detectVboxManageCommand();
    }
    
    @Override
    protected void loadBoxImpl() throws Exception {
        logger.info("Importing box '{}' from file '{}'...", boxName, baseImageFile.getAbsolutePath());
        //vboxManage("import", baseImageFile.getAbsolutePath(), "--dry-run");
        vboxManage("import", baseImageFile.getAbsolutePath(), "--vsys", "0", "--vmname", boxName);
        logger.info("Box '{}' was imported.", boxName);
    }

    @Override
    protected void prepareBoxImpl() throws Exception {
        deleteForwardedPorts();
        forwardPort("ssh", 22, sshForwardPort);
        forwardPort("web-gui", DEFAULT_WEB_ADMIN_PORT, webAdminForwardPort);
    }

    @Override
    protected void startBoxImpl() throws Exception {
        logger.info("Starting box '{}'", boxName);
        vboxManage("startvm", boxName, "--type", "headless");
        Thread.sleep(40000);
    }

    @Override
    protected void provisionBoxImpl() throws Exception {
    }

    @Override
    protected void unprovisionBoxImpl() throws Exception {
    }

    @Override
    protected void stopBoxImpl() throws Exception {
        logger.info("Performing graceful shutdown...");
        boolean skipShutdown = false;
        try {
            executeSudoSsh("shutdown -P now");
        }
        catch (Exception e) {
            logger.error("Unable to connect to box! Cause: '{}'", e.getMessage());
            logger.info("Graceful shutdown is impossible. Skipping.");
            skipShutdown = true;
        }
        boolean poweredOff = false;
        if (!skipShutdown) {
            int retry = 0;
            while (!(poweredOff = isPoweredOff()) && retry < 20) {
                Thread.sleep(5000);
                retry++;
            }
        }
        if (poweredOff) {
            logger.info("Graceful shutdown complete.");
        }
        else {
            logger.info("Machine is still running. Halting...");
            vboxManage("controlvm", boxName, "poweroff");
        }
    }

    @Override
    protected void cleanupBoxImpl() throws Exception {
        deleteForwardedPorts();
        forwardPort("ssh", 22, DEFAULT_SSH_FORWARD_PORT);
        forwardPort("web-gui", DEFAULT_WEB_ADMIN_PORT, DEFAULT_WEB_ADMIN_PORT);
    }

    @Override
    protected void exportBoxImpl() throws Exception {
        logger.info("Exporting box '{}' to file '{}'", boxName, imageOutputFile.getAbsolutePath());
        vboxManage("export", boxName, "--output", imageOutputFile.getAbsolutePath());
        logger.info("Box '{}' successfuly exported", boxName);
    }

    @Override
    protected void unloadBoxImpl() throws Exception {
        logger.info("Destroying box '{}'...", boxName);
        vboxManage("unregistervm", boxName, "--delete");
        logger.info("Box '{}' was destroyed.", boxName);
    }

    @Override
    protected boolean boxLoaded() throws Exception {
        String info = vboxManage(false, "list", "vms");
        for (String line : info.split("\n")) {
            Matcher m = vmNamePattern.matcher(line.trim());
            if (m.matches() && m.group(1).equals(boxName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean boxRunning() throws Exception {
        return getVmState().equals("running");
    }
    
    private void forwardPort(String name, int guestPort, int hostPort) throws Exception {
        logger.info("Forwarding '"+name+"' port '{}'-->'{}' ...", guestPort, hostPort);
        vboxManage("modifyvm", boxName, "--natpf1", name+",tcp,"+DEFAULT_HOST+","+hostPort+",,"+guestPort);
    }
    
    private void deleteForwardedPorts() throws Exception{
        logger.info("Deleting ports forwarding...");
        String info = vboxManage(false, "showvminfo", boxName, "--machinereadable");
        for (String line : info.split("\n")) {
            Matcher m = portForwardingPattern.matcher(line.trim());
            if (m.matches()) {
                String name = m.group(1);
                vboxManage("modifyvm", boxName, "--natpf1", "delete", name);
            }
        }
    }
    
    private boolean isPoweredOff() throws Exception {
        return getVmState().equals("poweroff");
    }

    private String getVmState() throws Exception {
        String info = vboxManage(false, "showvminfo", boxName, "--machinereadable");
        for (String line : info.split("\n")) {
            Matcher m = vmStatePattern.matcher(line.trim());
            if (m.matches()) {
                return m.group(1);
            }
        }
        return "unknown";
    }
    
    private void detectVboxManageCommand() throws Exception {
        vboxManagePath = "VBoxManage";
        if (isWin32()) {
            logger.debug("Windows. Trying [VBOX_INSTALL_PATH, VBOX_MSI_INSTALL_PATH] for VBoxManage");
            if (System.getenv().containsKey("VBOX_INSTALL_PATH") ||
                    System.getenv().containsKey("VBOX_MSI_INSTALL_PATH")) {
                String path = System.getenv().get("VBOX_INSTALL_PATH");
                logger.debug("VBOX_INSTALL_PATH value: {}", path);
                if (path == null || path.length() == 0) {
                    path = System.getenv().get("VBOX_MSI_INSTALL_PATH");
                    logger.debug("VBOX_MSI_INSTALL_PATH value: {}", path);
                }
                for (String single : path.split(";")) {
                    if (!single.endsWith("\\")) {
                        single += "\\";
                    }
                    String vboxmanage = single + "VBoxManage.exe";
                    File vboxmanageFile = new File(vboxmanage);
                    if (vboxmanageFile.exists() && vboxmanageFile.isFile()) {
                        vboxManagePath = vboxmanage;
                        break;
                    }
                            
                }
            }
        }
        logger.info("VBoxManage path: {}", vboxManagePath);
    }
    
    private String vboxManage(String... command) throws Exception {
        return vboxManage(true, command);
    }

    private String vboxManage(boolean logOutput, String... command) throws Exception {
        List<String> commandList = new ArrayList<>();
        commandList.add(vboxManagePath);
        commandList.addAll(Arrays.asList(command));
        String commandString = "";
        for (String _command : commandList) {
            if (StringUtils.isNotBlank(commandString)) {
                commandString += " ";
            }
            commandString += _command;
        }
        logger.info(commandString);
        return execute(logOutput, commandList).trim();
    }


}
