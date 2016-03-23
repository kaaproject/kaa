package net.paf.application;

import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import net.paf.ApplicationRouter;
import net.paf.SystemPafBean;

public class ApplicationPafBean implements InitializingBean, DisposableBean, ApplicationContextAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationPafBean.class);

    private String appId;
    private SystemPafBean systemLevelBean;
    private ApplicationRouter systemLevelAppRouter;

    private ApplicationContext appContext;
    
    @Autowired
    private KaaNodeServerConfig kaaNodeServerConfig;
    
    @Autowired
    private ControlService controlService;
    
    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setSystemLevelBean(SystemPafBean systemLevelBean) {
        this.systemLevelBean = systemLevelBean;
    }

    public void setSystemLevelAppRouter(ApplicationRouter systemLevelAppRouter) {
        this.systemLevelAppRouter = systemLevelAppRouter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        LOG.info("Initializing ApplicationPafBean!");
//        LOG.info("App context: [{}]", appContext);
//        LOG.info("Parent app context: [{}]", appContext.getParent());
        LOG.info("Application PAF [{}] Loaded dynamically!", appId);
        
        systemLevelBean.saySome("Hello from app [" + appId + "]");
        
        MessageChannel channel = this.appContext.getBean("appRequestChannel", MessageChannel.class);
        systemLevelAppRouter.registerAppChannel(appId, channel);
    }
    
    public Message<String> onMessage(Message<?> message) {
        LOG.info("[{}] Received message with payload [{}]", appId, message.getPayload());
        return MessageBuilder.withPayload("reply from " + appId).copyHeaders(message.getHeaders()).build();
    }

    @Override
    public void destroy() throws Exception {
        systemLevelAppRouter.deregisterAppChannel(appId);
    }

}
