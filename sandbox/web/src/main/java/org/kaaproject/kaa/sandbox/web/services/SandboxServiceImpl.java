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
package org.kaaproject.kaa.sandbox.web.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.iharder.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.gwt20.managed.AtmosphereMessageInterceptor;
import org.atmosphere.gwt20.server.GwtRpcInterceptor;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.IdleResourceInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.demo.projects.ProjectsConfig;
import org.kaaproject.kaa.sandbox.web.services.cache.CacheService;
import org.kaaproject.kaa.sandbox.web.services.util.Utils;
import org.kaaproject.kaa.sandbox.web.shared.dto.BuildOutputData;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataKey;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataType;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxService;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("sandboxService")
@ManagedService(path = "/sandbox/atmosphere/rpc",
interceptors = {
    /**
* Handle lifecycle for us
*/
    AtmosphereResourceLifecycleInterceptor.class,
    /**
* Send to the client the size of the message to prevent serialization error.
*/
    TrackMessageSizeInterceptor.class,
    /**
* Serialize/Deserialize GWT message for us
*/
    GwtRpcInterceptor.class,
    /**
* Make sure our {@link AtmosphereResourceEventListener#onSuspend} is only called once for transport
* that reconnect on every requests.
*/
    SuspendTrackerInterceptor.class,
    /**
* Deserialize the GWT message
*/
    AtmosphereMessageInterceptor.class,
    /**
* Discard idle AtmosphereResource in case the network didn't advise us the client disconnected
*/
    IdleResourceInterceptor.class
})
public class SandboxServiceImpl implements SandboxService, InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SandboxServiceImpl.class);
    
    private static final String DEMO_PROJECTS_FOLDER = "demo_projects";
   
    private static final String DEMO_PROJECTS_XML_FILE = "demo_projects.xml";
    
    private static final String SANDBOX_ENV_FILE = "sandbox-env.properties";
    
    private static final String CHANGE_KAA_HOST_DIALOG_SHOWN_PROPERTY = "changeKaaHostDialogShown";
    
    @Autowired
    private CacheService cacheService;
    
    /** The thrift host. */
    @Value("#{properties[sandbox_home]}")
    private String sandboxHome;

    /** The thrift port. */
    @Value("#{properties[gui_change_host_enabled]}")
    private boolean guiChangeHostEnabled;
    
    private Map<String, Project> projectsMap = new HashMap<>();
    
    private static String[] sandboxEnv;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            
            LOG.info("Initializing Sandbox Service...");
            LOG.info("sandboxHome [{}]", sandboxHome);
            LOG.info("guiChangeHostEnabled [{}]", guiChangeHostEnabled);
            
            JAXBContext jc = JAXBContext.newInstance("org.kaaproject.kaa.sandbox.demo.projects");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            
            String demoProkectsXmlFile = sandboxHome + "/" + DEMO_PROJECTS_FOLDER + "/" + DEMO_PROJECTS_XML_FILE;
            
            ProjectsConfig projectsConfig = (ProjectsConfig) unmarshaller.unmarshal(new File(demoProkectsXmlFile));
            for (Project project : projectsConfig.getProjects()) {
                projectsMap.put(project.getId(), project);
                LOG.info("Demo project: id [{}] name [{}]", project.getId(), project.getName());
            }
            
            if (sandboxEnv == null) {
                Properties sandboxEnvProperties = 
                        org.kaaproject.kaa.server.common.utils.FileUtils.readResourceProperties(SANDBOX_ENV_FILE);
                
                sandboxEnv = new String[sandboxEnvProperties.size()];
                int i=0;
                for (Object key : sandboxEnvProperties.keySet()) {
                    String keyValue = key + "=" + sandboxEnvProperties.getProperty(key.toString());
                    sandboxEnv[i++] = keyValue;
                    LOG.info("Sandbox env: [{}]", keyValue);
                }
            }
            LOG.info("Initialized Sandbox Service.");
        } catch (JAXBException e) {
            LOG.error("Unable to initialize Sandbox Service", e);
            throw e;
        }
    }
    
    @Ready
    public void onReady(final AtmosphereResource r) {
    	LOG.info("Received RPC GET, uuid: {}", r.uuid());
    	r.getBroadcaster().broadcast(r.uuid(), r);
    }
    
    @Override
    public boolean changeKaaHostEnabled() throws SandboxServiceException {
        return guiChangeHostEnabled;
    }
    
    @Override
    public boolean showChangeKaaHostDialog() throws SandboxServiceException {
        if (guiChangeHostEnabled) {
            Boolean result = (Boolean) cacheService.getProperty(CHANGE_KAA_HOST_DIALOG_SHOWN_PROPERTY);
            return result == null || !result.booleanValue();
        } else {
            return false;
        }
    }

    @Override
    public void changeKaaHostDialogShown() throws SandboxServiceException {
        cacheService.putProperty(CHANGE_KAA_HOST_DIALOG_SHOWN_PROPERTY, Boolean.TRUE);
    }
    
    @Override
    public void changeKaaHost(String uuid, String host) throws SandboxServiceException {
    	AtmosphereResource res = AtmosphereResourceFactory.getDefault().find(uuid);
        try {
        	ClientMessageOutputStream outStream = new ClientMessageOutputStream(res, null);
        	if (guiChangeHostEnabled) {
        	    executeCommand(outStream, new String[]{"sudo",sandboxHome + "/change_kaa_host.sh",host}, null);
        	    cacheService.flushAllCaches();
        	} else {
        	    outStream.println("WARNING: change host from GUI is disabled!");
        	}
        } finally {
            res.getBroadcaster().broadcast(uuid + " finished", res);
        }
    }
    
    @Override
    public List<Project> getDemoProjects() throws SandboxServiceException {
        return new ArrayList<Project>(projectsMap.values());
    }

    @Override
    public Project getDemoProject(String projectId)
            throws SandboxServiceException {
        return projectsMap.get(projectId);
    }

    @Override
    public boolean checkProjectDataExists(String projectId,
            ProjectDataType dataType) throws SandboxServiceException {
        ProjectDataKey dataKey = new ProjectDataKey(projectId, dataType);
        FileData data = cacheService.getProjectFile(dataKey);
        return data != null;
    }

    @Override
    public void buildProjectData(String uuid, BuildOutputData outputData, String projectId, ProjectDataType dataType) throws SandboxServiceException {
        AtmosphereResource res = null;
        PrintStream outPrint = null;
        ClientMessageOutputStream outStream = null;
        ByteArrayOutputStream byteOutStream = null;
        if (uuid != null) {
            res = AtmosphereResourceFactory.getDefault().find(uuid);
        } else if (outputData != null) {
            byteOutStream = new ByteArrayOutputStream();
            outPrint = new PrintStream(byteOutStream);
        }
        try {
            outStream = new ClientMessageOutputStream(res, outPrint);
            Project project = projectsMap.get(projectId);
            if (project != null) {
                String sdkKeyBase64 = project.getSdkKeyBase64();
                SdkPropertiesDto sdkPropertiesDto = (SdkPropertiesDto)Base64.decodeToObject(sdkKeyBase64, Base64.URL_SAFE, null);
                outStream.println("Getting SDK for requested project...");
                FileData sdkFileData = cacheService.getSdk(sdkPropertiesDto);
                if (sdkFileData != null) {
                    outStream.println("Successfuly got SDK.");
                    File rootDir = createTempDirectory("demo-project");
                    try {
                        outStream.println("Processing project archive...");
                        String sourceArchiveFile = sandboxHome + "/" + DEMO_PROJECTS_FOLDER + "/" + project.getSourceArchive();
                        String rootProjectDir = rootDir.getAbsolutePath();
                        
                        executeCommand(outStream, new String[]{"tar","-C",rootProjectDir,"-xzvf", sourceArchiveFile}, null);
                        
                        File sdkFile = new File(rootProjectDir + "/" + project.getSdkLibDir() + "/" + sdkFileData.getFileName());
                        FileOutputStream fos = FileUtils.openOutputStream(sdkFile);
                        fos.write(sdkFileData.getFileData());
                        fos.flush();
                        fos.close();
                        
                        ProjectDataKey dataKey = new ProjectDataKey(projectId, dataType);
                        if (dataType==ProjectDataType.SOURCE) {
                            String sourceArchiveName = FilenameUtils.getName(sourceArchiveFile);
                            outStream.println("Compressing source project archive...");
                            
                            File sourceFile = new File(rootDir, sourceArchiveName);
                            
                            String[] files = rootDir.list();
                            String[] command = (String[]) ArrayUtils.addAll(new String[]{"tar","-czvf",sourceFile.getAbsolutePath(),"-C", rootProjectDir}, files);
                            
                            executeCommand(outStream, command, null);
                            
                            outStream.println("Source project archive compressed.");
                            byte[] sourceFileBytes = FileUtils.readFileToByteArray(sourceFile);
                            FileData sourceFileData = new FileData();
                            sourceFileData.setFileName(sourceArchiveName);
                            sourceFileData.setFileData(sourceFileBytes);
                            sourceFileData.setContentType("application/x-compressed");
                            cacheService.putProjectFile(dataKey, sourceFileData);
                        } else {
                            outStream.println("Building binary file...");
                            File projectFolder = rootDir;
                            if (project.getProjectFolder() != null && !project.getProjectFolder().trim().isEmpty()) {
                                projectFolder = new File(rootDir, project.getProjectFolder());
                            }
                            
                            executeCommand(outStream, new String[]{"ant"}, projectFolder);
                            
                            outStream.println("Build finished.");
                            
                            File binaryFile = new File(rootDir, project.getDestBinaryFile());
                            byte[] binaryFileBytes = FileUtils.readFileToByteArray(binaryFile);
                            FileData binaryFileData = new FileData();
                            
                            String binaryFileName = FilenameUtils.getName(binaryFile.getAbsolutePath());
                            
                            binaryFileData.setFileName(binaryFileName);
                            binaryFileData.setFileData(binaryFileBytes);
                            if (project.getPlatform()==Platform.ANDROID) {
                                binaryFileData.setContentType("application/vnd.android.package-archive");
                            } else if (project.getPlatform()==Platform.JAVA) {
                                binaryFileData.setContentType("application/java-archive");
                            }
                            cacheService.putProjectFile(dataKey, binaryFileData);
                        }
                    } finally {
                        FileUtils.deleteDirectory(rootDir);
                    }
                } else {
                    outStream.println("Unable to get/create SDK for requested project!");
                }
            } else {
                outStream.println("No project configuration found!");
            }
        } catch (Exception e) {
            if (outStream != null) {
                outStream.println("Unexpected error occurred: " + e.getMessage());
            }
            throw Utils.handleException(e);
        } finally {
            if (res != null) {
                res.getBroadcaster().broadcast(uuid + " finished", res);
            }
            if (outPrint != null) {
                outPrint.flush();
                outPrint.close();
                outputData.setOutputData(byteOutStream.toByteArray());
            }
        }
    }
    
    private static void executeCommand(ClientMessageOutputStream outStream, 
            String[] command, 
            File workingDir) throws SandboxServiceException {
        try {
            Execute exec = new Execute(new PumpStreamHandler(outStream));
            exec.setEnvironment(sandboxEnv);
            if (workingDir != null) {
                exec.setWorkingDirectory(workingDir);
            }
            exec.setCommandline(command);
            exec.execute();
            if (exec.isFailure()) {
                throw new SandboxServiceException("Process returned bad exit value: " + exec.getExitValue());
            }
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    private static File createTempDirectory(String prefix) throws IOException {
        final File temp = File.createTempFile(prefix, Long.toString(System.nanoTime()));
        if (!temp.delete()) {
            throw new IOException("Could not delete temp file: "
                    + temp.getAbsolutePath());
        }
        if (!temp.mkdir()) {
            throw new IOException("Could not create temp directory: "
                    + temp.getAbsolutePath());
        }
        return temp;
    }

    class ClientMessageOutputStream extends OutputStream {

    	private AtmosphereResource res;
    	private PrintStream out;
    	
    	ClientMessageOutputStream(AtmosphereResource res, PrintStream out) {
    		this.res = res;
    		this.out = out;
    	}
    	
		@Override
		public void write(int b) throws IOException {
		}
		
		@Override
		public void write(byte b[], int off, int len) throws IOException {
			byte[] data = new byte[len];
			System.arraycopy(b, off, data, 0, len);
			String message = new String(data);
			if (res != null) {
			    res.getBroadcaster().broadcast(message, res);
			}
			if (out != null) {
			    out.print(message);
			}
		}
		
		public void println(String text) {
		    if (res != null) {
		        res.getBroadcaster().broadcast((text+"\n"), res);
		    }
	        if (out != null) {
	            out.println(text);
	        }
		}
    }


}
