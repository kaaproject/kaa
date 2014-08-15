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
package org.kaaproject.kaa.server.flume.sink.hdfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.kaaproject.kaa.server.common.flume.shared.avro.gen.LogData;

import com.google.common.collect.Lists;

public class KaaEventFactory {

    private SpecificDatumReader<LogData> avroReader;
    private BinaryDecoder decoder;
    private Map<KaaSinkKey, Map<String,String>> headersMap = new HashMap<>();
    
    public KaaEventFactory() {
        avroReader = new SpecificDatumReader<>(LogData.class);
    }
    
    public Map<KaaSinkKey, List<Event>> processIncomingFlumeEvent(Event event) throws IOException {
        Map<KaaSinkKey, List<Event>> eventsMap = new LinkedHashMap<KaaSinkKey, List<Event>>(); 
        
        byte[] body = event.getBody();
        decoder = DecoderFactory.get().binaryDecoder(body, decoder);
        
        LogData data = avroReader.read(null, decoder);
        KaaSinkKey sinkKey = new KaaSinkKey(data.getApplicationToken(), data.getSchemaVersion());
        Map<String,String> headers = headersMap.get(sinkKey);
        if (headers == null) {
            headers = new HashMap<>();
            sinkKey.updateHeaders(headers);
            headersMap.put(sinkKey, headers);
        }
        List<Event> events = Lists.newArrayList();
        for (ByteBuffer eventData : data.getLogEvents()) {
            Event logEvent = EventBuilder.withBody(eventData.array(), headers);
            events.add(logEvent);
        }
        eventsMap.put(sinkKey, events);
        return eventsMap;
    }
    
}
