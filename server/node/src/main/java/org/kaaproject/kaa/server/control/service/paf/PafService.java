package org.kaaproject.kaa.server.control.service.paf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class PafService implements InitializingBean, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(PafService.class);

    private ApplicationContext appContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.appContext = context;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info("Initializing PAF service!");
        LOG.info("App context: [{}]", appContext);
        
        int startHttpPort = 9010;
        
        List<GenericApplicationContext> systemContexts = new ArrayList<>();
        
        for (int i=0;i<10;i++) {
            systemContexts.add(createSystemChain("sys"+i, startHttpPort++, 20));
        }
    }
    
    private GenericApplicationContext createSystemChain(String systemId, int httpPort, int appCount) throws IOException {
        GenericApplicationContext systemPafContext = loadContext("pafContext/systemPafContext.xml", appContext);
        StandardEnvironment env = new StandardEnvironment();
        Properties props = new Properties();
        props.setProperty("systemId", systemId);
        props.setProperty("httpPort", ""+httpPort);
        PropertiesPropertySource pps = new PropertiesPropertySource("sysprops", props);
        env.getPropertySources().addLast(pps);
        systemPafContext.setEnvironment(env);
        systemPafContext.refresh();
        
        for (int i=0;i<appCount;i++) {
            GenericApplicationContext appContext = createAppContext("app" + i, systemPafContext);
        }       
        
        return systemPafContext;
    }
    
    private GenericApplicationContext createAppContext(String appId, ApplicationContext parent) throws IOException {
        GenericApplicationContext applicationPafContext = loadContext("pafContext/applicationPafContext.xml", parent);
        StandardEnvironment env = new StandardEnvironment();
        Properties props = new Properties();
        props.setProperty("applicationId", appId);
        PropertiesPropertySource pps = new PropertiesPropertySource("appprops", props);
        env.getPropertySources().addLast(pps);
        applicationPafContext.setEnvironment(env);
        applicationPafContext.refresh();
        return applicationPafContext;
    }

    private GenericApplicationContext loadContext(String path, ApplicationContext parent) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(new File(path));
        ByteArrayResource resource = new ByteArrayResource(data);
        
        GenericApplicationContext createdContext = new GenericApplicationContext(parent);
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(createdContext);
        reader.loadBeanDefinitions(resource);
        createdContext.setParent(parent);
        
        return createdContext;
    }
}
