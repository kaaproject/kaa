package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;

import java.util.List;

public interface UserConfigurationService {

    /**
     *
     * @param dto
     * @return
     */
    EndpointUserConfigurationDto saveUserConfiguration(EndpointUserConfigurationDto dto);

    /**
     *
     * @param userId
     * @param appToken
     * @param schemaVersion
     * @return
     */
    EndpointUserConfigurationDto findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);

    /**
     *
     * @param userId
     * @return
     */
    List<EndpointUserConfigurationDto> findUserConfigurationByUserId(String userId);

    /**
     *
     * @param userId
     * @param appToken
     * @param schemaVersion
     */
    void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);
}
