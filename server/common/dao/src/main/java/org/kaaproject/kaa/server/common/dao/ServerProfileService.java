package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;

import java.util.List;

/**
 * Server profile service
 */
public interface ServerProfileService {

    /**
     * Save server profile schema.
     *
     * @param dto the unsaved server profile schema.
     * @return the saved server profile schema.
     */
    ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto dto);

    /**
     * Find latest server profile schema for application with given identifier.
     *
     * @param appId the application identifier
     * @return the latest server profile schema.
     */
    ServerProfileSchemaDto findLatestServerProfileSchema(String appId);

    /**
     * Find server profile schema with given identifier.
     *
     * @param profileId the server profile schema identifier.
     * @return the server profile schema.
     */
    ServerProfileSchemaDto findServerProfileSchema(String profileId);

    /**
     * Find server profile schemas with given application identifier.
     *
     * @param appId the application identifier
     * @return the list of server profile schemas for corresponding application.
     */
    List<ServerProfileSchemaDto> findServerProfileSchemasByAppId(String appId);

    /**
     * @param keyHash
     * @return
     */
    ServerProfileSchemaDto findServerProfileSchemaByKeyHash(byte[] keyHash);

    /**
     * Remove server profile schema with given identifier.
     *
     * @param profileId the server profile schema identifier.
     */
    void removeServerProfileSchemaById(String profileId);

    /**
     * Remove server profile schemas for application with the given identifier.
     *
     * @param appId the application identifier
     */
    void removeServerProfileSchemaByAppId(String appId);

    /**
     * Save server profile data to endpoint profile.
     *
     * @param keyHash       the endpoint key hash identifier.
     * @param serverProfile server profile data in string representation.
     * @return the saved endpoint profile.
     */
    EndpointProfileDto saveServerProfile(byte[] keyHash, String serverProfile);

}
