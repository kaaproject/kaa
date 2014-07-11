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

import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * CommandProcessor abstract Class.
 * Implements some base setters and getters and define abstract processing flow.
 * CommandProcessor implements callable interface and used from DefaultHandler to
 * decode HTTP request, process command in Executor and encode HTTP response.
 * Following flow is applied:
 * parse() - decode HTTP request in Netty inbound pipeline flow
 * process() - process command in executor thread
 * getHttpResponse() - encode HTTP response in Netty outbound pipeline flow.
 *
 * @author Andrey Panasenko
 */
public abstract class CommandProcessor implements Callable<CommandProcessor> {

    protected static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(CommandProcessor.class);

    public static final String COMMAND_METHOD_NAME = "getCommandName";

    protected static String COMMAND_NAME = "";

    private HttpRequest httpRequest;
    private NettyHttpServer server;

    /** Session UUID */
    private UUID sessionUuid;

    /** Time of SYNC processing */
    private long syncTime = 0;

    /** integer representing ID of HTTP request */
    private int commandId;

    /**
     * @return the commandId
     */
    public int getCommandId() {
        return commandId;
    }

    /**
     * @param commandId the commandId to set
     */
    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    /**
     * NettyHttpServer getter.
     * Used to allow access to KeyStore and EndPointService from CommandProcessor
     * @return NettyHttpServer
     */
    public NettyHttpServer getServer() {
        return server;
    }

    /**
     * NettyHttpServer setter.
     * @param server - NettyHttpServer
     */
    public void setServer(NettyHttpServer server) {
        this.server = server;
    }

    /**
     * HttpRequest getter.
     * @return HttpRequest
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * HttpRequest setter.
     * @param httpRequest - HTTP request
     */
    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * Static method getCommandName.
     * Used to represent command part of URI.
     * @return - String CommandName
     */
    public static String getCommandName() {
        return COMMAND_NAME;
    }

    /**
     * Command Name getter.
     * @return String
     */
    public String getName() {
        return COMMAND_NAME;
    }

    /**
     * Default CommandProcessor Class constructor.
     */
    public CommandProcessor() {

    }

    /**
     * parse() - used to decoder HttpRequest, find necessary CommandProcessor
     * and create CommandProcessor instance using CommanFactory.getProcessor()
     * @throws Exception - if HttpRequest parse failed or CommandProcessor not found.
     */
    abstract public void parse() throws Exception; //NOSONAR

    /**
     * process() - is run in executor thread and process requests.
     * @throws Exception - if some error occurred during processing.
     */
    abstract public void process() throws Exception; //NOSONAR

    /**
     * getHttpResponse() - encode processing result into HTTP Response.
     * @return HttpResponse.
     */
    abstract public HttpResponse getHttpResponse();

    /**
     * isNeedConnectionClose() - used to indicate is it necessary to close Channel after
     * HTTP response returned to client.
     * @return boolean - true to Close connection.
     */
    abstract public boolean isNeedConnectionClose();


    @Override
    public CommandProcessor call() throws Exception {
        LOG.trace("{} : Process start", getCommandName());
        process();
        LOG.trace("{}: Process finish", getCommandName());
        return this;
    }

    /**
     * @return the sessionUuid
     */
    public UUID getSessionUuid() {
        return sessionUuid;
    }

    /**
     * @param sessionUuid the sessionUuid to set
     */
    public void setSessionUuid(UUID sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    /**
     * @return the syncTime
     */
    public long getSyncTime() {
        return syncTime;
    }

    /**
     * @param syncTime the syncTime to set
     */
    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

}
