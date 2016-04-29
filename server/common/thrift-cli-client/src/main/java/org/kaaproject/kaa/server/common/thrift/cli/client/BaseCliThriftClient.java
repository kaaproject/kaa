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

import jline.console.ConsoleReader;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftException;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * The Class BaseCliThriftClient.<br>
 * Main class to start CLI Client session.
 */
public class BaseCliThriftClient {
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(BaseCliThriftClient.class);

    /** The prompt used in console. */
    public static String prompt = "kaa-console";

    /** The prompt2. */
    public static String prompt2 = "           ";

    /**
     * Instantiates a new base cli thrift client.
     */
    public BaseCliThriftClient() {

    }

    /**
     * Process CLI command.
     * 
     * @param cmd
     *            the CLI command to execute
     * @return the int execution result code
     */
    public int processCmd(String cmd) {
        CliSessionState ss = CliSessionState.get();
        ss.out.println();
        String cmdTrimmed = cmd.trim();
        int ret = 0;
        if (cmdTrimmed.equalsIgnoreCase("quit") //NOSONAR
                || cmdTrimmed.equalsIgnoreCase("exit")) { //NOSONAR
            System.exit(0); //NOSONAR
        } else if (cmdTrimmed.equalsIgnoreCase("disconnect")) { //NOSONAR
            if (ss.isRemoteMode()) {
                ss.close();
                prompt = "kaa-console";
                prompt2 = "           ";
            } else {
                ss.err.println("Not connected!");
            }
        } else if (ss.isRemoteMode()) {
            CliClient client = ss.getClient();
            PrintStream out = ss.out;
            PrintStream err = ss.err;
            try {
                CommandResult result = client.executeCommand(cmdTrimmed);
                out.print(result.message);
            } catch (CliThriftException cliException) {
                LOG.error("Exception catched: ", cliException);
                ret = cliException.errorCode;
                if (ret != 0) {
                    String errMsg = cliException.message;
                    if (errMsg == null) {
                        errMsg = cliException.toString();
                    }
                    err.println("[Thrift CLI Error]: " + errMsg);
                }
            } catch (TException e) {
                LOG.error("Exception catched: ", e);
                String errMsg = e.getMessage();
                if (errMsg == null) {
                    errMsg = e.toString();
                }
                ret = -10002;
                err.println("[Thrift Error]: " + errMsg);
            }

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
                            LOG.error("Unexpected exception while parsing port. Can not parse String: {} to Integer, exception catched {}", strPort, e);
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
                                prompt = "[" + ss.host + ':' + ss.port + "] "
                                        + ss.remoteServerName;
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
        out.println("connect <host:port>    - connect to remote thrift server");
        out.println("disconnect             - disconnect from remote thrift server");
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
     * The main method.
     * 
     * @param args
     *            the arguments
     * @throws Exception
     *             the exception
     */
    public static void main(String[] args) throws Exception { //NOSONAR
        OptionsProcessor oproc = new OptionsProcessor();
        if (!oproc.parse(args)) {
            System.exit(1); //NOSONAR
        }

        CliSessionState ss = new CliSessionState();
        ss.in = System.in;
        try {
            ss.out = new PrintStream(System.out, true, "UTF-8"); //NOSONAR
            ss.err = new PrintStream(System.err, true, "UTF-8"); //NOSONAR
        } catch (UnsupportedEncodingException e) {
            LOG.error("Exception catched: ", e);
            System.exit(3); //NOSONAR
        }

        if (!oproc.process(ss)) {
            System.exit(2); //NOSONAR
        }

        CliSessionState.start(ss);
        // connect to Thrift Server
        if (ss.getHost() != null) {
            ss.connect();
            if (ss.isRemoteMode()) {
                prompt = "[" + ss.host + ':' + ss.port + "] "
                        + ss.remoteServerName;
                char[] spaces = new char[prompt.length()];
                Arrays.fill(spaces, ' ');
                prompt2 = new String(spaces);
            }
        }

        BaseCliThriftClient cli = new BaseCliThriftClient();
        if (ss.execString != null) {
            System.exit(cli.processLine(ss.execString)); //NOSONAR
        }

        ConsoleReader reader = new ConsoleReader();
        reader.setBellEnabled(false);

        String line;
        int ret = 0;
        String prefix = "";
        String curPrompt = prompt;
        while ((line = reader.readLine(curPrompt + "> ")) != null) {
            if (!prefix.equals("")) { //NOSONAR
                prefix += '\n';
            }
            // if (line.trim().endsWith(";") && !line.trim().endsWith("\\;")) {
            line = prefix + line;
            ret = cli.processLine(line);
            prefix = "";
            curPrompt = prompt;
            // }
            // else {
            // prefix = prefix + line;
            // curPrompt = prompt2;
            // continue;
            // }
        }

        ss.close();
        System.exit(ret); //NOSONAR
    }

}
