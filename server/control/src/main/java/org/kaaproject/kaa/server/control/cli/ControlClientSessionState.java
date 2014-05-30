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

package org.kaaproject.kaa.server.control.cli;

import java.io.InputStream;
import java.io.PrintStream;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * ControlClientSessionState.
 * 
 */
public class ControlClientSessionState {

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

    /** Is remote mode. */
    private boolean remoteMode;

    /** The Thrift transport. */
    private TTransport transport;
    
    /** The control client. */
    private ControlClient client;

    /**
     * Instantiates a new control client session state.
     */
    public ControlClientSessionState() {
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
        client = new ControlClient(protocol);
        transport.open();
        remoteMode = true;
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
     * Gets the client thrift interface.
     * 
     * @return the client thrift interface
     */
    public ControlClient getClient() {
        return client;
    }

    /** The Client Session State thread local storage. */
    private static ThreadLocal<ControlClientSessionState> tss = new ThreadLocal<ControlClientSessionState>();

    /**
     * Start Control Client Session State.
     * 
     * @param startSs
     *            the Control Client Session State to start
     * @return the control client session state
     */
    public static ControlClientSessionState start(
            ControlClientSessionState startSs) {
        tss.set(startSs);

        return startSs;
    }

    /**
     * Gets the control client session state.
     * 
     * @return the control client session state
     */
    public static ControlClientSessionState get() {
        return tss.get();
    }

}
