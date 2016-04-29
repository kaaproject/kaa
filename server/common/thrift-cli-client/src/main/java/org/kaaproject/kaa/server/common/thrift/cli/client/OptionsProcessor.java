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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI client OptionsProcessor.
 * 
 */
public class OptionsProcessor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OptionsProcessor.class);

    /** The CLI options. */
    private final Options options = new Options();

    /** The command line. */
    private org.apache.commons.cli.CommandLine commandLine;

    /**
     * Instantiates a new CLI options processor.
     */
    @SuppressWarnings("static-access")
    public OptionsProcessor() {

        // -e 'quoted-query-string'
        options.addOption(OptionBuilder.hasArg()
                .withArgName("quoted-command-string")
                .withDescription("Remote command line").create('e'));

        // -h hostname/ippaddress
        options.addOption(OptionBuilder.hasArg().withArgName("hostname")
                .withDescription("connecting to Thrift Server on remote host")
                .create('h'));

        // -p port
        options.addOption(OptionBuilder.hasArg().withArgName("port")
                .withDescription("connecting to Thrift Server on port number")
                .create('p'));

        // -c 'thrift-config'
        options.addOption(OptionBuilder.hasArg().withArgName("thrift-config")
                .withDescription("Thrift property file").create('c'));

        // [-H|--help]
        options.addOption(new Option("H", "help", false,
                "Print help information"));

    }

    /**
     * Parses the command line arguments.
     * 
     * @param argv
     *            the command line arguments
     * @return true, if successful
     */
    public boolean parse(String[] argv) {
        try {
            commandLine = new GnuParser().parse(options, argv);
        } catch (ParseException e) {
            System.err.println(e.getMessage()); //NOSONAR
            printUsage();
            return false;
        }
        return true;
    }

    /**
     * Process CLI Client Session State.
     * 
     * @param ss
     *            the CLI Client Session State
     * @return true, if successful
     */
    public boolean process(CliSessionState ss) {

        if (commandLine.hasOption('H')) {
            printUsage();
            return false;
        }

        ss.execString = commandLine.getOptionValue('e');

        if (commandLine.hasOption('c')) {
            String propertyFile = commandLine.getOptionValue('c');
            File f = new File(propertyFile);

            Properties props = new Properties();
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                LOG.error("Exception catched: ", e);
                ss.out.println("Thrift property file '" + propertyFile
                        + "' does not exists.");
                return false;
            }
            try {
                props.load(fis);
                ss.host = props.getProperty("thrift_host", "localhost");
                ss.port = Integer.parseInt(props.getProperty("thrift_port",
                        "9090"));
            } catch (IOException e) {
                ss.out.println("Unable to read property file '" + propertyFile
                        + "'. Error: " + e);
                return false;
            }
        } else {
            ss.host = (String) commandLine.getOptionValue('h');
            ss.port = Integer.parseInt((String) commandLine.getOptionValue('p',
                    "9090"));
        }

        return true;
    }

    /**
     * Prints the usage.
     */
    private void printUsage() {
        new HelpFormatter().printHelp("thriftCli", options);
    }
}
