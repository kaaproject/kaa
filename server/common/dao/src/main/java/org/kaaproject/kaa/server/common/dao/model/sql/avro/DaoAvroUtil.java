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
package org.kaaproject.kaa.server.common.dao.model.sql.avro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.dto.logs.avro.FileAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.MongoAppenderParametersDto;
import org.kaaproject.kaa.server.common.dao.model.sql.avro.gen.FileAppenderParameters;
import org.kaaproject.kaa.server.common.dao.model.sql.avro.gen.FlumeAppenderParameters;
import org.kaaproject.kaa.server.common.dao.model.sql.avro.gen.FlumeBalancingType;
import org.kaaproject.kaa.server.common.dao.model.sql.avro.gen.HostInfo;
import org.kaaproject.kaa.server.common.dao.model.sql.avro.gen.LogAppenderParameters;
import org.kaaproject.kaa.server.common.dao.model.sql.avro.gen.MongoAppenderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoAvroUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DaoAvroUtil.class);

    public static LogAppenderParametersDto convertParametersFromBytes(byte[] bytes) {
        LogAppenderParametersDto dto = null;
        AvroByteArrayConverter<LogAppenderParameters> converter = new AvroByteArrayConverter<LogAppenderParameters>(LogAppenderParameters.class);
        try {
            LogAppenderParameters logParameters = converter.fromByteArray(bytes);
            if (logParameters != null) {
                dto = new LogAppenderParametersDto();

                Object parameters = logParameters.getAppenderParameters();
                if (parameters instanceof FlumeAppenderParameters) {
                    FlumeAppenderParametersDto flumeParameters = new FlumeAppenderParametersDto();
                    FlumeAppenderParameters avroParameters = (FlumeAppenderParameters) parameters;
                    List<Object> hosts = avroParameters.getHostsInfo();
                    if (hosts != null && !hosts.isEmpty()) {
                        List<HostInfoDto> hostInfoList = new ArrayList<>(hosts.size());
                        for (Object o : hosts) {
                            HostInfo host = (HostInfo) o;
                            hostInfoList.add(new HostInfoDto(host.getHostname(), host.getPort(), host.getPriority()));
                        }
                        flumeParameters.setHosts(hostInfoList);
                    }
                    flumeParameters.setBalancingType(balancingTypeFromAvro(avroParameters.getBalancingType()));
                    dto.setParameters(flumeParameters);
                } else if (parameters instanceof MongoAppenderParameters) {

                    MongoAppenderParameters mongoParameters = (MongoAppenderParameters) parameters;
                    dto.setParameters(new MongoAppenderParametersDto(mongoParameters.getCollectionName()));
                } else if (parameters instanceof FileAppenderParameters) {

                    FileAppenderParameters fileParameters = (FileAppenderParameters) parameters;
                    FileAppenderParametersDto fileDto = new FileAppenderParametersDto();
                    fileDto.setLogDirectoryPath(fileParameters.getLogDirectoryPath());
                    fileDto.setUsername(fileParameters.getUsername());
                    fileDto.setSshKey(fileParameters.getSshKey());
                    dto.setParameters(fileDto);
                }
            }

        } catch (IOException e) {
            LOG.warn("Can't convert bytes to log appender parameters.");
        }
        return dto;
    }

    public static <T> byte[] convertParametersToBytes(LogAppenderParametersDto parameters) {
        byte[] bytes = null;
        if (parameters != null) {
            LogAppenderParameters avroParameters = new LogAppenderParameters();

            Object params = parameters.getParameters();
            if (params instanceof FlumeAppenderParametersDto) {
                FlumeAppenderParametersDto flumeDto = (FlumeAppenderParametersDto) params;
                List<HostInfoDto> hostInfoList = flumeDto.getHosts();
                List<Object> hosts = Collections.emptyList();
                if (hostInfoList != null && !hostInfoList.isEmpty()) {
                    hosts = new ArrayList<>();
                    for (HostInfoDto dto : hostInfoList) {
                        hosts.add(new HostInfo(dto.getHostname(), dto.getPort(), dto.getPriority()));
                    }
                }
                avroParameters.setAppenderParameters(new FlumeAppenderParameters(hosts, balancingTypeToAvro(flumeDto.getBalancingType())));

            } else if (params instanceof MongoAppenderParametersDto) {
                MongoAppenderParametersDto mongoDto = (MongoAppenderParametersDto) params;
                avroParameters.setAppenderParameters(new MongoAppenderParameters(mongoDto.getCollectionName()));
            } else if (params instanceof FileAppenderParametersDto) {
                FileAppenderParametersDto fileDto = (FileAppenderParametersDto) params;
                avroParameters.setAppenderParameters(new FileAppenderParameters(fileDto.getLogDirectoryPath(), fileDto.getUsername(), fileDto.getSshKey()));
            }
            AvroByteArrayConverter<LogAppenderParameters> converter = new AvroByteArrayConverter<LogAppenderParameters>(LogAppenderParameters.class);
            try {
                bytes = converter.toByteArray(avroParameters);
            } catch (IOException e) {
                LOG.warn("Can't convert log appender parameters to bytes.");
            }
        }
        return bytes;
    }

    private static FlumeBalancingType balancingTypeToAvro(FlumeBalancingTypeDto type) {
        FlumeBalancingType converted = null;
        for (FlumeBalancingType current : FlumeBalancingType.values()) {
            if (current.name().equalsIgnoreCase(type.name())) {
                converted = current;
            }
        }
        return converted;
    }

    private static FlumeBalancingTypeDto balancingTypeFromAvro(FlumeBalancingType type) {
        FlumeBalancingTypeDto converted = null;
        for (FlumeBalancingTypeDto current : FlumeBalancingTypeDto.values()) {
            if (current.name().equalsIgnoreCase(type.name())) {
                converted = current;
            }
        }
        return converted;
    }
}
