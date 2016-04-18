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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.kaaproject.kaa.avro.avrogen.compiler.CCompiler;
import org.kaaproject.kaa.avro.avrogen.compiler.Compiler;
import org.kaaproject.kaa.avro.avrogen.StyleUtils;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CEventSourcesGenerator {
    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CppEventSourcesGenerator.class);

    /**
     * The KAA_SRC_FOLDER variable is also set in CMakeList.txt located in the root folder of the C SDK project.
     */
    private static final String KAA_SRC_FOLDER = "src/kaa";
    private static final String EVENT_SOURCE_OUTPUT = KAA_SRC_FOLDER + "/gen/";

    private static final String NAME_PREFIX_TEMPLATE = "kaa_{name}";
    private static final String EVENT_FAMILY_DEFINITION_PATTERN = "kaa_{name}_definitions";

    private static final String EVENT_FAMILIES_H_PATTERN = "sdk/c/event/kaa_event_families.hvm";
    private static final String EVENT_FAMILIES_H_FILE = "kaa_{name}.h";
    private static final String EVENT_FAMILIES_C_PATTERN = "sdk/c/event/kaa_event_families.cvm";
    private static final String EVENT_FAMILIES_C_FILE = "kaa_{name}.c";
    private static final String EVENT_FQN_H_FILE = "kaa_event_fqn_definitions.h";
    private static final String EVENT_FQN_PATTERN = "sdk/c/event/kaa_event_fqn_definitions.hvm";
    
    private static final VelocityEngine velocityEngine; //NOSONAR
    static {
        velocityEngine = new VelocityEngine();

        velocityEngine.addProperty("resource.loader", "class, file");
        velocityEngine.addProperty("class.resource.loader.class",
                                   "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.addProperty("file.resource.loader.class",
                                   "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        velocityEngine.addProperty("file.resource.loader.path", "/, .");
        velocityEngine.setProperty("runtime.references.strict", true);
        velocityEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
    }

    public static List<TarEntryData> generateEventSources(List<EventFamilyMetadata> eventFamilies) {
        List<TarEntryData> eventSources = new ArrayList<>();

        LOG.debug("[sdk generateEventSources] eventFamilies.size(): {}", eventFamilies.size());

        VelocityContext context = new VelocityContext();
        context.put("eventFamilies", eventFamilies);
        StringWriter commonWriter = new StringWriter();
        velocityEngine.getTemplate(EVENT_FQN_PATTERN).merge(context, commonWriter);

        for (EventFamilyMetadata eventFamily : eventFamilies) {
            context.put("StyleUtils", StyleUtils.class);
            String name = StyleUtils.toLowerUnderScore(eventFamily.getEcfClassName());
            String NAME = StyleUtils.toUpperUnderScore(eventFamily.getEcfClassName());
            context.put("event_family_name", name);
            context.put("EVENT_FAMILY_NAME", NAME);
            context.put("namespacePrefix", NAME_PREFIX_TEMPLATE.replace("{name}", name));
            Schema eventFamilySchema = new Schema.Parser().parse(eventFamily.getEcfSchema());

            List<Schema> schemas = eventFamilySchema.getTypes();
            List<String> emptyRecords = new ArrayList<>();
            if (schemas != null) {
                for (Schema recordS : schemas) {
                    if (recordS.getType() == Type.RECORD && recordS.getFields() != null && recordS.getFields().size() == 0) {
                        emptyRecords.add(recordS.getFullName());
                    }
                }
            }
            context.put("emptyRecords", emptyRecords);

            List<String> incomingEventFqns = new ArrayList<>();
            List<String> outgoingEventFqns = new ArrayList<>();
            if (eventFamily.getEventMaps() != null) {
                for (ApplicationEventMapDto appEventDto : eventFamily.getEventMaps()) {
                    if (appEventDto.getAction() == ApplicationEventAction.SINK ||
                            appEventDto.getAction() == ApplicationEventAction.BOTH) {
                        incomingEventFqns.add(appEventDto.getFqn());
                    }
                    if (appEventDto.getAction() == ApplicationEventAction.SOURCE ||
                            appEventDto.getAction() == ApplicationEventAction.BOTH) {
                        outgoingEventFqns.add(appEventDto.getFqn());
                    }
                }
            }
            context.put("incomingEventFqns", incomingEventFqns);
            context.put("outgoingEventFqns", outgoingEventFqns);

            StringWriter headerWriter = new StringWriter();
            LOG.debug("[sdk generateEventSources] header generating:");
            velocityEngine.getTemplate(EVENT_FAMILIES_H_PATTERN).merge(context, headerWriter);

            TarArchiveEntry entry = new TarArchiveEntry(EVENT_SOURCE_OUTPUT +
                    EVENT_FAMILIES_H_FILE.replace("{name}", name));

            LOG.debug("[sdk generateEventSources] header generated: {}", entry.getName());

            byte[] data = headerWriter.toString().getBytes();
            entry.setSize(data.length);
            TarEntryData tarEntry = new TarEntryData(entry, data);
            eventSources.add(tarEntry);


            StringWriter sourceWriter = new StringWriter();
            LOG.debug("[sdk generateEventSources] source generating:");
            velocityEngine.getTemplate(EVENT_FAMILIES_C_PATTERN).merge(context, sourceWriter);

            entry = new TarArchiveEntry(EVENT_SOURCE_OUTPUT +
                    EVENT_FAMILIES_C_FILE.replace("{name}", name));

            LOG.debug("[sdk generateEventSources] source generated: {}", entry.getName());

            data = sourceWriter.toString().getBytes();
            entry.setSize(data.length);
            tarEntry = new TarEntryData(entry, data);
            eventSources.add(tarEntry);

            entry = new TarArchiveEntry(EVENT_SOURCE_OUTPUT + EVENT_FQN_H_FILE);
            data = commonWriter.toString().getBytes();
            entry.setSize(data.length);
            tarEntry = new TarEntryData(entry, data);
            eventSources.add(tarEntry);


            try (
                OutputStream hdrStream = new ByteArrayOutputStream();
                OutputStream srcStream = new ByteArrayOutputStream();) {
                String fileName = EVENT_FAMILY_DEFINITION_PATTERN.replace("{name}", name);
                Compiler compiler = new CCompiler(eventFamilySchema, fileName, hdrStream, srcStream);
                compiler.setNamespacePrefix(NAME_PREFIX_TEMPLATE.replace("{name}", name));
                compiler.generate();

                String eventData = hdrStream.toString();

                entry = new TarArchiveEntry(EVENT_SOURCE_OUTPUT + fileName + ".h");
                entry.setSize(eventData.length());
                tarEntry = new TarEntryData(entry, eventData.getBytes());
                eventSources.add(tarEntry);

                entry = new TarArchiveEntry(EVENT_SOURCE_OUTPUT + fileName + ".c");
                eventData = srcStream.toString();
                entry.setSize(eventData.length());
                tarEntry = new TarEntryData(entry, eventData.getBytes());
                eventSources.add(tarEntry);
            } catch (Exception e) {
                LOG.error("got exception", e);
            }
        }

        LOG.debug("[sdk generateEventSources] exit. Generated {} files", eventSources.size());

        return eventSources;
    }
}
