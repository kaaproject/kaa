package org.kaaproject.kaa.server.common.dao.impl;

/**
 * Provides methods to retrieve plugins
 * @param <T> the generic plugin type
 */
public interface PluginDao<T> extends SqlDao<T> {

    /**
     * Find plugin by its name and version
     * @param name the plugin name
     * @param version the plugin version
     * @return the found plugin
     */
    T findByNameAndVersion(String name, Integer version);

    /**
     * Find plugin by its class name
     * @param className the plugin class name
     * @return the found plugin
     */
    T findByClassName(String className);
}
