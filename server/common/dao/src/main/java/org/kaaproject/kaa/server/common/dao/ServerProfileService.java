package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;

import java.util.List;

public interface ServerProfileService {

    ServerProfileSchemaDto getServerProfileSchema(String profileId);

    List<ServerProfileSchemaDto> getServerProfileSchemasByAppId(String appId);

    ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto dto);


}
