package net.paf.system;

import java.util.EventListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.admin.AdminContextLoaderListener;
import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.springframework.web.servlet.DispatcherServlet;

import net.paf.SystemPafBean;

public class SystemPafBeanImpl implements SystemPafBean, InitializingBean, DisposableBean, ApplicationContextAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(SystemPafBeanImpl.class);

    private String sysId;

    private ApplicationContext appContext;
    
    private int httpPort;
    
    private SystemPafContextLoader contextLoader;
    
    @Autowired
    private KaaNodeServerConfig kaaNodeServerConfig;
    
    @Autowired
    private ControlService controlService;
    
    private Server server;
    private ServletContextHandler webAppContext;
    
    public void setSysId(String sysId) {
        this.sysId = sysId;
    }
 
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }
    
    public void setContextLoader(SystemPafContextLoader contextLoader) {
        this.contextLoader = contextLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        LOG.info("Initializing SystemPafBeanImpl!");
//        LOG.info("App context: [{}]", appContext);
//        LOG.info("Parent app context: [{}]", appContext.getParent());
        LOG.info("System PAF [{}] Loaded dynamically!", sysId);
        LOG.info("[{}] Http port: {}", sysId, httpPort);
        
        server = new Server(httpPort);
        webAppContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        webAppContext.setEventListeners(new EventListener[]{contextLoader});
        webAppContext.setContextPath("/");
        
        ServletHolder holder = new ServletHolder("dispatcher", DispatcherServlet.class);
        holder.setInitParameter("contextConfigLocation", "");
        holder.setInitOrder(1);
        webAppContext.addServlet(holder, "/*");
        
        server.setHandler(webAppContext);
        
        try {
            server.start();
            LOG.info("HTTP server started on port {}", httpPort);
        } catch (Exception e) {
            LOG.error("Error starting HTTP Server!", e);
        }
    } 

    @Override
    public void saySome(String text) {
        LOG.info("[{}] Received text [{}]", sysId, text);
    }

    @Override
    public void destroy() throws Exception {
        try {
            LOG.info("Stopping HTTP Server...");
            server.stop();
            webAppContext.destroy();
            LOG.info("HTTP Server stopped.");
        } catch (Exception e) {
            LOG.error("Error stopping HTTP Server!", e);
        }
    }

}
