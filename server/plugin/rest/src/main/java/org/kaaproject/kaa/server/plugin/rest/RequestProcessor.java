package org.kaaproject.kaa.server.plugin.rest;

import org.springframework.web.client.HttpClientErrorException;

@FunctionalInterface
public interface RequestProcessor<T> {

    T send(HttpRequestDetails request) throws HttpClientErrorException;
}
