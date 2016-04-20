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

package org.kaaproject.kaa.server.common.thrift.cli.server;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * The Class Command.<br>
 * Abstract class used to describe thrift CLI commands
 */
public abstract class Command {

    /** The command. */
    private String command;

    /** The command description. */
    private String desc;

    /** The command options. */
    private Options options = new Options();

    /**
     * Instantiates a new thrift CLI command.
     * 
     * @param command
     *            the command
     * @param desc
     *            the command description
     */
    public Command(String command, String desc) {
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
     * Sets the command.
     * 
     * @param command
     *            the new command
     */
    public void setCommand(String command) {
        this.command = command;
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
     * Sets the command description.
     * 
     * @param desc
     *            the new command description
     */
    public void setDesc(String desc) {
        this.desc = desc;
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
     * Run CLI command.
     * 
     * @param line
     *            the command line
     * @param writer
     *            the writer to output command results
     */
    public abstract void runCommand(CommandLine line, PrintWriter writer);

}
