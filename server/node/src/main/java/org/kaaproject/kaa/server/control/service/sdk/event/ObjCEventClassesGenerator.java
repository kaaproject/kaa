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

import org.kaaproject.kaa.avro.avrogen.compiler.ObjectiveCCompiler;
import org.kaaproject.kaa.avro.avrogen.compiler.Compiler;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.server.control.service.sdk.CommonSdkUtil;
import org.kaaproject.kaa.server.control.service.sdk.SdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.avro.Schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ObjCEventClassesGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ObjCEventClassesGenerator.class);

    private static final String GEN_ROOT = "Kaa/avro/gen/";

    private static final String EVENT_GEN = "EventGen";
    private static final String EVENT_FACTORY_PATH = GEN_ROOT + "EventFamilyFactory.";

    private static final String EVENT_FAMILY_TEMPLATE = "sdk/objc/event/EventFamily.%s.template";

    private static final String EVENT_FAMILY_ADD_SUPPORTED_FQN_TEMPLATE = "sdk/objc/event/eventFamilyAddSupportedFqn.template";

    private static final String EVENT_FAMILY_ON_GENERIC_EVENT_TEMPLATE = "sdk/objc/event/eventFamilyOnGenericEvent.template";

    private static final String EVENT_FAMILY_SEND_EVENT_H_METHODS_TEMPLATE = "sdk/objc/event/eventFamilySendEventHeaderMethods.template";
    private static final String EVENT_FAMILY_SEND_EVENT_M_METHODS_TEMPLATE = "sdk/objc/event/eventFamilySendEventSourceMethods.template";

    private static final String EVENT_FAMILY_LISTENER_METHOD_TEMPLATE = "sdk/objc/event/eventFamilyListenerMethod.template";

    private static final String EVENT_FAMILY_FACTORY_TEMPLATE = "sdk/objc/event/EventFamilyFactory.%s.template";

    private static final String EVENT_FAMILY_FACTORY_METHOD_H_TEMPLATE = "sdk/objc/event/eventFamilyFactoryMethodHeader.template";
    private static final String EVENT_FAMILY_FACTORY_METHOD_M_TEMPLATE = "sdk/objc/event/eventFamilyFactoryMethodSource.template";
    private static final String EVENT_FAMILY_FACTORY_PROPERTY_TEMPLATE = "sdk/objc/event/eventFamilyFactoryProperty.template";

    private static final String EVENT_FAMILY_NAMESPACE_VAR = "\\{event_family_namespace\\}";
    private static final String EVENT_FAMILY_CLASS_NAME_VAR = "\\{event_family_class_name\\}";
    private static final String EVENT_FAMILY_VAR_NAME_VAR = "\\{event_family_var_name\\}";

    private static final String ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR = "\\{add_supported_event_class_fqns\\}";
    private static final String EVENT_FAMILY_LISTENERS_ON_GENERIC_EVENT_VAR = "\\{event_family_listeners_on_generic_event\\}";
    private static final String EVENT_FAMILY_SEND_EVENT_METHODS_VAR = "\\{event_family_send_event_methods\\}";
    private static final String EVENT_FAMILY_LISTENER_METHODS_VAR = "\\{event_family_listener_methods\\}";
    private static final String EVENT_CLASS_FQN_VAR = "\\{event_class_fqn\\}";
    private static final String EVENT_CLASS_NAME_VAR = "\\{event_class_name\\}";

    private static final String EVENT_FAMILY_FACTORY_IMPORTS_VAR = "\\{event_family_factory_imports\\}";
    private static final String EVENT_FAMILY_FACTORY_PROPERTIES_VAR = "\\{event_family_factory_properties\\}";
    private static final String EVENT_FAMILY_FACTORY_METHODS_VAR = "\\{event_family_factory_methods\\}";

    private static String eventFamilyHeader;
    private static String eventFamilySource;
    private static String eventFamilyAddSupportedFqn;
    private static String eventFamilyOnGenericEvent;
    private static String eventFamilySendEventHeaderMethod;
    private static String eventFamilySendEventSourceMethod;
    private static String eventFamilyListenerMethod;
    private static String eventFamilyFactoryHeader;
    private static String eventFamilyFactorySource;
    private static String eventFamilyFactoryMethodHeader;
    private static String eventFamilyFactoryMethodSource;
    private static String eventFamilyFactoryProperty;

    static {
        try {
            eventFamilyHeader = SdkGenerator.readResource(String.format(EVENT_FAMILY_TEMPLATE, "h"));
            eventFamilySource = SdkGenerator.readResource(String.format(EVENT_FAMILY_TEMPLATE, "m"));
            eventFamilyAddSupportedFqn = SdkGenerator.readResource(EVENT_FAMILY_ADD_SUPPORTED_FQN_TEMPLATE);
            eventFamilyOnGenericEvent = SdkGenerator.readResource(EVENT_FAMILY_ON_GENERIC_EVENT_TEMPLATE);
            eventFamilySendEventHeaderMethod = SdkGenerator.readResource(EVENT_FAMILY_SEND_EVENT_H_METHODS_TEMPLATE);
            eventFamilySendEventSourceMethod = SdkGenerator.readResource(EVENT_FAMILY_SEND_EVENT_M_METHODS_TEMPLATE);
            eventFamilyListenerMethod = SdkGenerator.readResource(EVENT_FAMILY_LISTENER_METHOD_TEMPLATE);
            eventFamilyFactoryHeader = SdkGenerator.readResource(String.format(EVENT_FAMILY_FACTORY_TEMPLATE, "h"));
            eventFamilyFactorySource = SdkGenerator.readResource(String.format(EVENT_FAMILY_FACTORY_TEMPLATE, "m"));
            eventFamilyFactoryMethodHeader = SdkGenerator.readResource(EVENT_FAMILY_FACTORY_METHOD_H_TEMPLATE);
            eventFamilyFactoryMethodSource = SdkGenerator.readResource(EVENT_FAMILY_FACTORY_METHOD_M_TEMPLATE);
            eventFamilyFactoryProperty = SdkGenerator.readResource(EVENT_FAMILY_FACTORY_PROPERTY_TEMPLATE);
        } catch (IOException e) {
            LOG.error("Unable to initialize ObjCEventClassesGenerator", e);
        }
    }

    public static List<TarEntryData> generateEventSources(List<EventFamilyMetadata> eventFamilies) {
        List<TarEntryData> eventSources = new ArrayList<>();

        String eventFamilyFactoryImports = "";
        String eventFamilyFactoryProperties = "";
        String eventFamilyFactoryMethodsHeader = "";
        String eventFamilyFactoryMethodsSource = "";

        StringBuilder eventGenHeaderBuilder = new StringBuilder();
        StringBuilder eventGenSourceBuilder = new StringBuilder();

        LOG.debug("Received {} event families", eventFamilies.size());
        for (EventFamilyMetadata efm : eventFamilies) {

            LOG.debug("Generating schemas for event family {}", efm.getEcfName());
            Schema schema = new Schema.Parser().parse(efm.getEcfSchema());
            try (
                    OutputStream hdrStream = new ByteArrayOutputStream();
                    OutputStream srcStream = new ByteArrayOutputStream()
            ) {

                Compiler compiler = new ObjectiveCCompiler(schema, EVENT_GEN, hdrStream, srcStream);
                compiler.generate();

                eventGenHeaderBuilder.append(hdrStream.toString()).append("\n");
                eventGenSourceBuilder.append(srcStream.toString()).append("\n");
            } catch (Exception e) {
                LOG.error("Got exception while generating object from schema: " + efm.getEcfSchema(), e);
            }

            LOG.error("Processing {}", efm.getEcfName());
            eventFamilyFactoryImports += "#import \"" + efm.getEcfClassName() + ".h\"\n";

            String eventFamilyVarName = efm.getEcfClassName().substring(0, 1).toLowerCase();
            if (efm.getEcfClassName().length() > 1) {
                eventFamilyVarName += efm.getEcfClassName().substring(1);
            }

            eventFamilyFactoryProperties += eventFamilyFactoryProperty.
                    replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()).
                    replaceAll(EVENT_FAMILY_VAR_NAME_VAR, eventFamilyVarName);
            eventFamilyFactoryMethodsHeader += eventFamilyFactoryMethodHeader.
                    replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()).
                    replaceAll(EVENT_FAMILY_VAR_NAME_VAR, eventFamilyVarName);
            eventFamilyFactoryMethodsSource += eventFamilyFactoryMethodSource.
                    replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()).
                    replaceAll(EVENT_FAMILY_VAR_NAME_VAR, eventFamilyVarName);

            String addSupportedEventClassFqns = "";
            String eventFamilyListenersOnGenericEvent = "";
            String eventFamilySendEventHeaderMethods = "";
            String eventFamilySendEventSourceMethods = "";
            String eventFamilyListenerMethods = "";

            for (ApplicationEventMapDto eventMap : efm.getEventMaps()) {
                String eventClassName = eventFqnToClassName(eventMap.getFqn());
                if (eventMap.getAction() == ApplicationEventAction.SINK ||
                        eventMap.getAction() == ApplicationEventAction.BOTH) {
                    addSupportedEventClassFqns += eventFamilyAddSupportedFqn
                            .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName)
                            .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn());
                    if (eventFamilyListenersOnGenericEvent.length() > 0) {
                        eventFamilyListenersOnGenericEvent += "else ";
                    }
                    eventFamilyListenersOnGenericEvent += eventFamilyOnGenericEvent
                            .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName)
                            .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
                            .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                    eventFamilyListenerMethods += eventFamilyListenerMethod
                            .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName)
                            .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                }
                if (eventMap.getAction() == ApplicationEventAction.SOURCE ||
                        eventMap.getAction() == ApplicationEventAction.BOTH) {
                    eventFamilySendEventHeaderMethods += eventFamilySendEventHeaderMethod
                            .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName)
                            .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                    eventFamilySendEventSourceMethods += eventFamilySendEventSourceMethod
                            .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName)
                            .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn()) + "\n";
                }
            }
            String resultFamilyHeader = eventFamilyHeader
                    .replaceAll(EVENT_FAMILY_NAMESPACE_VAR, efm.getEcfNamespace())
                    .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
                    .replaceAll(ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR, addSupportedEventClassFqns)
                    .replaceAll(EVENT_FAMILY_LISTENERS_ON_GENERIC_EVENT_VAR, eventFamilyListenersOnGenericEvent)
                    .replaceAll(EVENT_FAMILY_SEND_EVENT_METHODS_VAR, eventFamilySendEventHeaderMethods)
                    .replaceAll(EVENT_FAMILY_LISTENER_METHODS_VAR, eventFamilyListenerMethods);

            eventGenHeaderBuilder.append(resultFamilyHeader).append("\n");

            String resultFamilySource = eventFamilySource
                    .replaceAll(EVENT_FAMILY_NAMESPACE_VAR, efm.getEcfNamespace())
                    .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
                    .replaceAll(ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR, addSupportedEventClassFqns)
                    .replaceAll(EVENT_FAMILY_LISTENERS_ON_GENERIC_EVENT_VAR, eventFamilyListenersOnGenericEvent)
                    .replaceAll(EVENT_FAMILY_SEND_EVENT_METHODS_VAR, eventFamilySendEventSourceMethods)
                    .replaceAll(EVENT_FAMILY_LISTENER_METHODS_VAR, eventFamilyListenerMethods);

            eventGenSourceBuilder.append(resultFamilySource).append("\n");
        }

        eventSources.add(CommonSdkUtil.tarEntryForSources(eventGenHeaderBuilder.toString(), GEN_ROOT + EVENT_GEN + ".h"));
        eventSources.add(CommonSdkUtil.tarEntryForSources(eventGenSourceBuilder.toString(), GEN_ROOT + EVENT_GEN + ".m"));

        LOG.debug("Generating event family factory");
        String resultEventFamilyFactory = eventFamilyFactoryHeader.
                replaceAll(EVENT_FAMILY_FACTORY_IMPORTS_VAR, eventFamilyFactoryImports).
                replaceAll(EVENT_FAMILY_FACTORY_METHODS_VAR, eventFamilyFactoryMethodsHeader);

        eventSources.add(CommonSdkUtil.tarEntryForSources(resultEventFamilyFactory, EVENT_FACTORY_PATH + "h"));
        resultEventFamilyFactory = eventFamilyFactorySource.
                replaceAll(EVENT_FAMILY_FACTORY_PROPERTIES_VAR, eventFamilyFactoryProperties).
                replaceAll(EVENT_FAMILY_FACTORY_METHODS_VAR, eventFamilyFactoryMethodsSource);

        eventSources.add(CommonSdkUtil.tarEntryForSources(resultEventFamilyFactory, EVENT_FACTORY_PATH + "m"));

        return eventSources;
    }

    private static String eventFqnToClassName(String fqn) {
        if (fqn == null || fqn.isEmpty()) {
            throw new RuntimeException("Failed to get class name from fqn: " + fqn);
        }
        return fqn.substring(fqn.lastIndexOf('.') + 1);
    }
}
