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

package org.kaaproject.kaa.examples.robotrun;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.EnumSymbol;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.examples.robotrun.gen.Border;
import org.kaaproject.kaa.examples.robotrun.gen.Borders;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordData;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class MapAggregatorSink extends AbstractSink implements Configurable, ConfigurationConstants {

    private static final Logger LOG = LoggerFactory.getLogger(MapAggregatorSink.class);
    
    private long txnEventMax;
    private String kaaRestHost;
    private int kaaRestPort;
    private String kaaRestUser;
    private String kaaRestPassword;

    private AdminClient adminClient;
    
    private RpcClient nextSink = null;
    
    private SpecificDatumReader<RecordData> avroRecordDataReader;
    private SpecificDatumReader<Borders> avroBordersReader;
    private BinaryDecoder decoder;
    
    private Map<String, String> appTokenToGroupMap = new HashMap<>();
    private Map<String, GenericAvroConverter<GenericRecord>> converterMap = new HashMap<>();
    
    @Override
    public Status process() throws EventDeliveryException {
        Channel channel = getChannel();
        Transaction transaction = channel.getTransaction();
        LOG.trace("process(), transaction.begin");
        transaction.begin();
        try {
            Event event = null;
            int txnEventCount = 0;
            
            Map<String, List<Borders>> updatedBordersMap = new HashMap<>();
            List<Event> events = new ArrayList<>();
            
            for (txnEventCount = 0; txnEventCount < txnEventMax; txnEventCount++) {
                event = channel.take();
                if (event == null) {
                    break;
                }
                events.add(event);
                byte[] body = event.getBody();
                decoder = DecoderFactory.get().binaryDecoder(body, decoder);
                
                RecordData data = avroRecordDataReader.read(null, decoder);
                
                String appToken = data.getRecordHeader().getApplicationToken();
                List<Borders> updatedBorders = updatedBordersMap.get(appToken);
                if (updatedBorders == null) {
                    updatedBorders = new ArrayList<>();
                    updatedBordersMap.put(appToken, updatedBorders);
                }
                
                for (ByteBuffer eventData : data.getEventRecords()) {
                    decoder = DecoderFactory.get().binaryDecoder(eventData.array(), decoder);
                    Borders borders = avroBordersReader.read(null, decoder);
                    updatedBorders.add(borders);
                }
            }
            processUpdatedBorders(updatedBordersMap);
            
            if (nextSink != null && !events.isEmpty()) {
                nextSink.appendBatch(events);
            }
            
            transaction.commit();
            
            if(event == null) {
                return Status.BACKOFF;
            }
            return Status.READY;
        }
        catch (Throwable th) {
            transaction.rollback();
            LOG.error("process failed", th);
            if (th instanceof Error) {
              throw (Error) th;
            } else {
              throw new EventDeliveryException(th);
            }
        } finally {
            LOG.trace("process(), transaction.close");
            transaction.close();
        }
    }
    
    private void processUpdatedBorders(Map<String, List<Borders>> updatedBordersMap) throws Exception {
        LOG.trace("processUpdatedBorders, updatedBordersMap.size={}", updatedBordersMap.size());
        for (String appToken : updatedBordersMap.keySet()) {
            List<Borders> updatedBorders = updatedBordersMap.get(appToken);
            ConfigurationDto configuration = getLastActiveConfiguration(appToken);
            GenericRecord labirynth = getLabyrinth(configuration);
            updateLabyrinth(labirynth, updatedBorders);
            updateConfiguration(configuration, labirynth);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateLabyrinth(GenericRecord labirynth, List<Borders> updatedBorders) {
        LOG.trace("Updating labyrinth, updatedBorders.size={}", updatedBorders.size());
        for (Borders borders : updatedBorders) {
            GenericArray<GenericRecord> hBordersArray = (GenericArray<GenericRecord>) labirynth.get("hBorders");
            GenericArray<GenericRecord> vBordersArray = (GenericArray<GenericRecord>) labirynth.get("vBorders");
            if (borders.getHBorders() != null) {
                LOG.trace("Updating hBorders, size={}", borders.getHBorders().size());
                updateBorders(hBordersArray, borders.getHBorders());
            }
            if (borders.getVBorders() != null) {
                LOG.trace("Updating vBorders, size={}", borders.getVBorders().size());
                updateBorders(vBordersArray, borders.getVBorders());
            }
        }
    }
    
    private void updateBorders(GenericArray<GenericRecord> originalBorders, List<Border> updatedBorders) {
        if (updatedBorders != null) {
            for (GenericRecord originalBorder : originalBorders) {
                int x = (Integer)originalBorder.get("x");
                int y = (Integer)originalBorder.get("y");
                for (Border updatedBorder : updatedBorders) {
                    if (x == updatedBorder.getX() &&
                            y == updatedBorder.getY()) {
                        LOG.trace("Updating border [{},{}] with type {}", x,y, updatedBorder.getType().name());
                        EnumSymbol borderTypeEnum = (EnumSymbol)originalBorder.get("type");
                        borderTypeEnum = new GenericData.EnumSymbol(borderTypeEnum.getSchema(), updatedBorder.getType().name());
                        originalBorder.put("type", borderTypeEnum);
                    }
                }
            }
        }
    }
    
    private ConfigurationDto getLastActiveConfiguration(String appToken) throws Exception {
        
        String endpointGroupId = appTokenToGroupMap.get(appToken);
        if (endpointGroupId == null) {
            List<ApplicationDto> applications = adminClient.getApplications();
            ApplicationDto application = null;
            for (ApplicationDto app : applications) {
                if (app.getApplicationToken().equals(appToken)) {
                    application = app;
                    break;
                }
            }
            if (application == null) {
                throw new RuntimeException("Can't find application by application token " +  appToken);
            }
            List<EndpointGroupDto> endpointGroups = adminClient.getEndpointGroups(application.getId());
            EndpointGroupDto endpointGroup = null;
            for (EndpointGroupDto group : endpointGroups) {
                if (group.getWeight()==0) {
                    endpointGroup = group;
                }
            }
            if (endpointGroup == null) {
                throw new RuntimeException("Can't get default endpoint group for application with token " +  appToken);
            }
            endpointGroupId = endpointGroup.getId();
            appTokenToGroupMap.put(appToken, endpointGroupId);
        }
        
        List<ConfigurationRecordDto> configurationRecords = adminClient.getConfigurationRecords(endpointGroupId, false);
        int version = -1;
        ConfigurationDto config = null;
        for (ConfigurationRecordDto configurationRecord : configurationRecords) {
            if (configurationRecord.getActiveConfiguration() != null && 
                    configurationRecord.getActiveConfiguration().getMajorVersion() > version) {
                    config = configurationRecord.getActiveConfiguration();
            }
        }
        return config;
    }
    
    private GenericRecord getLabyrinth(ConfigurationDto configuration) throws Exception {
        String body = configuration.getBody();
        GenericAvroConverter<GenericRecord> converter = getConverter(configuration.getSchemaId());
        return converter.decodeJson(body);
    }
    
    private void updateConfiguration(ConfigurationDto configuration, GenericRecord labirynth) throws Exception {
        LOG.trace("Updating configuration...");
        configuration.setId(null);
        GenericAvroConverter<GenericRecord> converter = getConverter(configuration.getSchemaId());
        String body = converter.encodeToJson(labirynth);
        configuration.setBody(body);
        configuration.setStatus(UpdateStatus.INACTIVE);
        configuration = adminClient.editConfiguration(configuration);
        adminClient.activateConfiguration(configuration.getId());
        LOG.trace("Configuration updated.");
    }
    
    private GenericAvroConverter<GenericRecord> getConverter(String schemaId) throws Exception {
        GenericAvroConverter<GenericRecord> converter = converterMap.get(schemaId);
        if (converter == null) {
            ConfigurationSchemaDto configSchemaDto = adminClient.getConfigurationSchema(schemaId);
            String baseSchemaString = configSchemaDto.getBaseSchema();
            Parser parser = new Parser();
            Schema baseSchema = parser.parse(baseSchemaString);
            converter = new GenericAvroConverter<GenericRecord>(baseSchema);
            converterMap.put(schemaId, converter);
        }
        return converter;
    }

    @Override
    public void configure(Context context) {
        txnEventMax = context.getLong(CONFIG_TXN_EVENT_MAX, DEFAULT_TXN_EVENT_MAX);
        kaaRestHost = context.getString(CONFIG_KAA_REST_HOST, DEFAULT_KAA_REST_HOST);
        kaaRestPort = context.getInteger(CONFIG_KAA_REST_PORT, DEFAULT_KAA_REST_PORT);
        kaaRestUser = context.getString(CONFIG_KAA_REST_USER);
        kaaRestPassword = context.getString(CONFIG_KAA_REST_PASSWORD);
        
        Preconditions.checkArgument(kaaRestUser != null && kaaRestUser.length() > 0,
                CONFIG_KAA_REST_USER + " must be specified.");
        Preconditions.checkArgument(kaaRestPassword != null && kaaRestPassword.length() > 0,
                CONFIG_KAA_REST_PASSWORD + " must be specified.");

        adminClient = new AdminClient(kaaRestHost, kaaRestPort);
        adminClient.login(kaaRestUser, kaaRestPassword);
        
        String nextSinkHost = context.getString(CONFIG_SINK_HOST, null);
        if (nextSinkHost != null) {
            int nextSinkPort = context.getInteger(CONFIG_SINK_PORT, -1);
            Preconditions.checkArgument(nextSinkPort > -1,
                    CONFIG_SINK_PORT + " must be specified.");
            nextSink = RpcClientFactory.getDefaultInstance(nextSinkHost, nextSinkPort);
        }

        avroRecordDataReader = new SpecificDatumReader<>(RecordData.class);
        avroBordersReader = new SpecificDatumReader<>(Borders.class);
    }


}
