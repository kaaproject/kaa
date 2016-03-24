package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.model.Credentials;

import java.nio.ByteBuffer;

/**
 * The interface Credentials dao
 * @param <T> the type parameter
 */
public interface CredentialsDao<T extends Credentials> extends Dao<T, ByteBuffer> {

    /**
     * Saves given dto
     * @param dto
     * @return
     */
    T save(CredentialsDto dto);


    /**
     * Find credential by id
     * @param id the credential id
     * @return credential object
     */
    T findById(String id);


    /**
     * Updates credential's status by id
     * @param id credential's id to be updated
     * @param status status to update
     * @return updated credential object
     */
    T update(String id, CredentialsStatus status);


    /**
     * Removes credential by id
     * @param id credential id to be deleted
     */
    void removeById(String id);
}
