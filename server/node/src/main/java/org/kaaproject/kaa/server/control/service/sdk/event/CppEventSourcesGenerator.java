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

import static java.util.stream.Collectors.joining;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.server.control.service.sdk.SdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CppEventSourcesGenerator {


  private static final Logger LOG = LoggerFactory.getLogger(CppEventSourcesGenerator.class);


  private static final String EVENT_FAMILY_HPP_TEMPLATE = "sdk/cpp/event/EventFamily.hpp.template";


  private static final String EVENT_FAMILY_ADD_SUPPORTED_FQN_TEMPLATE =
      "sdk/cpp/event/addSupportedEventClassFqns.template";


  private static final String EVENT_FAMILY_ON_GENERIC_EVENT_TEMPLATE =
      "sdk/cpp/event/eventFamilyOnGenericEvent.template";


  private static final String EVENT_FAMILY_NOTIFY_LISTENER_TEMPLATE =
      "sdk/cpp/event/eventFamilyNotifyListener.template";


  private static final String EVENT_FAMILY_SEND_EVENT_METHODS_TEMPLATE =
      "sdk/cpp/event/eventFamilySendEventMethods.template";


  private static final String EVENT_FAMILY_LISTENER_METHOD_TEMPLATE =
      "sdk/cpp/event/eventFamilyListenerMethod.template";


  private static final String EVENT_FAMILY_FACTORY_HPP_TEMPLATE =
      "sdk/cpp/event/EventFamilyFactory.hpp.template";


  private static final String EVENT_FAMILY_INCLUDE_HEADERS_TEMPLATE =
      "sdk/cpp/event/includeEventFamilyHeaders.template";


  private static final String EVENT_FAMILY_FACTORY_ADD_EVENT_FAMILY_TEMPLATE =
      "sdk/cpp/event/eventFamilyFactoryAddConcreteEventFamily.template";


  private static final String EVENT_FAMILY_FACTORY_GET_EVENT_FAMILY_TEMPLATE =
      "sdk/cpp/event/eventFamilyFactoryGetConcreteEventFamily.template";


  private static final String AVRO_STRUCT_GENERATOR_TEMPLATE = "sdk/cpp/event/AvroStructGenerator.cmake.template";

  private static final String EVENT_FAMILY_FACTORY_SET_CONCRETE_EVENT_FAMILY_NAMES_TEMPLATE =
      "sdk/cpp/event/eventFamilyFactorySetEventFamilyClassNames.template";

  private static final String EVENT_FAMILY_CLASS_NAME_VAR = "\\$\\{event_family_class_name\\}";
  private static final String EVENT_FAMILY_LISTENER_METHODS_VAR =
      "\\$\\{event_family_listener_methods\\}";

  private static final String ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR =
      "\\$\\{add_supported_event_class_fqns\\}";

  private static final String EVENT_FAMILY_LISTENERS_ON_GENERIC_EVENT_VAR =
      "\\$\\{event_family_listeners_on_generic_event\\}";

  private static final String EVENT_FAMILY_LISTENERS_NOTIFY_LISTENER_VAR =
      "\\$\\{event_family_listeners_notify_listener\\}";

  private static final String EVENT_FAMILY_SEND_EVENT_METHODS_VAR =
      "\\$\\{event_family_send_event_methods\\}";

  private static final String EVENT_CLASS_FQN_VAR = "\\$\\{event_class_fqn\\}";
  private static final String EVENT_CLASS_NAME_VAR = "\\$\\{event_class_name\\}";

  private static final String INCLUDE_EVENT_FAMILY_HEADERS_VAR =
      "\\$\\{include_event_family_headers\\}";

  private static final String EVENT_FAMILY_FACTORY_GET_CONCRETE_EVENT_FAMILY_VAR =
      "\\$\\{event_family_factory_get_concrete_event_family\\}";

  private static final String EVENT_FAMILY_FACTORY_SET_CONCRETE_EVENT_FAMILY_NAMES_VAR =
      "\\$\\{event_family_factory_set_concrete_event_family_names\\}";

  private static final String EVENT_FAMILY_FACTORY_ADD_CONCRETE_EVENT_FAMILY_VAR =
      "\\$\\{event_family_factory_add_concrete_event_family\\}";

  private static final String EVENT_FAMILY_CLASS_LIST_VAR = "\\[event_family_class_list\\]";

  private static final String EVENT_FAMILY_FACTORY_PATH = "kaa/event/gen/EventFamilyFactory.hpp";

  private static final String EVENT_FAMILY_PATH_TEMPLATE =
      "kaa/event/gen/${event_family_class_name}.hpp";

  private static final String EVENT_FAMILY_SCHEMA_PATH_TEMPLATE =
      "avro/event/${event_family_class_name}.avsc";

  private static final String AVRO_STRUCT_GENERATOR = "Modules/AvroStructGenerator.cmake";

  private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(".*?([^.]+)$");

  private static String eventFamilyHpp;
  private static String eventFamilyAddSupportedFqn;
  private static String eventFamilyOnGenericEvent;
  private static String eventFamilyNotifyListener;
  private static String eventFamilySendEventMethod;
  private static String eventFamilyListenerMethod;
  private static String eventFamilyFactoryHpp;
  private static String eventFamilyIncludeHeaders;
  private static String eventFamilyFactoryGetEventFamily;
  private static String eventFamilyFactorySetEventFamilyNames;
  private static String eventFamilyFactoryAddEventFamily;
  private static String avrogen;

  static {
    try {
      eventFamilyHpp = SdkGenerator.readResource(EVENT_FAMILY_HPP_TEMPLATE);

      eventFamilyAddSupportedFqn = SdkGenerator
          .readResource(EVENT_FAMILY_ADD_SUPPORTED_FQN_TEMPLATE);

      eventFamilyOnGenericEvent = SdkGenerator.readResource(EVENT_FAMILY_ON_GENERIC_EVENT_TEMPLATE);
      eventFamilyNotifyListener = SdkGenerator.readResource(EVENT_FAMILY_NOTIFY_LISTENER_TEMPLATE);

      eventFamilySendEventMethod = SdkGenerator
          .readResource(EVENT_FAMILY_SEND_EVENT_METHODS_TEMPLATE);

      eventFamilyListenerMethod = SdkGenerator.readResource(EVENT_FAMILY_LISTENER_METHOD_TEMPLATE);
      eventFamilyFactoryHpp = SdkGenerator.readResource(EVENT_FAMILY_FACTORY_HPP_TEMPLATE);
      eventFamilyIncludeHeaders = SdkGenerator.readResource(EVENT_FAMILY_INCLUDE_HEADERS_TEMPLATE);

      eventFamilyFactoryGetEventFamily = SdkGenerator
          .readResource(EVENT_FAMILY_FACTORY_GET_EVENT_FAMILY_TEMPLATE);

      eventFamilyFactorySetEventFamilyNames = SdkGenerator
          .readResource(EVENT_FAMILY_FACTORY_SET_CONCRETE_EVENT_FAMILY_NAMES_TEMPLATE);

      eventFamilyFactoryAddEventFamily = SdkGenerator
          .readResource(EVENT_FAMILY_FACTORY_ADD_EVENT_FAMILY_TEMPLATE);

      avrogen = SdkGenerator.readResource(AVRO_STRUCT_GENERATOR_TEMPLATE);
    } catch (IOException ex) {
      LOG.error("Unable to initialize CppEventSourcesGenerator", ex);
    }
  }

  private CppEventSourcesGenerator() {
  }

  /**
   * Create new zip entry data for the event family metadata.
   *
   * @param eventFamilies the event family metadata
   * @return a new zip entry data
   */
  public static List<TarEntryData> generateEventSources(List<EventFamilyMetadata> eventFamilies) {
    List<TarEntryData> eventSources = new ArrayList<>();

    String includeEventFamilyHeaders = "";
    String factoryGetEventFamilies = "";
    String factorySetEventFamilyNames = "";
    String factoryAddEventFamilies = "";
    String eventFamilyClassList = "";

    for (EventFamilyMetadata efm : eventFamilies) {
      includeEventFamilyHeaders += eventFamilyIncludeHeaders
          .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()) + "\n";

      factoryGetEventFamilies += eventFamilyFactoryGetEventFamily
          .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()) + "\n";

      factoryAddEventFamilies += eventFamilyFactoryAddEventFamily
          .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName()) + "\n";

      if (eventFamilyClassList.length() > 0) {
        eventFamilyClassList += " ";
      }
      eventFamilyClassList += efm.getEcfClassName();

      String supportedFqnsList = "";
      String eventFamilyListenersOnGenericEvent = "";
      String eventFamilyListenersNotifyListener = "";
      String eventFamilySendEventMethods = "";
      String eventFamilyListenerMethods = "";

      for (ApplicationEventMapDto eventMap : efm.getEventMaps()) {

        Matcher matcher = CLASS_NAME_PATTERN.matcher(eventMap.getFqn());
        String eventClassName = (matcher.find()) ? matcher.group(1) : "";

        if (eventMap.getAction() == ApplicationEventAction.SINK
            || eventMap.getAction() == ApplicationEventAction.BOTH) {

          if (supportedFqnsList.length() > 0) {
            supportedFqnsList += ",";
          }
          supportedFqnsList += "\"" + eventMap.getFqn() + "\"";
          if (eventFamilyListenersOnGenericEvent.length() > 0) {
            eventFamilyListenersOnGenericEvent += "else ";
          }
          if (eventFamilyListenersNotifyListener.length() > 0) {
            eventFamilyListenersNotifyListener += "else ";
          }
          eventFamilyListenersOnGenericEvent += eventFamilyOnGenericEvent
              .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
              .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn())
              .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName) + "\n";

          eventFamilyListenersNotifyListener += eventFamilyNotifyListener
              .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
              .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn())
              .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName) + "\n";

          eventFamilyListenerMethods += eventFamilyListenerMethod
              .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
              .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName) + "\n";

        }
        if (eventMap.getAction() == ApplicationEventAction.SOURCE
            || eventMap.getAction() == ApplicationEventAction.BOTH) {
          eventFamilySendEventMethods += eventFamilySendEventMethod
              .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
              .replaceAll(EVENT_CLASS_FQN_VAR, eventMap.getFqn())
              .replaceAll(EVENT_CLASS_NAME_VAR, eventClassName) + "\n";

        }
      }


      String addSupportedEventClassFqns = eventFamilyAddSupportedFqn
          .replaceAll(EVENT_CLASS_FQN_VAR, supportedFqnsList);

      factorySetEventFamilyNames += eventFamilyFactorySetEventFamilyNames
          .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
          .replaceAll(ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR, addSupportedEventClassFqns) + "\n";

      String eventFamilySource = eventFamilyHpp
          .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName())
          .replaceAll(ADD_SUPPORTED_EVENT_CLASS_FQNS_VAR, addSupportedEventClassFqns)
          .replaceAll(EVENT_FAMILY_LISTENERS_ON_GENERIC_EVENT_VAR,
              eventFamilyListenersOnGenericEvent)
          .replaceAll(EVENT_FAMILY_LISTENERS_NOTIFY_LISTENER_VAR,
              eventFamilyListenersNotifyListener)
          .replaceAll(EVENT_FAMILY_SEND_EVENT_METHODS_VAR, eventFamilySendEventMethods)
          .replaceAll(EVENT_FAMILY_LISTENER_METHODS_VAR, eventFamilyListenerMethods);

      String eventFamilyPath = EVENT_FAMILY_PATH_TEMPLATE
          .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName());

      TarArchiveEntry entry = new TarArchiveEntry(eventFamilyPath);
      byte[] data = eventFamilySource.getBytes();
      entry.setSize(data.length);
      TarEntryData tarEntry = new TarEntryData(entry, data);
      eventSources.add(tarEntry);

      String eventCtlSchemas = "[" + efm.getRawCtlsSchemas().stream().collect(joining(",")) + "]";
      String eventFamilySchemaPath = EVENT_FAMILY_SCHEMA_PATH_TEMPLATE
          .replaceAll(EVENT_FAMILY_CLASS_NAME_VAR, efm.getEcfClassName());

      entry = new TarArchiveEntry(eventFamilySchemaPath);
      data = eventCtlSchemas.getBytes();
      entry.setSize(data.length);
      tarEntry = new TarEntryData(entry, data);
      eventSources.add(tarEntry);

    }

    String eventFamilyFactorySource = eventFamilyFactoryHpp
        .replaceAll(INCLUDE_EVENT_FAMILY_HEADERS_VAR, includeEventFamilyHeaders)
        .replaceAll(EVENT_FAMILY_FACTORY_GET_CONCRETE_EVENT_FAMILY_VAR, factoryGetEventFamilies)
        .replaceAll(EVENT_FAMILY_FACTORY_ADD_CONCRETE_EVENT_FAMILY_VAR, factoryAddEventFamilies)
        .replaceAll(EVENT_FAMILY_FACTORY_SET_CONCRETE_EVENT_FAMILY_NAMES_VAR,
            factorySetEventFamilyNames);

    TarArchiveEntry entry = new TarArchiveEntry(EVENT_FAMILY_FACTORY_PATH);
    byte[] data = eventFamilyFactorySource.getBytes();
    entry.setSize(data.length);
    TarEntryData tarEntry = new TarEntryData(entry, data);
    eventSources.add(tarEntry);

    String avrogenSource = avrogen.replaceAll(EVENT_FAMILY_CLASS_LIST_VAR, eventFamilyClassList);

    entry = new TarArchiveEntry(AVRO_STRUCT_GENERATOR);
    data = avrogenSource.getBytes();
    entry.setSize(data.length);
    tarEntry = new TarEntryData(entry, data);
    eventSources.add(tarEntry);

    return eventSources;
  }

}
