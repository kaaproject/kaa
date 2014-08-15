/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.common.server.http.DefaultHttpServerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommandFactory Class. Used as factory for Command processing classes
 *
 * @author Andrey Panasenko
 */
@SuppressWarnings("rawtypes")
public class CommandFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpServerInitializer.class);

    private final Map<String, KaaCommandProcessorFactory> factories;

    public CommandFactory(List<KaaCommandProcessorFactory> factories) {
        this.factories = new HashMap<>();
        for (KaaCommandProcessorFactory factory : factories) {
            this.factories.put(factory.getCommandName(), factory);
        }
    }

    /**
     * getCommandProcessor - used to instantiate CommandProcessor for specific
     * URI
     *
     * @param uri
     *            - HTTP request URI, should have following format:
     *            /DOMAIN/CommandName
     * @return - CommandProcessor
     * @throws Exception
     *             - throws if URI is incorrect or request command not found
     */
    public KaaCommandProcessor getCommandProcessor(String uri) throws Exception { // NOSONAR
        if (uri == null || uri.length() <= 0) {
            throw new Exception("URI parameter incorrect"); // NOSONAR
        }
        String[] params = uri.split("/");
        if (params.length < 3) {
            throw new Exception("URI parameter " + uri + " incorrect"); // NOSONAR
        }
        // String domain = params[1];
        String command = params[2];
        LOG.trace("Command {} looking for URI {}", command, uri);
        KaaCommandProcessorFactory factory = factories.get(command);
        if (factory != null) {
            LOG.trace("Command {} found", command);
            return factory.createCommandProcessor();
        } else {
            LOG.warn("Requested command not found: {}", command);
            throw new Exception("Requested command not found"); // NOSONAR
        }
    }

    public KaaCommandProcessor getCommandProcessorByName(String name) throws Exception { // NOSONAR
        LOG.trace("Command {} looking in the map", name);
        KaaCommandProcessorFactory factory = factories.get(name);
        if (factory != null) {
            LOG.trace("Command {} found", name);
            return factory.createCommandProcessor();
        }
        LOG.warn("Requested command not found: {}", name);
        throw new Exception("Requested command not found"); // NOSONAR
    }
}
