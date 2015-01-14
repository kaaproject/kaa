package org.kaaproject.kaa.server.sync.platform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class PlatformLookup {
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(PlatformLookup.class);
    
    public static final String DEFAULT_PROTOCOL_LOOKUP_PACKAGE_NAME = "org.kaaproject.kaa.server";
    
    public static Set<String> lookupPlatformProtocols(String... packageNames) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(KaaPlatformProtocol.class));
        Set<BeanDefinition> beans = new HashSet<BeanDefinition>();
        for(String packageName : packageNames){
            beans.addAll(scanner.findCandidateComponents(packageName));
        }
        Set<String> protocols = new HashSet<>();
        for (BeanDefinition bean : beans) {
            protocols.add(bean.getBeanClassName());
        }
        return protocols;
    }
    
    public static Map<Integer, PlatformEncDec> initPlatformProtocolMap(Set<String> platformProtocols) {
        Map<Integer, PlatformEncDec> platformEncDecMap = new HashMap<>();
        for (String platformProtocol : platformProtocols) {
            try {
                Class<?> clazz = Class.forName(platformProtocol);
                PlatformEncDec protocol = (PlatformEncDec) clazz.newInstance();
                platformEncDecMap.put(protocol.getId(), protocol);
                LOG.info("Successfully initialized platform protocol {}", platformProtocol);
            } catch (ReflectiveOperationException e) {
                LOG.error("Error during instantiation of platform protocol", e);
            }
        }
        return platformEncDecMap;
    }
}
