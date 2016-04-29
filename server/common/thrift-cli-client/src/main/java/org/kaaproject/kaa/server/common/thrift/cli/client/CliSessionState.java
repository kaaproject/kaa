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

package org.kaaproject.kaa.server.common.thrift.cli.client;

import java.io.InputStream;
import java.io.PrintStream;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;

/**
 * CliSessionState.
 * 
 */
public class CliSessionState {

    /** The input stream to handle inputs. */
    public InputStream in;

    /** The output print stream. */
    public PrintStream out;

    /** The error print stream. */
    public PrintStream err;

    /**
     * -e option if any that the session has been invoked with.
     */
    public String execString;

    /** host name and port number of remote Thrift server. */
    protected String host;

    /** The port. */
    protected int port;

    /** The remote server name. */
    public String remoteServerName;

    /** The remote mode. */
    private boolean remoteMode;

    /** The Thrift transport. */
    private TTransport transport;

    /** The CLI client. */
    private CliClient client;
    
    /** The CLI Client Session State thread local storage. */
    private static ThreadLocal<CliSessionState> tss = new ThreadLocal<CliSessionState>();

    /**
     * Instantiates a new cli session state.
     */
    public CliSessionState() {
        remoteMode = false;
    }

    /**
     * Connect to Thrift Server.
     * 
     * @throws TException
     *             the t exception
     */
    public void connect() throws TException {
        transport = new TSocket(host, port);
        TProtocol protocol = new TBinaryProtocol(transport);
        TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, KaaThriftService.KAA_NODE_SERVICE.getServiceName());
        client = new CliClient(mp);
        transport.open();
        remoteServerName = client.serverName();
        remoteMode = true;
    }

    /**
     * Sets the host of thrift server.
     * 
     * @param host
     *            the new host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the host of thrift server.
     * 
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the port of thrift server.
     * 
     * @param port
     *            the new port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the port of thrift server.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Close thrift connection.
     */
    public void close() {
        // client.clean();
        // client.shutdown();
        transport.close();
        remoteMode = false;
    }

    /**
     * Checks if is remote mode.
     * 
     * @return true, if is remote mode
     */
    public boolean isRemoteMode() {
        return remoteMode;
    }

    /**
     * Gets the CLI client.
     * 
     * @return the CLI client
     */
    public CliClient getClient() {
        return client;
    }

    /**
     * Start CLI Client Session State.
     * 
     * @param startSs
     *            the CLI Client Session State to start
     * @return the cli client session state
     */
    public static CliSessionState start(CliSessionState startSs) {
        tss.set(startSs);

        return startSs;
    }

    /**
     * Gets the CLI client session state.
     * 
     * @return the client cli session state
     */
    public static CliSessionState get() {
        return tss.get();
    }

}
