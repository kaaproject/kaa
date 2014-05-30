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

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;

/**
 * The Class ControlApiCommand.<br>
 * Abstract class used to describe control api commands
 */
public abstract class ControlApiCommand {

    /** The command. */
    private String command;

    /** The command description. */
    private String desc;

    /** The command options. */
    private Options options = new Options();

    /**
     * Instantiates a new control api command.
     * 
     * @param command
     *            the command
     * @param desc
     *            the command description
     */
    public ControlApiCommand(String command, String desc) {
        this.command = command;
        this.desc = desc;
    }

    /**
     * Gets the command.
     * 
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the command description.
     * 
     * @return the command description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Adds the command option.
     * 
     * @param option
     *            the command option
     */
    public void addOption(Option option) {
        options.addOption(option);
    }

    /**
     * Gets the command options.
     * 
     * @return the command options
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Run API command.
     * 
     * @param line
     *            the command line
     * @param client
     *            the Control Thrift Interface Client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    public abstract void runCommand(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter);

}
