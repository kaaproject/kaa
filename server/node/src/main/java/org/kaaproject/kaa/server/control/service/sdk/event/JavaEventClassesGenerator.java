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

package org.kaaproject.kaa.server.control.service.sdk.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.server.control.service.sdk.SdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaEventClassesGenerator {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(JavaEventClassesGenerator.class);

    /** The Constant EVENT_FAMILY_JAVA_TEMPLATE. */
    private static final String EVENT_FAMILY_JAVA_TEMPLATE = "sdk/java/event/EventFamily.java.template";

    /** The Constant EVENT_FAMILY_ADD_SUPPORTED_FQN_TEMPLATE. */
    private static final String EVENT_FAMILY_ADD_SUPPORTED_FQN_TEMPLATE = "sdk/java/event/eventFamilyAddSupportedFqn.template";

    /** The Constant EVENT_FAMILY_ON_GENERIC_EVENT_TEMPLATE. */
    private static final String EVENT_FAMILY_ON_GENERIC_EVENT_TEMPLATE = "sdk/java/event/eventFamilyOnGenericEvent.template";

    /** The Constant EVENT_FAMILY_SEND_EVENT_METHODS_TEMPLATE. */
    private static final String EVENT_FAMILY_SEND_EVENT_METHODS_TEMPLATE = "sdk/java/event/eventFamilySendEventMethods.template";

    /** The Constant EVENT_FAMILY_LISTENER_METHOD_TEMPLATE. */
    private static final String EVENT_FAMILY_LISTENER_METHOD_TEMPLATE = "sdk/java/event/eventFamilyListenerMethod.template";

    /** The Constant EVENT_FAMILY_FACTORY_JAVA_TEMPLATE. */
    private static final String EVENT_FAMILY_FACTORY_JAVA_TEMPLATE = "sdk/java/event/EventFamilyFactory.java.template";

    /** The Constant EVENT_FAMILY_FACTORY_METHOD_TEMPLATE. */
    private static final String EVENT_FAMILY_FACTORY_METHOD_TEMPLATE = "sdk/java/event/eventFamilyFactoryMethod.template";

    private static final String EVENT_FAMILY_NAMESPACE_VAR = "\\$\\{event_family_namespace\\}";
    private static final String EVENT_FAMILY_CLASS_NAME_VAR = "\\$\\{event_family_class_name\\}";
    private static final String EVENT_FAMILY_VAR_NAME_VAR = "\\$\\{event_family_var_name\\}";

    private static final String ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR = "\\$\\{add_supported_event_class_fqns\\}";
    private static final String EVENT_FAMILY_LISTENERS_ON_GENERIC_EVENT_VAR = "\\$\\{event_family_listeners_on_generic_event\\}";
    private static final String EVENT_FAMILY_SEND_EVENT_METHODS_VAR = "\\$\\{event_family_send_event_methods\\}";
    private static final String EVENT_FAMILY_LISTENER_METHODS_VAR = "\\$\\{event_family_listener_methods\\}";
    private static final String EVENT_CLASS_FQN_VAR = "\\$\\{event_class_fqn\\}";
    private static final String EVENT_FAMILY_FACTORY_IMPORTS_VAR = "\\$\\{event_family_factory_imports\\}";
    private static final String EVENT_FAMILY_FACTORY_METHODS_VAR = "\\$\\{event_family_factory_methods\\}";

    private static final String EVENT_FAMILY_FACTORY = "EventFamilyFactory";

    private static String eventFamilyJava;
    private static String eventFamilyAddSupportedFqn;
    private static String eventFamilyOnGenericEvent;
    private static String eventFamilySendEventMethod;
    private static String eventFamilyListenerMethod;
    private static String eventFamilyFactoryJava;
    private static String eventFamilyFactoryMethod;

    static {
        try {
            eventFamilyJava = SdkGenerator.readResource(EVENT_FAMILY_JAVA_TEMPLATE);
            eventFamilyAddSupportedFqn = SdkGenerator.readResource(EVENT_FAMILY_ADD_SUPPORTED_FQN_TEMPLATE);
            eventFamilyOnGenericEvent = SdkGenerator.readResource(EVENT_FAMILY_ON_GENERIC_EVENT_TEMPLATE);
            eventFamilySendEventMethod = SdkGenerator.readResource(EVENT_FAMILY_SEND_EVENT_METHODS_TEMPLATE);
            eventFamilyListenerMethod = SdkGenerator.readResource(EVENT_FAMILY_LISTENER_METHOD_TEMPLATE);
            eventFamilyFactoryJava = SdkGenerator.readResource(EVENT_FAMILY_FACTORY_JAVA_TEMPLATE);
            eventFamilyFactoryMethod = SdkGenerator.readResource(EVENT_FAMILY_FACTORY_METHOD_TEMPLATE);
        } catch (IOException e) {
            LOG.error("Unable to initialize JavaEventClassesGenerator", e);
        }
    }

    private JavaEventClassesGenerator() {
    }

    public static List<JavaDynamicBean> generateEventClasses(List<EventFamilyMetadata> eventFamilies) {
        List<JavaDynamicBean> javaSources = new ArrayList<>();

        String eventFamilyFactoryImports = "";
        String eventFamilyFactoryMethods = "";

        for (EventFamilyMetadata efm : eventFamilies) {
            eventFamilyFactoryImports +="import " + efm.getEcfNamespace() + "." + efm.getEcfClassName() + ";\n";
            String eventFamilyVarName = efm.getEcfClassName().substring(0, 1).toLowerCase();
            if (efm.getEcfClassName().length()>1) {
                eventFamilyVarName += efm.getEcfClassName().substring(1);
            }
            eventFamilyFactoryMethods += eventFamilyFactoryMethod.
                    replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()).
                    replaceAll(EVENT_FAMILY_VAR_NAME_VAR, eventFamilyVarName);

            String addSupportedEventClassFqns = "";
            String eventFamilyListenersOnGenericEvent = "";
            String eventFamilySendEventMethods = "";
            String eventFamilyListenerMethods = "";

            for (ApplicationEventMapDto eventMap : efm.getEventMaps()) {
                if (eventMap.getAction()==ApplicationEventAction.SINK ||
                    eventMap.getAction()==ApplicationEventAction.BOTH) {
                    addSupportedEventClassFqns += eventFamilyAddSupportedFqn.
                            replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                    if (eventFamilyListenersOnGenericEvent.length()>0) {
                        eventFamilyListenersOnGenericEvent += "else ";
                    }
                    eventFamilyListenersOnGenericEvent += eventFamilyOnGenericEvent.
                            replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                    eventFamilyListenerMethods += eventFamilyListenerMethod.
                            replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                }
                if (eventMap.getAction()==ApplicationEventAction.SOURCE ||
                    eventMap.getAction()==ApplicationEventAction.BOTH) {
                    eventFamilySendEventMethods += eventFamilySendEventMethod.
                            replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                }
            }

            String eventFamilySource = eventFamilyJava.
                    replaceAll(EVENT_FAMILY_NAMESPACE_VAR, efm.getEcfNamespace()).
                    replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()).
                    replaceAll(ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR, addSupportedEventClassFqns).
                    replaceAll(EVENT_FAMILY_LISTENERS_ON_GENERIC_EVENT_VAR, eventFamilyListenersOnGenericEvent).
                    replaceAll(EVENT_FAMILY_SEND_EVENT_METHODS_VAR, eventFamilySendEventMethods).
                    replaceAll(EVENT_FAMILY_LISTENER_METHODS_VAR, eventFamilyListenerMethods);

            LOG.trace("Going to compile {} using source {}", efm.getEcfClassName(), eventFamilySource);

            JavaDynamicBean eventFamily = new JavaDynamicBean(efm.getEcfClassName(), eventFamilySource);

            javaSources.add(eventFamily);
        }

        String eventFamilyFactorySource = eventFamilyFactoryJava.
                replaceAll(EVENT_FAMILY_FACTORY_IMPORTS_VAR, eventFamilyFactoryImports).
                replaceAll(EVENT_FAMILY_FACTORY_METHODS_VAR, eventFamilyFactoryMethods);


        LOG.trace("Going to compile {} using source {}", EVENT_FAMILY_FACTORY, eventFamilyFactorySource);
        JavaDynamicBean eventFamilyFactory = new JavaDynamicBean(EVENT_FAMILY_FACTORY, eventFamilyFactorySource);
        javaSources.add(eventFamilyFactory);

        return javaSources;
    }

}
