package org.kaaproject.kaa.server.common.dao.impl;

public interface SqlDao<T> extends Dao<T, String> {

    /**
     * Find object by id.
     *
     * @param id   the id
     * @param lazy specifies whether return initialized object (if false is set)
     *             or proxy (if true is set)
     * @return the found object
     */
    T findById(String id, boolean lazy);

    /**
     * @param o
     * @return
     */
    T persist(T o);
}
