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

import java.util.List;

/**
 * Configuration Class.
 *
 * Used to configure Netty HTTP server.
 *
 */
public class Config implements ConfigConst {

    /**
     * port to which binds Netty HTTP server, see default value.
     * @see org.kaaproject.kaa.server.common.server.ConfigConst
     */
    private int port = DEFAULT_PORT;

    /**
     * Interface hostname or IP address to which binds Netty HTTP server, see default value.
     * @see org.kaaproject.kaa.server.common.server.ConfigConst
     */
    private String bindInterface = DEFAULT_BIND_INTERFACE;

    /**
     * Number of threads to process HTTP requests, default value 3.
     */
    private int executorThreadSize = DEFAULT_EXECUTOR_THREAD_SIZE;

    /**
     * Maximum size of HTTP request body, default 10240 bytes.
     */
    private int clientMaxBodySize = DEFAULT_MAX_SIZE_VALUE;

    /**
     * commandFactories - used to store list of Classes which create CommanProcessors.
     */
    private List<KaaCommandProcessorFactory> commandFactories;

    /** Statistics collector */
    private SessionTrackable sessionTrack;

    /**
     * ClientMaxBodySize getter.
     * @return ClientMaxBodySize
     */
    public final int getClientMaxBodySize() {
        return clientMaxBodySize;
    }

    /**
     * ClientMaxBodySize setter.
     * @param httpMaxBodySize maximum size of HTTP request
     */
    public final void setClientMaxBodySize(final int httpMaxBodySize) {
        this.clientMaxBodySize = httpMaxBodySize;
    }

    /**
     * ExecutorThreadSize getter.
     * @return ExecutorThreadSize.
     */
    public final int getExecutorThreadSize() {
        return executorThreadSize;
    }

    /**
     * ExecutorThreadSize setter.
     * @param executorThreadNumber number of thread
     */
    public final void setExecutorThreadSize(final int executorThreadNumber) {
        this.executorThreadSize = executorThreadNumber;
    }

    /**
     * HTTP bind port getter.
     * @return HTTP bind port
     */
    public final int getPort() {
        return port;
    }

    /**
     * HTTP bind port setter.
     * @param bindPort HTTP bind port.
     */
    public final void setPort(final int bindPort) {
        this.port = bindPort;
    }

    /**
     * Return Command List.
     * @return List<String> command list.
     */
    public List<KaaCommandProcessorFactory> getCommandList() {
        return commandFactories;
    }

    /**
     * Set Command List
     * @param commandList List<String>
     */
    public void setCommandList(List<KaaCommandProcessorFactory> commandList) {
        this.commandFactories = commandList;
    }

    /**
     * getter for interface to which server bind listen socket
     * @return the bindInterface String, hostname or IP address
     */
    public String getBindInterface() {
        return bindInterface;
    }

    /**
     * Set interface to which server bind listen socket
     * @param bindInterface String, hostname or IP address
     */
    public void setBindInterface(String bindInterface) {
        this.bindInterface = bindInterface;
    }

    /**
     * @return the sessionTrack
     */
    public SessionTrackable getSessionTrack() {
        return sessionTrack;
    }

    /**
     * @param sessionTrack the sessionTrack to set
     */
    public void setSessionTrack(SessionTrackable sessionTrack) {
        this.sessionTrack = sessionTrack;
    }

    @Override
    public final String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("NettyServer config:\n");
        sb.append("Netty HTTP Port = " + getPort() + "\n");
        sb.append("Netty HTTP Interface = " + getBindInterface() + "\n");
        sb.append("Client MAX Body Size = " + getClientMaxBodySize() + "\n");
        sb.append("Task Executor pool Size = "
                + getExecutorThreadSize() + "\n");
        if (commandFactories != null) {
            sb.append("Command List:\n");
            for(KaaCommandProcessorFactory command : commandFactories) {
                sb.append("\t"+command+"\n");
            }
        }
        return sb.toString();
    }


}
