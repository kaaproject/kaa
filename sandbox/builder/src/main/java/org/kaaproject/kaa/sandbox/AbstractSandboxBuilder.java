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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHBase;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.apache.tools.ant.types.FileSet;
import org.kaaproject.kaa.sandbox.demo.AbstractDemoBuilder;
import org.kaaproject.kaa.sandbox.demo.DemoBuilder;
import org.kaaproject.kaa.sandbox.demo.DemoBuildersRegistry;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.demo.projects.ProjectsConfig;
import org.kaaproject.kaa.sandbox.rest.SandboxClient;
import org.kaaproject.kaa.sandbox.ssh.SandboxSshExec;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSandboxBuilder implements SandboxBuilder, SandboxConstants {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSandboxBuilder.class);
    private static final String MD5 = "MD5";
    private static final String MD5_FILE_EXT = ".md5";

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
            LOG.info("Box with name '{}' already exists. Going to unload old instance ...", boxName);
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
            LOG.info("Executing Cassandra cql script...");
            scheduleSudoSshCommand("cqlsh -f "+CASSANDRA_INIT_SCRIPT);
            scheduleServicesStart();
            LOG.info("Executing remote ssh commands...");
            executeScheduledSshCommands();
            LOG.info("Remote ssh commands execution is completed.");
            LOG.info("Sleeping 80 sec.");
            Thread.sleep(80000);
            initBoxData();
            LOG.info("Sleeping 20 sec.");
            Thread.sleep(20000);
            unprovisionBox();
            stopBox();
            cleanupBox();
            exportBox();
        } catch (Exception e) {
            LOG.error("Failed to build sandbox image!", e);
            dumpLogs();
            LOG.error("Look for sandbox build logs at: " + LOG_DUMP_LOCATION);
            throw e;
        } finally {
            if (boxLoaded()) {
                if (boxRunning()) {
                    stopBox();
                }
//                unloadBox();
            }
        }
    }

    private void loadBox() throws Exception {
        LOG.info("Loading box '{}' ...", boxName);
        String fileName = FilenameUtils.getName(baseImageUrl.toString());
        baseImageFile = new File(System.getProperty("java.io.tmpdir"), fileName);
        LOG.info("Downloading base image to file '{}'...", baseImageFile.getAbsolutePath());
        downloadFile(baseImageUrl, baseImageFile);

        if (baseImageFile.exists() && baseImageFile.isFile()) {
            LOG.info("Downloaded base image to file '{}'.", baseImageFile.getAbsolutePath());
        } else {
            LOG.error("Unable to download image file to '{}'", baseImageFile.getAbsolutePath());
            throw new RuntimeException("Unable to download image file!");
        }
        loadBoxImpl();
        LOG.info("Box '{}' is loaded.", boxName);
    }

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 64;

    private void downloadFile(URL sourceUrl, File targetFile) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext context = new BasicHttpContext();
        if (targetFile.exists()) {
            String checksumFileUrl = sourceUrl.toString() + MD5_FILE_EXT;
            String md5sum = downloadCheckSumFile(httpClient, context, checksumFileUrl);
            boolean result = compareChecksum(targetFile, md5sum);
            LOG.debug("Compare checksum result is {}", result);
            if (!result) {
                targetFile.delete();
                downloadFile(httpClient, context, sourceUrl, targetFile);
            }
        } else {
            downloadFile(httpClient, context, sourceUrl, targetFile);
        }
    }

    private void downloadFile(HttpClient httpClient, HttpContext context, URL sourceUrl, File targetFile) throws Exception {
        LOG.debug("Download {} to file {}", sourceUrl.toString(), targetFile.getAbsolutePath());
        HttpEntity entity = null;
        try {
            HttpGet httpGet = new HttpGet(sourceUrl.toURI());
            HttpResponse response = httpClient.execute(httpGet, context);
            entity = response.getEntity();
            long length = entity.getContentLength();
            InputStream in = new BufferedInputStream(entity.getContent());
            OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
            copyLarge(in, out, new byte[DEFAULT_BUFFER_SIZE], length);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer, long length)
            throws IOException {
        long count = 0;
        int n = 0;
        LOG.info("0%");
        int loggedPercents = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            int percentsComplete = (int)((double)count/(double)length*100f);
            if (percentsComplete-loggedPercents>=10) {
                LOG.info(percentsComplete+"%");
                loggedPercents = percentsComplete;
            }
        }
        return count;
    }

    private void prepareBox() throws Exception {
        LOG.info("Preparing box '{}' ...", boxName);
        prepareBoxImpl();
        LOG.info("Box '{}' is prepared.", boxName);
    }

    private void startBox() throws Exception {
        LOG.info("Starting box '{}' ...", boxName);
        startBoxImpl();
        LOG.info("Box '{}' is started.", boxName);
    }

    private void provisionBox() throws Exception {
        provisionBoxImpl();
        scheduleSudoSshCommand("rm -rf " + "/"+SHARED_FOLDER);
        scheduleSudoSshCommand("mkdir -p " + "/"+SHARED_FOLDER);
        scheduleSudoSshCommand("mkdir -p " + SANDBOX_FOLDER);
        scheduleSudoSshCommand("chown -R "+SSH_USERNAME+":"+SSH_USERNAME+" " + "/"+SHARED_FOLDER);
        scheduleSudoSshCommand("chown -R "+SSH_USERNAME+":"+SSH_USERNAME+" " + SANDBOX_FOLDER);
        executeScheduledSshCommands();

        LOG.info("Transfering sandbox data...");
        transferAllFromDir(basePath.getAbsolutePath(), "/"+SHARED_FOLDER);
        LOG.info("Sandbox data transfered");

    }

    private void unprovisionBox() throws Exception {
        executeSudoSsh("rm -rf " + "/"+SHARED_FOLDER);
        unprovisionBoxImpl();
    }

    private void stopBox() throws Exception {
        LOG.info("Stopping box '{}' ...", boxName);
        stopBoxImpl();
        LOG.info("Box '{}' is stopped.", boxName);
    }

    private void cleanupBox() throws Exception {
        LOG.info("Cleaning box '{}' ...", boxName);
        cleanupBoxImpl();
        LOG.info("Box '{}' is cleaned.", boxName);
    }

    private void exportBox() throws Exception {
        LOG.info("Exporting box '{}' ...", boxName);
        if (imageOutputFile.exists()) {
            if (!imageOutputFile.delete()) {
                LOG.error("Unable to delete previous output image file '{}'", imageOutputFile.getAbsoluteFile());
                throw new RuntimeException("Failed to export sandbox image!");
            }
        } else if (!imageOutputFile.getParentFile().exists()){
            imageOutputFile.getParentFile().mkdirs();
        }
        exportBoxImpl();
        LOG.info("Box '{}' was exported.", boxName);
    }

    private void unloadBox() throws Exception {
        LOG.info("Unloading box '{}' ...", boxName);
        unloadBoxImpl();
        LOG.info("Box '{}' was unloaded.", boxName);
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

        //Change kaa hosts file
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
        FileOutputStream fos = new FileOutputStream(changeKaaHostFile);
        fos.write(changeKaaHostFileSource.getBytes());
        fos.flush();
        fos.close();

        //Load demo data via REST API
        AdminClient adminClient = new AdminClient(DEFAULT_HOST, webAdminForwardPort);
        List<DemoBuilder> demoBuilders = DemoBuildersRegistry.getRegisteredDemoBuilders();
        List<Project> projects = new ArrayList<>();
        for (DemoBuilder demoBuilder : demoBuilders) {
            demoBuilder.buildDemoApplication(adminClient);
            projects.addAll(demoBuilder.getProjectConfigs());
        }

        //Prepare projects XML file
        File projectsXmlFile = new File(demoProjectsPath, DEMO_PROJECTS_XML);
        ProjectsConfig projectsConfig = new ProjectsConfig();
        projectsConfig.getProjects().addAll(projects);

        JAXBContext jc = JAXBContext.newInstance("org.kaaproject.kaa.sandbox.demo.projects");
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(projectsConfig, projectsXmlFile);

        //Prepare sandbox splash Python script
        String sandboxSplashFileTemplate = FileUtils.readResource(SANDBOX_SPLASH_PY_TEMPLATE);
        String sandboxSplashFileSource = AbstractDemoBuilder.updateCredentialsInfo(sandboxSplashFileTemplate);

        sandboxSplashFileSource = sandboxSplashFileSource.replaceAll(WEB_ADMIN_PORT_VAR, DEFAULT_WEB_ADMIN_PORT+"")
                                                         .replaceAll(SSH_FORWARD_PORT_VAR, DEFAULT_SSH_FORWARD_PORT + "");

        File sandboxSplashFile = new File(distroPath, SANDBOX_SPLASH_PY);
        fos = new FileOutputStream(sandboxSplashFile);
        fos.write(sandboxSplashFileSource.getBytes());
        fos.flush();
        fos.close();

        scheduleSudoSshCommand("rm -rf " + SANDBOX_FOLDER + "/" + DEMO_PROJECTS);
        scheduleSudoSshCommand("rm -f " + SANDBOX_FOLDER + "/" + CHANGE_KAA_HOST);
        scheduleSudoSshCommand("rm -f " + SANDBOX_FOLDER + "/" + SANDBOX_SPLASH_PY);
        scheduleSudoSshCommand("cp -r " + DEMO_PROJECTS_PATH + " " + SANDBOX_FOLDER + "/");
        scheduleSudoSshCommand("chown -R "+SSH_USERNAME+":"+SSH_USERNAME+" " + SANDBOX_FOLDER + "/" + DEMO_PROJECTS);
        scheduleSudoSshCommand("cp -r " + SANDBOX_PATH+"/*" + " " + ADMIN_FOLDER + "/");
        scheduleSudoSshCommand("chown -R "+SSH_USERNAME+":"+SSH_USERNAME+" " + ADMIN_FOLDER + "/webapps");
        scheduleSudoSshCommand("sed -i \"s/\\(tenant_developer_user=\\).*\\$/\\1"+AbstractDemoBuilder.tenantDeveloperUser+"/\" " + ADMIN_FOLDER +"/conf/sandbox-server.properties");
        scheduleSudoSshCommand("sed -i \"s/\\(tenant_developer_password=\\).*\\$/\\1"+AbstractDemoBuilder.tenantDeveloperPassword+"/\" " + ADMIN_FOLDER +"/conf/sandbox-server.properties");

        String stopAdminCommand = osType.getStopServiceTemplate().
                replaceAll(SERVICE_NAME_VAR, KaaPackage.ADMIN.getServiceName());
        scheduleSudoSshCommand(stopAdminCommand);

        executeScheduledSshCommands();

        transferFile(changeKaaHostFile.getAbsolutePath(), SANDBOX_FOLDER);
        transferFile(projectsXmlFile.getAbsolutePath(), SANDBOX_FOLDER + "/" + DEMO_PROJECTS);
        transferFile(sandboxSplashFile.getAbsolutePath(), SANDBOX_FOLDER);

        executeSudoSsh("chmod +x " + SANDBOX_FOLDER + "/"+CHANGE_KAA_HOST);
        executeSudoSsh("chmod +x " + SANDBOX_FOLDER + "/"+SANDBOX_SPLASH_PY);

        String startAdminCommand = osType.getStartServiceTemplate().
                replaceAll(SERVICE_NAME_VAR, KaaPackage.ADMIN.getServiceName());
        executeSudoSsh(startAdminCommand);

        LOG.info("Sleeping 50 sec.");
        Thread.sleep(50000);

        LOG.info("Building demo applications...");
        SandboxClient sandboxClient = new SandboxClient(DEFAULT_HOST, webAdminForwardPort);

        List<Project> sandboxProjects = sandboxClient.getDemoProjects();
        if (projects.size() != sandboxProjects.size()) {
            LOG.error("Demo projects count mismatch, expected {}, actual {}", projects.size(), sandboxProjects.size());
            throw new RuntimeException("Demo projects count mismatch!");
        }
        for (Project sandboxProject : sandboxProjects) {
            if (sandboxProject.getDestBinaryFile() != null &&
                    sandboxProject.getDestBinaryFile().length()>0) {
                LOG.info("[{}][{}] Building Demo Project...", sandboxProject.getPlatform(), sandboxProject.getName());
                String output = sandboxClient.buildProjectBinary(sandboxProject.getId());
                LOG.info("[{}][{}] Build output:\n{}", sandboxProject.getPlatform(), sandboxProject.getName(), output);
                if (!sandboxClient.isProjectBinaryDataExists(sandboxProject.getId())) {
                    LOG.error("Failed to build demo project '{}'", sandboxProject.getName());
                    throw new RuntimeException("Failed to build demo project '" + sandboxProject.getName() + "'!");
                }
            } else {
                LOG.info("[{}][{}] Skipping Demo Project build...", sandboxProject.getPlatform(), sandboxProject.getName());
            }
        }
        LOG.info("Finished building demo applications!");
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
                LOG.info(line);
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
        initSsh(sshExec);
        return sshExec;
    }

    private void transferAllFromDir(String dir, String to) throws IOException {
        Scp scp = createScp();
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File(dir));
        fileSet.setIncludes("**/*");
        scp.addFileset(fileSet);
        scp.setRemoteTodir(SSH_USERNAME+"@"+DEFAULT_HOST+":"+to);
        scp.execute();
    }

    private void transferFile(String file, String to) {
        Scp scp = createScp();
        scp.setLocalFile(file);
        scp.setRemoteTodir(SSH_USERNAME+"@"+DEFAULT_HOST+":"+to);
        scp.execute();
    }

    private void dumpLogs(){
        File dumpedLogsFolder  = new File(LOG_DUMP_LOCATION);
        if(!dumpedLogsFolder.exists()){
            dumpedLogsFolder.mkdirs();
        }
        Scp scp = createScp();
        scp.setLocalTodir(LOG_DUMP_LOCATION);
        scp.setFile(SSH_USERNAME+"@"+DEFAULT_HOST+":/var/log/kaa/*");
        scp.execute();
    }

    private Scp createScp() {
        Scp scp = new Scp();
        initSsh(scp);
        return scp;
    }

    private void initSsh(SSHBase ssh) {
        ssh.setProject(sandboxProject);
        ssh.setUsername(SSH_USERNAME);
        ssh.setPassword(SSH_PASSWORD);
        ssh.setPort(sshForwardPort);
        ssh.setHost(DEFAULT_HOST);
        ssh.setTrust(true);
    }

    private boolean compareChecksum(File targetFile, String downloadedCheckSum) {
        String checkSum = "";
        try (FileInputStream fileInput = new FileInputStream(targetFile)) {
            MessageDigest messageDigest = MessageDigest.getInstance(MD5);
            byte[] dataBytes = new byte[1024 * 100];
            int bytesRead = 0;
            while ((bytesRead = fileInput.read(dataBytes)) != -1) {
                messageDigest.update(dataBytes, 0, bytesRead);
            }
            byte[] digestBytes = messageDigest.digest();
            checkSum = DatatypeConverter.printHexBinary(digestBytes);
            LOG.debug("Calculated checksum for filer[{}]: [{}], downloaded is [{}]", targetFile.getAbsolutePath(), checkSum, downloadedCheckSum);
        } catch (IOException | NoSuchAlgorithmException e) {
            LOG.debug("Can't calculate checksum for file [{}]", targetFile.getAbsolutePath());
        }
        return checkSum.equalsIgnoreCase(downloadedCheckSum);
    }

    public String downloadCheckSumFile(HttpClient httpClient, HttpContext context, String url) throws Exception {
        LOG.debug("Starting download [{}] ...", url);
        HttpGet httpGet = new HttpGet(URI.create(url));
        HttpResponse response = httpClient.execute(httpGet, context);
        HttpEntity entity = response.getEntity();
        InputStream in = entity.getContent();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        String checkSum = new String(out.toByteArray(), StandardCharsets.UTF_8);
        return checkSum != null ? checkSum.trim() : "";
    }

}
