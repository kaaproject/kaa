package org.kaaproject.kaa.server.plugin.rest;

/**
 * @author Bohdan Khablenko
 */
@FunctionalInterface
public interface Task {

    void complete() throws Exception;
}
