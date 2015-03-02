package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;

import java.util.List;

public interface EndpointUserConfigurationDao<T extends EndpointUserConfiguration> extends Dao<T, String> {

    /**
     *
     * @param dto the endpoint user configuration
     * @return the saved endpoint user configuration object
     */
    T save(EndpointUserConfigurationDto dto);

    /**
     *
     * @param userId the endpoint user id
     * @param appToken the application token
     * @param schemaVersion the schema version
     * @return
     */
    T findByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);


    /**
     *
     * @param userId
     * @return
     */
    List<T> findByUserId(String userId);

    /**
     *
     * @param userId the endpoint user id
     * @param appToken the application token
     * @param schemaVersion the schema version
     */
    void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);

}
