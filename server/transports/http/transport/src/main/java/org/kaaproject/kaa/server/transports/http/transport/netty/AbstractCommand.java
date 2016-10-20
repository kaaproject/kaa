/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.transports.http.transport.netty;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.kaaproject.kaa.server.common.server.KaaCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * AbstractCommand abstract Class.
 * Implements some base setters and getters and define abstract processing flow.
 * AbstractCommand implements callable interface and used from DefaultHandler to
 * decode HTTP request, process command in Executor and encode HTTP response.
 * Following flow is applied:
 * parse() - decode HTTP request in Netty inbound pipeline flow
 * process() - process command in executor thread
 * getHttpResponse() - encode HTTP response in Netty outbound pipeline flow.
 *
 * @author Andrey Panasenko
 */
public abstract class AbstractCommand
    implements Callable<AbstractCommand>, KaaCommandProcessor<HttpRequest, HttpResponse> {

  protected static final Logger LOG = LoggerFactory //NOSONAR
      .getLogger(AbstractCommand.class);

  protected static String COMMAND_NAME = "";

  private HttpRequest httpRequest;


  private UUID sessionUuid;


  private long syncTime = 0;


  private int commandId;

  /**
   * Default CommandProcessor Class constructor.
   */
  public AbstractCommand() {

  }

  /**
   * Static method getCommandName.
   * Used to represent command part of URI.
   *
   * @return - String CommandName
   */
  public static String getCommandName() {
    return COMMAND_NAME;
  }


  @Override
  public int getCommandId() {
    return commandId;
  }


  @Override
  public void setCommandId(int commandId) {
    this.commandId = commandId;
  }


  @Override
  public HttpRequest getRequest() {
    return httpRequest;
  }

  /**
   * HttpRequest setter.
   *
   * @param httpRequest - HTTP request
   */
  @Override
  public void setRequest(HttpRequest httpRequest) {
    this.httpRequest = httpRequest;
  }


  @Override
  public String getName() {
    return COMMAND_NAME;
  }

  /**
   * parse() - used to decoder HttpRequest, find necessary CommandProcessor
   * and create CommandProcessor instance using CommanFactory.getProcessor()
   *
   * @throws Exception - if HttpRequest parse failed or CommandProcessor not found.
   */
  public abstract void parse() throws Exception; //NOSONAR

  /**
   * process() - is run in executor thread and process requests.
   *
   * @throws Exception - if some error occurred during processing.
   */
  public abstract void process() throws Exception; //NOSONAR

  /**
   * getHttpResponse() - encode processing result into HTTP Response.
   *
   * @return HttpResponse.
   */
  @Override
  public abstract HttpResponse getResponse();

  @Override
  public void setResponse(HttpResponse response) {
    // Nothing to do
  }

  /**
   * isNeedConnectionClose() - used to indicate is it necessary to close Channel after
   * HTTP response returned to client.
   *
   * @return boolean - true to Close connection.
   */
  public abstract boolean isNeedConnectionClose();

  @Override
  public AbstractCommand call() throws Exception {
    LOG.trace("{} : Process start", getCommandName());
    process();
    LOG.trace("{}: Process finish", getCommandName());
    return this;
  }


  public UUID getSessionUuid() {
    return sessionUuid;
  }


  public void setSessionUuid(UUID sessionUuid) {
    this.sessionUuid = sessionUuid;
  }


  @Override
  public long getSyncTime() {
    return syncTime;
  }


  @Override
  public void setSyncTime(long syncTime) {
    this.syncTime = syncTime;
  }

  public abstract int getNextProtocol();

}
