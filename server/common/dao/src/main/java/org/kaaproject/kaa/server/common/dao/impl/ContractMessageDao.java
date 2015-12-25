package org.kaaproject.kaa.server.common.dao.impl;

/**
 * Provides methods to retrieve contract messages
 * @param <T> the generic contract message parameter
 */
public interface ContractMessageDao<T> extends SqlDao<T> {

    /**
     * Find contract message by its fqn and version
     * @param fqn the fully qualified name of a contract message
     * @param version the contract message version
     * @return the found contract message
     */
    T findByFqnAndVersion(String fqn, Integer version);
}
