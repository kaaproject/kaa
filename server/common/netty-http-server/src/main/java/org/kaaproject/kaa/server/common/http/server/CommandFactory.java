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

package org.kaaproject.kaa.server.common.http.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommandFactory Class.
 * Used as factory for Command processing classes
 *
 * @author Andrey Panasenko
 */
public class CommandFactory {

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultServerInitializer.class);

    private static Hashtable<String, Class<?>> commands = new Hashtable<String, Class<?>>();

    /**
     * addCommandClass - used for initializing factory during startup.
     * @param className - Command class name.
     * @throws ClassNotFoundException - throws if specified class name not found.
     * @throws NoSuchMethodException - throws in specified class don't have static method getCommandName()
     * @throws SecurityException - other reflection invocation exceptions
     * @throws IllegalAccessException - other reflection invocation exceptions
     * @throws IllegalArgumentException - other reflection invocation exceptions
     * @throws InvocationTargetException - other reflection invocation exceptions
     */
    public static void addCommandClass(String className)
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (!commands.containsKey(className)) {
            Class<?> c = null;
            Class<?> noparams[] = {};
            Object[] args = {};

            c = Class.forName(className);
            Method m = c.getMethod(CommandProcessor.COMMAND_METHOD_NAME,
                    noparams);
            Object o = m.invoke(null, args);
            if (o instanceof String) {
                commands.put((String) o, c);
                LOG.info("Command {} added with className {}", o , className);
            }
        }
    }

    /**
     * getCommandProcessor - used to instantiate CommandProcessor for specific URI
     * @param uri - HTTP request URI, should have following format: /DOMAIN/CommandName
     * @return - CommandProcessor
     * @throws Exception - throws if URI is incorrect or request command not found
     */
    public static CommandProcessor getCommandProcessor(String uri)
            throws Exception {
        if (uri == null || uri.length() <= 0) {
            throw new Exception("URI parameter incorrect");
        }
        String[] params = uri.split("/");
        if (params.length < 3) {
            throw new Exception("URI parameter " + uri + " incorrect");
        }
        // String domain = params[1];
        String command = params[2];
        LOG.trace("Command {} looking for URI {}", command, uri);
        if (commands.containsKey(command)) {
            LOG.trace("Command {} found", command);
            Class<?> c = commands.get(command);
            CommandProcessor cp = (CommandProcessor) c.newInstance();
            return cp;
        }
        LOG.warn("Requested command not found: {}", command);
        throw new Exception("Requested command not found");
    }

    /**
     * release - not implemented, for highly loaded servers need to have Cache for processing objects.
     * @param processor CommandProcessor which should be returned to store.
     */
    public static void release(CommandProcessor processor) {

    }
}
