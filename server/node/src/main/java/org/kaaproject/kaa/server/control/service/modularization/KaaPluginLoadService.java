package org.kaaproject.kaa.server.control.service.modularization;

import org.kaaproject.kaa.server.control.service.exception.KaaPluginLoadException;

/**
 * Enables Kaa plugins loading from the specified in the properties filesystem location
 */
public interface KaaPluginLoadService {

    /**
     * Loads plugin metadata to the database by the following rules:
     * 1. Only new plugin definitions should are loaded, old definitions remain in database.
     * 2. If there is a definition in database, but not present in classpath/folder the exception is thrown.
     */
    void load() throws KaaPluginLoadException;
}
