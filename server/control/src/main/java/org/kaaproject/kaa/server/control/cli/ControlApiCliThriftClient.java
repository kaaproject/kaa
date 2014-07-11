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

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import jline.console.ConsoleReader;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ControlApiCliThriftClient.<br>
 * Main class to start control API Client session.
 */
public class ControlApiCliThriftClient {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(ControlApiCliThriftClient.class);

    /** The prompt used in console. */
    public static String prompt = "kaa-control-api";

    /** The prompt2. */
    public static String prompt2 = "               ";

    /** The control api command processor. */
    private final ControlApiCommandProcessor apiCommandProcessor;

    /**
     * Instantiates a new control api cli thrift client.
     */
    public ControlApiCliThriftClient() {
        apiCommandProcessor = new ControlApiCommandProcessor();
    }

    /**
     * Process control API command.
     *
     * @param cmd
     *            the API command to execute
     * @return the int execution result code
     */
    public int processCmd(String cmd) {
        ControlClientSessionState ss = ControlClientSessionState.get();
        ss.out.println();
        String cmdTrimmed = cmd.trim();
        int ret = 0;
        if (cmdTrimmed.equalsIgnoreCase("quit") //NOSONAR
                || cmdTrimmed.equalsIgnoreCase("exit")) { //NOSONAR
            return -1;
        } else if (cmdTrimmed.equalsIgnoreCase("disconnect")) { //NOSONAR
            if (ss.isRemoteMode()) {
                ss.close();
                prompt = "kaa-control-api";
                prompt2 = "               ";
            } else {
                ss.err.println("Not connected!");
            }
        } else if (ss.isRemoteMode()) {
            apiCommandProcessor.executeCommand(ss, cmdTrimmed);
        } else {
            // local mode
            PrintStream out = ss.out;
            PrintStream err = ss.err;
            if (cmdTrimmed.equalsIgnoreCase("help") //NOSONAR
                    || cmdTrimmed.equalsIgnoreCase("?")) { //NOSONAR
                printHelp(out);
            } else if (cmdTrimmed.toLowerCase().startsWith("connect")) {
                boolean parsed = false;
                String[] args = cmdTrimmed.toLowerCase().split(" ");
                if (args.length == 2) {
                    String connectString = args[1];
                    String[] hostPort = connectString.split(":");
                    if (hostPort.length == 2) {
                        String host = hostPort[0];
                        String strPort = hostPort[1];
                        int port = 0;
                        try {
                            port = Integer.valueOf(strPort);
                        } catch (Exception e) {
                        }
                        if (port > 0) {
                            parsed = true;
                            ss.host = host;
                            ss.port = port;
                            try {
                                ss.connect();
                            } catch (TException e) {
                                String errMsg = e.getMessage();
                                if (errMsg == null) {
                                    errMsg = e.toString();
                                }
                                err.println("[Thrift Error]: " + errMsg);
                            }
                            if (ss.isRemoteMode()) {
                                prompt = "[" + ss.host + ':' + ss.port
                                        + "] kaa-control-api";
                                char[] spaces = new char[prompt.length()];
                                Arrays.fill(spaces, ' ');
                                prompt2 = new String(spaces);
                            }
                        }
                    }
                }
                if (!parsed) {
                    out.println("Unable to parse arguments.");
                    out.println();
                    out.println("Usage: connect <host:port>");
                }
            } else {
                out.println("Unknown command '" + cmdTrimmed + "'");
                out.println();
                printHelp(out);
            }
        }
        ss.out.println();
        return ret;
    }

    /**
     * Prints the help.
     *
     * @param out
     *            the output print stream
     */
    private void printHelp(PrintStream out) {
        out.println("Available commands: ");
        out.println();
        out.println("help, ?                - prints this help");
        out.println("connect <host:port>    - connect to remote control server");
        out.println("disconnect             - disconnect from remote control server");
        out.println("quit, exit             - quit");
    }

    /**
     * Process command line.
     *
     * @param line
     *            the command line
     * @return the int execution result code
     */
    public int processLine(String line) {
        int lastRet = 0, ret = 0;

        String command = "";
        for (String oneCmd : line.split(";")) {
            if (StringUtils.endsWith(oneCmd, "\\")) {
                command += StringUtils.chop(oneCmd) + ";";
                continue;
            } else {
                command += oneCmd;
            }
            if (StringUtils.isBlank(command)) {
                continue;
            }

            ret = processCmd(command);
            command = "";
            lastRet = ret;
            if (ret != 0) {
                return ret;
            }

        }
        return lastRet;
    }

    /**
     * Creates the reader.
     *
     * @return the console reader
     * @throws IOException the IO exception
     */
    public ConsoleReader createReader() throws IOException {
        return new ConsoleReader();
    }

    /**
     * Process.
     *
     * @param args the args
     * @return the int
     */
    public int process(String[] args) {
        ControlOptionsProcessor oproc = new ControlOptionsProcessor();
        if (!oproc.parse(args)) {
            return 1;
        }

        ControlClientSessionState ss = new ControlClientSessionState();
        ss.in = System.in;
        try {
            ss.out = new PrintStream(System.out, true, "UTF-8"); //NOSONAR
            ss.err = new PrintStream(System.err, true, "UTF-8"); //NOSONAR
        } catch (UnsupportedEncodingException e) {
            return 3;
        }

        if (!oproc.process(ss)) {
            return 2;
        }

        ControlClientSessionState.start(ss);
        // connect to Thrift Server
        if (ss.host != null) {
            try {
                ss.connect();
            } catch (TException e) {
                LOG.error("Failed connection to Thrift Server", e);
                return 1;
            }
            if (ss.isRemoteMode()) {
                prompt = "[" + ss.host + ':' + ss.port + "]  kaa-control-api";
                char[] spaces = new char[prompt.length()];
                Arrays.fill(spaces, ' ');
                prompt2 = new String(spaces);
            }
        }


        if (ss.execString != null) {
            return processLine(ss.execString);
        }

        try {
            ConsoleReader reader = createReader();
            reader.setBellEnabled(false);

            String line;
            String prefix = "";
            String curPrompt = prompt;
            while ((line = reader.readLine(curPrompt + "> ")) != null) {
                if (!prefix.equals("")) { //NOSONAR
                    prefix += '\n';
                }
                line = prefix + line;
                int ret = processLine(line);
                if (ret != 0) {
                    return ret;
                }
                prefix = "";
                curPrompt = prompt;
            }
        } catch (Exception e) {
            LOG.error("Exception while reading line", e);
            return 1;
        } finally {
            ss.close();
        }

        return 0;
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     * @throws Exception
     *             the exception
     */
    public static void main(String[] args) throws Exception { //NOSONAR
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int ret = cli.process(args);
        if (ret == -1) {
            ret = 0;
        }
        System.exit(ret); //NOSONAR
    }

}
