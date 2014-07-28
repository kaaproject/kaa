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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.kaaproject.kaa.sandbox.admin.AdminClient;
import org.kaaproject.kaa.sandbox.demo.AbstractDemoBuilder;
import org.kaaproject.kaa.sandbox.demo.DemoBuilder;
import org.kaaproject.kaa.sandbox.demo.DemoBuildersRegistry;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.demo.projects.ProjectsConfig;
import org.kaaproject.kaa.sandbox.ssh.SandboxSshExec;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSandboxBuilder implements SandboxBuilder, SandboxConstants {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSandboxBuilder.class);
    
    protected final File basePath;
    protected final OsType osType;
    protected final URL baseImageUrl;
    protected final String boxName;
    protected final File imageOutputFile;
    protected final int sshForwardPort;
    protected final int webAdminForwardPort;
    
    protected File distroPath;
    protected File demoProjectsPath;
    protected File baseImageFile;
    
    private CommandsResource commandsResource = new CommandsResource();
    private SandboxProject sandboxProject = new SandboxProject();
    
    public AbstractSandboxBuilder(File basePath,
            OsType osType,
            URL baseImageUrl,
            String boxName,
            File imageOutputFile,
            int sshForwardPort,
            int webAdminForwardPort) {
     
        this.basePath = basePath;
        this.osType = osType;
        this.baseImageUrl = baseImageUrl;
        this.boxName = boxName;
        this.imageOutputFile = imageOutputFile;
        this.sshForwardPort = sshForwardPort;
        this.webAdminForwardPort = webAdminForwardPort;
        this.distroPath = new File(basePath, "distro");
        this.demoProjectsPath = new File(basePath, "demo_projects");
    }
    
    @Override
    public void buildSandboxImage() throws Exception {
        if (boxLoaded()) {
            logger.info("Box with name '{}' already exists. Going to unload old instance ...", boxName);
            if (boxRunning()) {
                stopBox();
            }
            unloadBox();
        }
        loadBox();
        try {
            prepareBox();
            startBox();
            provisionBox();
            schedulePackagesInstall();
            scheduleServicesStart();
            logger.info("Executing remote ssh commands...");
            executeScheduledSshCommands();
            logger.info("Remote ssh commands execution is completed.");
            logger.info("Sleeping 20 sec.");
            Thread.sleep(20000);
            initBoxData();
            logger.info("Sleeping 5 sec.");
            Thread.sleep(5000);
            unprovisionBox();
            logger.info("Executing remote ssh commands...");
            executeScheduledSshCommands();
            logger.info("Remote ssh commands execution is completed.");
            stopBox();
            cleanupBox();
            exportBox();
        }
        finally {
            if (boxLoaded()) {
                if (boxRunning()) {
                    stopBox();
                }
                unloadBox();
            }
        }
    }
    
    private void loadBox() throws Exception {
        logger.info("Loading box '{}' ...", boxName);
        String fileName = FilenameUtils.getName(baseImageUrl.toString());
        baseImageFile = new File(basePath, fileName);
        logger.info("Downloading base image to file '{}'...", baseImageFile.getAbsolutePath());
        if (baseImageFile.exists()) {
            baseImageFile.delete();
        }
        
        downloadFile(baseImageUrl, baseImageFile);

        if (baseImageFile.exists() && baseImageFile.isFile()) {
            logger.info("Downloaded base image to file '{}'.", baseImageFile.getAbsolutePath());
        }
        else {
            logger.error("Unable to download image file to '{}'", baseImageFile.getAbsolutePath());
            throw new RuntimeException("Unable to download image file!");
        }
        loadBoxImpl();
        
        baseImageFile.delete();
        
        logger.info("Box '{}' is loaded.", boxName);
    }
    
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 64;
    
    private void downloadFile(URL sourceUrl, File targetFile) throws Exception {
        CloseableHttpClient httpClient = (CloseableHttpClient)HttpClients.createSystem();
        CloseableHttpResponse response = null;
        try {
            HttpContext context = HttpClientContext.create();
            HttpGet httpGet = new HttpGet(sourceUrl.toURI());
            response = httpClient.execute(httpGet, context);
            HttpEntity entity = response.getEntity();
            long length = entity.getContentLength();
            
            InputStream in = new BufferedInputStream(entity.getContent());  
            OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
            copyLarge(in,out, new byte[DEFAULT_BUFFER_SIZE], length);  
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        finally {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpClient);
        }
    }
    
    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer, long length)
            throws IOException {
        long count = 0;
        int n = 0;
        logger.info("0%");
        int loggedPercents = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            int percentsComplete = (int)((double)count/(double)length*100f);
            if (percentsComplete-loggedPercents>=10) {
                logger.info(percentsComplete+"%");
                loggedPercents = percentsComplete;
            }
        }
        return count;
    }

    private void prepareBox() throws Exception {
        logger.info("Preparing box '{}' ...", boxName);
        prepareBoxImpl();
        logger.info("Box '{}' is prepared.", boxName);
    }

    private void startBox() throws Exception {
        logger.info("Starting box '{}' ...", boxName);
        startBoxImpl();
        logger.info("Box '{}' is started.", boxName);
    }

    private void provisionBox() throws Exception {
        provisionBoxImpl();
    }

    private void unprovisionBox() throws Exception {
        unprovisionBoxImpl();
    }
    
    private void stopBox() throws Exception {
        logger.info("Stopping box '{}' ...", boxName);
        stopBoxImpl();
        logger.info("Box '{}' is stopped.", boxName);
    }
    
    private void cleanupBox() throws Exception {
        logger.info("Cleaning box '{}' ...", boxName);
        cleanupBoxImpl();
        logger.info("Box '{}' is cleaned.", boxName);
    }
    
    private void exportBox() throws Exception {
        logger.info("Exporting box '{}' ...", boxName);
        if (imageOutputFile.exists()) {
            if (!imageOutputFile.delete()) {
                logger.error("Unable to delete previous output image file '{}'", imageOutputFile.getAbsoluteFile());
                throw new RuntimeException("Failed to export sandbox image!");
            }
        }
        else if (!imageOutputFile.getParentFile().exists()){
            imageOutputFile.getParentFile().mkdirs();
        }
        exportBoxImpl();
        logger.info("Box '{}' was exported.", boxName);
    }
    
    private void unloadBox() throws Exception {
        logger.info("Unloading box '{}' ...", boxName);
        unloadBoxImpl();
        logger.info("Box '{}' was unloaded.", boxName);
    }

    protected abstract void loadBoxImpl() throws Exception;
    protected abstract void prepareBoxImpl() throws Exception;
    protected abstract void startBoxImpl() throws Exception;
    protected abstract void provisionBoxImpl() throws Exception;
    protected abstract void unprovisionBoxImpl() throws Exception;
    protected abstract void stopBoxImpl() throws Exception;
    protected abstract void cleanupBoxImpl() throws Exception;
    protected abstract void exportBoxImpl() throws Exception;
    protected abstract void unloadBoxImpl() throws Exception;

    protected abstract boolean boxLoaded() throws Exception;
    protected abstract boolean boxRunning() throws Exception;

    
    protected void schedulePackagesInstall() {
        for (KaaPackage kaaPackage : KaaPackage.values()) {
            String command = osType.getInstallPackageTemplate().
                    replaceAll(DISTRO_PATH_VAR, DISTRO_PATH).
                    replaceAll(PACKAGE_NAME_VAR, kaaPackage.getPackageName());
            scheduleSudoSshCommand(command);
        }
    }
    
    protected void scheduleServicesStart() {
        for (KaaPackage kaaPackage : KaaPackage.values()) {
            String command = osType.getStartServiceTemplate().
                    replaceAll(SERVICE_NAME_VAR, kaaPackage.getServiceName());
            scheduleSudoSshCommand(command);
        }
    }
    
    protected void initBoxData() throws Exception {
        AdminClient adminClient = new AdminClient(DEFAULT_HOST, webAdminForwardPort);
        List<DemoBuilder> demoBuilders = DemoBuildersRegistry.getRegisteredDemoBuilders();
        List<Project> projects = new ArrayList<>();
        for (DemoBuilder demoBuilder : demoBuilders) {
            demoBuilder.buildDemoApplication(adminClient);
            projects.add(demoBuilder.getProjectConfig());
        }
        
        File projectsXmlFile = new File(demoProjectsPath, DEMO_PROJECTS_XML);
        ProjectsConfig projectsConfig = new ProjectsConfig();
        projectsConfig.getProjects().addAll(projects);
        
        JAXBContext jc = JAXBContext.newInstance("org.kaaproject.kaa.sandbox.demo.projects");
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(projectsConfig, projectsXmlFile);
        
        String sandboxSplashFileTemplate = FileUtils.readResource(SANDBOX_SPLASH_PY_TEMPLATE);
        String sandboxSplashFileSource = AbstractDemoBuilder.updateCredentialsInfo(sandboxSplashFileTemplate);
        
        sandboxSplashFileSource = sandboxSplashFileSource.replaceAll(WEB_ADMIN_PORT_VAR, DEFAULT_WEB_ADMIN_PORT+"")
                                                         .replaceAll(SSH_FORWARD_PORT_VAR, DEFAULT_SSH_FORWARD_PORT+"");
        
        File sandboxSplashFile = new File(distroPath, SANDBOX_SPLASH_PY);
        FileOutputStream fos = new FileOutputStream(sandboxSplashFile);
        fos.write(sandboxSplashFileSource.getBytes());
        fos.close();
        executeSudoSsh("cp " + DISTRO_PATH+"/" + SANDBOX_SPLASH_PY + " " + SANDBOX_FOLDER + "/"+SANDBOX_SPLASH_PY);
        
        String changeKaaHostFileTemplate = FileUtils.readResource(CHANGE_KAA_HOST_TEMPLATE);
        String stopServices = "";
        String setNewHosts = "";
        String startServices = "";
        for (KaaPackage kaaPackage : KaaPackage.values()) {
            if (kaaPackage.getHostProperties() != null && 
                    kaaPackage.getHostProperties().length>0) {
                if (StringUtils.isNotBlank(stopServices)) {
                    stopServices += "\n";
                    startServices += "\n";
                }
                stopServices += osType.getStopServiceTemplate().
                        replaceAll(SERVICE_NAME_VAR, kaaPackage.getServiceName());
                startServices += osType.getStartServiceTemplate().
                        replaceAll(SERVICE_NAME_VAR, kaaPackage.getServiceName());
                
                for (String propertyName : kaaPackage.getHostProperties()) {
                    if (StringUtils.isNotBlank(setNewHosts)) {
                        setNewHosts += "\n";
                    }
                    setNewHosts += "setNewHost " + kaaPackage.getPropertiesFile() + " " + propertyName;
                }
            }
        }

        String changeKaaHostFileSource = changeKaaHostFileTemplate.replaceAll(STOP_SERVICES_VAR, stopServices)
                                                                  .replaceAll(SET_NEW_HOSTS, setNewHosts)
                                                                  .replaceAll(START_SERVICES_VAR, startServices);
        
        File changeKaaHostFile = new File(distroPath, CHANGE_KAA_HOST);
        fos = new FileOutputStream(changeKaaHostFile);
        fos.write(changeKaaHostFileSource.getBytes());
        fos.close();
        executeSudoSsh("cp " + DISTRO_PATH+"/" + CHANGE_KAA_HOST + " " + SANDBOX_FOLDER + "/"+CHANGE_KAA_HOST);
        executeSudoSsh("chmod +x " + SANDBOX_FOLDER + "/"+CHANGE_KAA_HOST);
        executeSudoSsh("cp -r " + DEMO_PROJECTS_PATH + " " + SANDBOX_FOLDER + "/");
        executeSudoSsh("cp -r " + SANDBOX_PATH+"/*" + " " + ADMIN_FOLDER + "/");
        executeSudoSsh("sed -i \"s/\\(tenant_developer_user=\\).*\\$/\\1"+AbstractDemoBuilder.tenantDeveloperUser+"/\" " + ADMIN_FOLDER +"/conf/sandbox-server.properties");
        executeSudoSsh("sed -i \"s/\\(tenant_developer_password=\\).*\\$/\\1"+AbstractDemoBuilder.tenantDeveloperPassword+"/\" " + ADMIN_FOLDER +"/conf/sandbox-server.properties");
    }
    
    protected boolean isWin32() {
        return System.getProperty("os.name").startsWith("Windows");
    }
    
    protected String execute(boolean logOutput, String... command) throws Exception {
        return execute(logOutput, Arrays.asList(command));
    }
    
    protected String execute(boolean logOutput, List<String> command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command).directory(basePath);
        Process p = pb.start();
        String result = handleStream(p.getInputStream(), logOutput);
        handleStream(p.getErrorStream(), true);
        p.waitFor();
        p.destroy();
        return result;
    }
    
    protected String handleStream(InputStream input) throws IOException {
        return handleStream(input, false);
    }

    protected String handleStream(InputStream input, boolean logOutput) throws IOException {
        String line;
        StringWriter output = new StringWriter();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        BufferedWriter writer = new BufferedWriter(output);
        while ((line = reader.readLine()) != null) {
            if (logOutput) {
                logger.info(line);
            }
            writer.write(line);
            writer.newLine();
        }
        input.close();
        writer.close();
        return output.toString();
    }

    protected String executeSudoSsh(String command) {
        SandboxSshExec sshExec = createSshExec();
        sshExec.setCommand("sudo " + command);
        sshExec.setOutputproperty("sshOutProp");
        sshExec.execute();
        return sandboxProject.getProperty("sshOutProp");
    }
    
    protected void scheduleSudoSshCommand(String command) {
        commandsResource.addCommand("sudo " + command);
    }
    
    protected String executeScheduledSshCommands() {
        SandboxSshExec sshExec = createSshExec();
        sshExec.setCommandResource(commandsResource);
        sshExec.setOutputproperty("sshOutProp");
        sshExec.execute();
        return sandboxProject.getProperty("sshOutProp");
    }
    
    private SandboxSshExec createSshExec() {
        SandboxSshExec sshExec = new SandboxSshExec();
        sshExec.setProject(sandboxProject);
        sshExec.setUsername(SSH_USERNAME);
        sshExec.setPassword(SSH_PASSWORD);
        sshExec.setPort(sshForwardPort);
        sshExec.setHost(DEFAULT_HOST);
        sshExec.setTrust(true);
        return sshExec;
    }

}
